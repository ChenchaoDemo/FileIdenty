package com.example.fileidenty.utils

import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.file.Files
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import java.nio.charset.Charset
import java.util.Enumeration
import com.github.junrar.Archive
import com.github.junrar.rarfile.FileHeader
object FileExtractUtils {

    fun unzipFile(zipFile: File): List<File> {
        val extractedFiles = mutableListOf<File>()
        val tempDir = Files.createTempDirectory("unzipped_").toFile()

        ZipFile(zipFile, "GBK").use { zf ->  // 传编码名称字符串
            val entries: Enumeration<ZipArchiveEntry> = zf.entries
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (entry.isDirectory) continue

                val outFile = File(tempDir, entry.name)
                outFile.parentFile.mkdirs()

                zf.getInputStream(entry).use { input ->
                    FileOutputStream(outFile).use { output ->
                        input.copyTo(output)
                    }
                }
                extractedFiles.add(outFile)
            }
        }

        return extractedFiles
    }

    fun unrarFile(rarFile: File): List<File> {
        val extractedFiles = mutableListOf<File>()
        val tempDir = Files.createTempDirectory("unrar_").toFile()
        Archive(rarFile).use { archive ->
            var fileHeader: FileHeader? = archive.nextFileHeader()
            while (fileHeader != null) {
                // 获取文件路径（带子目录）
                val itemPath = fileHeader.fileNameString.trim()
                val currentFile = File(tempDir, itemPath)
                if (fileHeader.isDirectory) {
                    // 是文件夹则创建
                    currentFile.mkdirs()
                } else {
                    // 创建父目录
                    currentFile.parentFile?.mkdirs()
                    // 解压文件流
                    FileOutputStream(currentFile).use { outputStream ->
                        archive.extractFile(fileHeader, outputStream)
                    }
                    extractedFiles.add(currentFile)
                }
                fileHeader = archive.nextFileHeader()
            }
        }
        return extractedFiles
    }

    fun extract7zWithCompress(sevenZFile: File): List<File> {
        val extractedFiles = mutableListOf<File>()
        val tempDir = Files.createTempDirectory("sevenzip_").toFile()

        SevenZFile(sevenZFile).use { archive ->
            var entry = archive.nextEntry
            while (entry != null) {
                println("开始解压 entry: ${entry.name}, size=${entry.size}")
                if (!entry.isDirectory) {
                    val safeName = entry.name.replace("[<>:\"/\\\\|?*]".toRegex(), "_")
                    val outFile = File(tempDir, safeName)
                    outFile.parentFile.mkdirs()

                    FileOutputStream(outFile).use { outStream ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        while (archive.read(buffer).also { bytesRead = it } > 0) {
                            outStream.write(buffer, 0, bytesRead)
                        }
                    }

                    extractedFiles.add(outFile)
                }
                entry = archive.nextEntry
            }
        }

        return extractedFiles
    }


}
