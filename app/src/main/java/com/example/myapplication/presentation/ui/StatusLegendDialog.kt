package com.example.myapplication.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.BackHand
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Downloading
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.example.domain.model.BucketStatus
import com.example.domain.model.LoadStatus
import com.example.myapplication.presentation.icon.Bucket
import com.example.myapplication.presentation.util.IconSize
import com.example.myapplication.presentation.util.Spacing
import com.example.myapplication.presentation.util.StatusColors

/**
 * Элемент легенды статусов: визуальное обозначение и его расшифровка.
 *
 * Единый источник правды: одни и те же данные используются и для иконок
 * в списках ([BucketStatusIcon], [StatusIcon]), и для карточки-легенды,
 * поэтому расшифровка гарантированно соответствует отображаемым иконкам.
 *
 * @property icon Векторная иконка статуса.
 * @property tint Цвет иконки из семантической палитры [StatusColors].
 * @property title Краткая человекочитаемая подпись (для списка и TalkBack).
 * @property description Развёрнутое описание значения статуса для легенды.
 */
data class StatusLegendItem(
    val icon: ImageVector,
    val tint: Color,
    val title: String,
    val description: String
)

/**
 * Тематическая секция легенды (например, «Загруженность» и «Статус телеги»).
 *
 * @property title Заголовок секции.
 * @property items Элементы легенды в порядке отображения.
 */
data class StatusLegendSection(
    val title: String,
    val items: List<StatusLegendItem>
)

/**
 * Возвращает визуальное обозначение статуса бетоновозной телеги.
 * null трактуется как [BucketStatus.UNKNOWN].

 * @param status Статус телеги или null, если статус не получен.
 * @return Элемент легенды с иконкой, цветом, подписью и описанием.
 */
fun bucketStatusLegendItem(status: BucketStatus?): StatusLegendItem = when (status) {
    BucketStatus.WAITING_FOR_LOADING -> StatusLegendItem(
        icon = Icons.Outlined.Schedule,
        tint = StatusColors.Orange,
        title = "Ожидает загрузки",
        description = "Телега остановлена под смесителем в ожидании загрузки."
    )
    BucketStatus.MOVING -> StatusLegendItem(
        icon = Icons.Filled.FastForward,
        tint = StatusColors.Blue,
        title = "В движении",
        description = "Телега движется по маршруту."
    )
    BucketStatus.STOPPED_AT_POINT -> StatusLegendItem(
        icon = Icons.Filled.Pause,
        tint = StatusColors.Gray,
        title = "Остановлена",
        description = "Телега остановлена и зафиксирована в произвольной точке."
    )
    BucketStatus.UNLOADING -> StatusLegendItem(
        icon = Icons.Outlined.Downloading,
        tint = StatusColors.Green,
        title = "Разгрузка",
        description = "Идёт разгрузка телеги в точке назначения."
    )
    BucketStatus.DISCONNECTED -> StatusLegendItem(
        icon = Icons.Outlined.WifiOff,
        tint = StatusColors.Red,
        title = "Нет связи",
        description = "Телега отключена: связь с ней потеряна."
    )
    BucketStatus.UNDER_REPAIR -> StatusLegendItem(
        icon = Icons.Filled.Construction,
        tint = StatusColors.Brown,
        title = "Ремонт",
        description = "Телега находится на ремонте."
    )
    BucketStatus.MANUAL_CONTROL -> StatusLegendItem(
        icon = Icons.Filled.BackHand,
        tint = StatusColors.Purple,
        title = "Ручное управление",
        description = "Телега находится на ручном управлении."
    )
    BucketStatus.UNKNOWN, null -> StatusLegendItem(
        icon = Icons.AutoMirrored.Outlined.Help,
        tint = StatusColors.LightGray,
        title = "Неизвестный статус",
        description = "Статус не распознан или данные от сервера некорректны."
    )
}

/**
 * Возвращает визуальное обозначение флага загруженности телеги.
 *
 * @param isFull true — телега загружена, false — пуста.
 * @return Элемент легенды с иконкой, цветом, подписью и описанием.
 */
fun bucketFullnessLegendItem(isFull: Boolean): StatusLegendItem = if (isFull) {
    StatusLegendItem(
        icon = Icons.Filled.Bucket,
        tint = StatusColors.Green,
        title = "Загружена",
        description = "Телега загружена."
    )
} else {
    StatusLegendItem(
        icon = Icons.Outlined.Bucket,
        tint = StatusColors.Gray,
        title = "Пуста",
        description = "Телега пуста."
    )
}

/**
 * Возвращает визуальное обозначение статуса заявки на отгрузку.
 *
 * Иконки, цвета и подписи полностью соответствуют историческому
 * маппингу `StatusIcon` на экране очереди.
 *
 * @param status Статус заявки ([LoadStatus]).
 * @return Элемент легенды с иконкой, цветом, подписью и описанием.
 */
fun loadStatusLegendItem(status: LoadStatus): StatusLegendItem = when (status) {
    LoadStatus.IN_QUEUE -> StatusLegendItem(
        icon = Icons.Outlined.Schedule,
        tint = StatusColors.Orange,
        title = "В очереди",
        description = "Заявка ожидает отгрузки в очереди терминала."
    )
    LoadStatus.COMPLETED -> StatusLegendItem(
        icon = Icons.Outlined.CheckCircle,
        tint = StatusColors.Green,
        title = "Выполнена",
        description = "Заявка выполнена."
    )
    LoadStatus.DEFECT -> StatusLegendItem(
        icon = Icons.Outlined.ErrorOutline,
        tint = StatusColors.Red,
        title = "Брак",
        description = "Заявка признана браком."
    )
    LoadStatus.CANCELLED -> StatusLegendItem(
        icon = Icons.Outlined.Cancel,
        tint = StatusColors.Gray,
        title = "Отменена",
        description = "Заявка аннулирована и не будет выполнена."
    )
    LoadStatus.WEIGHED -> StatusLegendItem(
        icon = Icons.Filled.Scale,
        tint = StatusColors.Green,
        title = "Взвешена",
        description = "Заявка выполнена и проведено взвешивание."
    )
    LoadStatus.ARCHIVED -> StatusLegendItem(
        icon = Icons.Filled.Archive,
        tint = StatusColors.Purple,
        title = "В архиве",
        description = "Заявка отправлена в архив вручную."
    )
    LoadStatus.LOADING -> StatusLegendItem(
        icon = Icons.Filled.LocalShipping,
        tint = StatusColors.Blue,
        title = "Под загрузкой",
        description = "Машина находится под загрузкой."
    )
    LoadStatus.UNKNOWN -> StatusLegendItem(
        icon = Icons.AutoMirrored.Outlined.Help,
        tint = StatusColors.LightGray,
        title = "Неизвестный статус",
        description = "Статус не распознан: некорректные данные от сервера."
    )
}

/**
 * Собирает секции легенды для экрана бетоновозных телег.
 *
 * Порядок статусов соответствует порядку объявления в [BucketStatus].
 *
 * @return Секции «Загруженность телеги» и «Статус телеги».
 */
fun bucketStatusLegendSections(): List<StatusLegendSection> = listOf(
    StatusLegendSection(
        title = "Загруженность телеги",
        items = listOf(
            bucketFullnessLegendItem(isFull = true),
            bucketFullnessLegendItem(isFull = false)
        )
    ),
    StatusLegendSection(
        title = "Статус телеги",
        items = BucketStatus.entries.map { bucketStatusLegendItem(it) }
    )
)

/**
 * Собирает секции легенды для экрана очереди заявок.
 *
 * Порядок статусов соответствует порядку объявления в [LoadStatus].
 *
 * @return Единственная секция «Статус заявки».
 */
fun loadStatusLegendSections(): List<StatusLegendSection> = listOf(
    StatusLegendSection(
        title = "Статус заявки",
        items = LoadStatus.entries.map { loadStatusLegendItem(it) }
    )
)

/**
 * Кнопка открытия карточки с расшифровкой иконок статусов.
 *
 * Единый вид триггера на экранах телег и очереди: текстовая кнопка
 * с инлайн-иконкой информации (паттерн текстовых кнопок проекта).
 *
 * @param onClick Колбэк нажатия (открытие легенды).
 * @param modifier Модификатор кнопки.
 */
@Composable
fun StatusLegendButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription =  "Обозначения статусов",
            modifier = Modifier.size(IconSize.Inline)
        )
    }
}

/**
 * Карточка-легенда с расшифровкой иконок статусов в модальном диалоге.
 *
 * Оформлена по паттерну [LoadCardDialog]: Material 3 [AlertDialog]
 * с кнопкой «Закрыть». Содержимое прокручивается, поэтому легенда
 * полностью доступна и на маленьких экранах. Секции разделены
 * горизонтальными разделителями, стиль заголовков секций совпадает
 * с заголовками раскрытых карточек списков.
 *
 * @param title Заголовок карточки (зависит от экрана).
 * @param sections Секции легенды с элементами-расшифровками.
 * @param onDismiss Колбэк закрытия карточки.
 */
@Composable
fun StatusLegendDialog(
    title: String,
    sections: List<StatusLegendSection>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(Spacing.Smd)
            ) {
                sections.forEachIndexed { index, section ->
                    if (index > 0) {
                        HorizontalDivider()
                    }
                    StatusLegendSectionContent(section = section)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        }
    )
}

/**
 * Секция легенды: заголовок и столбец строк-расшифровок.
 *
 * @param section Секция легенды для отображения.
 */
@Composable
private fun StatusLegendSectionContent(section: StatusLegendSection) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.Sm)) {
        Text(
            text = section.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        section.items.forEach { item ->
            StatusLegendRow(item = item)
        }
    }
}

/**
 * Строка легенды: иконка статуса слева, подпись и описание справа.
 *
 * Иконка декоративная (contentDescription = null): её смысл полностью
 * дублируется текстовой подписью рядом, чтобы TalkBack не зачитывал одно
 * и то же дважды. Размер иконки совпадает с размером иконок в списках.
 *
 * @param item Элемент легенды для отображения.
 */
@Composable
private fun StatusLegendRow(item: StatusLegendItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = item.tint,
            modifier = Modifier.size(IconSize.Md)
        )
        Spacer(modifier = Modifier.width(Spacing.Smd))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}