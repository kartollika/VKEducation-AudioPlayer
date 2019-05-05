package kartollika.vkeducation.audioplayer.views.player_view

import android.content.Context
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.FragmentManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import kartollika.vkeducation.audioplayer.R
import kartollika.vkeducation.audioplayer.common.utils.dpToPx
import kartollika.vkeducation.audioplayer.common.utils.onRenderFinished
import kotlinx.android.synthetic.main.view_audio_player.view.*

class FloatingBottomPlayer(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private val callbacks: MutableList<BottomSheetBehavior.BottomSheetCallback> = mutableListOf()
    private lateinit var miniPlayer: View
    private lateinit var fullPlayer: View

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    init {
        initContent()
    }

    private fun initContent() {
        LayoutInflater.from(context).inflate(R.layout.view_audio_player, this, true)

        onRenderFinished(this, Runnable {
            val layoutParams = layoutParams as CoordinatorLayout.LayoutParams
            bottomSheetBehavior = layoutParams.behavior as BottomSheetBehavior<View>
            bottomSheetBehavior.peekHeight = dpToPx(60f, context)

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

        miniPlayer = view_mini_player
        fullPlayer = bottom_sheet_container

        bottom_sheet_hide_arrow.setOnClickListener {
            if (getSheetState() == BottomSheetBehavior.STATE_EXPANDED) {
                collapseSheet()
            } else {
                expandeSheet()
            }
        }
    }

    fun initPlayerFragment(fragmentManager: FragmentManager) {
        fragmentManager
            .beginTransaction()
            .replace(R.id.player_container, PlayerFragment())
            .commit()

        initSmoothAnimations()
    }

    private fun initSmoothAnimations() {
        addCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, offset: Float) {
                miniPlayer.alpha = 1 - 1 / 0.3f * offset
                fullPlayer.alpha =
                    Math.min(1 / 0.3f * offset - 1 - offset, 1f)
            }

            override fun onStateChanged(p0: View, state: Int) {
            }
        })
        fullPlayer.visibility = View.VISIBLE
        miniPlayer.visibility = View.VISIBLE
        fullPlayer.alpha = 0f
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