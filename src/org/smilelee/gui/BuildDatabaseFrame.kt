package org.smilelee.gui

import org.smilelee.eventhorizonfileanalyzer.EHFileParser
import org.smilelee.eventhorizonfileanalyzer.buildDatabase
import java.awt.Insets
import java.awt.Rectangle
import java.io.File
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JOptionPane

class BuildDatabaseFrame : JFrame("数据库制作") {
    var inputPath: File? = null
        set(value) {
            field = value
            inputLabel.text = value.toString()
        }
    var outputPath: File? = null
        set(value) {
            field = value
            outputLabel.text = value.toString()
        }
    val inputLabel = JLabel("null")
    val outputLabel = JLabel("null")
    
    init {
        this.layout = null
        this.bounds = Rectangle(200, 100, 600, 220)
        this.font = defaultFont
        this.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        
        val maker = JLabel("本程序由笑姐姐制作。请将安装包/assets/bin/Data解压，并将输入目录设为解压得到的文件夹。")
        maker.bounds = Rectangle(20, 20, 550, 20)
        maker.font = defaultFont
        this.add(maker)
        
        inputLabel.font = defaultFont
        inputLabel.bounds = Rectangle(140, 60, 300, 20)
        this.add(inputLabel)
        
        outputLabel.font = defaultFont
        outputLabel.bounds = Rectangle(140, 100, 300, 20)
        this.add(outputLabel)
        
        val getInput = JButton("设置输入目录")
        getInput.bounds = Rectangle(20, 60, 100, 20)
        getInput.font = defaultFont
        getInput.margin = Insets(0, 0, 0, 0)
        getInput.addActionListener {
            inputPath = getSelectedPath()
        }
        this.add(getInput)
        
        val getOutput = JButton("设置输出目录")
        getOutput.bounds = Rectangle(20, 100, 100, 20)
        getOutput.font = defaultFont
        getOutput.margin = Insets(0, 0, 0, 0)
        getOutput.addActionListener {
            outputPath = getSelectedPath()
        }
        this.add(getOutput)
        
        val parse = JButton("生成数据库")
        parse.bounds = Rectangle(20, 140, 100, 20)
        parse.font = defaultFont
        parse.margin = Insets(0, 0, 0, 0)
        parse.addActionListener {
            val inputPath = inputPath
            val outputPath = outputPath
            if (inputPath == null)
                JOptionPane.showMessageDialog(this, contentWithFont("请设置输入目录!"))
            else if (outputPath == null) {
                JOptionPane.showMessageDialog(this, contentWithFont("请设置输出目录!"))
            } else {
                EHFileParser.parseData(inputPath)
                        .buildDatabase(outputPath)
                JOptionPane.showMessageDialog(this, contentWithFont("生成成功!"))
            }
        }
        this.add(parse)
        
        this.isVisible = true
    }
    
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            BuildDatabaseFrame()
        }
    }
}
