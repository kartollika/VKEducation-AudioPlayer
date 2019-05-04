package kartollika.vkeducation.audioplayer.common.mvp

abstract class BasePresenter<T : MvpView>(view: T) : MvpPresenter<T> {

    protected var view: T? = view
}