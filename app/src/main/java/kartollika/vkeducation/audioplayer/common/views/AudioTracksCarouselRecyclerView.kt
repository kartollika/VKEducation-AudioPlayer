package kartollika.vkeducation.audioplayer.common.views

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View

class AudioTracksCarouselRecyclerView(context: Context, attrs: AttributeSet?, defStyle: Int) :
    RecyclerView(context, attrs, defStyle) {

    private var itemsMaxSize = 0

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    fun <T : ViewHolder> setupCarouselRecyclerView(
        adapter: Adapter<T>, layoutManager: LayoutManager) {
        this.layoutManager = layoutManager

        adapter.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                post {
                    if (childCount > 0) {
                        val sidePadding = (width / 2) - (getChildAt(0).width / 2)
                        setPadding(sidePadding, 0, sidePadding, 0)
                    }
                }
            }
        })
        this.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)

        val params = child?.layoutParams
        params?.height = itemsMaxSize
        params?.width = itemsMaxSize
        child?.layoutParams = params
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(widthSpec, heightSpec)
        itemsMaxSize = measuredHeight
    }
}