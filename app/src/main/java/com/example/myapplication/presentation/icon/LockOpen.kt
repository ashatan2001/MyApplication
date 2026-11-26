package com.example.test

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
public val lock_open_right: ImageVector
    get() {
        if (_lock_open_right != null) {
            return _lock_open_right!!
        }
        _lock_open_right =
            ImageVector.Builder(
                name = "lock_open_right",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            )
                .apply {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 1f,
                        stroke = null,
                        strokeAlpha = 1f,
                        strokeLineWidth = 1f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Bevel,
                        strokeLineMiter = 1f,
                        pathFillType = PathFillType.Companion.NonZero,
                    ) {
                        moveTo(6f, 20f)
                        horizontalLineTo(18f)
                        verticalLineTo(10f)
                        horizontalLineTo(6f)
                        verticalLineTo(20f)
                        close()
                        moveToRelative(7.41f, -3.59f)
                        quadTo(14f, 15.83f, 14f, 15f)
                        reflectiveQuadTo(13.41f, 13.59f)
                        reflectiveQuadTo(12f, 13f)
                        reflectiveQuadToRelative(-1.41f, 0.59f)
                        quadTo(10f, 14.18f, 10f, 15f)
                        reflectiveQuadToRelative(0.59f, 1.41f)
                        reflectiveQuadTo(12f, 17f)
                        reflectiveQuadToRelative(1.41f, -0.59f)
                        close()
                        moveTo(6f, 20f)
                        verticalLineTo(10f)
                        verticalLineTo(20f)
                        close()
                        moveToRelative(0f, 2f)
                        quadTo(5.18f, 22f, 4.59f, 21.41f)
                        reflectiveQuadTo(4f, 20f)
                        verticalLineTo(10f)
                        quadTo(4f, 9.17f, 4.59f, 8.59f)
                        reflectiveQuadTo(6f, 8f)
                        horizontalLineToRelative(7f)
                        verticalLineTo(6f)
                        quadTo(13f, 3.92f, 14.46f, 2.46f)
                        reflectiveQuadTo(18f, 1f)
                        reflectiveQuadToRelative(3.54f, 1.46f)
                        reflectiveQuadTo(23f, 6f)
                        horizontalLineTo(21f)
                        quadTo(21f, 4.75f, 20.13f, 3.88f)
                        reflectiveQuadTo(18f, 3f)
                        reflectiveQuadTo(15.88f, 3.88f)
                        reflectiveQuadTo(15f, 6f)
                        verticalLineTo(8f)
                        horizontalLineToRelative(3f)
                        quadToRelative(0.82f, 0f, 1.41f, 0.59f)
                        reflectiveQuadTo(20f, 10f)
                        verticalLineTo(20f)
                        quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                        reflectiveQuadTo(18f, 22f)
                        horizontalLineTo(6f)
                        close()
                    }
                }
                .build()
        return _lock_open_right!!
    }

private var _lock_open_right: ImageVector? = null