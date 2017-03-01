package com.siams.ml.images.api.client

import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.client.protocol.HttpClientContext
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
class SiamsImages private constructor(val uri: URI, userName: String?, password: String?) {
    val user: String = userName ?: "guest"
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
        private val DEFAULT_API_URI = System.getenv("SIAMS_IMAGES_URI") ?: "http://127.0.0.1:8888/json-rpc/api"
        private val LOG: Logger = Logger.getLogger(SiamsImages::class.java.name)

        fun open(
                uri: URI = URI.create(DEFAULT_API_URI),
                userName: String? = System.getenv("SIAMS_IMAGES_USER"),
                password: String? = System.getenv("SIAMS_IMAGES_PASSWORD")): SiamsImages
                =
                SiamsImages(uri, userName, password)
    }

    fun getFolders(workspace: String): List<FolderRef> =
            callJsonRpc("getFolders", workspace)
                    .arrN("folders")
                    .asList<JsonObject>()
                    .map { FolderRef.of(workspace, it) }

    fun getProjects(folder: FolderRef): List<ProjectRef> =
            callJsonRpc("getProjects", folder.workspace, folder.folderId)
                    .arrN("projects")
                    .asList<JsonObject>()
                    .map { ProjectRef.of(folder, it) }


    fun getProject(project: ProjectRef): ProjectRef =
            callJsonRpc("getProject", project.workspace, project.projectId).let { json ->
                ProjectRef.of(project.folder, json.obj("project") {
                    throw JsonRpcException("Invalid getProject result: " + json)
                })
            }

    fun getOrigins(project: ProjectRef): List<MarkerRef> = getMarkers(project, "originsAsBox")

    fun getMarkers(project: ProjectRef): List<MarkerRef> = getMarkers(project, "box", "polygon")

    fun getMarkers(project: ProjectRef, vararg types: String): List<MarkerRef> =
            callJsonRpc("getMarkers", project.workspace, project.projectId, *types)
                    .arrN("markers")
                    .asList<JsonObject>()
                    .map { MarkerRef.of(project, it) }

    fun openImage(project: ProjectRef, format: String = "png"): ImageRef = ImageRef.of(project,
            callJsonRpc("openImage", project.workspace, project.projectId, format))

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