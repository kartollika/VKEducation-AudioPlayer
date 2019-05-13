package kartollika.vkeducation.audioplayer.common.views

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import kotlin.math.sign


class AlphaAndZoomCentralLayoutManager(
    context: Context?, orientation: Int, reverseLayout: Boolean) :
    LinearLayoutManager(context, orientation, reverseLayout) {

    var lowerBoundAlpha: Float = 0.5f
    var upperBoundAplha = 1f
    var scaleToMiniCoefficient: Float = 0.82730923694779116465863453815261f
    var midpointThresholdAlpha = 20
    var midpointThresholdScale = 20


    override fun scrollHorizontallyBy(
        dx: Int, recycler: RecyclerView.Recycler?, state: RecyclerView.State?): Int {
        val scrolled = super.scrollHorizontallyBy(dx, recycler, state)

        calculateScale()
        calculateFade()
        return scrolled
    }

    /** f(X1)-( f(X1) - f(X2) )*(X - X1)/(X2 - X1)
     * X1 - border to set upper scale bound
     * X2 = border to set lower scale bound
     * X - midpoint of view
     * */
    private fun calculateScale() {
        val midpoint = width / 2f

        for (i in 0..childCount) {
            val view = getChildAt(i)
            view?.let {
                val childMidpoint = (getDecoratedRight(view) + getDecoratedLeft(view)) / 2f
                val sideSign = sign(childMidpoint - midpoint)
                val x1 = midpoint + sideSign * midpointThresholdScale
                val x2 = if (sideSign >= 0) {
                    width
                } else {
                    0
                }

                val scale = if (childMidpoint * sideSign >= x2) {
                    scaleToMiniCoefficient
                } else {
                    (1f - (1f - scaleToMiniCoefficient) * (childMidpoint - x1) / (x2 - x1))
                }

                view.scaleX = scale
                view.scaleY = scale
            }
        }
    }

    /** f(X1)-( f(X1) - f(X2) )*(X - X1)/(X2 - X1)
     * X1 - border to set upper alpha bound
     * X2 = border to set lower alpha bound
     * X - midpoint of view
     * */
    private fun calculateFade() {
        val midpoint = width / 2f

        for (i in 0..childCount) {
            val view = getChildAt(i)
            view?.let {
                val childMidpoint = (getDecoratedRight(view) + getDecoratedLeft(view)) / 2f
                val sideSign = sign(childMidpoint - midpoint)

                val x1 = midpoint + sideSign * midpointThresholdAlpha
                val x2 = if (sideSign >= 0) {
                    width
                } else {
                    0
                }

                if (childMidpoint * sideSign <= midpointThresholdAlpha) {
                    view.alpha = 1f
                } else {
                    view.alpha =
                        (upperBoundAplha - (upperBoundAplha - lowerBoundAlpha) * (childMidpoint - x1) / (x2 - x1))
                }
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