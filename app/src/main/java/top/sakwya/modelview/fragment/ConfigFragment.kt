package top.sakwya.modelview.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import top.sakwya.modelview.databinding.FragmentConfigBinding
import top.sakwya.modelview.viewmodel.SharedViewModel
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class ConfigFragment : Fragment() {

    private var _binding: FragmentConfigBinding? = null
    private val fileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { uri ->
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    val fileName = uri.lastPathSegment ?: "file.glb"
                    println(uri.path)
                    if (fileName.endsWith(".glb") || fileName.endsWith(".gltf")) {
                        inputStream?.use { input ->
                            val outputFile = File(requireContext().filesDir, fileName)
                            val outputStream = FileOutputStream(outputFile)
                            outputStream.use { output ->
                                input.copyTo(output)
                            }
                            // 将文件复制到 assets 文件夹中
                            copyFileToAssets(outputFile)
                            Toast.makeText(
                                requireContext(),
                                "复制完成",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "文件类型不符合 *.glb/*.gltf",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // 权限已授予，打开相机
                Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show()
                binding.useCamera.isChecked = true
            } else {
                // 权限被拒绝
                // 显示消息提示
                Toast.makeText(requireContext(), "Fail", Toast.LENGTH_SHORT).show()
                binding.useCamera.isChecked = false
            }
        }

    private fun requestCameraPermission() {
        // 请求相机权限
        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConfigBinding.inflate(inflater, container, false)


        val spinner: Spinner = binding.spinner
        val options = requireContext().assets.list("models") ?: emptyArray()
        // 创建 ArrayAdapter
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // 设置 Adapter
        spinner.adapter = adapter

        spinner.setSelection(options.indexOf(sharedViewModel.modelName.value))

        // 设置选项选择监听器
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedOption = parent.getItemAtPosition(position).toString()
                sharedViewModel.setModelName(selectedOption)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // 处理没有选中任何选项的情况
            }
        }

        val switchButton = binding.useCamera
        switchButton.isChecked = sharedViewModel.useCamera.value == true
        switchButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    sharedViewModel.setUseCamera(true)
                } else {
                    // 请求相机权限
                    requestCameraPermission()
                }
            } else {
                sharedViewModel.setUseCamera(false)
            }
        }

        binding.addButton.setOnClickListener { openFilePicker() }

        return binding.root
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*" // 设置要选择的文件类型
        fileLauncher.launch(intent)
    }

    private fun copyFileToAssets(file: File) {
        val assetsDir = File("file:///android_asset/model")
        val outputFile = File(assetsDir, file.name)

        try {
            val inputStream = FileInputStream(file)
            val outputStream = FileOutputStream(outputFile)
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            Toast.makeText(requireContext(), "文件传输完成", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // 处理异常
        }
    }

    override fun onResume() {
        super.onResume()
        binding.useCamera.isChecked = sharedViewModel.useCamera.value == true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}