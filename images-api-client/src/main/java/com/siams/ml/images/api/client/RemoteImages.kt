package com.siams.ml.images.api.client

import com.siams.ml.images.api.proto.IMG
import java.net.URI

/**
 * <p>
 * Created by alexei.vylegzhanin@gmail.com on 3/1/2017.
 */
interface RemoteImages {
    val uri: URI
    val user: String
    fun getFolders(workspace: String): List<FolderRef>
    fun getProjects(folder: FolderRef): List<ProjectRef>
    fun getProject(project: ProjectRef): ProjectRef
    fun getOrigins(project: ProjectRef): List<MarkerRef>
    fun getMarkers(project: ProjectRef): List<MarkerRef>
    fun getMarkers(project: ProjectRef, vararg types: String): List<MarkerRef>
    fun openImage(project: ProjectRef): ImageRef
    fun openImageAs(project: ProjectRef, format: String = "png"): ImageRef

    fun requestImageTile(image: ImageRef, dimX: Int, dimY: Int, x: Long, y: Long, compression: Double = 1.0): IMG.Tile
    fun requestImageBytes(image: ImageRef, region: Region, compression: Double? = null): ByteArray

    companion object {
        fun connect(uri: URI? = null, userName: String? = null, password: String? = null): RemoteImages
                = SiamsImages.connect(uri, userName, password)
    }

}