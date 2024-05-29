package top.sakwya.modelview.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import top.sakwya.modelview.databinding.FragmentViewBinding
import java.io.IOException
import java.io.InputStream
import kotlin.math.PI
import kotlin.math.asin


class ViewFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentViewBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var sensorManager: SensorManager

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewBinding.inflate(inflater, container, false)
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val webView = binding.page
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.loadUrl("file:///android_asset/index.html")

        webView.setWebViewClient(object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                val url = request.url.toString()
                // 如果请求的是本地文件，则自定义处理
                println(url)
                if (url.startsWith("glb://")) {
                    println("Getttttttttttttttttttttttttttttttti!!")
                    try {
                        // 从assets目录中读取本地文件内容
                        val inputStream: InputStream = requireContext().assets
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
                        println("wronnnnnnnnnnnnnnnnnnnnnnnnnng!!")
                        e.printStackTrace()
                    }
                }

                // 其他情况，继续默认行为
                return super.shouldInterceptRequest(view, request)
            }
        })

        val webSettings: WebSettings = webView.getSettings()
        webSettings.allowFileAccess = true;
        webSettings.allowContentAccess = true;
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW;
        return binding.root
    }

    override fun onResume() {
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
            SensorManager.SENSOR_DELAY_NORMAL
        )

        super.onResume()
    }

    override fun onPause() {
        sensorManager.unregisterListener(this)
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ROTATION_VECTOR -> {
                    val rotationVector = it.values.clone()
                    println(rotationVector[0].toString() + " " + rotationVector[1].toString() + " " + rotationVector[2].toString())
                    val rotationMatrix = FloatArray(9)
                    val orientationAngles = FloatArray(3)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector);
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)
                    binding.page.evaluateJavascript(
                        "render.setRotation(${
                            orientationAngles[1]+ PI*2
                        }, ${
                            orientationAngles[2]+ PI*2
                        },${
                            orientationAngles[0]+ PI*2
                        })"
                    ) { result ->
                        { println(result) }
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
//        TODO("Not yet implemented")
    }

}