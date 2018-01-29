package org.smilelee.gui

import com.google.gson.JsonSyntaxException
import org.smilelee.eventhorizonfileanalyzer.Data
import org.smilelee.eventhorizonfileanalyzer.EHFileContentEditor
import org.smilelee.eventhorizonfileanalyzer.EHFileParser
import org.smilelee.eventhorizonfileanalyzer.values
import org.smilelee.kotson.get
import org.smilelee.kotson.string
import java.awt.Font
import java.awt.Rectangle
import java.io.File
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

class EHFileEditorFrame : JFrame("EH 文件编辑器") {
    private var path: File? = null
        set(value) {
            showEditor(value)
            field = value
        }
    private val hint = JLabel("请将安装包/assets/bin/Data解压，并选择解压得到的文件夹。")
    private val selectPathButton = JButton("选择文件夹")
    var lastSelected: FileSelected? = null
    var originText: String? = null
    val editor = JTextArea()
    
    var shipFrame: ShipFrame? = null
    
    init {
        this.layout = null
        this.bounds = Rectangle(200, 100, 600, 600)
        this.font = defaultFont
        this.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        
        hint.bounds = Rectangle(20, 20, 400, 20)
        hint.isVisible = true
        hint.font = defaultFont
        this.add(hint)
        
        selectPathButton.bounds = Rectangle(440, 20, 100, 20)
        selectPathButton.isVisible = true
        selectPathButton.font = defaultFont
        selectPathButton.addActionListener { path = getSelectedPath() }
        this.add(selectPathButton)
        
        this.isVisible = true
    }
    
    private fun showEditor(path: File?) {
        if (path == null) return
        
        val editorScrollPane = JScrollPane(editor)
        editorScrollPane.bounds = Rectangle(280, 60, 280, 400)
        editorScrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        editorScrollPane.isVisible = true
        this.add(editorScrollPane)
        
        val saveButton = JButton("保存")
        saveButton.bounds = Rectangle(280, 480, 100, 20)
        saveButton.font = defaultFont
        saveButton.isVisible = true
        saveButton.addActionListener { save() }
        this.add(saveButton)
        
        val arrangeButton = JButton("整理所有Json")
        arrangeButton.bounds = Rectangle(440, 20, 120, 20)
        arrangeButton.font = defaultFont
        arrangeButton.isVisible = true
        arrangeButton.addActionListener { EHFileContentEditor.editAll(path) }
        this.add(arrangeButton)
        
        val shipBuilderButton = JButton("打开船体编辑器")
        shipBuilderButton.bounds = Rectangle(400, 480, 150, 20)
        shipBuilderButton.font = defaultFont
        shipBuilderButton.isVisible = false
        shipBuilderButton.addActionListener shipBuilder@ {
            val shipBuilderFrame = shipFrame
            if (shipBuilderFrame == null) {
                if (!checkForSaving()) return@shipBuilder
                val json = EHFileParser.defaultJsonParser.parse(originText)
                val layout = json["Layout"].string
                val name = json["Name"].string
                this.shipFrame = ShipFrame(this, layout, name)
            } else {
                JOptionPane.showMessageDialog(
                        this, contentWithFont("船体配置编辑器已经打开!"), "提示", JOptionPane.INFORMATION_MESSAGE
                )
                shipBuilderFrame.requestFocus()
            }
        }
        this.add(shipBuilderButton)
        
        val root = DefaultMutableTreeNode("文件")
        val children = HashMap<Int, DefaultMutableTreeNode>()
        Data.nameFromItemType.entries
                .sortedBy { (itemType, _) -> itemType }
                .forEach { (itemType, name) ->
                    val child = DefaultMutableTreeNode(name)
                    children[itemType] = child
                    root.add(child)
                }
        selectPathButton.isVisible = false
        val data = EHFileParser.parseData(path)
        data.values.sortedBy { it.id }.forEach {
            children[it.itemType]!!.add(DefaultMutableTreeNode(FileSelected(it.name, it.file, it.itemType)))
        }
        val fileSelector = JTree(root)
        fileSelector.font = defaultFont
        fileSelector.isVisible = true
        fileSelector.addTreeSelectionListener select@ { event ->
            val selected = (event.path.lastPathComponent as DefaultMutableTreeNode?)?.userObject
                    as? FileSelected ?: return@select
            if (selected == lastSelected) return@select
            if (!checkForSaving()) return@select
            lastSelected = selected
            val jsonString = EHFileParser.getJson(selected.file)
            editor.text = jsonString
            originText = jsonString
            hint.text = selected.file.name
            shipBuilderButton.isVisible = selected.itemType == Data.ItemType.SHIP ||
                    selected.itemType == Data.ItemType.SATELLITE
        }
        
        val treeScrollPane = JScrollPane(fileSelector)
        treeScrollPane.bounds = Rectangle(20, 60, 240, 480)
        treeScrollPane.isWheelScrollingEnabled = true
        treeScrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        treeScrollPane.isVisible = true
        this.add(treeScrollPane)
        
        hint.text = "文件处理完毕。"
    }
    
    fun checkForSaving() = if (shipFrame != null) {
        JOptionPane.showMessageDialog(this, contentWithFont("请保存船体配置!"), "请注意保存", JOptionPane.ERROR_MESSAGE)
        false
    } else originText == null || editor.text == originText || askForSaving()
    
    fun askForSaving() = when (JOptionPane.showConfirmDialog(
            this, contentWithFont("是否保存?"), "请注意保存", JOptionPane.OK_CANCEL_OPTION
    )) {
        JOptionPane.OK_OPTION     -> save()
        JOptionPane.CANCEL_OPTION -> true
        else                      -> throw IllegalStateException()
    }
    
    fun save(): Boolean {
        val lastSelected = lastSelected ?: return true
        val jsonString = try {
            EHFileParser.defaultGson.toJson(
                    EHFileParser.defaultJsonParser.parse(editor.text)
            )
        } catch (_: JsonSyntaxException) {
            JOptionPane.showMessageDialog(this, contentWithFont("请确认Json格式!"), "警告", JOptionPane.ERROR_MESSAGE)
            return false
        }
        editor.text = jsonString
        originText = jsonString
        EHFileContentEditor.writeJson(lastSelected.file, jsonString)
        JOptionPane.showMessageDialog(this, contentWithFont("保存成功。"), "提示", JOptionPane.INFORMATION_MESSAGE)
        return true
    }
    
    data class FileSelected(val name: String, val file: File, val itemType: Int) {
        override fun toString() = name
    }
    
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            EHFileEditorFrame()
        }
    }
}
