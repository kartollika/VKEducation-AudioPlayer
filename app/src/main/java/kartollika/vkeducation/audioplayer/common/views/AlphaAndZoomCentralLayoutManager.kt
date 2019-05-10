package kartollika.vkeducation.audioplayer.common.views

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import kotlin.math.sign


class AlphaAndZoomCentralLayoutManager(context: Context?, orientation: Int, reverseLayout: Boolean) :
    LinearLayoutManager(context, orientation, reverseLayout) {

    private val mShrinkAmount = 0.15f
    private val mShrinkDistance = 0.9f
    private val alphaLowerLimit = 0.5f

    override fun scrollHorizontallyBy(
        dx: Int, recycler: RecyclerView.Recycler?, state: RecyclerView.State?): Int {
        val scrolled = super.scrollHorizontallyBy(dx, recycler, state)

        calculateScale()
        calculateFade()
        return scrolled
    }

    private fun calculateScale() {
        val midpoint = width / 2f
        val d0 = 0f
        val d1 = mShrinkDistance * midpoint
        val s0 = 1f
        val s1 = 1f - mShrinkAmount

        for (i in 0..childCount) {
            val view = getChildAt(i)
            view?.let {
                val childMidpoint = (getDecoratedRight(view) + getDecoratedLeft(view)) / 2f
                val d = Math.min(d1, Math.abs(midpoint - childMidpoint))
                val scale = s0 + (s1 - s0) * (d - d0) / (d1 - d0)
                view.scaleX = scale
                view.scaleY = scale
            }
        }
    }

    /** f(X1)-( f(X1) - f(X3) )*(X - X1)/(X2 - X1)
     * X1 - border to set alpha = f(X1) = 1
     * X2 = border to set alpha f(X2) = 0.5
     * X3 - midpoint of view
     * */
    private fun calculateFade() {
        val midpoint = width / 2f

        for (i in 0..childCount) {
            val view = getChildAt(i)
            view?.let {
                val childMidpoint = (getDecoratedRight(view) + getDecoratedLeft(view)) / 2f
                val sideSign = sign(childMidpoint - midpoint)
                val x2 = if (sideSign >= 0) {
                    width
                } else {
                    0
                }
                view.alpha =
                    (1f - (1f - 0.5) * (childMidpoint - midpoint + sideSign * 20) / (x2 - midpoint + sideSign * 20)).toFloat()
            }
        }
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        super.onLayoutChildren(recycler, state)
        scrollHorizontallyBy(0, recycler, state)
    }

    override fun canScrollHorizontally(): Boolean {
        return childCount >= 2

    }
}