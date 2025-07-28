package com.example.fileidenty
import com.example.fileidenty.utils.AlertUtils
import com.example.fileidenty.utils.FileExtractUtils
import com.example.fileidenty.utils.IdentyUtils
import javafx.application.Application
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.ContextMenuEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.*
import javafx.stage.Stage
import java.io.*
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

data class FileItem(val name: String, val path: String, val size: String, val imageView: ImageView?)

class HelloApplication : Application() {

    private val fileData = FXCollections.observableArrayList<FileItem>()
    private val tableView = TableView<FileItem>()

    override fun start(primaryStage: Stage) {
        val dropArea = VBox()
        dropArea.style = "-fx-border-color: #aaa; -fx-background-color: #f4f4f4;"
        dropArea.prefWidth = 300.0
        dropArea.children.add(Label("将文件拖拽到这里"))
        dropArea.alignment = Pos.CENTER

        dropArea.setOnDragOver { event ->
            if (event.dragboard.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY)
            }
            event.consume()
        }

        dropArea.setOnDragDropped { event ->
            val db = event.dragboard
            if (db.hasFiles()) {
                db.files.forEach { file ->
                    when {
                        file.name.lowercase().endsWith(".zip") -> {
                            try {
                                val extracted = FileExtractUtils.unzipFile(file)
                                extracted.forEach { addFileToTable(it) }
                            } catch (e: Exception) {
                                println("解压 ZIP 文件出错：${e.message}，请手动解压拖拽")
                                AlertUtils.showAlert("解压 ZIP 文件出错，请手动解压拖拽")
                            }
                        }
                        file.name.lowercase().endsWith(".rar") -> {
                            try {
                                val extractedFiles = FileExtractUtils.unrarFile(file)
                                extractedFiles.forEach { extracted ->
                                    addFileToTable(extracted)
                                }
                            } catch (e: Exception) {
                                println("解压 RAR 文件出错：${e.message}，请手动解压拖拽")
                                AlertUtils.showAlert("解压 RAR 文件出错,请手动解压拖拽")
                            }
                        }
                        file.name.lowercase().endsWith(".7z") -> {
                            try {
                                val extractedFiles = FileExtractUtils.extract7zWithCompress(file)
                                extractedFiles.forEach { extracted ->
                                    addFileToTable(extracted)
                                }
                            } catch (e: Exception) {
                                println("解压 7z 文件出错：${e.message}，请手动解压拖拽")
                                AlertUtils.showAlert("解压 7z 文件出错,请手动解压拖拽")
                            }
                        }
                        else -> {
                            addFileToTable(file)
                        }
                    }
                }
                event.isDropCompleted = true
            } else {
                event.isDropCompleted = false
            }
            event.consume()
        }

        val nameCol = TableColumn<FileItem, String>("文件名")
        nameCol.setCellValueFactory { SimpleStringProperty(it.value.name) }
        nameCol.prefWidth = 150.0

        val sizeCol = TableColumn<FileItem, String>("大小")
        sizeCol.setCellValueFactory { SimpleStringProperty(it.value.size) }
        sizeCol.prefWidth = 100.0

        val pathCol = TableColumn<FileItem, String>("路径")
        pathCol.setCellValueFactory { SimpleStringProperty(it.value.path) }
        pathCol.prefWidth = 250.0

        val thumbCol = TableColumn<FileItem, ImageView>("预览")
        thumbCol.setCellValueFactory { javafx.beans.property.SimpleObjectProperty(it.value.imageView) }
        thumbCol.prefWidth = 60.0

        tableView.columns.addAll(thumbCol, nameCol, sizeCol, pathCol)
        tableView.items = fileData
        tableView.columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY

        tableView.setRowFactory {
            val row = TableRow<FileItem>()
            val contextMenu = ContextMenu()
            val removeItem = MenuItem("删除此项")
            removeItem.setOnAction {
                fileData.remove(row.item)
            }
            contextMenu.items.add(removeItem)
            val recognizeItem = MenuItem("识别内容")
            recognizeItem.setOnAction {
                val fileItem = row.item
                if (fileItem != null) {
                    IdentyUtils.identyContent(fileItem)
                }
            }
            contextMenu.items.addAll(recognizeItem, removeItem)
            row.setOnContextMenuRequested { e: ContextMenuEvent ->
                if (!row.isEmpty) {
                    contextMenu.show(row, e.screenX, e.screenY)
                }
            }
            row
        }

        val clearButton = Button("清空列表")
        clearButton.setOnAction {
            fileData.clear()
        }

        val rightPane = VBox(10.0, Label("文件信息列表："), tableView, clearButton)
        rightPane.padding = Insets(10.0)

        val root = HBox(dropArea, rightPane)
        val scene = Scene(root, 900.0, 500.0)
        primaryStage.title = "工信局文件识别系统"
        primaryStage.scene = scene
        primaryStage.show()
    }

    private fun addFileToTable(file: File) {
        val imageView = if (file.isImage()) {
            ImageView(Image(file.toURI().toString(), 50.0, 50.0, true, true))
        } else null

        val item = FileItem(
            name = file.name,
            path = file.absolutePath,
            size = readableFileSize(file.length()),
            imageView = imageView
        )
        fileData.add(item)
    }

    private fun readableFileSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }

    private fun File.isImage(): Boolean {
        return name.lowercase().matches(Regex(".*\\.(png|jpg|jpeg|gif|bmp)$"))
    }
}

fun main() {
    Application.launch(HelloApplication::class.java)
}