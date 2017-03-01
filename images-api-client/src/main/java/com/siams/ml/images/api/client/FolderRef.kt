package com.siams.ml.images.api.client

import com.siams.ml.images.api.client.json.int
import com.siams.ml.images.api.client.json.stringE
import javax.json.JsonObject


/**
 * <p>
 * Created by alexei.vylegzhanin@gmail.com on 2/28/2017.
 */
data class FolderRef private constructor(
        val workspace: String,
        val folderId: String,
        val folderName: String,
        val projectCount: Int) {

    val id: String get() = "$workspace/F:$folderId"

    companion object {
        fun of(workspace: String, json: JsonObject): FolderRef = FolderRef(
                workspace = workspace,
                folderId = json.stringE("id"),
                folderName = json.stringE("name"),
                projectCount = json.int("projectCount")
        )
    }
}