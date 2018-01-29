package org.smilelee.gui

import java.awt.Component
import java.awt.Container
import java.awt.Font
import java.io.File
import javax.swing.JComponent
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JLabel

val defaultFont = Font("Microsoft YaHei (UI)", Font.PLAIN, 12)

fun contentWithFont(content: String) = JLabel(content).also { it.font = defaultFont }

fun Component.setFontForAll(font: Font) {
    this.font = font
    (this as? Container)?.components?.forEach { it.setFontForAll(font) }
}

fun JFrame.getSelectedPath(): File? {
    val fileChooser = JFileChooser()
    fileChooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
    fileChooser.setFontForAll(defaultFont)
    fileChooser.showDialog(this, "确定")
    return fileChooser.selectedFile
}
