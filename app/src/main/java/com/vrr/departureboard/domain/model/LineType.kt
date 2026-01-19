package com.vrr.departureboard.domain.model

import androidx.compose.ui.graphics.Color
import com.vrr.departureboard.ui.theme.LineBus
import com.vrr.departureboard.ui.theme.LineOther
import com.vrr.departureboard.ui.theme.LineRegional
import com.vrr.departureboard.ui.theme.LineSBahn
import com.vrr.departureboard.ui.theme.LineStrassenbahn
import com.vrr.departureboard.ui.theme.LineUBahn

enum class LineType {
    U_BAHN,
    S_BAHN,
    STRASSENBAHN,
    BUS,
    REGIONAL,
    OTHER;

    val color: Color
        get() = when (this) {
            U_BAHN -> LineUBahn
            S_BAHN -> LineSBahn
            STRASSENBAHN -> LineStrassenbahn
            BUS -> LineBus
            REGIONAL -> LineRegional
            OTHER -> LineOther
        }

    companion object {
        fun fromLineAndMotType(lineNumber: String, motType: Int): LineType {
            val lower = lineNumber.lowercase()
            return when {
                lower.startsWith("u") || motType == 2 -> U_BAHN
                lower.startsWith("s") || motType == 1 -> S_BAHN
                motType == 4 || motType == 5 -> STRASSENBAHN
                lower.startsWith("re") || lower.startsWith("rb") || motType == 0 -> REGIONAL
                else -> BUS
            }
        }
    }
}
