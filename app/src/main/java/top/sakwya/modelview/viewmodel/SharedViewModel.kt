package top.sakwya.modelview.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _useCamera = MutableLiveData(false)
    private val _modelName = MutableLiveData("city.glb")
    val useCamera: LiveData<Boolean> get() = _useCamera
    val modelName: LiveData<String> get() = _modelName
    fun setUseCamera(data: Boolean) {
        _useCamera.value = data
    }

    fun setModelName(data: String) {
        _modelName.value = data
    }
}
