package kartollika.vkeducation.audioplayer.common.views.audio_seekbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import kartollika.vkeducation.audioplayer.R

class AudioSeekbar(context: Context,
                   attrs: AttributeSet?,
                   defStyleAttr: Int) : FrameLayout(context, attrs, defStyleAttr) {

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        initContent()
    }

    private fun initContent() {
        LayoutInflater.from(context).inflate(R.layout.audio_seekbar, this)
    }
}