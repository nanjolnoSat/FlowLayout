package com.mishaki.flowlayout.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.annotation.IdRes
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ListView
import android.widget.ScrollView
import android.widget.TextView
import com.mishaki.flowlayout.R
import com.mishaki.flowlayout.util.parseHorizontalSize
import com.mishaki.flowlayout.util.parseThisSize
import com.mishaki.flowlayout.util.parseVerticalSize

class FlowLayout : ViewGroup, View.OnTouchListener {
    private var widthPercent = -1f
    private var heightPercent = -1f
    private var paddingPercent = -1f
    private var paddingLeftPercent = -1f
    private var paddingTopPercent = -1f
    private var paddingRightPercent = -1f
    private var paddingBottomPercent = -1f

    //水平分隔符大小
    private var horizontalDividerSize = -1f
    //垂直分隔大小
    private var verticalDividerSize = -1f
    private val DEFAULT_HORIZONTAL_DIVIDER_COLOR = 0x00ffffff.toInt()
    private val DEFAULT_VERTICAL_DIVIDER_COLOR = 0x00ffffff.toInt()
    //分隔符的颜色
    private var horizontalDividerColor = DEFAULT_HORIZONTAL_DIVIDER_COLOR
    private var verticalDividerColor = DEFAULT_VERTICAL_DIVIDER_COLOR
    //使用资源文件作为分隔符
    private var horizontalDividerBmp: Bitmap? = null
    private var verticalDividerBmp: Bitmap? = null

    private val horizontalDividerList = ArrayList<RectF>()
    private val verticalDividerList = ArrayList<RectF>()
    private val horizontalDividerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val verticalDividerPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    var onChildClickListener: OnChildClickListener? = null

    private val viewIndexMap = HashMap<View, Int>()

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        if (attrs != null) {
            val array = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout)
            val widthPercent = array.getString(R.styleable.FlowLayout_layout_fl_widthPercent)
            val heightPercent = array.getString(R.styleable.FlowLayout_layout_fl_heightPercent)
            val paddingPercent = array.getString(R.styleable.FlowLayout_layout_fl_paddingPercent)
            val paddingLeftPercent = array.getString(R.styleable.FlowLayout_layout_fl_paddingLeftPercent)
            val paddingTopPercent = array.getString(R.styleable.FlowLayout_layout_fl_paddingTopPercent)
            val paddingRightPercent = array.getString(R.styleable.FlowLayout_layout_fl_paddingRightPercent)
            val paddingBottomPercent = array.getString(R.styleable.FlowLayout_layout_fl_paddingBottomPercent)

            val horizontalDividerSize = array.getDimension(R.styleable.FlowLayout_layout_fl_horizontalDividerSize, -1f)
            val verticalDividerSize = array.getDimension(R.styleable.FlowLayout_layout_fl_verticalDividerSize, -1f)
            val horizontalDividerPercent = array.getString(R.styleable.FlowLayout_layout_fl_horizontalDividerPercent)
            val verticalDividerPercent = array.getString(R.styleable.FlowLayout_layout_fl_verticalDividerPercent)
            horizontalDividerColor = array.getColor(R.styleable.FlowLayout_layout_fl_horizontalDividerColor, DEFAULT_HORIZONTAL_DIVIDER_COLOR)
            verticalDividerColor = array.getColor(R.styleable.FlowLayout_layout_fl_verticalDividerColor, DEFAULT_HORIZONTAL_DIVIDER_COLOR)
            if (array.hasValue(R.styleable.FlowLayout_layout_fl_horizontalDividerDrawable)) {
                horizontalDividerBmp = BitmapFactory.decodeResource(context.resources, array.getResourceId(R.styleable.FlowLayout_layout_fl_horizontalDividerDrawable, -1))
            }
            if (array.hasValue(R.styleable.FlowLayout_layout_fl_verticalDividerDrawable)) {
                verticalDividerBmp = BitmapFactory.decodeResource(context.resources, array.getResourceId(R.styleable.FlowLayout_layout_fl_verticalDividerDrawable, -1))
            }
            array.recycle()

            this.widthPercent = context.parseThisSize(widthPercent)
            this.heightPercent = context.parseThisSize(heightPercent)
            this.paddingPercent = context.parseThisSize(paddingPercent)
            this.paddingLeftPercent = context.parseThisSize(paddingLeftPercent)
            this.paddingTopPercent = context.parseThisSize(paddingTopPercent)
            this.paddingRightPercent = context.parseThisSize(paddingRightPercent)
            this.paddingBottomPercent = context.parseThisSize(paddingBottomPercent)

            this.horizontalDividerSize = horizontalDividerSize
            this.verticalDividerSize = verticalDividerSize
            if (horizontalDividerPercent.isNotEmpty()) {
                this.horizontalDividerSize = context.parseThisSize(horizontalDividerPercent)
            }
            if (verticalDividerPercent.isNotEmpty()) {
                this.verticalDividerSize = context.parseThisSize(verticalDividerPercent)
            }
        }
        horizontalDividerPaint.color = horizontalDividerColor
        verticalDividerPaint.color = verticalDividerColor
        if (paddingPercent != -1f) {
            setPadding(paddingPercent.toInt(), paddingPercent.toInt(), paddingPercent.toInt(), paddingPercent.toInt())
        } else {
            var myPaddingLeft = paddingStart
            paddingLeftPercent.let {
                if (it != -1f) {
                    myPaddingLeft = it.toInt()
                }
            }
            var myPaddingTop = paddingTop
            paddingTopPercent.let {
                if (it != -1f) {
                    myPaddingTop = it.toInt()
                }
            }
            var myPaddingRight = paddingEnd
            paddingRightPercent.let {
                if (it != -1f) {
                    myPaddingRight = it.toInt()
                }
            }
            var myPaddingBottom = paddingBottom
            paddingBottomPercent.let {
                if (it != -1f) {
                    myPaddingBottom = it.toInt()
                }
            }
            setPadding(myPaddingLeft, myPaddingTop, myPaddingRight, myPaddingBottom)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildren(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val widthModel = MeasureSpec.getMode(widthMeasureSpec)
        val heightModel = MeasureSpec.getMode(heightMeasureSpec)

        var actualWidth = 0
        if (widthPercent != -1f) {
            actualWidth = widthPercent.toInt()
        } else {
            //当wrap_content的时候或者父控件为HorizontalScrollView的时候
            if (widthModel == MeasureSpec.AT_MOST || (parent != null && parent is HorizontalScrollView)) {
                actualWidth = 0
            } else {
                actualWidth = width
            }
        }
        //宽度必须指定
        if (actualWidth <= 0) {
            throw IllegalArgumentException("width must > 0")
        }
        var actualHeight = 0
        var isHeightAtMost = false
        if (heightPercent != -1f) {
            actualHeight = heightPercent.toInt()
        } else {
            if (heightModel == MeasureSpec.AT_MOST || (parent != null && parent is ScrollView)) {
                isHeightAtMost = true
            } else {
                actualHeight = height
            }
        }
        //当父控件为ScrollView或者高度为wrap_content的时候
        if (isHeightAtMost) {
            //记录当前行已经包含的子view的总宽度
            var flowWidth = 0
            //当前view的在当前行的index
            var flowIndex = -1
            //当前行所有子view中最大的高度
            var maxChildHeight = 0
            val horizontalDividerSize = calcHorizontalDividerSize()
            val verticalDividerSize = calcVerticalDividerSize()
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child.visibility != View.GONE) {
                    val param = child.layoutParams as LayoutParams

                    flowIndex++
                    //子view增加的宽度
                    var increaseWidth = param.marginStart + param.marginEnd
                    //获取子view的宽度
                    val childWidth = calcChildWidth(actualWidth, actualHeight, child)
                    //垂直的margin值
                    val marginVertical = param.topMargin + param.bottomMargin
                    increaseWidth += childWidth
                    val childHeight = calcChildHeight(actualWidth, actualHeight, child)
                    //当不是当前行第一个的时候,增加的宽度需要包行水平分隔符的宽度
                    if (flowIndex != 0) {
                        increaseWidth += horizontalDividerSize
                    }
                    //当 当前行的宽度+增加的宽度大于view的宽度的时候,换行
                    if (flowWidth + increaseWidth > actualWidth) {
                        flowIndex = 0
                        flowWidth = childWidth
                        actualHeight += maxChildHeight
                        actualHeight += verticalDividerSize
                        maxChildHeight = childHeight
                    } else {
                        flowWidth += increaseWidth
                        //获取最大的高度
                        maxChildHeight = Math.max(maxChildHeight, childHeight + marginVertical)
                    }
                }
            }
            actualHeight += maxChildHeight
            actualHeight += paddingTop + paddingBottom
        }
        setMeasuredDimension(actualWidth, resolveSizeAndState(actualHeight, heightMeasureSpec, 0))
    }

    private fun calcChildWidth(viewWidth: Int, viewHeight: Int, child: View): Int {
        var width = 0
        val param = child.layoutParams as LayoutParams
        param.calcSize(viewWidth, viewHeight)
        if (child is TextView) {
            if (param.textSizePercent != -1f) {
                child.setTextSize(TypedValue.COMPLEX_UNIT_PX, param.textSizePercent)
            }
        }
        if (param.widthPercent != -1f) {
            width += param.widthPercent.toInt()
        } else {
            width += child.measuredWidth
        }
        if (param.paddingPercent != -1f) {
            val increaseWidth = param.paddingPercent * 2f
            if (width < increaseWidth) {
                width = increaseWidth.toInt()
            }
        } else {
            var increaseWidth = 0
            if (param.paddingLeftPercent != -1f) {
                increaseWidth += param.paddingLeftPercent.toInt()
            } else {
                increaseWidth += child.paddingStart
            }
            if (param.paddingRightPercent != -1f) {
                increaseWidth += param.paddingRightPercent.toInt()
            } else {
                increaseWidth = child.paddingEnd
            }
            if (width != 0 && width < increaseWidth) {
                width = increaseWidth
            }
        }
        if (param.marginPercent != -1f) {
            param.marginStart = param.marginPercent.toInt()
            param.marginEnd = param.marginPercent.toInt()
        } else {
            if (param.marginLeftPercent != -1f) {
                param.marginStart = param.marginLeftPercent.toInt()
            }
            if (param.marginRightPercent != -1f) {
                param.marginEnd = param.marginRightPercent.toInt()
            }
        }
        child.layoutParams = param
        return width
    }

    private fun calcChildHeight(viewWidth: Int, viewHeight: Int, child: View): Int {
        var height = 0
        val param = child.layoutParams as LayoutParams
        param.calcSize(viewWidth, viewHeight)
        if (child is TextView) {
            if (param.textSizePercent != -1f) {
                child.setTextSize(TypedValue.COMPLEX_UNIT_PX, param.textSizePercent)
            }
            if (param.textLineSpacePercent != -1f) {
                child.setLineSpacing(param.textLineSpacePercent, 1f)
            }
        }
        if (child is ListView) {
            if (param.lvDividerHeightPercent != -1f) {
                child.dividerHeight = param.lvDividerHeightPercent.toInt()
            }
        }
        if (param.heightPercent != -1f) {
            height += param.heightPercent.toInt()
        } else {
            height += child.measuredHeight
        }
        if (param.paddingPercent != -1f) {
            val increaseHeight = param.paddingPercent * 2f
            if (height < increaseHeight) {
                height = increaseHeight.toInt()
            }
        } else {
            var increaseHeight = 0
            if (param.paddingTopPercent != -1f) {
                increaseHeight = +param.paddingTopPercent.toInt()
            } else {
                increaseHeight = +child.paddingTop
            }
            if (param.paddingBottomPercent != -1f) {
                increaseHeight += param.paddingBottomPercent.toInt()
            } else {
                increaseHeight += child.paddingBottom
            }
            if (height != 0 && height < increaseHeight) {
                height = increaseHeight
            }
        }
        if (param.marginPercent != -1f) {
            param.topMargin = param.marginPercent.toInt()
            param.bottomMargin = param.marginPercent.toInt()
        } else {
            if (param.marginTopPercent != -1f) {
                param.topMargin = param.marginTopPercent.toInt()
            }
            if (param.marginBottomPercent != -1f) {
                param.bottomMargin = param.marginBottomPercent.toInt()
            }
        }
        child.layoutParams = param
        return height
    }

    private fun calcHorizontalDividerSize(): Int {
        var size = 0
        if (horizontalDividerBmp != null) {
            size = horizontalDividerBmp!!.width
        } else {
            if (horizontalDividerSize != -1f) {
                size = horizontalDividerSize.toInt()
            }
        }
        return size
    }

    private fun calcVerticalDividerSize(): Int {
        var size = 0
        if (verticalDividerBmp != null) {
            size = verticalDividerBmp!!.width
        } else {
            if (verticalDividerSize != -1f) {
                size = verticalDividerSize.toInt()
            }
        }
        return size
    }

    private val leftList = ArrayList<Float>()
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val showWidth = width - paddingRight
        var flowWidth = 0
        var flowHeight = 0
        var flowIndex = -1
        var maxHeight = 0
        //最后一行的top
        var lastTop = 0
        val horizontalDividerSize = calcHorizontalDividerSize()
        val verticalDividerSize = calcVerticalDividerSize()
        horizontalDividerList.clear()
        verticalDividerList.clear()
        viewIndexMap.clear()
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            viewIndexMap.put(child, i)
            child.setOnTouchListener(this)
            if (child.visibility != View.GONE) {
                val param = child.layoutParams as LayoutParams

                flowIndex++
                val childWidth = calcChildWidth(width, height, child)
                val childHeight = calcChildHeight(width, height, child)
                val marginVertical = param.topMargin + param.bottomMargin

                var left = paddingStart + param.marginStart
                var top = paddingTop + param.topMargin
                var right = 0
                var bottom = 0
                if (flowIndex != 0) {
                    if (flowWidth + childWidth + horizontalDividerSize + param.marginStart + param.marginEnd > showWidth) {
                        leftList.forEach {
                            val rectF = RectF()
                            rectF.left = it
                            rectF.top = flowHeight.toFloat()
                            rectF.right = it + horizontalDividerSize
                            rectF.bottom = rectF.top + maxHeight
                            horizontalDividerList.add(rectF)
                        }
                        leftList.clear()
                        flowWidth = childWidth + param.marginStart + param.marginEnd
                        flowHeight += verticalDividerSize
                        flowHeight += maxHeight
                        flowHeight += marginVertical
                        maxHeight = childHeight + marginVertical
                        lastTop = flowHeight

                        val verticalRectF = RectF()
                        verticalRectF.left = 0f
                        verticalRectF.top = flowHeight.toFloat() - verticalDividerSize
                        verticalRectF.right = width.toFloat()
                        verticalRectF.bottom = flowHeight.toFloat()
                        verticalDividerList.add(verticalRectF)
                    } else {
                        leftList.add(flowWidth.toFloat())
                        flowWidth += horizontalDividerSize
                        left += flowWidth
                        flowWidth += childWidth + param.marginStart + param.marginEnd
                        maxHeight = Math.max(maxHeight, childHeight + marginVertical)
                    }
                    top += flowHeight
                } else {
                    flowWidth += childWidth + param.marginStart + param.marginEnd
                    maxHeight = Math.max(maxHeight, childHeight + marginVertical)
                }
                right = left + childWidth
                bottom = top + childHeight
                child.layout(left, top, right, bottom)
            }
            leftList.forEach {
                val rectF = RectF()
                rectF.left = it
                rectF.top = lastTop.toFloat()
                rectF.right = rectF.left + horizontalDividerSize
                rectF.bottom = rectF.top + maxHeight
                horizontalDividerList.add(rectF)
            }
        }
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams?): LayoutParams {
        return LayoutParams(p)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean {
        return p is LayoutParams
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        horizontalDividerList.forEach {
            if (horizontalDividerBmp != null) {
                canvas.drawBitmap(horizontalDividerBmp, it.left, it.top, null)
            } else {
                canvas.drawRect(it, horizontalDividerPaint)
            }
        }
        verticalDividerList.forEach {
            if (verticalDividerBmp != null) {
                canvas.drawBitmap(verticalDividerBmp, 0f, it.top, null)
            } else {
                canvas.drawRect(it, verticalDividerPaint)
            }
        }
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val index = viewIndexMap[v]
            if (index != null) {
                onChildClickListener?.onChildClick(v, index)
            }
        }
        return false
    }

    private fun drawableToBmp(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun String?.isNotEmpty(): Boolean {
        this ?: return false
        return trim().length != 0
    }

    fun setHorizontalDividerDrawable(drawable: Drawable) {
        horizontalDividerBmp = drawableToBmp(drawable)
        refresh()
    }

    fun setVerticalDividerDrawable(drawable: Drawable) {
        verticalDividerBmp = drawableToBmp(drawable)
        refresh()
    }

    fun setHorizontalDividerResourceId(@IdRes idRes: Int) {
        horizontalDividerBmp = BitmapFactory.decodeResource(context.resources, idRes)
        refresh()
    }

    fun setVerticalDividerResourceId(@IdRes idRes: Int) {
        verticalDividerBmp = BitmapFactory.decodeResource(context.resources, idRes)
        refresh()
    }

    fun setHorizontalDividerPercent(percent: String) {
        horizontalDividerSize = context.parseThisSize(percent)
        refresh()
    }

    fun setVerticalDividerPercent(percent: String) {
        verticalDividerSize = context.parseThisSize(percent)
        refresh()
    }

    fun setHorizontalDividerSize(size: Float) {
        setHorizontalDividerSize(TypedValue.COMPLEX_UNIT_PX, size)
    }

    /**
     * @see android.util.TypedValue.COMPLEX_UNIT_PX
     */
    fun setHorizontalDividerSize(unit: Int, size: Float) {
        horizontalDividerSize = TypedValue.applyDimension(unit, size, context.resources.displayMetrics)
        refresh()
    }

    fun setVerticalDividerSize(size: Float) {
        setVerticalDividerSize(TypedValue.COMPLEX_UNIT_PX, size)
    }

    /**
     * @see android.util.TypedValue.COMPLEX_UNIT_PX
     */
    fun setVerticalDividerSize(unit: Int, size: Float) {
        verticalDividerSize = TypedValue.applyDimension(unit, size, context.resources.displayMetrics)
        refresh()
    }

    private fun refresh() {
        requestLayout()
        invalidate()
    }

    class LayoutParams : MarginLayoutParams {
        private var context: Context? = null

        private var widthPercentStr: String? = ""
        private var heightPercentStr: String? = ""
        private var marginPercentStr: String? = ""
        private var marginLeftPercentStr: String? = ""
        private var marginTopPercentStr: String? = ""
        private var marginRightPercentStr: String? = ""
        private var marginBottomPercentStr: String? = ""
        private var paddingPercentStr: String? = ""
        private var paddingLeftPercentStr: String? = ""
        private var paddingTopPercentStr: String? = ""
        private var paddingRightPercentStr: String? = ""
        private var paddingBottomPercentStr: String? = ""
        private var textSizePercentStr: String? = null
        private var textLineSpacePercentStr: String? = null
        private var lvDividerHeightPercentStr: String? = null

        var widthPercent = -1f
        var heightPercent = -1f
        var marginPercent = -1f
        var marginLeftPercent = -1f
        var marginTopPercent = -1f
        var marginRightPercent = -1f
        var marginBottomPercent = -1f
        var paddingPercent = -1f
        var paddingLeftPercent = -1f
        var paddingTopPercent = -1f
        var paddingRightPercent = -1f
        var paddingBottomPercent = -1f
        var textSizePercent = -1f
        var textLineSpacePercent = -1f
        var lvDividerHeightPercent = -1f

        constructor(width: Int, height: Int) : super(width, height)

        constructor(source: ViewGroup.LayoutParams?) : super(source)

        constructor(c: Context?, attrs: AttributeSet?) : super(c, attrs) {
            context = c
            if (c != null && attrs != null) {
                val array = c.obtainStyledAttributes(attrs, R.styleable.FlowLayout_Layout)
                widthPercentStr = array.getString(R.styleable.FlowLayout_Layout_layout_fll_widthPercent)
                heightPercentStr = array.getString(R.styleable.FlowLayout_Layout_layout_fll_heightPercent)
                marginPercentStr = array.getString(R.styleable.FlowLayout_Layout_layout_fll_marginPercent)
                marginLeftPercentStr = array.getString(R.styleable.FlowLayout_Layout_layout_fll_marginLeftPercent)
                marginTopPercentStr = array.getString(R.styleable.FlowLayout_Layout_layout_fll_marginTopPercent)
                marginRightPercentStr = array.getString(R.styleable.FlowLayout_Layout_layout_fll_marginRightPercent)
                marginBottomPercentStr = array.getString(R.styleable.FlowLayout_Layout_layout_fll_marginBottomPercent)
                paddingPercentStr = array.getString(R.styleable.FlowLayout_Layout_layout_fll_paddingPercent)
                paddingLeftPercentStr = array.getString(R.styleable.FlowLayout_Layout_layout_fll_paddingLeftPercent)
                paddingTopPercentStr = array.getString(R.styleable.FlowLayout_Layout_layout_fll_paddingTopPercent)
                paddingRightPercentStr = array.getString(R.styleable.FlowLayout_Layout_layout_fll_paddingRightPercent)
                paddingBottomPercentStr = array.getString(R.styleable.FlowLayout_Layout_layout_fll_paddingBottomPercent)
                textSizePercentStr = array.getString(R.styleable.FlowLayout_Layout_layout_fll_textSizePercent)
                textLineSpacePercentStr = array.getString(R.styleable.FlowLayout_Layout_layout_fll_textLineSpacePercent)
                lvDividerHeightPercentStr = array.getString(R.styleable.FlowLayout_Layout_layout_fll_lvDividerHeightPercent)
                array.recycle()
            }
        }

        fun calcSize(width: Int, height: Int) {
            context?.let {
                widthPercent = it.parseHorizontalSize(widthPercentStr, width, height)
                heightPercent = it.parseVerticalSize(heightPercentStr, width, height)
                marginPercent = it.parseHorizontalSize(marginPercentStr, width, height)
                marginLeftPercent = it.parseHorizontalSize(marginLeftPercentStr, width, height)
                marginTopPercent = it.parseVerticalSize(marginTopPercentStr, width, height)
                marginRightPercent = it.parseHorizontalSize(marginRightPercentStr, width, height)
                marginBottomPercent = it.parseVerticalSize(marginBottomPercentStr, width, height)
                paddingPercent = it.parseHorizontalSize(paddingPercentStr, width, height)
                paddingLeftPercent = it.parseHorizontalSize(paddingLeftPercentStr, width, height)
                paddingTopPercent = it.parseVerticalSize(paddingTopPercentStr, width, height)
                paddingRightPercent = it.parseHorizontalSize(paddingRightPercentStr, width, height)
                paddingBottomPercent = it.parseVerticalSize(paddingBottomPercentStr, width, height)
                textSizePercent = it.parseHorizontalSize(textSizePercentStr, width, height)
                textLineSpacePercent = it.parseVerticalSize(textLineSpacePercentStr, width, height)
                lvDividerHeightPercent = it.parseVerticalSize(lvDividerHeightPercentStr, width, height)
            }
        }
    }

    interface OnChildClickListener {
        fun onChildClick(view: View, index: Int)
    }
}