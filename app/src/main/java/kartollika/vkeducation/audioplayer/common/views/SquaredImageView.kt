package kartollika.vkeducation.audioplayer.common.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView

class SquaredImageView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
    ImageView(context, attrs, defStyleAttr) {

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?) : this(context, null)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val maxDimension = Math.max(measuredHeight, measuredWidth)
        setMeasuredDimension(maxDimension, maxDimension)
    }
}