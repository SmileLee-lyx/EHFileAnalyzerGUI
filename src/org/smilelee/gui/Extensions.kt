package org.smilelee.gui

import java.awt.Component
import java.awt.Container
import java.awt.Font

fun Component.setFontForAll(font: Font) {
    this.font = font
    (this as? Container)?.components?.forEach { it.setFontForAll(font) }
}
