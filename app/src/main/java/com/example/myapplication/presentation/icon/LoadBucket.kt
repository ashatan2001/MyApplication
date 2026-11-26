package com.example.myapplication.presentation.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * «Загрузка в открытый бункер» — иконка действия бетоновозной телеги, стиль Material Symbols (rounded).
 * Метафора download: стрелка входит в лоток сверху. Пара к [UnloadBucket].
 *
 * Геометрия (сетка 24×24, обводка 2, круглые терминалы и соединения):
 * - лоток повторяет пропорции бункера телеги: верх по осевой 18 (x3..21), низ 6 (x9..15),
 *   высота 6, стенки под 45°; внешний габарит лотка 20 (внешняя граница по ширине точно
 *   вписана в live area);
 * - стержень стрелки заходит внутрь лотка на 2 ниже его верхнего края;
 * - головка шириной 6 совпадает с шириной дна лотка, вершина на конце стержня;
 * - общий габарит иконки 20×18 (2..22 × 3..21), центр композиции (12;12).
 *
 * Цвет внутри — Color.Black: реальный цвет задаётся через tint в Icon (Material 3).
 */
object LoadBucket {

    /** Outlined-версия: лоток и стрелка — только обводка 2. */
    val AppIcons.Outlined.LoadBucket: ImageVector by lazy {
        ImageVector.Builder(
            name = "LoadBucketOutlined",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            // Открытый лоток: верх 18 по осевой (внешний габарит 20), низ 6, высота 6.
            addPath(
                pathData = addPathNodes("M3 14L9 20L15 20L21 14"),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
            // Стержень стрелки: заходит внутрь лотка на 2 ниже его верхнего края (14 + 2).
            addPath(
                pathData = addPathNodes("M12 4V16"),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
            )
            // Головка стрелки: ширина 6 (совпадает с шириной дна лотка), вершина на конце стержня.
            addPath(
                pathData = addPathNodes("M9 12L12 16L15 12"),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }.build()
    }
}