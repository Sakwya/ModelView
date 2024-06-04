package top.sakwya.modelview.data

data class Quaternion(
    val w: Float,
    val x: Float,
    val y: Float,
    val z: Float
) {
    // 计算四元数的共轭
    fun conjugate(): Quaternion {
        return Quaternion(w, -x, -y, -z)
    }

    // 重载四元数乘法
    operator fun times(other: Quaternion): Quaternion {
        val newW = w * other.w - x * other.x - y * other.y - z * other.z
        val newX = w * other.x + x * other.w + y * other.z - z * other.y
        val newY = w * other.y - x * other.z + y * other.w + z * other.x
        val newZ = w * other.z + x * other.y - y * other.x + z * other.w
        return Quaternion(newW, newX, newY, newZ)
    }
}