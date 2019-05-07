package kartollika.vkeducation.audioplayer.presentation.folder_chooser

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_choose_folder.*
import java.io.File
import java.util.*
import kotlin.properties.Delegates


class FolderChooserActivity : AppCompatActivity() {

    private lateinit var foldersRecyclerView: RecyclerView
    private lateinit var chooseFolderActionView: View
    private lateinit var foldersAdapter: ChooseFolderAdapter
    private var currentFolderIndicatorView: TextView? = null
    private val roots: MutableList<String> = mutableListOf()
    private var previousBackstack: ArrayDeque<String> = ArrayDeque()

    private var currentFolder by Delegates.observable("") { _, _, newValue ->
        run {
            if (currentFolderIndicatorView != null) {
                currentFolderIndicatorView!!.text = newValue
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(kartollika.vkeducation.audioplayer.R.layout.activity_choose_folder)
        initStartFolders()
        foldersRecyclerView = folders_recyclerview
        chooseFolderActionView = choose_folder_textview
        currentFolderIndicatorView = choose_folder_current_textview

        if (savedInstanceState != null) {
            currentFolder = savedInstanceState.getString("current_folder") ?: ""
            previousBackstack =
                savedInstanceState.getSerializable("backstack") as ArrayDeque<String>? ?: ArrayDeque()
        }

        initFinalChooseAction()
        initRecyclerView()

    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putString("current_folder", currentFolder)
        outState?.putSerializable("backstack", previousBackstack)
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
                putExtra("chosen_folder", currentFolder)
            })
            finish()
        }
    }

    private fun initStartFolders() {
        ContextCompat.getExternalFilesDirs(this, null)
        val appsDir = ContextCompat.getExternalFilesDirs(this, null)
        for (file in appsDir) {
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
            currentFolder = previousBackstack.pop()
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