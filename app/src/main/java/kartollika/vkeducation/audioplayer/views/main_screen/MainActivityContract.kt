package kartollika.vkeducation.audioplayer.views.main_screen

import kartollika.vkeducation.audioplayer.common.mvp.MvpView

interface MainActivityContract {
    interface MainActivityView : MvpView {
        fun openFolderSelectView()
    }

    interface MainActivityPresenter {
        fun onOpenFolderAction()
    }
}