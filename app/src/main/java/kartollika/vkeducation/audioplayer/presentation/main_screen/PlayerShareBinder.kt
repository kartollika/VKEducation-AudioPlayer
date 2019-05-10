package kartollika.vkeducation.audioplayer.presentation.main_screen

import android.os.Binder

interface OnBinderResultListener {
    fun onReceiveBinder(binder: Binder)
}

interface PlayerShareBinder {
    fun getBinder(listener: OnBinderResultListener): Binder?
}