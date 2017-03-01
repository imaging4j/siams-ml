package com.siams.ml.images.api.client

import org.junit.Test
import kotlin.test.assertEquals


/**
 *
 *
 * Created by alexei.vylegzhanin@gmail.com on 2/28/2017.
 */
internal class SiamsImagesTest {
    @Test
    fun open() {
        val images = RemoteImages.connect()
        assertEquals(images.uri.port, 8888)
    }

}

fun main(args: Array<String>) {
    val images = RemoteImages.connect()
    var imagesCountDown = 100
    images.getFolders(images.user).forEach { folder ->
        println(folder)
        images.getProjects(folder).forEach { project ->
            println("\t $project")
            if (project.isImage && imagesCountDown-- > 0) {
                try {
                    println("\t\t Origins: ${images.getOrigins(project)}")
                    println("\t\t ${images.openImageAs(project)}")
                } catch(e: JsonRpcException) {
                    e.printStackTrace()
                }
            }
            if (project.markerCount > 0) {
                images.getMarkers(project).forEach { marker ->
                    println("\t\t $marker")
                }
            }
        }
    }

}