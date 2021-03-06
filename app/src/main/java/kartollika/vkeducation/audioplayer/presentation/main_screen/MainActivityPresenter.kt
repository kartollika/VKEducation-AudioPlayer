package kartollika.vkeducation.audioplayer.presentation.main_screen

import kartollika.vkeducation.audioplayer.common.mvp.BasePresenter

class MainActivityPresenter(view: MainActivityContract.MainActivityView) :
    BasePresenter<MainActivityContract.MainActivityView>(view),
    MainActivityContract.MainActivityPresenter {

    override fun onOpenFolderAction() {
        view.checkStoragePermission()
    }

    override fun onOpenFolderStoragePermissionGranted() {
        view.openFolderSelectView()
    }
}