package com.siams.ml.images.api.client

import com.siams.ml.images.api.client.json.*
import javax.json.JsonNumber
import javax.json.JsonObject

/**
 * <p>
 * Created by alexei.vylegzhanin@gmail.com on 3/2/2017.
 */
data class VrGrid private constructor(
        val layer: ImageLayer,
        var name: String,
        var labels: MutableMap<String, Int> = mutableMapOf(),
        var cellSize: Int = 0,
        var cellSizeInPhysicalUnits: Double = 0.toDouble()
) {
    fun resetLabels(vararg labels: String) {
        this.labels = mutableMapOf<String, Int>().apply {
            for (i in labels.indices) {
                this[labels[i]] = i
            }
        }
    }

    companion object {
        fun of(layer: ImageLayer, json: JsonObject): VrGrid = VrGrid(
                layer = layer,
                name = json.string("name"),
                labels = mutableMapOf<String, Int>().apply {
                    json.obj("labels").entries.forEach { entry ->
                        entry.value.let {
                            if (it is JsonNumber) this[entry.key] = it.intValue()
                        }
                    }
                },
                cellSize = json.int("cellSize"),
                cellSizeInPhysicalUnits = json.double("cellSizeInPhysicalUnits")
        )
    }
}