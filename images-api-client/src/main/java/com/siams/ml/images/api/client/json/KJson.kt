package com.siams.ml.images.api.client.json

import com.siams.ml.images.api.client.JsonRpcException
import java.io.StringWriter
import java.util.*
import javax.json.*
import javax.json.stream.JsonGenerator

/**
 * <p>
 * Created by alexei.vylegzhanin@gmail.com on 2/28/2017.
 */

internal inline fun <reified T> JsonArray?.asList(): List<T> =
        if (this == null) emptyList()
        else {
            val resFun: (Int) -> T = when (T::class) {
                Int::class -> {
                    { getInt(it) as T }
                }
                Long::class -> {
                    { getJsonNumber(it).longValue() as T }
                }
                Double::class -> {
                    { getJsonNumber(it).doubleValue() as T }
                }
                String::class -> {
                    { getString(it) as T }
                }
                Boolean::class -> {
                    { getBoolean(it) as T }
                }
                JsonObject::class -> {
                    { getJsonObject(it) as T }
                }
                else -> {
                    throw AssertionError("Class ${T::class} is not supported")
                }
            }

            object : AbstractList<T>() {
                override fun get(index: Int): T = resFun(index)
                override val size: Int get() = this@asList.size
            }
        }


internal fun JsonObject?.int(name: String): Int = int(name, { 0 })
internal fun JsonObject?.intE(name: String): Int = int(name, { throw JsonRpcException("$it field required") })

internal fun JsonObject?.long(name: String): Long = long(name, { 0 })
internal fun JsonObject?.longE(name: String): Long = long(name, { throw JsonRpcException("$it field required") })

internal fun JsonObject?.double(name: String): Double = double(name, { 0.0 })
internal fun JsonObject?.doubleE(name: String): Double = double(name, { throw JsonRpcException("$it field required") })

internal fun JsonObject?.string(name: String): String = string(name, { "" })
internal fun JsonObject?.stringN(name: String): String? = stringN(name, { null })
internal fun JsonObject?.stringE(name: String): String = string(name, { throw JsonRpcException("$it field required") })

internal fun JsonObject?.bool(name: String): Boolean = bool(name, { false })
internal fun JsonObject?.boolN(name: String): Boolean? = boolN(name, { null })
internal fun JsonObject?.boolE(name: String): Boolean = bool(name, { throw JsonRpcException("$it field required") })

internal fun JsonObject?.obj(name: String): JsonObject = obj(name, { Json.createObjectBuilder().build() })
internal fun JsonObject?.objN(name: String): JsonObject? = objN(name, { null })
internal fun JsonObject?.objE(name: String): JsonObject = obj(name, { throw JsonRpcException("$it field required") })
internal fun JsonObject?.arrN(name: String): JsonArray? = arrN(name, { null })

internal inline fun JsonObject?.int(name: String, def: (String) -> Int): Int =
        if (this == null) def(name)
        else getJsonNumber(name)?.intValue() ?: def(name)

internal inline fun JsonObject?.long(name: String, def: (String) -> Long): Long =
        if (this == null) def(name)
        else getJsonNumber(name)?.longValue() ?: def(name)

internal inline fun JsonObject?.double(name: String, def: (String) -> Double): Double =
        if (this == null) def(name)
        else getJsonNumber(name)?.doubleValue() ?: def(name)

internal inline fun JsonObject?.string(name: String, def: (String) -> String): String =
        stringN(name, def) ?: def(name)

internal inline fun JsonObject?.stringN(name: String, def: (String) -> String?): String? =
        if (this == null) def(name)
        else {
            val value = this[name]
            when (value) {
                null -> def(name)
                is JsonString -> value.string
                else -> value.toString()
            }
        }

internal inline fun JsonObject?.bool(name: String, def: (String) -> Boolean): Boolean =
        boolN(name, def) ?: def(name)

internal inline fun JsonObject?.boolN(name: String, def: (String) -> Boolean?): Boolean? =
        this?.get(name)?.let {
            when (it.valueType) {
                javax.json.JsonValue.ValueType.TRUE -> true
                javax.json.JsonValue.ValueType.FALSE -> false
                else -> def(name)
            }
        } ?: def(name)

internal inline fun JsonObject?.obj(name: String, def: (String) -> JsonObject): JsonObject =
        if (this == null) def(name)
        else getJsonObject(name) ?: def(name)

internal inline fun JsonObject?.objN(name: String, def: (String) -> JsonObject?): JsonObject? =
        if (this == null) def(name)
        else getJsonObject(name) ?: def(name)

internal fun JsonObject?.arrN(name: String, def: (String) -> JsonArray?): JsonArray? =
        if (this == null) def(name)
        else getJsonArray(name) ?: def(name)

internal fun Array<out Any?>.toJsonArray(): JsonArrayBuilder? = Json.createArrayBuilder().apply {
    forEach {
        when (it) {
            is Int -> this.add(it)
            is Long -> this.add(it)
            is Double -> this.add(it)
            is String -> this.add(it)
            is Boolean -> this.add(it)
            is JsonValue -> this.add(it)
            else -> throw AssertionError("Unsupported item: $it")
        }
    }
}

private val jsonWriterFactoryPP: JsonWriterFactory by lazy {
    Json.createWriterFactory(mapOf(JsonGenerator.PRETTY_PRINTING to true))
}

internal fun JsonStructure.toPrintString(): String = StringWriter().use {
    jsonWriterFactoryPP.createWriter(it).write(this)
}.toString()
