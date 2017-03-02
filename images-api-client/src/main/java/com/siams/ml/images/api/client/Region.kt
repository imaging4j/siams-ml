package com.siams.ml.images.api.client

import javax.json.Json
import javax.json.JsonObject

/**
 *
 * Created by alexei.vylegzhanin@gmail.com on 3/1/2017.
 */
class Region private constructor(val fromX: Long = 0, val fromY: Long = 0, val toX: Long = 0, val toY: Long = 0) {

    companion object {
        fun of(fromX: Long = 0, fromY: Long = 0, toX: Long = 0, toY: Long = 0): Region = Region(
                Math.min(fromX, toX),
                Math.min(fromY, toY),
                Math.max(fromX, toX),
                Math.max(fromY, toY)
        )

        fun of(imageRef: ImageRef): Region = of(
                0,
                0,
                imageRef.width,
                imageRef.height
        )

        fun of(vararg regionRect: String): Region = of(
                regionRect[0].toLong(),
                regionRect[1].toLong(),
                regionRect[2].toLong(),
                regionRect[3].toLong()
        )
    }

    val centerX: Long get() = fromX + width / 2

    val centerY: Long get() = fromY + height / 2

    val width: Long get() = Math.abs(toX - fromX)

    val height: Long get() = Math.abs(toY - fromY)

    fun toParameter(): String = String.format("%d,%d,%d,%d", fromX, fromY, toX, toY)

    fun toPixelDimX(compression: Double): Int = (width / compression).toInt()

    fun toPixelDimY(compression: Double): Int = (height / compression).toInt()

    fun invertY(height: Long): Region = Region(
            fromX = fromX,
            fromY = height - fromY,
            toX = toX,
            toY = height - toY
    )

    fun toJson(): JsonObject = Json.createObjectBuilder()
            .add("fromX", fromX)
            .add("fromY", fromY)
            .add("toX", toX)
            .add("toY", toY)
            .build()

    override fun toString(): String {
        return "Region(fromX=$fromX, fromY=$fromY, toX=$toX, toY=$toY)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Region) return false

        if (fromX != other.fromX) return false
        if (fromY != other.fromY) return false
        if (toX != other.toX) return false
        if (toY != other.toY) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fromX.hashCode()
        result = 31 * result + fromY.hashCode()
        result = 31 * result + toX.hashCode()
        result = 31 * result + toY.hashCode()
        return result
    }
}
