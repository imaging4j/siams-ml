package com.siams.ml.images.api.client

import org.junit.Test

import org.junit.Assert.*
import javax.json.Json

/**
 *
 *
 * Created by alexei.vylegzhanin@gmail.com on 3/2/2017.
 */
class VrGridTest {
    @Test
    fun resetLabels() {
        val folder = FolderRef.of("*", Json.createObjectBuilder()
                .add("id", "1")
                .add("name", "folder-name")
                .build())
        val project = ProjectRef.of(folder, Json.createObjectBuilder()
                .add("id", "1")
                .add("name", "project-name")
                .build())
        val layer = ImageLayer.of(project, Json.createObjectBuilder()
                .build())
        val vrGrid = VrGrid.of(layer, Json.createObjectBuilder().build())

        vrGrid.resetLabels("test0", "test1")

        assertEquals(vrGrid.labels["test0"], 0)
        assertEquals(vrGrid.labels["test1"], 1)
    }

}