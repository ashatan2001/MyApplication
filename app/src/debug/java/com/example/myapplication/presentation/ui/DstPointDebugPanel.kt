package com.example.myapplication.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.domain.model.Bucket
import com.example.domain.model.BucketStatus
import com.example.domain.model.DstPoint
import com.example.myapplication.presentation.viewmodel.DstPointListViewModel
import java.time.LocalDateTime

/**
 * Отладочная панель симуляции телег (только debug-сборка).
 *
 * Гоняет реальный код-путь мониторинга через debugInjectBuckets: прибытие,
 * вторая телега, разгрузка, завершение, отъезд — без сервера и без ожидания
 * 5-секундных интервалов. Любое симулирующее действие останавливает реальный
 * опрос, чтобы ответы сервера не перетирали сценарий; переключатель возвращает
 * живой мониторинг.
 *
 * @param viewModel ViewModel экрана, принимающая симулированные ответы.
 * @param zones Загруженные точки выгрузки для выбора цели симуляции.
 * @param modifier Модификатор размещения панели на экране.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DstPointDebugPanel(
    viewModel: DstPointListViewModel,
    zones: List<DstPoint>,
    modifier: Modifier = Modifier
) {
    if (zones.isEmpty()) return

    var zoneIndex by remember { mutableIntStateOf(0) }
    var simBuckets by remember { mutableStateOf(listOf<Bucket>()) }
    var nextId by remember { mutableIntStateOf(1000) }
    var polling by remember { mutableStateOf(true) }
    val zone = zones[zoneIndex.coerceIn(zones.indices)]

    /** Применяет новый симулированный список, предварительно глуша реальный опрос. */
    fun inject(updated: List<Bucket>) {
        if (polling) {
            viewModel.stopBucketMonitoring()
            polling = false
        }
        simBuckets = updated
        viewModel.debugInjectBuckets(updated)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Симулятор телег",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { zoneIndex = (zoneIndex - 1 + zones.size) % zones.size }) { Text("‹") }
                Text(text = zone.zoneName, style = MaterialTheme.typography.labelMedium)
                TextButton(onClick = { zoneIndex = (zoneIndex + 1) % zones.size }) { Text("›") }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = {
                    val id = nextId++
                    val now = LocalDateTime.now()
                    inject(
                        simBuckets + Bucket(
                            id = id, name = "Симулятор $id", zoneNumber = zone.zoneId,
                            isFull = true, status = BucketStatus.MOVING, loadId = 9_000L + id,
                            dstZone = zone.zoneId, loadDate = now, arrivalDate = now, departDate = null
                        )
                    )
                }) { Text("Прибытие") }
                TextButton(onClick = {
                    inject(simBuckets.map {
                        if (it.zoneNumber == zone.zoneId) it.copy(status = BucketStatus.UNLOADING) else it
                    })
                }) { Text("Разгрузка") }
                TextButton(onClick = {
                    inject(simBuckets.map {
                        if (it.zoneNumber == zone.zoneId) it.copy(departDate = LocalDateTime.now()) else it
                    })
                }) { Text("Завершена") }
                TextButton(onClick = {
                    inject(simBuckets.filterNot { it.zoneNumber == zone.zoneId })
                }) { Text("Уехала") }
                TextButton(onClick = { inject(emptyList()) }) { Text("Сброс") }
                TextButton(onClick = {
                    if (polling) viewModel.stopBucketMonitoring() else viewModel.startBucketMonitoring()
                    polling = !polling
                }) { Text(if (polling) "Опрос: вкл" else "Опрос: выкл") }
            }
        }
    }
}