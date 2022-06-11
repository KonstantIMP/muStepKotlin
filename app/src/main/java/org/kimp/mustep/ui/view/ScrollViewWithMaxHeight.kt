package org.kimp.mustep.ui.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.ScrollView


class ScrollViewWithMaxHeight : ScrollView {
    private var maxHeight = WITHOUT_MAX_HEIGHT_VALUE

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) { }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec
        try {
            var heightSize = MeasureSpec.getSize(heightMeasureSpec)
            if (maxHeight != WITHOUT_MAX_HEIGHT_VALUE
                && heightSize > maxHeight
            ) {
                heightSize = maxHeight
            }
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST)
            layoutParams.height = heightSize
        } catch (e: Exception) {
            Log.e("onMeasure", e.message.toString())
        } finally {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    fun setMaxHeight(maxHeight: Int) {
        this.maxHeight = maxHeight
    }

    companion object {
        var WITHOUT_MAX_HEIGHT_VALUE = -1
    }
}
