package kartollika.vkeducation.audioplayer.common.mvp

abstract class BasePresenter<T : MvpView>(protected var view: T) : MvpPresenter<T>