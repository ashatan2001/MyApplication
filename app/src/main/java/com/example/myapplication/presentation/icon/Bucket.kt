package com.example.myapplication.presentation.icon

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Бетоновозная телега — вариант Filled.
 *
 * Геометрия (сетка 24 dp, живое поле 20 dp):
 * - бункер — перевёрнутая трапеция ≈16.7×12.3 dp со скруглёнными углами;
 * - рама — 16.8×2.8 dp со скруглёнными торцами (ось рамы y = 17.7);
 * - колёса — Ø4.6 dp (колея 9.2 dp) с отверстиями ступиц Ø2.0 dp;
 *   центры колёс лежат на оси рамы, поэтому колесо наполовину заходит на раму.
 *
 * Каждое колесо с ступицей вынесено в отдельный путь с [PathFillType.EvenOdd]:
 * правило чётности вырезает только ступицу, а перекрытие колеса с рамой и бункером
 * просто закрашивается — без паразитных вырезов, которые возникают, если
 * пересекающиеся фигуры лежат в одном even-odd пути.
 */
public val Icons.Filled.Bucket: ImageVector
    get() {
        if (_BucketFilled != null) return _BucketFilled!!
        _BucketFilled = ImageVector.Builder(
            name = "Filled.Bucket",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Бункер: перевёрнутая трапеция со скруглёнными углами (литая).
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(4.5f, 4.0f)
                lineTo(19.5f, 4.0f)
                arcTo(1f, 1f, 0f, false, true, 20.35f, 5.45f)
                lineTo(16.3f, 15.75f)
                arcTo(1f, 1f, 0f, false, true, 15.4f, 16.3f)
                lineTo(8.6f, 16.3f)
                arcTo(1f, 1f, 0f, false, true, 7.7f, 15.75f)
                lineTo(3.65f, 5.45f)
                arcTo(1f, 1f, 0f, false, true, 4.5f, 4.0f)
                close()
            }
            // Рама со скруглёнными торцами (отдельный путь — см. KDoc).
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(4.6f, 16.3f)
                lineTo(19.4f, 16.3f)
                arcTo(1f, 1f, 0f, false, true, 20.4f, 17.3f)
                lineTo(20.4f, 18.1f)
                arcTo(1f, 1f, 0f, false, true, 19.4f, 19.1f)
                lineTo(4.6f, 19.1f)
                arcTo(1f, 1f, 0f, false, true, 3.6f, 18.1f)
                lineTo(3.6f, 17.3f)
                arcTo(1f, 1f, 0f, false, true, 4.6f, 16.3f)
                close()
            }
            // Левое колесо (центр 7.4; 17.7, радиус 2.3) со ступицей радиуса 1.0.
            path(
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(9.7f, 17.7f)
                arcTo(2.3f, 2.3f, 0f, false, true, 5.1f, 17.7f)
                arcTo(2.3f, 2.3f, 0f, false, true, 9.7f, 17.7f)
                close()
                // Отверстие ступицы — вырезается правилом even-odd.
                moveTo(8.4f, 17.7f)
                arcTo(1f, 1f, 0f, false, true, 6.4f, 17.7f)
                arcTo(1f, 1f, 0f, false, true, 8.4f, 17.7f)
                close()
            }
            // Правое колесо (центр 16.6; 17.7, радиус 2.3) со ступицей радиуса 1.0.
            path(
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(18.9f, 17.7f)
                arcTo(2.3f, 2.3f, 0f, false, true, 14.3f, 17.7f)
                arcTo(2.3f, 2.3f, 0f, false, true, 18.9f, 17.7f)
                close()
                // Отверстие ступицы — вырезается правилом even-odd.
                moveTo(17.6f, 17.7f)
                arcTo(1f, 1f, 0f, false, true, 15.6f, 17.7f)
                arcTo(1f, 1f, 0f, false, true, 17.6f, 17.7f)
                close()
            }
        }.build()
        return _BucketFilled!!
    }

/** Ленивый кэш ассета, чтобы не пересобирать ImageVector на каждой рекомпозиции. */
private var _BucketFilled: ImageVector? = null

/**
 * Бетоновозная телега — вариант Outlined.
 *
 * Геометрия совпадает с [Icons.Filled.Bucket]; контур (штрих 2 dp) получает
 * только бункер — его путь втянут на 1 dp (половину штриха), чтобы внешний
 * край обводки совпадал с силуэтом залитой версии. Рама и колёса остаются
 * литыми, колёса наполовину заходят на раму и имеют отверстия ступиц.
 *
 * Ассет намеренно рисуется чёрным: раскраска задаётся через `Icon(tint = ...)`.
 */
public val Icons.Outlined.Bucket: ImageVector
    get() {
        if (_BucketOutlined != null) return _BucketOutlined!!
        _BucketOutlined = ImageVector.Builder(
            name = "Outlined.Bucket",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            // Бункер: контурная трапеция без дублей вершин;
            // скругление углов — за счёт StrokeJoin.Round.
            path(
                stroke = SolidColor(Color(0xFF000000)),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4.5f, 5.0f)
                lineTo(19.5f, 5.0f)
                lineTo(15.4f, 15.3f)
                lineTo(8.6f, 15.3f)
                close()
            }
            // Рама — идентична Filled-варианту.
            path(fill = SolidColor(Color(0xFF000000))) {
                moveTo(4.6f, 16.3f)
                lineTo(19.4f, 16.3f)
                arcTo(1f, 1f, 0f, false, true, 20.4f, 17.3f)
                lineTo(20.4f, 18.1f)
                arcTo(1f, 1f, 0f, false, true, 19.4f, 19.1f)
                lineTo(4.6f, 19.1f)
                arcTo(1f, 1f, 0f, false, true, 3.6f, 18.1f)
                lineTo(3.6f, 17.3f)
                arcTo(1f, 1f, 0f, false, true, 4.6f, 16.3f)
                close()
            }
            // Левое колесо со ступицей — идентично Filled-варианту.
            path(
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(9.7f, 17.7f)
                arcTo(2.3f, 2.3f, 0f, false, true, 5.1f, 17.7f)
                arcTo(2.3f, 2.3f, 0f, false, true, 9.7f, 17.7f)
                close()
                moveTo(8.4f, 17.7f)
                arcTo(1f, 1f, 0f, false, true, 6.4f, 17.7f)
                arcTo(1f, 1f, 0f, false, true, 8.4f, 17.7f)
                close()
            }
            // Правое колесо со ступицей — идентично Filled-варианту.
            path(
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(18.9f, 17.7f)
                arcTo(2.3f, 2.3f, 0f, false, true, 14.3f, 17.7f)
                arcTo(2.3f, 2.3f, 0f, false, true, 18.9f, 17.7f)
                close()
                moveTo(17.6f, 17.7f)
                arcTo(1f, 1f, 0f, false, true, 15.6f, 17.7f)
                arcTo(1f, 1f, 0f, false, true, 17.6f, 17.7f)
                close()
            }
        }.build()
        return _BucketOutlined!!
    }

/** Ленивый кэш ассета, чтобы не пересобирать ImageVector на каждой рекомпозиции. */
private var _BucketOutlined: ImageVector? = null