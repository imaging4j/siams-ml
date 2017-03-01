package com.siams.ml.images.api.client

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File


/**
 *
 *
 * Created by alexei.vylegzhanin@gmail.com on 2/28/2017.
 */
internal class SiamsImagesTest {
    @Test
    fun open() {
        val images = RemoteImages.connect()
        assertTrue(images.uri.port > 0)
    }

}

fun main(args: Array<String>) {
    val images = RemoteImages.connect()
    var imagesCountDown = 100
    images.getFolders(images.user).forEach { folder ->
        println(folder)
        images.getProjects(folder).forEach { project ->
            println("\t $project")
            images.getLayers(project).forEach { layer ->
                println("\t\t layer: $layer")
                if (layer.isVr) {
                    val vrGridList = images.getVrGrids(layer)
                    vrGridList.forEach { vrGrid ->
                        println("\t\t\t vrGrid: $vrGrid")
                    }
                }
            }

            if (project.isImage && imagesCountDown-- > 0) {
                try {

                    println("\t\t origins: ${images.getOrigins(project)}")
                    val image = images.openImage(project)
                    println("\t\t image: $image")

                    val region = Region.of(image)
                    val tile = images.requestImageTile(image, 256, 256, region.centerX, region.centerY)
                    println("\t\t tile.bytesCase: ${tile.bytesCase}")
                    println("\t\t tile.region: ${tile.region}")

                    val imageAsPNG = images.openImageAs(project, "png")
                    println("\t\t imageAsPNG: $imageAsPNG")

                    File.createTempFile("test.", ".png").let { tempFile ->
                        tempFile.writeBytes(images.requestImageBytes(imageAsPNG, Region.of(
                                region.centerX - 127, region.centerY - 127,
                                region.centerX + 128, region.centerY + 128)))
                        tempFile.delete()
                    }
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