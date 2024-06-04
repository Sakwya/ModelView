package top.sakwya.modelview.utills

import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.IOException
import java.io.InputStream

class ProxyWebViewClient(private val context: Context,private val callback:()->Unit) : WebViewClient() {

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        callback()
    }
    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest
    ): WebResourceResponse? {
        val url = request.url.toString()
        // 如果请求的是本地文件，则自定义处理
        println(url)
        if (url.startsWith("glb://")) {
            try {
                // 从assets目录中读取本地文件内容
                val inputStream: InputStream = context.assets
                    .open(url.replace("glb://", ""))
                // 创建WebResourceResponse对象
                val responseHeaders = HashMap<String, String>()
                responseHeaders["Access-Control-Allow-Origin"] =
                    "*" // 允许所有源的跨域请求，根据需要设置为合适的值
                return WebResourceResponse(
                    "model/gltf-binary",
                    "UTF-8",
                    200,
                    "OK",
                    responseHeaders,
                    inputStream
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        // 其他情况，继续默认行为
        return super.shouldInterceptRequest(view, request)
    }
}