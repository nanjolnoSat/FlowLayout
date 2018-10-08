package com.mishaki.flowlayout.util

import android.content.Context
import android.view.View
import java.lang.StringBuilder

const val PARSE_HORIZONTAL = 0x1
const val PARSE_VERTICAL = 0x2

fun Context.parseThisSize(sizeStr: String?): Float {
    sizeStr ?: return -1f
    val regex = "\\d+(\\.\\d+)?(%w|%h)"
    if (sizeStr.matches(regex.toRegex())) {
        val index = sizeStr.indexOf("%")
        //在百分号后面插入s
        val actualSizeStr = StringBuilder(sizeStr).insert(index + 1, "s").toString()
        return getSize(this, actualSizeStr, 0, 0, 0)
    } else {
        return -1f
    }
}

fun View.parseThisSize(sizeStr: String?): Float {
    return context.parseThisSize(sizeStr)
}


fun Context.parseHorizontalSize(sizeStr: String?, width: Int, height: Int): Float {
    return parseSize(sizeStr, PARSE_HORIZONTAL, width, height)
}

fun View.parseHorizontalSize(sizeStr: String?, width: Int = this.width, height: Int = this.height): Float {
    return context.parseHorizontalSize(sizeStr, width, height)
}

fun Context.parseVerticalSize(sizeStr: String?, width: Int, height: Int): Float {
    return parseSize(sizeStr, PARSE_VERTICAL, width, height)
}

fun View.parseVerticalSize(sizeStr: String?, width: Int = this.width, height: Int = this.height): Float {
    return context.parseVerticalSize(sizeStr, width, height)
}

fun Context.parseSize(sizeStr: String?, orientation: Int, width: Int, height: Int): Float {
    sizeStr ?: return -1f
    val regex = "\\d+(\\.\\d+)?(%w|%h|%sw|%sh|%)"
    if (sizeStr.matches(regex.toRegex())) {
        return getSize(this, sizeStr, orientation, width, height)
    } else {
        return -1f
    }
}

fun View.parseSize(sizeStr: String?, orientation: Int, width: Int = this.width, height: Int = this.height): Float {
    return context.parseSize(sizeStr, orientation, width, height)
}

private fun getSize(context: Context, sizeStr: String, orientation: Int, width: Int, height: Int): Float {
    return when {
        sizeStr.contains("%w") -> {
            sizeStr.substring(0, sizeStr.length - 2).toFloat() / 100f * width
        }
        sizeStr.contains("%h") -> {
            sizeStr.substring(0, sizeStr.length - 2).toFloat() / 100f * height
        }
        sizeStr.contains("%sw") -> {
            sizeStr.substring(0, sizeStr.length - 3).toFloat() / 100f * context.resources.displayMetrics.widthPixels
        }
        sizeStr.contains("%sh") -> {
            sizeStr.substring(0, sizeStr.length - 3).toFloat() / 100f * context.resources.displayMetrics.heightPixels
        }
        else -> {
            if (orientation == PARSE_HORIZONTAL) {
                sizeStr.substring(0, sizeStr.length - 1).toFloat() / 100f * width
            } else {
                sizeStr.substring(0, sizeStr.length - 1).toFloat() / 100f * height
            }
        }
    }
}