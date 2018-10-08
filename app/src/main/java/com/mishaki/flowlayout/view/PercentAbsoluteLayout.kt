package com.mishaki.flowlayout.view

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.mishaki.flowlayout.R

class PercentAbsoluteLayout : ViewGroup {
    private val PARSE_HORIZONTAL = 0x1
    private val PARSE_VERTICAL = 0x2

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val originalWidth = MeasureSpec.getSize(widthMeasureSpec)
        val originalHeight = MeasureSpec.getSize(heightMeasureSpec)
        var width = originalWidth
        var height = originalHeight
        if (widthMode == MeasureSpec.AT_MOST) {
            width = 0
        }
        if (widthMode == MeasureSpec.UNSPECIFIED && parent != null && parent is HorizontalScrollView) {
            width = 0
        }
        if (heightMode == MeasureSpec.AT_MOST) {
            height = 0
        }
        if (heightMode == MeasureSpec.UNSPECIFIED && parent != null && parent is ScrollView) {
            height = 0
        }
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {

                val param = child.layoutParams as LayoutParams
                val percentX = parseHorizontalSize(param.xPercent, width, height)
                val percentY = parseVerticalSize(param.yPercent, width, height)
                val percentWidth = parseHorizontalSize(param.widthPercent, width, height)
                val percentHeight = parseVerticalSize(param.heightPercent, width, height)
                if (percentX != -1f) {
                    param.x = percentX.toInt()
                }
                if (percentY != -1f) {
                    param.y = percentY.toInt()
                }
                if (child is ListView) {
                    val dividerHeightPercent = parseVerticalSize(param.lvDividerHeightPercent, width, height)
                    if (dividerHeightPercent != -1f) {
                        child.dividerHeight = dividerHeightPercent.toInt()
                    }
                }
                if (child is TextView) {
                    val textSizePercent = parseHorizontalSize(param.textSizePercent, width, height)
                    if (textSizePercent != -1f) {
                        child.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePercent)
                    }
                    val textLineSpacingPercent = parseVerticalSize(param.textLineSpacingPercent, width, height)
                    if (textLineSpacingPercent != -1f) {
                        child.setLineSpacing(textLineSpacingPercent, 1.0f)
                    }
                }
                val childRight = param.x + if (percentWidth != -1f) {
                    param.width = percentWidth.toInt()
                    param.width
                } else {
                    child.measuredWidth
                }
                val childBottom = param.y + if (percentHeight != -1f) {
                    param.height = percentHeight.toInt()
                    param.height
                } else {
                    child.measuredHeight
                }
                child.layoutParams = param
                width = Math.max(width, childRight)
                height = Math.max(height, childBottom)
            }
        }
        width += paddingStart + paddingEnd
        height += paddingTop + paddingBottom
        width = Math.max(width, suggestedMinimumWidth)
        height = Math.max(height, suggestedMinimumHeight)
        setMeasuredDimension(View.resolveSizeAndState(width, widthMeasureSpec, 0),
                View.resolveSizeAndState(height, heightMeasureSpec, 0))
    }
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                val param = child.layoutParams as LayoutParams
                val widthPercent = parseHorizontalSize(param.widthPercent)
                val heightPercent = parseVerticalSize(param.heightPercent)

                val childLeft = paddingStart + param.x
                val childTop = paddingTop + param.y

                if (child is ListView) {
                    val dividerHeightPercent = parseVerticalSize(param.lvDividerHeightPercent)
                    if (dividerHeightPercent != -1f) {
                        child.dividerHeight = dividerHeightPercent.toInt()
                    }
                }
                if (child is TextView) {
                    val textSizePercent = parseHorizontalSize(param.textSizePercent)
                    if (textSizePercent != -1f) {
                        child.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePercent)
                    }
                    val textLineSpacingPercent = parseVerticalSize(param.textLineSpacingPercent)
                    if (textLineSpacingPercent != -1f) {
                        child.setLineSpacing(textLineSpacingPercent, 1.0f)
                    }
                }

                val childRight = childLeft + if (widthPercent != -1f) {
                    param.width = widthPercent.toInt()
                    param.width
                } else {
                    child.measuredWidth
                }
                val childBottom = childTop + if (heightPercent != -1f) {
                    param.height = heightPercent.toInt()
                    param.height
                } else {
                    child.measuredHeight
                }
                child.layoutParams = param
                child.layout(childLeft, childTop, childRight, childBottom)
            }
        }
    }

    override fun shouldDelayChildPressedState(): Boolean {
        return false
    }

    class LayoutParams : AbsoluteLayout.LayoutParams {
        var direction: Int = 0
        var xPercent: String? = ""
        var yPercent: String? = ""
        var widthPercent: String? = ""
        var heightPercent: String? = ""
        var textSizePercent: String? = ""
        var textLineSpacingPercent: String? = ""
        var lvDividerHeightPercent: String? = ""

        constructor(source: ViewGroup.LayoutParams?) : super(source)
        constructor(width: Int, height: Int) : super(width, height, 0, 0)
        constructor(width: Int, height: Int, x: Int, y: Int) : super(width, height, x, y)
        constructor(width: Int,height: Int,percentX:String,percentY: String) :super(width,height,0,0){
            this.xPercent = percentX
            this.yPercent = percentY
        }

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            if (attrs != null) {
                val array = context.obtainStyledAttributes(attrs, R.styleable.PercentAbsoluteLayout_Layout)
                xPercent = array.getString(R.styleable.PercentAbsoluteLayout_Layout_layout_percentX)
                yPercent = array.getString(R.styleable.PercentAbsoluteLayout_Layout_layout_percentY)
                widthPercent = array.getString(R.styleable.PercentAbsoluteLayout_Layout_layout_percentWidth)
                heightPercent = array.getString(R.styleable.PercentAbsoluteLayout_Layout_layout_percentHeight)
                textSizePercent = array.getString(R.styleable.PercentAbsoluteLayout_Layout_layout_percentTextSize)
                textLineSpacingPercent = array.getString(R.styleable.PercentAbsoluteLayout_Layout_layout_percentTextLineSpacing)
                lvDividerHeightPercent = array.getString(R.styleable.PercentAbsoluteLayout_Layout_layout_percentLvDividerHeight)
                array.recycle()
            }
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet?): ViewGroup.LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams?): ViewGroup.LayoutParams {
        return LayoutParams(p)
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean {
        return p is LayoutParams
    }

    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
        return LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun parseHorizontalSize(sizeStr: String?, width: Int = this.width, height: Int = this.height): Float {
        return parseSize(sizeStr, PARSE_HORIZONTAL, width, height)
    }

    private fun parseVerticalSize(sizeStr: String?, width: Int = this.width, height: Int = this.height): Float {
        return parseSize(sizeStr, PARSE_VERTICAL, width, height)
    }

    private fun parseSize(sizeStr: String?, orientation: Int, width: Int, height: Int): Float {
        sizeStr ?: return -1f
        val regex = "\\d+(\\.\\d+)?(%w|%h|%sw|%sh|%)"
        if (sizeStr.matches(regex.toRegex())) {
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
        } else {
            return -1f
        }
    }
}