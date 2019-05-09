package kartollika.vkeducation.audioplayer.presentation.folder_chooser

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kartollika.vkeducation.audioplayer.R

class ChooseFolderAdapter(private var folders: List<String>) :
    RecyclerView.Adapter<ChooseFolderAdapter.ChooseItemViewHolder>() {

    init {
        folders = folders.toMutableList().apply { add(0, "/..") }
    }

    private val TYPE_FOLDER = 0
    private val TYPE_BACK = 1

    interface ChooseFolderListener {
        fun onFolderChosen(folder: String)
        fun onBackAction()
    }

    var chooseFolderListener: ChooseFolderListener? = null

    fun setFolders(folders: List<String>) {
        this.folders = folders.toMutableList().apply {
            add(0, "/..")
        }
    }

    private fun getHolderView(context: Context, layout: Int): View {
        return LayoutInflater.from(context).inflate(layout, null)
    }

    override fun onCreateViewHolder(p0: ViewGroup, viewType: Int): ChooseItemViewHolder {
        val view = getHolderView(p0.context, R.layout.folder_item)
        return when (viewType) {
            TYPE_BACK -> ChooseBackFolderViewHolder(view)
            TYPE_FOLDER -> ChooseFolderViewHolder(view)
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return TYPE_BACK
        }
        return TYPE_FOLDER
    }

    override fun getItemCount(): Int = folders.size

    override fun onBindViewHolder(p0: ChooseItemViewHolder, p1: Int) {
        p0.bind(folders[p1])
    }

    abstract inner class ChooseItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val pathTextView = itemView.findViewById<TextView>(R.id.folder_path_textview)

        open fun bind(path: String) {
            pathTextView.text = path
        }
    }

    inner class ChooseFolderViewHolder(itemView: View) : ChooseItemViewHolder(itemView) {

        private val pathContainer = itemView.findViewById<View>(R.id.folder_path_container_root)

        override fun bind(path: String) {
            super.bind(path)
            pathContainer.setOnClickListener {
                chooseFolderListener?.onFolderChosen(
                    path
                )
            }
        }
    }

    inner class ChooseBackFolderViewHolder(itemView: View) : ChooseItemViewHolder(itemView) {

        private val pathContainer = itemView.findViewById<View>(R.id.folder_path_container_root)

        override fun bind(path: String) {
            super.bind(path)
            pathContainer.setOnClickListener {
                chooseFolderListener?.onBackAction()
            }
        }
    }
}