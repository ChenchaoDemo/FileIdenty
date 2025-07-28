package com.example.fileidenty.utils

import javafx.scene.control.Alert

object AlertUtils {

    fun showAlert(message: String) {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.title = "解压错误"
        alert.headerText = null
        alert.contentText = message
        alert.showAndWait()
    }

}