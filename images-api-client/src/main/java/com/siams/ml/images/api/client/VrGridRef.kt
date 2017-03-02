package com.siams.ml.images.api.client

import com.siams.ml.images.api.client.json.string
import javax.json.Json
import javax.json.JsonObject

/**
 * <p>
 * Created by alexei.vylegzhanin@gmail.com on 3/2/2017.
 */
data class VrGridRef constructor(
        val grid: VrGrid,
        val layerId: String,
        val pyramidId: String
) {
    companion object {
        fun of(grid: VrGrid, json: JsonObject): VrGridRef = VrGridRef(
                grid = grid,
                layerId = json.string("layerId"),
                pyramidId = json.string("pyramidId")
        )
    }

    fun toJson(): JsonObject = Json.createObjectBuilder().apply {
        add("workspace", grid.layer.project.workspace)
        add("projectId", grid.layer.project.projectId)
        add("layerId", layerId)
        add("pyramidId", pyramidId)
        add("gridName", grid.name)
    }.build()
}