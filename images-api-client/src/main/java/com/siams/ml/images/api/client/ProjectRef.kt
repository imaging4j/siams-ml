package com.siams.ml.images.api.client

import javax.json.JsonObject


/**
 * <p>
 * Created by alexei.vylegzhanin@gmail.com on 2/28/2017.
 */
data class ProjectRef private constructor(
        val folder: FolderRef,
        val projectId: String,
        val projectName: String,
        val projectTags: Set<String>,
        val markerCount: Int,
        val imageWidth: Long,
        val imageHeight: Long
) {
    val workspace: String get() = folder.workspace
    val isImage: Boolean get() = imageWidth > 0 && imageHeight > 0

    companion object {
        fun of(folder: FolderRef, json: JsonObject): ProjectRef {
            val image: JsonObject? = json.objN("image")
            return ProjectRef(
                    folder = folder,
                    projectId = json.stringE("id"),
                    projectName = json.stringE("name"),
                    projectTags = json.arrN("tags").asList<String>().toSet(),
                    markerCount = json.int("markerCount"),
                    imageWidth = image.long("width"),
                    imageHeight = image.long("height")
            )
        }
    }
}
