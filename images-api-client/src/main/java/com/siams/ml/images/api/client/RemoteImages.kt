package com.siams.ml.images.api.client

import com.siams.ml.images.api.proto.IMG
import java.net.URI
import javax.json.JsonObject

/**
 * <p>
 * Created by alexei.vylegzhanin@gmail.com on 3/1/2017.
 */
interface RemoteImages {
    companion object {
        fun connect(uri: URI? = null, userName: String? = null, password: String? = null): RemoteImages =
                SiamsImages.connect(uri, userName, password)
    }

    val uri: URI
    val user: String

    fun getFolders(workspace: String): List<FolderRef>
    fun getProjects(folder: FolderRef): List<ProjectRef>
    fun getProject(project: ProjectRef): ProjectRef
    fun getOrigins(project: ProjectRef): List<MarkerRef>
    fun getMarkers(project: ProjectRef): List<MarkerRef>
    fun getMarkers(project: ProjectRef, vararg types: String): List<MarkerRef>
    fun getMarkers(project: ProjectRef, types: Collection<String>): List<MarkerRef>
    fun getLayers(project: ProjectRef): List<ImageLayer>
    fun setLayersVisible(project: ProjectRef, layers: Collection<ImageLayer>, visible: Boolean = false)

    fun openImage(project: ProjectRef): ImageRef
    fun openImageAs(project: ProjectRef, format: String = "png"): ImageRef

    fun requestImageTile(image: ImageRef, dimX: Int, dimY: Int, x: Long, y: Long, compression: Double = 1.0): IMG.Tile
    fun requestImageBytes(image: ImageRef, region: Region, compression: Double? = null): ByteArray

    fun getVrGrids(overlay: ImageLayer): List<VrGrid>
    fun openVrGrid(grid: VrGrid): VrGridRef

    fun getVrStyles(grid: VrGridRef): Map<String, VrStyle>

    fun getVrCells(grid: VrGridRef): List<VrCell>
    fun addVrCells(grid: VrGridRef, cells: Collection<VrCell>)
    fun replaceVrCells(grid: VrGridRef, cells: Collection<VrCell>)
    fun deleteVrCells(grid: VrGridRef)
    fun deleteVrGrid(grid: VrGridRef)

    fun addProjectTags(project: ProjectRef, vararg tags: String): Set<String>
    fun addProjectTags(project: ProjectRef, tags: Collection<String>): Set<String>
    fun removeProjectTags(project: ProjectRef, vararg tags: String): Set<String>
    fun removeProjectTags(project: ProjectRef, tags: Collection<String>): Set<String>

    fun getJsonDocNames(project: ProjectRef): Set<String>
    fun getJsonDoc(project: ProjectRef, docName: String): JsonObject
    fun putJsonDoc(project: ProjectRef, docName: String, jsonDoc: JsonObject?)
    fun removeJsonDoc(project: ProjectRef, docNames: Collection<String>)
    fun removeJsonDoc(project: ProjectRef, vararg docNames: String)

    fun setImageComments(project: ProjectRef, html: String)

}