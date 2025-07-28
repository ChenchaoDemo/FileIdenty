package com.example.fileidenty.utils

import com.example.fileidenty.FileItem
import net.sourceforge.tess4j.Tesseract
import net.sourceforge.tess4j.TesseractException
import java.io.File

object IdentyUtils {
    fun identyContent(item: FileItem) {
        val result = recognizeText(
            File(item.path),               // 图片文件
            tessDataPath = "./tessdata",   // 相对路径（项目根目录下）
            language = "chi_sim"           // 使用简体中文
        )
        println("识别结果：\n$result")
    }

    fun recognizeText(imageFile: File, tessDataPath: String = "tessdata", language: String = "chi_sim"): String {
        val tesseract = Tesseract()
        tesseract.setDatapath(tessDataPath)  // 语言数据包路径
        tesseract.setLanguage(language)      // 语言：chi_sim简体中文，eng英文等

        return try {
            tesseract.doOCR(imageFile)
        } catch (e: TesseractException) {
            e.printStackTrace()
            "识别失败：${e.message}"
        }
    }

}

