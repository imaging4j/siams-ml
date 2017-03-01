package com.siams.ml.images.api.client

import com.siams.ml.images.api.client.json.boolN
import com.siams.ml.images.api.client.json.objN
import com.siams.ml.images.api.client.json.string
import com.siams.ml.images.api.client.json.stringN
import javax.json.Json
import javax.json.JsonObject
import javax.json.JsonValue

/**
 * <p>
 * Created by alexei.vylegzhanin@gmail.com on 3/2/2017.
 */
data class ImageLayer private constructor(
        val project: ProjectRef,
        val layerId: String,
        val vr: JsonValue,
        val tmsRenderer: JsonValue,
        val title: String?,
        val isVisible: Boolean?,
        val isBaseLayer: Boolean?
) {
    val isVr: Boolean get() = vr.valueType != JsonValue.ValueType.NULL

    companion object {
        fun of(project: ProjectRef, json: JsonObject): ImageLayer = ImageLayer(
                project = project,
                layerId = json.string("id"),
                vr = json.objN("vr") ?: JsonValue.NULL,
                tmsRenderer = json.objN("tmsRenderer") ?: JsonValue.NULL,
                title = json.stringN("title"),
                isVisible = json.boolN("visible"),
                isBaseLayer = json.boolN("baseLayer")
        )
    }

    fun toJson(): JsonObject = Json.createObjectBuilder().apply {
        add("id", layerId)
        if (vr != JsonValue.NULL) add("vr", vr)
        if (tmsRenderer != JsonValue.NULL) add("tmsRenderer", tmsRenderer)
        if (title != null) add("title", title)
        if (isVisible != null) add("visible", isVisible)
        if (isBaseLayer != null) add("baseLayer", isBaseLayer)
    }.build()
}