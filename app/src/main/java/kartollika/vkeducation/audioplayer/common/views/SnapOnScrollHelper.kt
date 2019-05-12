package kartollika.vkeducation.audioplayer.common.views

import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SnapHelper

fun RecyclerView.attachSnapHelperWithListener(
    snapHelper: SnapHelper,
    behavior: SnapOnScrollListener.Behavior = SnapOnScrollListener.Behavior.NOTIFY_ON_SCROLL,
    onSnapPositionChangeListener: SnapOnScrollListener.OnSnapPositionChangeListener) {
    snapHelper.attachToRecyclerView(this)
    val snapOnScrollListener =
        SnapOnScrollListener(snapHelper, onSnapPositionChangeListener, behavior)
    addOnScrollListener(snapOnScrollListener)
}

fun SnapHelper.getSnapPosition(recyclerView: RecyclerView): Int {
    val layoutManager = recyclerView.layoutManager ?: return RecyclerView.NO_POSITION
    val snapView = findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION
    return layoutManager.getPosition(snapView)
}

class SnapOnScrollListener(
    private val snapHelper: SnapHelper,
    private val onSnapPositionChangeListener: OnSnapPositionChangeListener,
    private val behavior: Behavior) : RecyclerView.OnScrollListener() {

    private var snapPosition = 0

    enum class Behavior {
        NOTIFY_ON_SCROLL, NOTIFY_ON_SCROLL_IDLE
    }

    interface OnSnapPositionChangeListener {
        fun onSnapPositionChange(position: Int)
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (behavior == Behavior.NOTIFY_ON_SCROLL) {
            maybeNotifySnapPositionChange(recyclerView)
        }
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        if (behavior == Behavior.NOTIFY_ON_SCROLL_IDLE && newState == RecyclerView.SCROLL_STATE_IDLE) {
            maybeNotifySnapPositionChange(recyclerView)
        }
    }

    private fun maybeNotifySnapPositionChange(recyclerView: RecyclerView) {
        val snapPosition = snapHelper.getSnapPosition(recyclerView)
        val snapPositionChanged = this.snapPosition != snapPosition
        if (snapPositionChanged) {
            onSnapPositionChangeListener.onSnapPositionChange(snapPosition)
            this.snapPosition = snapPosition
        }
    }
}