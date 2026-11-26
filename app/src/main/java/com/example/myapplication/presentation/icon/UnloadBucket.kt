package com.example.myapplication.presentation.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * «Отгрузка из бункера» — иконка действия бетоновозной телеги, стиль Material Symbols (rounded).
 * Метафора upload: стрелка выходит из лотка вверх. Пара к [LoadBucket]: лоток и посадка
 * стержня идентичны, отличается только направление стрелки.
 *
 * Геометрия (сетка 24×24, обводка 2, круглые терминалы и соединения):
 * - лоток: верх по осевой 18 (внешний габарит 20), низ 6 (x9..15), высота 6, стенки под 45°;
 * - стержень стрелки: нижний конец внутри лотка, на 2 ниже его верхнего края;
 * - головка шириной 6 с симметричными плечами, вершина на конце стержня;
 * - общий габарит иконки 20×18 (2..22 × 3..21), центр композиции (12;12).
 *
 * Цвет внутри — Color.Black: реальный цвет задаётся через tint в Icon (Material 3).
 */
object UnloadBucket {

    /** Outlined-версия: лоток и стрелка — только обводка 2. */
    val AppIcons.Outlined.UnloadBucket: ImageVector by lazy {
        ImageVector.Builder(
            name = "UnloadBucketOutlined",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            // Открытый лоток (идентичен лотку из LoadBucket).
            addPath(
                pathData = addPathNodes("M3 14L9 20L15 20L21 14"),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
            // Стержень стрелки: выходит из лотка вверх, нижний конец на 2 ниже верхнего края.
            addPath(
                pathData = addPathNodes("M12 16V4"),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
            )
            // Головка стрелки: ширина 6, симметричные плечи (ранее правое плечо было шире левого).
            addPath(
                pathData = addPathNodes("M9 8L12 4L15 8"),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }.build()
    }
}