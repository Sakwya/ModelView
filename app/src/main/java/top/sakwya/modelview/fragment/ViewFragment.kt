package top.sakwya.modelview.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import top.sakwya.modelview.R
import top.sakwya.modelview.data.Quaternion
import top.sakwya.modelview.databinding.FragmentViewBinding
import top.sakwya.modelview.utills.ProxyWebViewClient
import top.sakwya.modelview.utills.getPreviewOutputSize
import top.sakwya.modelview.viewmodel.SharedViewModel


class ViewFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentViewBinding? = null
    private val binding get() = _binding!!
    private var obFlag: Boolean = false
    private var recordQuaternion: Quaternion = Quaternion(1.0F, 0.0F, 0.0F, 0.0F)
    private var pageFinished:Boolean = false
    private lateinit var sensorManager: SensorManager


    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val useCamera get() = sharedViewModel.useCamera.value == true
    private var isCameraOpen: Boolean = false
    private val cameraId = "0"
    private val cameraManager: CameraManager by lazy {
        requireActivity().getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    //    private val TAG = CameraActivity::class.java.simpleName
    private lateinit var cameraDevice: CameraDevice
    private val cameraThread = HandlerThread("CameraThread").apply { start() }
    private val cameraHandler = Handler(cameraThread.looper)

    private lateinit var session: CameraCaptureSession


    private val myCallback = object : SurfaceHolder.Callback {
        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) =
            Unit

        override fun surfaceDestroyed(holder: SurfaceHolder) = Unit

        override fun surfaceCreated(holder: SurfaceHolder) {
            //为了确保设置了大小，需要在主线程中初始化camera
            setAspectRatio()
            binding.root.post {
                openCamera(cameraId)
            }
        }
    }

    private val characteristics: CameraCharacteristics by lazy {
        cameraManager.getCameraCharacteristics(cameraId)
    }

    // 获取所有摄像头的CameraID
    fun getCameraIds(): Array<String> {
        return cameraManager.cameraIdList
    }

    /**
     * 获取摄像头方向
     */
    fun getCameraOrientationString(cameraId: String): String {
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        val lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING)!!
        return when (lensFacing) {
            CameraCharacteristics.LENS_FACING_BACK -> "后摄(Back)"
            CameraCharacteristics.LENS_FACING_FRONT -> "前摄(Front)"
            CameraCharacteristics.LENS_FACING_EXTERNAL -> "外置(External)"
            else -> "Unknown"
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewBinding.inflate(inflater, container, false)
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        binding.reset.setOnClickListener {
            run {
                obFlag = true
            }
        }
        binding.config.setOnClickListener {
            findNavController().navigate(R.id.action_nav_view_to_configFragment)
        }
        val webView = binding.page

        webView.setBackgroundColor(0)

        val webSettings: WebSettings = webView.getSettings()
        webSettings.allowFileAccess = true;
        webSettings.allowContentAccess = true;
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW;
        webSettings.javaScriptEnabled = true
        webView.loadUrl("file:///android_asset/index.html")
        webView.setWebViewClient(ProxyWebViewClient(requireContext()){
            pageFinished = true
            webView.evaluateJavascript("render.loadModel(\"glb://models/${sharedViewModel.modelName.value}\")"){}
        })

        return binding.root
    }

    override fun onResume() {
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
            SensorManager.SENSOR_DELAY_UI
        )

        if (useCamera) {
            binding.surfaceView.holder.addCallback(myCallback)
            isCameraOpen = true
        }

        super.onResume()
    }

    override fun onPause() {
        sensorManager.unregisterListener(this)
        if (isCameraOpen) {
            binding.surfaceView.holder.removeCallback(myCallback)
        }
//        cameraDevice.close()
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
                    val quaternion = FloatArray(4)
                    SensorManager.getQuaternionFromVector(quaternion, it.values);
                    val currentQuaternion =
                        Quaternion(quaternion[0], quaternion[1], quaternion[2], quaternion[3])
                    if (obFlag) {
                        recordQuaternion = currentQuaternion.conjugate()
                        obFlag = false
                    }
                    val resultQuaternion = (recordQuaternion * currentQuaternion).conjugate()
                    binding.page.evaluateJavascript(
                        "render.setQuaternion(${resultQuaternion.w}, ${resultQuaternion.x}, ${resultQuaternion.y}, ${resultQuaternion.z})"
                    ) {}
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
//        TODO("Not yet implemented")
    }


    private fun setAspectRatio() {
        val previewSize = getPreviewOutputSize(
            binding.surfaceView.display,
            characteristics,
            SurfaceHolder::class.java
        )
//        Log.d(TAG, "Selected preview size: $previewSize")
        binding.surfaceView.setAspectRatio(previewSize.width, previewSize.height)
    }

    @SuppressLint("MissingPermission")
    private fun openCamera(cameraId: String) {
        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera
                startPreview()
            }

            override fun onDisconnected(camera: CameraDevice) {

            }

            override fun onError(camera: CameraDevice, error: Int) {
                Toast.makeText(requireContext(), "openCamera Failed:$error", Toast.LENGTH_SHORT)
                    .show()
            }
        }, cameraHandler)
    }

    private fun startPreview() {
        //因为摄像头设备可以同时输出多个流，所以可以传入多个surface
        val targets = listOf(binding.surfaceView.holder.surface /*,这里可以传入多个surface*/)
        cameraDevice.createCaptureSession(targets, object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(captureSession: CameraCaptureSession) {
                //赋值session
                session = captureSession

                val captureRequest = cameraDevice.createCaptureRequest(
                    CameraDevice.TEMPLATE_PREVIEW
                ).apply { addTarget(binding.surfaceView.holder.surface) }

                //这将不断地实时发送视频流，直到会话断开或调用session.stoprepeat()
                session.setRepeatingRequest(captureRequest.build(), null, cameraHandler)
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                Toast.makeText(requireContext(), "session configuration failed", Toast.LENGTH_SHORT)
                    .show()
            }
        }, cameraHandler)
    }

    override fun onStop() {
        super.onStop()
        try {
            cameraDevice.close()
        } catch (exc: Throwable) {
//            Log.e(TAG, "Error closing camera", exc)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraThread.quitSafely()
        //imageReaderThread.quitSafely()
    }

}