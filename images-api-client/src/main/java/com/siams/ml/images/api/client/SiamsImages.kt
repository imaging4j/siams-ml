package com.siams.ml.images.api.client

import com.siams.ml.images.api.client.json.*
import com.siams.ml.images.api.proto.IMG
import org.apache.http.HttpEntity
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.client.utils.URIBuilder
import org.apache.http.client.utils.URIUtils
import org.apache.http.entity.StringEntity
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.impl.client.*
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.util.EntityUtils
import java.net.URI
import java.util.logging.Level
import java.util.logging.Logger
import javax.json.Json
import javax.json.JsonException
import javax.json.JsonObject

/**
 * <p>
 * Created by alexei.vylegzhanin@gmail.com on 2/28/2017.
 */
internal class SiamsImages private constructor(override val uri: URI, userName: String?, password: String?) : RemoteImages {
    override val user: String = userName ?: "guest"
    private val httpHost: HttpHost = URIUtils.extractHost(uri)
    private val credentialsProvider: BasicCredentialsProvider = BasicCredentialsProvider()
    private val httpClient: CloseableHttpClient

    init {
        credentialsProvider.setCredentials(
                AuthScope(httpHost.hostName, httpHost.port, httpHost.schemeName),
                if (userName != null && password != null)
                    UsernamePasswordCredentials(userName, password)
                else
                    UsernamePasswordCredentials(uri.rawAuthority)
        )
        val connManager = PoolingHttpClientConnectionManager()
        connManager.maxTotal = 128
        connManager.defaultMaxPerRoute = 20
        val keepAliveStrategy = DefaultConnectionKeepAliveStrategy()
        httpClient = HttpClients.custom()
                .setConnectionManager(connManager)
                .setKeepAliveStrategy(keepAliveStrategy)
                .setDefaultCredentialsProvider(credentialsProvider)
                .build()
    }

    companion object {
        private val LOG: Logger = Logger.getLogger(SiamsImages::class.java.name)

        fun connect(uri: URI? = null, userName: String? = null, password: String? = null): RemoteImages
                = SiamsImages(
                uri ?: URI.create(System.getenv("SIAMS_IMAGES_URI") ?: "http://127.0.0.1:8888/json-rpc/api"),
                userName ?: System.getenv("SIAMS_IMAGES_USER"),
                password ?: System.getenv("SIAMS_IMAGES_PASSWORD"))
    }

    override fun getFolders(workspace: String): List<FolderRef> =
            callJsonRpc("getFolders", workspace)
                    .arrN("folders")
                    .asList<JsonObject>()
                    .map { FolderRef.of(workspace, it) }

    override fun getProjects(folder: FolderRef): List<ProjectRef> =
            callJsonRpc("getProjects", folder.workspace, folder.folderId)
                    .arrN("projects")
                    .asList<JsonObject>()
                    .map { ProjectRef.of(folder, it) }


    override fun getProject(project: ProjectRef): ProjectRef =
            callJsonRpc("getProject", project.workspace, project.projectId).let { json ->
                ProjectRef.of(project.folder, json.obj("project") {
                    throw JsonRpcException("Invalid getProject result: " + json)
                })
            }

    override fun getOrigins(project: ProjectRef): List<MarkerRef> = getMarkers(project, "originsAsBox")

    override fun getMarkers(project: ProjectRef): List<MarkerRef> = getMarkers(project, "box", "polygon")

    override fun getMarkers(project: ProjectRef, vararg types: String): List<MarkerRef> =
            callJsonRpc("getMarkers", project.workspace, project.projectId, *types)
                    .arrN("markers")
                    .asList<JsonObject>()
                    .map { MarkerRef.of(project, it) }

    override fun getLayers(project: ProjectRef): List<ImageLayer> =
            callJsonRpc("getLayers", project.workspace, project.projectId)
                    .arrN("layers")
                    .asList<JsonObject>()
                    .map { ImageLayer.of(project, it) }

    fun setLayersVisible(project: ProjectRef, layers: List<ImageLayer>, visible: Boolean) {
        callJsonRpc(
                "setLayersVisible",
                project.workspace,
                project.projectId,
                Json.createArrayBuilder().apply {
                    layers.forEach {
                        add(it.toJson())
                    }
                }.build(),
                visible
        )
    }

    override fun openImage(project: ProjectRef): ImageRef = ImageRef.of(project,
            callJsonRpc("openImage", project.workspace, project.projectId, "IMG-TILE"))

    override fun openImageAs(project: ProjectRef, format: String): ImageRef = ImageRef.of(project,
            callJsonRpc("openImage", project.workspace, project.projectId, format))

    override fun requestImageTile(image: ImageRef, dimX: Int, dimY: Int, x: Long, y: Long, compression: Double): IMG.Tile {
        try {
            val uri = URIBuilder(image.toImageUri())
                    .apply {
                        setParameter("dimX", dimX.toString())
                        setParameter("dimY", dimY.toString())
                        setParameter("x", x.toString())
                        setParameter("y", y.toString())
                        setParameter("compression", compression.toString())
                    }
                    .build()
            val tile = requestHttp<IMG.Tile>(HttpGet(uri), "IMG.Tile") { httpEntity ->
                httpEntity.content.use { stream -> IMG.Tile.parseDelimitedFrom(stream) }
            }
            assert(dimX == tile.dimX)
            assert(dimY == tile.dimY)
            return tile
        } catch (e: Exception) {
            throw JsonRpcException(e)
        }
    }

    override fun requestImageBytes(image: ImageRef, region: Region, compression: Double?): ByteArray = try {
        val uri = URIBuilder(image.toImageUri())
                .apply {
                    setParameter("region", region.toParameter())
                    if (compression != null) {
                        setParameter("compression", compression.toString())
                    }
                }.build()
        requestHttpBytes(HttpGet(uri))
    } catch (e: Exception) {
        throw JsonRpcException(e)
    }

    override fun getVrGrids(layer: ImageLayer): List<VrGrid> =
            callJsonRpc("getVrGrids", layer.project.workspace, layer.project.projectId, layer.toJson())
                    .arrN("grids")
                    .asList<JsonObject>()
                    .map { VrGrid.of(layer, it) }

    override fun setImageComments(project: ProjectRef, html: String) {
        callJsonRpc(
                "setImageComments",
                project.workspace,
                project.projectId,
                html
        )
    }

    private fun requestHttpBytes(httpRequestBase: HttpRequestBase): ByteArray
            = requestHttp(httpRequestBase, "byte[]") { EntityUtils.toByteArray(it) }

    private fun <R> requestHttp(httpRequestBase: HttpRequestBase, rpcMethod: String, map: (HttpEntity) -> R): R {
        val t1 = System.currentTimeMillis()
        try {
            httpClient.execute(httpRequestBase, newHttpClientContext(rpcMethod)).use { response ->
                checkStatus(response, httpRequestBase)
                return map(response.entity)
            }
        } finally {
            val time = System.currentTimeMillis() - t1
            if (time > 2000) {
                LOG.log(Level.WARNING, "too slow response (${time}ms): $httpRequestBase", Exception("dumpStack"))
            }
        }
    }

    private fun callJsonRpc(rpcMethod: String, vararg parameters: Any?): JsonObject {
        val t0 = System.currentTimeMillis()
        fun duration() = System.currentTimeMillis() - t0
        fun errMsg() = "call json-rpc $rpcMethod(${parameters.joinToString(", ")}); duration: (${duration()}ms)"

        try {
            val postData = Json
                    .createObjectBuilder()
                    .add("jsonrpc", "2.0")
                    .add("id", 0)
                    .add("method", rpcMethod)
                    .add("params", parameters.toJsonArray())
                    .build()

            val httpPost = HttpPost(uri).apply {
                entity = StringEntity(postData.toString(), Charsets.UTF_8)
            }

            val json = httpClient
                    .execute(httpPost, newHttpClientContext(rpcMethod))
                    .use { response ->
                        checkStatus(response, httpPost)
                        val text = EntityUtils.toString(response.entity, Charsets.UTF_8)
                        try {
                            Json.createReader(text.reader()).use { it.readObject() }
                        } catch (e: JsonException) {
                            throw JsonRpcException("invalid rpc call response format: $text", e)
                        }
                    }

            return json.objN("result") ?: json.objN("error")?.let { error ->
                throw JsonRpcException(
                        error.string("message") { "invalid rpc call result: $json" }
                                + " (code: ${error.int("code")})\n"
                                + httpPost.uri + "\n"
                                + postData.toPrintString()
                )
            } ?: throw JsonRpcException("invalid rpc call result: $json")

        } catch (e: JsonRpcException) {
            LOG.log(Level.WARNING, errMsg(), e)
            throw e
        } catch (e: Throwable) {
            LOG.log(Level.WARNING, errMsg(), e)
            throw JsonRpcException(e)
        }

    }

    private fun checkStatus(response: CloseableHttpResponse, request: HttpRequestBase) {
        val statusLine = response.statusLine
        if (statusLine.statusCode != 200) {
            throw JsonRpcException("Invalid response: $statusLine on $request")
        }
    }

    private fun newHttpClientContext(rpcMethod: String): HttpClientContext = HttpClientContext.create().apply {
        //<editor-fold desc="Preemptive authentication">
        credentialsProvider = credentialsProvider
        authCache = BasicAuthCache().apply {
            put(httpHost, BasicScheme())
        }
        //</editor-fold>

        //<editor-fold desc="Timeout">
        requestConfig = RequestConfig
                .custom()
                .setSocketTimeout(when (rpcMethod) {
                    "openImage",
                    "getMarkers",
                    "getVrCells" -> 30000
                    else -> 5000
                })
                .build()
        //</editor-fold>
    }
}
