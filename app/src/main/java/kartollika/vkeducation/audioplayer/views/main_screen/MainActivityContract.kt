package kartollika.vkeducation.audioplayer.views.main_screen

import kartollika.vkeducation.audioplayer.common.mvp.MvpView

interface MainActivityContract {
    interface MainActivityView : MvpView {
        fun openFolderSelectView()
        fun checkStoragePermission()
    }

    interface MainActivityPresenter {
        fun onOpenFolderAction()
        fun onOpenFolderStoragePermissionGranted()
    }
}