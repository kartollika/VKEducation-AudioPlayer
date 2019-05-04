package kartollika.vkeducation.audioplayer.views.player_view

import android.content.Context
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import kartollika.vkeducation.audioplayer.R
import kartollika.vkeducation.audioplayer.common.ui.onRenderFinished

class FloatingBottomPlayer(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var root: View

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        initContent()
    }

    private fun initContent() {
        LayoutInflater.from(context).inflate(R.layout.view_audio_player, this)

        onRenderFinished(this, Runnable {
            val layoutParams = layoutParams as CoordinatorLayout.LayoutParams
            bottomSheetBehavior = layoutParams.behavior as BottomSheetBehavior<View>
            bottomSheetBehavior.peekHeight = 200

            this.layoutParams = layoutParams
        })

        root = findViewById(R.id.root)
        root.background = ContextCompat.getDrawable(context, R.drawable.audio_player_background)
    }


}