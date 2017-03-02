package com.siams.ml.images.api.client

import com.siams.ml.images.api.client.json.doubleN
import com.siams.ml.images.api.client.json.stringN
import javax.json.Json
import javax.json.JsonObject

/**
 * <p>
 * Created by alexei.vylegzhanin@gmail.com on 3/2/2017.
 */
data class VrStyle(
        val color: String?,
        val minOpaque: Double?,
        val maxOpaque: Double?
) {
    companion object {
        fun of(json: JsonObject): VrStyle = VrStyle(
                color = json.stringN("color"),
                minOpaque = json.doubleN("minOpaque"),
                maxOpaque = json.doubleN("maxOpaque")
        )
    }

    fun toJson(): JsonObject = Json.createObjectBuilder().apply {
        if (color != null) add("color", color)
        if (minOpaque != null) add("minOpaque", minOpaque)
        if (maxOpaque != null) add("maxOpaque", maxOpaque)
    }.build()
}