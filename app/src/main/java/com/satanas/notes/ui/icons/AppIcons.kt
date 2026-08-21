package com.satanas.notes.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val PinIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "PinFilled",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(16f, 9f)
            verticalTo(4f)
            horizontalToRelative(1f)
            curveToRelative(0.55f, 0f, 1f, -0.45f, 1f, -1f)
            reflectiveCurveToRelative(-0.45f, -1f, -1f, -1f)
            horizontalTo(7f)
            curveToRelative(-0.55f, 0f, -1f, 0.45f, -1f, 1f)
            reflectiveCurveToRelative(0.45f, 1f, 1f, 1f)
            horizontalToRelative(1f)
            verticalTo(9f)
            curveToRelative(0f, 1.66f, -1.34f, 3f, -3f, 3f)
            verticalToRelative(2f)
            horizontalToRelative(5.97f)
            verticalToRelative(7f)
            lineToRelative(1f, 1f)
            lineToRelative(1f, -1f)
            verticalTo(14f)
            horizontalTo(19f)
            verticalTo(12f)
            curveToRelative(-1.66f, 0f, -3f, -1.34f, -3f, -3f)
            close()
        }
    }.build()
}

val NoteGlyph: ImageVector by lazy {
    ImageVector.Builder(
        name = "NoteGlyph",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(6f, 2f)
            horizontalLineToRelative(9f)
            lineToRelative(5f, 5f)
            verticalToRelative(13f)
            curveToRelative(0f, 1.1f, -0.9f, 2f, -2f, 2f)
            horizontalTo(6f)
            curveToRelative(-1.1f, 0f, -2f, -0.9f, -2f, -2f)
            verticalTo(4f)
            curveToRelative(0f, -1.1f, 0.9f, -2f, 2f, -2f)
            close()
        }
        path(
            fill = SolidColor(Color.Transparent),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(8.5f, 12.5f)
            horizontalLineToRelative(7f)
            moveTo(8.5f, 16.5f)
            horizontalLineToRelative(4.5f)
        }
    }.build()
}
