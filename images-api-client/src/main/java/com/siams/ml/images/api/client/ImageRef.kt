package com.siams.ml.images.api.client

import com.siams.ml.images.api.client.json.*
import org.apache.http.client.utils.URIBuilder
import java.net.URI
import javax.json.JsonObject

/**
 * <p>
 * Created by alexei.vylegzhanin@gmail.com on 2/28/2017.
 */
data class ImageRef constructor(
        val project: ProjectRef,
        val imageId: String,
        val pixelSizeMicrons: Double?,
        val width: Long,
        val height: Long,
        val url: String
) {
    fun toImageUri(): URI = URIBuilder(url).addParameter("image", imageId).build()

    companion object {
        fun of(project: ProjectRef, json: JsonObject): ImageRef {
            val image = json.objE("image")
            return ImageRef(
                    project = project,
                    imageId = image.stringE("id"),
                    pixelSizeMicrons = json.objN("pixelSize").toMicrons(),
                    width = image.longE("width"),
                    height = image.longE("height"),
                    url = json.objE("connection").stringE("url")
            )
        }

        private fun JsonObject?.toMicrons(): Double? {
            if (this == null) return null
            val unit = string("unit", { return null })
            val value = double("value", { return null })
            return when (unit) {
                "microns" -> value
                "nm" -> value / 1000
                "mm" -> value * 1000
                else -> throw JsonRpcException("Invalid pixel size unit: $unit")
            }
        }
    }
}