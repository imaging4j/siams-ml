package com.siams.ml.images.api.client


import com.siams.ml.images.api.client.json.asList
import org.junit.Test
import javax.json.Json
import javax.json.JsonObject
import kotlin.test.assertEquals

/**
 *
 *
 * Created by alexei.vylegzhanin@gmail.com on 3/1/2017.
 */
internal class KJsonKtTest {
    @Test
    fun asListInt() {
        assertEquals(Json.createArrayBuilder().add(1).build().asList<Int>().first(), 1)
    }

    @Test
    fun asListJsonObject() {
        val jsonObject = Json.createObjectBuilder().build()
        val jsonArray = Json.createArrayBuilder().add(jsonObject).build()
        assertEquals(jsonArray.asList<JsonObject>().firstOrNull(), jsonObject)
    }

}