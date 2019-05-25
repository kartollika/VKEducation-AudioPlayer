package kartollika.vkeducation.audioplayer.presentation.folder_chooser

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_choose_folder.*
import java.io.File
import java.util.*
import kotlin.properties.Delegates


class FolderChooserActivity : AppCompatActivity() {

    companion object {
        const val FOLDER_CHOOSE_REQUEST_CODE = 102
        const val CHOSEN_FOLDER_KEY = "chosen_folder"

        private const val CURRENT_FOLDER_KEY = "current_folder"
        private const val BACKSTACK_KEY = "backstack"
    }

    private lateinit var foldersAdapter: ChooseFolderAdapter
    private val roots: MutableList<String> = mutableListOf()
    private var previousBackstack: ArrayDeque<String> = ArrayDeque()

    private var currentFolder by Delegates.observable("") { _, _, newValue ->
        run {
            currentFolderTextView?.text = newValue
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(kartollika.vkeducation.audioplayer.R.layout.activity_choose_folder)
        initStartFolders()

        if (savedInstanceState != null) {
            currentFolder = savedInstanceState.getString(CURRENT_FOLDER_KEY) ?: ""
            previousBackstack =
                savedInstanceState.getSerializable(BACKSTACK_KEY) as ArrayDeque<String>?
                    ?: ArrayDeque()
        }

        initFinalChooseAction()
        initRecyclerView()

    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putString(CURRENT_FOLDER_KEY, currentFolder)
        outState?.putSerializable(BACKSTACK_KEY, previousBackstack)
    }

    override fun onBackPressed() {
        if (currentFolder == "") {
            super.onBackPressed()
            return
        }
        returnBackToPreviousFolder()
    }

    private fun initFinalChooseAction() {
        chooseFolderActionView.setOnClickListener {
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra(CHOSEN_FOLDER_KEY, currentFolder)
            })
            finish()
        }
    }

    private fun initStartFolders() {
        val appsDir = ContextCompat.getExternalFilesDirs(this, null)
        for (file in appsDir) {
            if (file == null) {
                continue
            }
            roots.add(file.parentFile.parentFile.parentFile.parentFile.absolutePath)
        }
    }

    private fun initRecyclerView() {
        foldersAdapter = ChooseFolderAdapter(
            if (currentFolder == "") {
                roots
            } else {
                getSubDirectories(currentFolder)
            }
        )

        foldersAdapter.apply {
            chooseFolderListener = object : ChooseFolderAdapter.ChooseFolderListener {
                override fun onFolderChosen(folder: String) {
                    proceedTo(folder)
                }

                override fun onBackAction() {
                    onBackPressed()
                }
            }
        }

        foldersRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@FolderChooserActivity)
            adapter = foldersAdapter
            addItemDecoration(
                DividerItemDecoration(
                    this@FolderChooserActivity, LinearLayoutManager.VERTICAL
                )
            )
        }
        foldersAdapter.notifyDataSetChanged()
    }

    private fun proceedTo(selectedPath: String) {
        if (currentFolder != "") {
            previousBackstack.add(currentFolder)
        }
        currentFolder = if (currentFolder == "") {
            selectedPath
        } else {
            "$currentFolder/$selectedPath"
        }
        foldersAdapter.setFolders(getSubDirectories(currentFolder))
        foldersAdapter.notifyDataSetChanged()
    }

    private fun returnBackToPreviousFolder() {
        if (previousBackstack.size == 0) {
            foldersAdapter.setFolders(roots)
            currentFolder = ""
        } else {
            currentFolder = previousBackstack.pollLast()
            foldersAdapter.setFolders(getSubDirectories(currentFolder))
        }
        foldersAdapter.notifyDataSetChanged()
    }

    private fun getSubDirectories(path: String): List<String> {
        val file = File(path)
        val subDirectories = mutableListOf<String>()
        if (!file.exists() || !file.isDirectory) {
            return subDirectories
        }

        for (child in file.listFiles()) {
            if (child.isDirectory) {
                subDirectories.add(child.name)
            }
        }
        return subDirectories.apply { sort() }
    }
}