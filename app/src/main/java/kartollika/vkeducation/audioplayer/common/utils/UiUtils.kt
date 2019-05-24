package kartollika.vkeducation.audioplayer.common.utils

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver

fun onRenderFinished(view: View, action: Runnable) {
    view.viewTreeObserver.addOnGlobalLayoutListener(
        object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)

                action.run()
            }
        }
    )
}

fun dpToPx(dp: Float, context: Context): Int {
    return dpToPxFloat(dp, context).toInt()
}

fun dpToPxFloat(dp: Float, context: Context): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        context.resources.displayMetrics
    )
}

fun rotateView(view: View, degree: Float, withAnimation: Boolean, duration: Long) {
    if (withAnimation) {
        view.animate()
            .rotation(degree)
            .duration = duration
    } else {
        view.rotation = degree
    }
}
