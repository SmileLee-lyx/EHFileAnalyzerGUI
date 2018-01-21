package org.smilelee.kotson

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive

val jsonNull: JsonNull get() = JsonNull.INSTANCE

val JsonElement?.isNull get() = this is JsonNull?
val JsonElement?.isObj get() = this is JsonObject
val JsonElement?.isArray get() = this is JsonArray
val JsonElement?.isPrimitive get() = this is JsonPrimitive

val JsonElement?.nullableObj: JsonObject? get() = this as? JsonObject
val JsonElement?.nullableArray: JsonArray? get() = this as? JsonArray
val JsonElement?.nullablePrimitive: JsonPrimitive? get() = this as? JsonPrimitive
val JsonElement?.nullableInt: Int? get() = nullablePrimitive?.takeIf { it.isNumber }?.asInt
val JsonElement?.nullableByte: Byte? get() = nullablePrimitive?.takeIf { it.isNumber }?.asByte
val JsonElement?.nullableLong: Long? get() = nullablePrimitive?.takeIf { it.isNumber }?.asLong
val JsonElement?.nullableFloat: Float? get() = nullablePrimitive?.takeIf { it.isNumber }?.asFloat
val JsonElement?.nullableDouble: Double? get() = nullablePrimitive?.takeIf { it.isNumber }?.asDouble
val JsonElement?.nullableChar: Char? get() = nullablePrimitive?.takeIf { it.isString }?.asCharacter
val JsonElement?.nullableBoolean: Boolean? get() = nullablePrimitive?.takeIf { it.isBoolean }?.asBoolean
val JsonElement?.nullableString: String? get() = nullablePrimitive?.takeIf { it.isString }?.asString

val JsonElement?.obj: JsonObject get() = this as JsonObject
val JsonElement?.array: JsonArray get() = this as JsonArray
val JsonElement?.primitive: JsonPrimitive get() = this as JsonPrimitive
val JsonElement?.int: Int get() = primitive.asInt
val JsonElement?.byte: Byte get() = primitive.asByte
val JsonElement?.long: Long get() = primitive.asLong
val JsonElement?.float: Float get() = primitive.asFloat
val JsonElement?.double: Double get() = primitive.asDouble
val JsonElement?.char: Char get() = primitive.asCharacter
val JsonElement?.boolean: Boolean get() = primitive.asBoolean
val JsonElement?.string: String get() = primitive.asString

fun Nothing?.toJson() = jsonNull
fun Number.toJson() = JsonPrimitive(this)
fun Char.toJson() = JsonPrimitive(this)
fun Boolean.toJson() = JsonPrimitive(this)
fun String.toJson() = JsonPrimitive(this)
fun JsonElement.toJson() = this

fun Any?.toJson(): JsonElement = when (this) {
    null           -> toJson()
    is Number      -> toJson()
    is Char        -> toJson()
    is Boolean     -> toJson()
    is String      -> toJson()
    is JsonElement -> toJson()
    else           -> throw IllegalArgumentException("$this cannot be converted to json")
}

operator fun JsonObject?.get(key: String): JsonElement? = this?.get(key)
operator fun JsonArray?.get(index: Int): JsonElement? = this?.get(index)

operator fun JsonElement?.get(key: String): JsonElement? = nullableObj[key]
operator fun JsonElement?.get(index: Int): JsonElement? = nullableArray[index]

operator fun JsonObject?.set(key: String, any: Any?) = this?.add(key, any.toJson())
operator fun JsonArray?.set(index: Int, any: Any?) = this?.set(index, any.toJson())

operator fun JsonElement?.set(key: String, any: Any?) = nullableObj.set(key, any)
operator fun JsonElement?.set(index: Int, any: Any?) = nullableArray.set(index, any)

operator fun JsonObject?.contains(key: String) = this != null && this.has(key)
