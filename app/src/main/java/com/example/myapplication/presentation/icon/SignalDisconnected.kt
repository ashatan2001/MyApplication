package com.example.myapplication.presentation.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Иконка «Нет сигнала / отключено соединение» — стиль Material Symbols.
 *
 * Перечёркнутая антенна с тремя дугами радиосигнала. Сетка 24×24dp, сплошная заливка.
 * Цвет — Color.Black, реальный tint задаётся через Icon (Material 3).
 */
val AppIcons.Outlined.SignalDisconnected: ImageVector by lazy {
    ImageVector.Builder(
        name = "SignalDisconnected",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        addPath(
            pathData = addPathNodes(
                "M17.1 14.27L15.88 13.05Q16.43 12.4 16.72 11.63T17 10Q17 9 16.6 8.1T15.5 6.5" +
                        "L16.7 5.3Q17.65 6.25 18.18 7.45T18.7 10Q18.7 11.2 18.28 12.29T17.1 14.27Z" +
                        "M14.13 11.3L10.7 7.88Q11 7.7 11.33 7.6T12 7.5Q13.05 7.5 13.78 8.22T14.5 10" +
                        "Q14.5 10.35 14.4 10.67T14.13 11.3ZM19.51 16.65L18.31 15.45Q19.31 14.32 19.81 12.91" +
                        "T20.3 10Q20.3 8.35 19.69 6.81Q19.08 5.27 17.9 4.1L19.1 2.9Q20.48 4.35 21.24 6.18" +
                        "T22 10Q22 11.85 21.36 13.56T19.5 16.65ZM19.79 22.6L13 15.83V21H11V13.83L7 9.85" +
                        "Q7 9.9 7 9.92T7 10Q7 11 7.4 11.9T8.5 13.5L7.3 14.7Q6.35 13.75 5.83 12.55T5.3 10" +
                        "Q5.3 9.57 5.35 9.17T5.53 8.35L4.25 7.07Q3.98 7.8 3.84 8.52T3.7 10Q3.7 11.65 4.31 13.19" +
                        "T6.1 15.9L4.9 17.1Q3.53 15.65 2.76 13.83T2 10Q2 8.9 2.24 7.84T2.95 5.77L1.4 4.22" +
                        "L2.8 2.8L21.2 21.2L19.77 22.6Z",
            ),
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.NonZero,
        )
    }.build()
}