package com.hustunique.vlive.data

import kotlin.math.sqrt

class Quaternion(
    var a: Float = 1f,
    val n: Vector3 = Vector3()
) {
    fun addAssign(q1: Quaternion, q2: Quaternion) = apply {
        a = q1.a + q2.a
        n.addAssign(q1.n, q2.n)
    }

    fun subAssign(q1: Quaternion, q2: Quaternion) = apply {
        a = q1.a - q2.a
        n.subAssign(q1.n, q2.n)
    }

    fun mulAssign(q1: Quaternion, q2: Quaternion) = apply {
        a = q1.a * q2.a - q1.n.dot(q2.n)
        n.crossAssign(q1.n, q2.n)
            .add(q2.n, q1.a)
            .add(q1.n, q2.a)
    }

    fun inverse() = apply {
        n.mul(-1f)
    }

    fun normalize() = apply {
        val factor = sqrt(a * a + n.x * n.x + n.y * n.y + n.z * n.z)
        if (factor != 0f) {
            a /= factor
            n.mul(1 / factor)
        }
    }

    fun toFloatArray(data: FloatArray) = apply {
        data[0] = n.x
        data[1] = n.y
        data[2] = n.z
        data[3] = a
    }

    fun fromFloatArray(data: FloatArray) = apply {
        n.x = data[0]
        n.y = data[1]
        n.z = data[2]
        a = data[3]
    }

    fun clone(q1: Quaternion) = apply {
        a = q1.a
        n.x = q1.n.x
        n.y = q1.n.y
        n.z = q1.n.z
    }
}