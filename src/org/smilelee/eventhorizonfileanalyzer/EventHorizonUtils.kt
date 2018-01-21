package org.smilelee.eventhorizonfileanalyzer

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.google.gson.JsonSyntaxException
import org.smilelee.eventhorizonfileanalyzer.VersionComparator.Difference.DifferenceType.CHANGED_FILE
import org.smilelee.eventhorizonfileanalyzer.VersionComparator.Difference.DifferenceType.DELETED_FILE
import org.smilelee.eventhorizonfileanalyzer.VersionComparator.Difference.DifferenceType.NEW_FILE
import org.smilelee.kotson.get
import org.smilelee.kotson.int
import org.smilelee.kotson.obj
import org.smilelee.kotson.registerTypeAdapter
import java.io.File
import kotlin.math.max

inline fun String.toByteList() = toByteArray().asList()

object EHFileParser {
    fun isJsonFile(file: File) = file.isFile && file.readText().contains("ItemType")
    
    fun getJson(file: File) = if (file.isFile) getJson(file.readBytes()) else ""
    fun getJson(bytes: ByteArray): String {
        if (!String(bytes).contains("\"ItemType\"")) return ""
        val assetNameLength = (bytes[4096].toInt() shl 0x00) or
                (bytes[4097].toInt() shl 0x08) or
                (bytes[4098].toInt() shl 0x10) or
                (bytes[4099].toInt() shl 0x18)
        val jsonIndex = 4107 + assetNameLength - (assetNameLength + 3) % 4
        val jsonLastIndex = bytes.lastIndexOf('}'.toByte())
        return String(bytes.asList().subList(jsonIndex, jsonLastIndex + 1).toByteArray())
    }
    
    fun getHeader(content: ByteArray): List<Byte> {
        return content.asList().subList(0, 4096)
    }
    
    fun fileFromName(path: File, name: String) = File("${path.absolutePath}\\$name")
    
    val defaultGson: Gson = GsonBuilder()
            .registerTypeAdapter(
                    JsonSerializer<Double> { src, _, _ ->
                        if (src == src.toLong().toDouble()) JsonPrimitive(src.toLong()) else JsonPrimitive(src)
                    }
            )
            .setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .create()
    val defaultJsonParser = JsonParser()
    
    fun parseData(path: File) = Data().also { data ->
        path.listFiles().forEach { eHFile ->
            parseFile(eHFile, data)
        }
        data.shipBuilds.values.forEach {
            it.components.forEach {
                it.data = data
            }
        }
        data.satelliteBuilds.values.forEach {
            it.components.forEach {
                it.data = data
            }
        }
    }
    
    fun parseFile(file: File) = if (file.isFile) {
        val bytes = file.readBytes()
        val json = EHFileParser.getJson(bytes)
        if (json != "") {
            val jsonObject = try {
                defaultJsonParser.parse(json)
            } catch (e: JsonSyntaxException) {
                println(json)
                throw e
            }
            defaultGson.fromJson(jsonObject, Data.classFromItemType[jsonObject["ItemType"].int])
                    .also { eHDataFile ->
                        eHDataFile.file = file
                        eHDataFile.rawContent = bytes
                        eHDataFile.content = json
                    }
        } else null
    } else null
    
    fun parseFile(file: File, data: Data) = parseFile(file)?.also { eHDataFile ->
        eHDataFile.addToData(data)
    }
}

//database

val Data.values: List<Data.EHDataFile>
    get() = components.values +
            devices.values +
            weapons.values +
            ammunition.values +
            droneBays.values +
            ships.values +
            satellites.values +
            shipBuilds.values +
            satelliteBuilds.values +
            technology.values +
            componentStats.values +
            componentModifications.values +
            technologyMap.values +
            shipBuilderSettings

fun Data.buildDatabase(path: String) {
    File(path).let {
        if (!it.exists())
            it.mkdir()
    }
    dirNames.values.forEach {
        File(path + it).let {
            if (!it.exists())
                it.mkdir()
        }
    }
    values.forEach { ehDataFile ->
        File(ehDataFile.databaseFileName(path)).let { databaseFile ->
            if (!databaseFile.exists()) databaseFile.createNewFile()
            databaseFile.writeText(ehDataFile.content)
        }
    }
}

fun Data.classify(path: String) {
    File(path).let {
        if (!it.exists())
            it.mkdir()
    }
    dirNames.values.forEach {
        File(path + it).let {
            if (!it.exists())
                it.mkdir()
        }
    }
    values.forEach { ehDataFile ->
        File(path + dirNames[ehDataFile.itemType] + ehDataFile.fileName).let { databaseFile ->
            if (!databaseFile.exists()) databaseFile.createNewFile()
            databaseFile.writeBytes(ehDataFile.rawContent)
        }
    }
}

object VersionTranslator {
    fun translate(oldModPath: File, newEventPath: File, newModPath: File) = translate(oldModPath, newModPath) {
        EHFileParser.fileFromName(newEventPath, oldModPath.name).takeIf { it.exists() }
    }
    
    inline fun translate(oldModPath: File, newModPath: File, getDestFile: (File) -> File?): List<File> {
        val result: MutableList<File> = ArrayList()
        if (!newModPath.exists()) newModPath.mkdir()
        oldModPath.listFiles().forEach { oldModFile ->
            if (oldModFile.isFile) {
                val json = EHFileParser.getJson(oldModFile)
                if (json != "") {
                    val destFile = getDestFile(oldModFile)
                    val header = destFile?.readBytes()?.asList()?.subList(0, 4096)
                    if (header != null) {
                        val byteList = (header + List(8) { 0.toByte() } + json.toByteList())
                        val newContent = ByteArray(byteList.size) { i -> byteList[i] }
                        val newModFile = EHFileParser.fileFromName(newModPath, destFile.name)
                        if (!newModFile.exists()) newModFile.createNewFile()
                        newModFile.writeBytes(EHFileContentEditor.editLength(newContent))
                    } else result.add(oldModFile)
                }
            }
        }
        return result
    }
    
    fun translateCyl(oldModPath: File, oldEventPath: File, newEventPath: File, newModPath: File) {
        val data = EHFileParser.parseData(newEventPath)
        println()
        val values = data.values.groupBy { it.itemType to it.id }
        with(VersionTranslator) {
            with(EHFileParser) {
                translate(oldModPath, newModPath) {
                    val eFile = EHFileParser.fileFromName(oldEventPath, it.name)
                    parseFile(eFile)?.let { f ->
                        values[f.itemType to f.id]
                                ?.takeUnless { f.itemType == 8 && f.id >= 192 }
                                ?.takeUnless { f.itemType == 6 && f.id >= 99 }
                                ?.let { fileFromName(newEventPath, it[0].fileName) }
                    }
                }.print { it.name }
            }
            
        }
    }
}

object VersionComparator {
    class Difference(val name: String, val difference: DifferenceType, val oldLength: Long, val newLength: Long) {
        override fun toString(): String = "$name $difference $oldLength $newLength"
        
        enum class DifferenceType(val _name: String) {
            NEW_FILE("    new file"),
            DELETED_FILE("deleted file"),
            CHANGED_FILE("changed file");
            
            override fun toString() = _name
        }
        
        companion object {
            fun newFile(file: File) = Difference(file.name, NEW_FILE, 0, file.length())
            fun deletedFile(file: File) = Difference(file.name, DELETED_FILE, file.length(), 0)
            fun changedFile(old: File, new: File) = Difference(old.name, CHANGED_FILE, old.length(), new.length())
        }
    }
    
    fun compare(pathOld: File, pathNew: File): List<Difference> {
        val result = ArrayList<Difference>()
        
        val oldFiles = HashMap<String, Pair<File, Boolean>>()
        
        pathOld.listFiles()
                .filter { it.isFile }
                .mapNotNull { it.takeUnless { EHFileParser.getJson(it) == "" } }
                .forEach { file -> oldFiles[file.name] = file to false }
        pathNew.listFiles()
                .filter { it.isFile }
                .mapNotNull { it.takeUnless { EHFileParser.getJson(it) == "" } }
                .forEach { file ->
                    val oldFile = oldFiles[file.name]?.first
                    if (oldFile != null) {
                        oldFiles[file.name] = oldFile to true
                        if (EHFileParser.getJson(oldFile) != EHFileParser.getJson(file))
                            result.add(Difference.changedFile(oldFile, file))
                    } else result.add(Difference.newFile(file))
                }
        oldFiles.values
                .mapNotNull { (file, visited) -> file.takeUnless { visited } }
                .forEach { file -> result.add(Difference.deletedFile(file)) }
        return result
    }
    
    fun compare0(pathEvent: File, pathMod: File, pathDifference: File) {
        if (!pathDifference.exists()) pathDifference.mkdir()
        pathDifference.listFiles().forEach { it.delete() }
        pathMod.listFiles().forEach {
            if (EHFileParser.getJson(it) != EHFileParser.getJson(EHFileParser.fileFromName(pathEvent, it.name))) {
                it.copyTo(EHFileParser.fileFromName(pathDifference, it.name), true)
                println(it.name + " " + EHFileParser.defaultJsonParser.parse(EHFileParser.getJson(it)).let {
                    it["ItemType"].int.toString() + " " + it["Id"]
                })
            }
        }
    }
}

inline fun <T, R> Iterable<T>.print(transform: (T) -> R) = forEach { println(transform(it)) }

object StringFormat {
    @JvmStatic fun appendTo(s: String, l: Int) = s + String(CharArray(max(l - s.length, 0)) { ' ' })
    @JvmStatic fun appendTo(s: Any?, l: Int) = appendTo(s.toString(), l)
    
    @JvmStatic fun formatTo(s: String, l: Int) = String(CharArray(max(l - s.length, 0)) { ' ' }) + s
    @JvmStatic fun formatTo(s: Any?, l: Int) = formatTo(s.toString(), l)
}

object EHFileContentEditor {
    fun editLength(b: ByteArray) = editLength(b, b.size)
    
    fun editLength(b: ByteArray, fl: Int) = b.also {
        val fli = 4
        val vi = 8
        val v = (b[vi + 3].toInt() shl 0x00) or
                (b[vi + 2].toInt() shl 0x08) or
                (b[vi + 1].toInt() shl 0x10) or
                (b[vi + 0].toInt() shl 0x18)
        val oli = when (v) {
            15 -> 76
            17 -> 80
            else -> 0
        }
        val ol = fl - 4096
        val anl = (b[4096].toInt() shl 0x00) or
                (b[4097].toInt() shl 0x08) or
                (b[4098].toInt() shl 0x10) or
                (b[4099].toInt() shl 0x18)
        val jli = 4103 + anl - (anl + 3) % 4
        val jl = fl - jli - 4
        b[fli + 3] = (fl ushr 0x00 and 0xff).toByte()
        b[fli + 2] = (fl ushr 0x08 and 0xff).toByte()
        b[fli + 1] = (fl ushr 0x10 and 0xff).toByte()
        b[fli + 0] = (fl ushr 0x18 and 0xff).toByte()
        b[oli + 0] = (ol ushr 0x00 and 0xff).toByte()
        b[oli + 1] = (ol ushr 0x08 and 0xff).toByte()
        b[oli + 2] = (ol ushr 0x10 and 0xff).toByte()
        b[oli + 3] = (ol ushr 0x18 and 0xff).toByte()
        b[jli + 0] = (jl ushr 0x00 and 0xff).toByte()
        b[jli + 1] = (jl ushr 0x08 and 0xff).toByte()
        b[jli + 2] = (jl ushr 0x10 and 0xff).toByte()
        b[jli + 3] = (jl ushr 0x18 and 0xff).toByte()
    }
    
    fun editLength(header: List<Byte>, json: String) = editLength(
            (header + List(8) { 0.toByte() } + json.toByteList())
                    .let { byteList -> ByteArray(byteList.size) { i -> byteList[i] } }
    )
    
    fun edit(file: File, transform: (JsonObject) -> JsonObject = { jsonObject -> jsonObject }) {
        with(EHFileParser) {
            if (file.exists()) {
                val jsonString = getJson(file)
                if (jsonString != "") {
                    val json = defaultJsonParser.parse(jsonString)
                    val newJsonString = defaultGson.toJson(transform(json.obj))
                    writeJson(file, newJsonString)
                }
            }
        }
    }
    
    fun writeJson(file: File, json: String) {
        file.writeBytes(editLength(file.readBytes().asList().subList(0, 4096), json))
    }
    
    fun editAll(path: File, transform: (JsonObject) -> JsonObject = { jsonObject -> jsonObject }) {
        path.listFiles().forEach { file -> edit(file, transform) }
    }
}
