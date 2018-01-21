package org.smilelee.gui

import org.smilelee.eventhorizonfileanalyzer.EHFileParser
import org.smilelee.kotson.set
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Insets
import java.awt.Rectangle
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane

class ShipFrame(val editorFrame: EHFileEditorFrame, layout: String, shipName: String) : JFrame(shipName) {
    var shipSize = Math.ceil(Math.sqrt(layout.length.toDouble())).toInt()
    var blocks: MutableList<MutableList<JPanel>>
    
    var shipLayout: CharArray = CharArray(shipSize * shipSize) { if (it < layout.length) layout[it] else '0' }
    var colorChosen = '0'
    var colorChosenPanel = JPanel()
    
    init {
        this.layout = null
        this.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent?) {
                super.windowClosing(e)
                editorFrame.shipFrame = null
                this@ShipFrame.dispose()
            }
        })
        this.bounds = Rectangle(200, 200, 600, 600)
        this.isVisible = true
        
        val panel = JPanel()
        panel.layout = null
        blocks = MutableList(shipSize) { i ->
            MutableList<JPanel>(shipSize) { j ->
                Block(i, j).also { block -> panel.add(block) }
            }
        }
        panel.preferredSize = Dimension(20 * shipSize, 20 * shipSize)
        
        val shipBuilderScrollPane = JScrollPane(panel)
        shipBuilderScrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        shipBuilderScrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS
        shipBuilderScrollPane.bounds = Rectangle(20, 60, 500, 500)
        shipBuilderScrollPane.isVisible = true
        this.add(shipBuilderScrollPane)
        
        colorChosenPanel.bounds = Rectangle(20, 20, 21, 21)
        colorChosenPanel.border = BorderFactory.createLineBorder(Color.BLACK)
        colorChosenPanel.background = colors[colorChosen]
        this.add(colorChosenPanel)
        ('0'..'5').forEach { this.add(ChooseColorPanel(it)) }
        
        val saveButton = JButton("保存船体配置")
        saveButton.bounds = Rectangle(200, 20, 150, 20)
        saveButton.isVisible = true
        saveButton.font = defaultFont
        saveButton.addActionListener {
            val json = EHFileParser.defaultJsonParser.parse(editorFrame.originText)
            json["Layout"] = String(shipLayout)
            val jsonString = EHFileParser.defaultGson.toJson(json)
            editorFrame.originText = jsonString
            editorFrame.editor.text = jsonString
        }
        this.add(saveButton)
        
        val sizeHintLabel = JLabel("船体大小:")
        sizeHintLabel.bounds = Rectangle(370, 20, 60, 20)
        sizeHintLabel.isVisible = true
        sizeHintLabel.font = defaultFont
        this.add(sizeHintLabel)
        
        val sizeLabel = JLabel(shipSize.toString())
        sizeLabel.horizontalAlignment = JLabel.CENTER
        sizeLabel.bounds = Rectangle(450, 20, 40, 20)
        sizeLabel.isVisible = true
        sizeLabel.font = defaultFont
        this.add(sizeLabel)
        
        val incSizeButton = JButton("+")
        incSizeButton.bounds = Rectangle(430, 20, 20, 20)
        incSizeButton.isVisible = true
        incSizeButton.font = defaultFont
        incSizeButton.margin = Insets(0, 0, 0, 0)
        incSizeButton.addActionListener inc@ {
            ++shipSize
            sizeLabel.text = shipSize.toString()
            shipLayout = CharArray(shipSize * shipSize) { index ->
                val i = index / shipSize
                val j = index % shipSize
                if (i < shipSize - 1 && j < shipSize - 1) shipLayout[i * (shipSize - 1) + j] else '0'
            }
            blocks.forEach { it.forEach { panel.remove(it) } }
            blocks = MutableList(shipSize) { i ->
                MutableList<JPanel>(shipSize) { j ->
                    Block(i, j).also { block -> panel.add(block) }
                }
            }
            panel.preferredSize = Dimension(20 * shipSize, 20 * shipSize)
            panel.isVisible = false
            panel.isVisible = true
        }
        this.add(incSizeButton)
        val decSizeButton = JButton("-")
        decSizeButton.bounds = Rectangle(490, 20, 20, 20)
        decSizeButton.isVisible = true
        decSizeButton.font = defaultFont
        decSizeButton.margin = Insets(0, 0, 0, 0)
        decSizeButton.addActionListener dec@ {
            if (shipSize == 1) return@dec
            --shipSize
            sizeLabel.text = shipSize.toString()
            shipLayout = CharArray(shipSize * shipSize) { index ->
                val i = index / shipSize
                val j = index % shipSize
                shipLayout[i * (shipSize + 1) + j]
            }
            blocks.forEach { it.forEach { panel.remove(it) } }
            blocks = MutableList(shipSize) { i ->
                MutableList<JPanel>(shipSize) { j ->
                    Block(i, j).also { block -> panel.add(block) }
                }
            }
            panel.preferredSize = Dimension(20 * shipSize, 20 * shipSize)
            panel.isVisible = false
            panel.isVisible = true
        }
        this.add(decSizeButton)
    }
    
    var mouseDown = false
    
    inner class Block(val i: Int, val j: Int) : JPanel() {
        val index get() = i * shipSize + j
        
        init {
            background = colors[shipLayout[index]]
            bounds = Rectangle(j * 20, i * 20, 21, 21)
            border = BorderFactory.createLineBorder(Color.BLACK)
            object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent) {
                    mouseDown = true
                    shipLayout[index] = colorChosen
                    background = colors[colorChosen]
                }
                
                override fun mouseEntered(e: MouseEvent?) {
                    if (mouseDown) {
                        shipLayout[index] = colorChosen
                        background = colors[colorChosen]
                    }
                }
                
                override fun mouseReleased(e: MouseEvent) {
                    mouseDown = false
                }
            }.let {
                addMouseListener(it)
                addMouseMotionListener(it)
            }
        }
    }
    
    inner class ChooseColorPanel(val color: Char) : JPanel() {
        init {
            val i = color - '0'
            background = colors[color]
            bounds = Rectangle(60 + i * 20, 20, 21, 21)
            border = BorderFactory.createLineBorder(Color.BLACK)
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent?) {
                    colorChosen = color
                    colorChosenPanel.background = colors[color]
                }
            })
        }
    }
    
    companion object {
        val colors = hashMapOf(
                '0' to Color(0xffffff),
                '1' to Color(0x2080ff),
                '2' to Color(0x00c000),
                '3' to Color(0x008040),
                '4' to Color(0xff4040),
                '5' to Color(0xffc000)
        )
        
        val defaultFont = Font("Microsoft YaHei (UI)", Font.PLAIN, 12)
    }
}
