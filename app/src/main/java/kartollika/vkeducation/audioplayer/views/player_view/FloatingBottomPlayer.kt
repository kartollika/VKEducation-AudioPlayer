package kartollika.vkeducation.audioplayer.views.player_view

import android.content.Context
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import kartollika.vkeducation.audioplayer.R
import kartollika.vkeducation.audioplayer.common.utils.onRenderFinished
import kotlinx.android.synthetic.main.view_audio_player.view.*

class FloatingBottomPlayer(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private val callbacks: MutableList<BottomSheetBehavior.BottomSheetCallback> = mutableListOf()

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

            bottomSheetBehavior.setBottomSheetCallback(object :
                BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(p0: View, p1: Float) {
                    for (callback in callbacks) {
                        callback.onSlide(p0, p1)
                    }
                }

                override fun onStateChanged(p0: View, p1: Int) {
                    for (callback in callbacks) {
                        callback.onStateChanged(p0, p1)
                    }
                }
            })

            this.layoutParams = layoutParams
        })

        root.background =
            ContextCompat.getDrawable(context, R.drawable.audio_player_background)
    }

    fun initPlayerFragment(fragmentManager: FragmentManager) {
        fragmentManager
            .beginTransaction()
            .replace(R.id.player_container, PlayerFragment())
            .commit()
    }

    fun addCallback(callback: BottomSheetBehavior.BottomSheetCallback) {
        callbacks.add(callback)
    }

    fun getSheetState() = bottomSheetBehavior.state

    fun collapseSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun expandeSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

}