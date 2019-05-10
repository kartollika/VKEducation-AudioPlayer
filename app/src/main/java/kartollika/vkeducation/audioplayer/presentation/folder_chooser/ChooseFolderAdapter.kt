package kartollika.vkeducation.audioplayer.presentation.folder_chooser

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kartollika.vkeducation.audioplayer.R
import kotlinx.android.synthetic.main.folder_item.view.*

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

    override fun onCreateViewHolder(p0: ViewGroup, viewType: Int): ChooseItemViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.folder_item, p0, false)
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
        private val pathTextView = itemView.findViewById<TextView>(R.id.folderPathTextView)

        open fun bind(path: String) {
            pathTextView.text = path
        }
    }

    inner class ChooseFolderViewHolder(itemView: View) : ChooseItemViewHolder(itemView) {

        private val pathContainer = itemView.folderPathTextView

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

        private val pathContainer = itemView.folderPathTextView

        override fun bind(path: String) {
            super.bind(path)
            pathContainer.setOnClickListener {
                chooseFolderListener?.onBackAction()
            }
        }
    }
}