package com.siams.ml.images.api.client

import com.siams.ml.images.api.client.json.string
import javax.json.JsonObject


/**
 * <p>
 * Created by alexei.vylegzhanin@gmail.com on 2/28/2017.
 */
data class MarkerRef private constructor(
        val project: ProjectRef,
        val markerId: String,
        private val json: JsonObject
) {
    companion object {
        fun of(project: ProjectRef, json: JsonObject): MarkerRef = MarkerRef(
                project = project,
                markerId = json.string("id"),
                json = json)
    }
}