package com.hustunique.vlive.data

import java.nio.FloatBuffer
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

    fun writeToBuffer(data: FloatBuffer) = apply {
        data.put(n.x)
        data.put(n.y)
        data.put(n.z)
        data.put(a)
    }

    fun readFromBuffer(data: FloatBuffer) = apply {
        n.x = data.get()
        n.y = data.get()
        n.z = data.get()
        a = data.get()
    }

    fun toRotation(R: FloatArray) {
        val q0 = a
        val q1 = n.x
        val q2 = n.y
        val q3 = n.z
        val sq_q1 = 2 * q1 * q1
        val sq_q2 = 2 * q2 * q2
        val sq_q3 = 2 * q3 * q3
        val q1_q2 = 2 * q1 * q2
        val q3_q0 = 2 * q3 * q0
        val q1_q3 = 2 * q1 * q3
        val q2_q0 = 2 * q2 * q0
        val q2_q3 = 2 * q2 * q3
        val q1_q0 = 2 * q1 * q0

        R[0] = 1 - sq_q2 - sq_q3
        R[1] = q1_q2 - q3_q0
        R[2] = q1_q3 + q2_q0

        R[3] = q1_q2 + q3_q0
        R[4] = 1 - sq_q1 - sq_q3
        R[5] = q2_q3 - q1_q0

        R[6] = q1_q3 - q2_q0
        R[7] = q2_q3 + q1_q0
        R[8] = 1 - sq_q1 - sq_q2
    }

    fun clone(q1: Quaternion) = apply {
        a = q1.a
        n.x = q1.n.x
        n.y = q1.n.y
        n.z = q1.n.z
    }

    companion object {
        fun mul(q1: Quaternion, q2: Quaternion) = Quaternion(
            a = q1.a * q2.a - q1.n.dot(q2.n),
            n = Vector3.mul(q1.n, q2.n)
                .add(q2.n, q1.a)
                .add(q1.n, q2.a),
        ).normalize()
    }
}