package com.siams.ml.images.api.client

import com.siams.ml.images.api.client.json.double
import java.util.*
import javax.json.Json
import javax.json.JsonObject

/**
 * <p>
 * Created by alexei.vylegzhanin@gmail.com on 3/2/2017.
 */
data class VrCell(
        val x: Double,
        val y: Double,
        val predictions: DoubleArray
) {
    companion object {
        fun of(json: JsonObject): VrCell = VrCell(
                x = json.double("x"),
                y = json.double("y"),
                predictions = json.getJsonArray("predictions")?.let { array ->
                    DoubleArray(array.size) { index -> array.getJsonNumber(index)?.doubleValue() ?: 0.toDouble() }
                } ?: DoubleArray(0)
        )
    }

    fun toJson(): JsonObject = Json.createObjectBuilder().apply {
        add("x", x)
        add("y", y)
        add("predictions", Json.createArrayBuilder().apply {
            predictions.forEach { add(it) }
        })
    }.build()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VrCell) return false

        if (x != other.x) return false
        if (y != other.y) return false
        if (!Arrays.equals(predictions, other.predictions)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + Arrays.hashCode(predictions)
        return result
    }

}