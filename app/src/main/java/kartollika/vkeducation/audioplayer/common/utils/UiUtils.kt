package kartollika.vkeducation.audioplayer.common.utils

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