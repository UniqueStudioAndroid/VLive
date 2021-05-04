package com.hustunique.vlive.data

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector3(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f,
) {
    fun add(other: Vector3, factor: Float = 1f) = apply {
        x += other.x * factor
        y += other.y * factor
        z += other.z * factor
    }

    fun sub(other: Vector3, factor: Float = 1f) = apply {
        x -= other.x * factor
        y -= other.y * factor
        z -= other.z * factor
    }

    fun addAssign(v1: Vector3, v2: Vector3) = apply {
        x = v1.x + v2.x
        y = v1.y + v2.y
        z = v1.z + v2.z
    }

    fun subAssign(v1: Vector3, v2: Vector3) = apply {
        x = v1.x - v2.x
        y = v1.y - v2.y
        z = v1.z - v2.z
    }

    fun mul(factor: Float) = apply {
        x *= factor
        y *= factor
        z *= factor
    }

    fun dot(other: Vector3) = x * other.x + y * other.y + z * other.z

    fun crossAssign(v1: Vector3, v2: Vector3) = apply {
        x = v1.y * v2.z - v1.z * v2.y
        y = -v1.x * v2.z + v1.z * v2.x
        z = v1.x * v2.y - v1.y * v2.x
    }

    fun normalize() = apply {
        val factor = sqrt(x * x + y * y + z * z)
        x /= factor
        y /= factor
        z /= factor
    }

    fun applyL(matrix: FloatArray) = apply {
        val tempX = x
        val tempY = y
        val tempZ = z
        x = matrix[0] * tempX + matrix[1] * tempY + matrix[2] * tempZ
        y = matrix[3] * tempX + matrix[4] * tempY + matrix[5] * tempZ
        z = matrix[6] * tempX + matrix[7] * tempY + matrix[8] * tempZ
    }

    fun clone() = Vector3(x, y, z)

    fun clone(v: Vector3) = apply {
        x = v.x
        y = v.y
        z = v.z
    }

    fun toRotationMatrix(data: FloatArray) = apply {
        val c1 = cos(x)
        val s1 = sin(x)
        val c2 = cos(y)
        val s2 = sin(y)
        val c3 = cos(z)
        val s3 = sin(z)
        data[0] = (c1 * c3 + s1 * s2 * s3)
        data[1] = (c3 * s1 * s2 - c1 * s3)
        data[2] = (c2 * s1)
        data[3] = (c2 * s3)
        data[4] = (c2 * c3)
        data[5] = (-s2)
        data[6] = (c1 * s2 * s3 - s1 * c3)
        data[7] = (s1 * s3 + c1 * c3 * s2)
        data[8] = (c1 * c2)
    }

    companion object {
        fun add(v1: Vector3, v2: Vector3): Vector3 {
            val target = v1.clone()
            target.add(v2)
            return target
        }

        fun sub(v1: Vector3, v2: Vector3): Vector3 {
            val target = v1.clone()
            target.sub(v2)
            return target
        }
    }
}