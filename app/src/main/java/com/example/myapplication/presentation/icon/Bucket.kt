package com.example.myapplication.presentation.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Бетоновозная телега (Material Symbols rounded, 24×24).
 *
 * Геометрия (сетка 24×24, обводка 2, круглые торцы и соединения):
 * - бункер: перевёрнутая трапеция, верх 18 (x3..21) на y4, низ 6 (x9..15) на y15;
 *   внешний габарит 20×13 (x2..22, y3..16) — по ширине точно в live area;
 * - рама: капсула 16×2 (x4..20, y14..16); её середина y15 — нижняя кромка бункера,
 *   т.е. рама сомкнута с бункером и заменяет её;
 * - колёса: залитые диски Ø4, центры (7;19)/(17;19), база 10, зазор рама↔колесо 1;
 * - общий габарит по вертикали y3..21 — центр композиции (12;12).
 *
 * Filled и Outlined построены по одним осевым линиям, поэтому внешние контуры
 * совпадают; в Outlined у бункера нет нижней кромки — её заменяет рама.
 * Цвет — [Color.Black], реальный tint задаётся через Icon (Material 3).
 */
object Bucket {

    /** Filled: бункер, рама и колёса залиты. */
    val AppIcons.Filled.Bucket: ImageVector by lazy { build(filled = true) }

    /** Outlined: бункер и рама — обводка 2, колёса залиты. */
    val AppIcons.Outlined.Bucket: ImageVector by lazy { build(filled = false) }

    /**
     * Собирает [ImageVector] на сетке 24×24.
     *
     * @param filled true — бункер залит (fill + stroke одним цветом дают скруглённые
     *               внешние углы и общий силуэт с Outlined),
     *               false — только контур бункера и рамы.
     */
    private fun build(filled: Boolean): ImageVector =
        ImageVector.Builder(
            name = if (filled) "BucketFilled" else "BucketOutlined",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            if (filled) {
                // Бункер: fill + stroke тем же цветом — скругление внешних углов (r=1)
                // и единый силуэт с Outlined; перекрытие одного цвета невидно.
                addPath(
                    pathData = addPathNodes("M3 4L21 4L15 15L9 15Z"),
                    fill = SolidColor(Color.Black),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            } else {
                // Бункер: только контур, без нижней кромки (её заменяет рама).
                addPath(
                    pathData = addPathNodes("M9 15L3 4L21 4L15 15"),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
            // Рама: капсула 16×2; середина (y15) совпадает с нижней кромкой бункера.
            addPath(
                pathData = addPathNodes("M5 15H19"),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
            )
            // Колёса: два залитых диска Ø4, центры (7;19)/(17;19).
            addPath(
                pathData = addPathNodes(
                    "M5 19a2 2 0 1 0 4 0a2 2 0 1 0 -4 0" +
                            "M15 19a2 2 0 1 0 4 0a2 2 0 1 0 -4 0"
                ),
                fill = SolidColor(Color.Black),
            )
        }.build()
}