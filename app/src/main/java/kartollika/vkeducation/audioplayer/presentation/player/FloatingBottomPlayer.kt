package kartollika.vkeducation.audioplayer.presentation.player

import android.content.Context
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.FragmentManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import kartollika.vkeducation.audioplayer.R
import kartollika.vkeducation.audioplayer.common.utils.onRenderFinished
import kartollika.vkeducation.audioplayer.presentation.player.full.PlayerFragment
import kartollika.vkeducation.audioplayer.presentation.player.mini.MiniPlayerFragment
import kotlinx.android.synthetic.main.view_bottom_sheet_audio_player.view.*

class FloatingBottomPlayer(
    context: Context, private val attrs: AttributeSet?, private val defStyleAttr: Int) :
    FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private val callbacks: MutableList<BottomSheetBehavior.BottomSheetCallback> = mutableListOf()
    private var peekHeightDp = 0

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    init {
        initAttributes()
        initContent()
    }

    private fun initAttributes() {
        val attributes =
            context.obtainStyledAttributes(attrs, R.styleable.FloatingBottomPlayer, defStyleAttr, 0)
        peekHeightDp =
            attributes.getDimension(R.styleable.FloatingBottomPlayer_peekHeight, 0f).toInt()
        attributes.recycle()
    }

    private fun initContent() {
        LayoutInflater.from(context).inflate(R.layout.view_bottom_sheet_audio_player, this, true)

        onRenderFinished(this, Runnable {
            val layoutParams = layoutParams as CoordinatorLayout.LayoutParams
            bottomSheetBehavior = layoutParams.behavior as BottomSheetBehavior<View>
            bottomSheetBehavior.peekHeight = peekHeightDp
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

        bottomSheetHideView.setOnClickListener {
            if (isExpanded()) {
                collapseSheet()
            }
        }
    }

    fun initPlayerFragment(fragmentManager: FragmentManager) {
        fragmentManager.beginTransaction()
            .replace(R.id.audioPlayerContainerView, PlayerFragment.newInstance()).commit()
        initSmoothAnimations()
    }

    fun initMiniPlayerFragment(fragmentManager: FragmentManager) {
        fragmentManager.beginTransaction()
            .replace(R.id.playerMiniContainerView, MiniPlayerFragment.newInstance()).commit()
    }

    private fun initSmoothAnimations() {
        addCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, offset: Float) {
                playerMiniContainerView.alpha = 1 - 1 / 0.3f * offset
                playerFullContainerView.alpha = Math.min(1 / 0.3f * offset - 1 - offset, 1f)
            }

            override fun onStateChanged(p0: View, state: Int) {
                when (state) {
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        playerFullContainerView.visibility = View.VISIBLE
                    }

                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        playerFullContainerView.visibility = View.GONE
                    }
                }
            }
        })
        playerFullContainerView.visibility = View.VISIBLE
        playerMiniContainerView.visibility = View.VISIBLE
        playerFullContainerView.alpha = 0f
    }

    fun addCallback(callback: BottomSheetBehavior.BottomSheetCallback) {
        callbacks.add(callback)
    }

    fun isExpanded() = bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED

    fun isCollapsed() = bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED

    fun collapseSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun expandSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun hideSheet() {
        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun showSheet() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            post {
                bottomSheetBehavior.isHideable = false
            }
        }
    }

}