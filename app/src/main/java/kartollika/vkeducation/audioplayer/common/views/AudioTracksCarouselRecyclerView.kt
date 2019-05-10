package kartollika.vkeducation.audioplayer.common.views

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.Log

class AudioTracksCarouselRecyclerView(context: Context, attrs: AttributeSet?, defStyle: Int) :
    RecyclerView(context, attrs, defStyle) {

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    fun <T : ViewHolder> setupAdapter(adapter: Adapter<T>) {
        layoutManager =
            AlphaAndZoomCentralLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        adapter.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                post {
                    if (childCount > 0) {
                        val sidePadding = (width / 2) - (getChildAt(0).width / 2)
                        setPadding(sidePadding, 0, sidePadding, 0)
                        Log.d(
                            "padding",
                            "width = $width, childwidth = ${getChildAt(0).width / 2}, sidepadding = $sidePadding )"
                        )
                    }
                }
            }
        })
        this.adapter = adapter
        adapter.notifyDataSetChanged()
    }
}