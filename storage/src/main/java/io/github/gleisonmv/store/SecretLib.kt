/*
 *  Copyright [2020] [Gleison M. Vasconcelos]
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.github.gleisonmv.store

import android.util.Base64

import java.security.SecureRandom

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


object Secret {

    private const val ALGORITHM = "PBKDF2WithHmacSHA1"
    private const val ITERATION = 1000
    private const val KEY_LENGTH = 256

    fun key(): String {
        val k = random(16)
        val r = Base64.encodeToString(k, Base64.URL_SAFE)
        val t = System.currentTimeMillis()
        return "$t.$r"
    }

    fun random(length: Int): ByteArray = SecureRandom().let { secure ->
        ByteArray(length).also {
            secure.nextBytes(it)
        }
    }

    fun generate(secret: ByteArray, salt: ByteArray, type: String): SecretKeySpec? =
        SecretKeySpec(
            SecretKeyFactory
                .getInstance(ALGORITHM)
                .generateSecret(
                    PBEKeySpec(
                        String(secret).toCharArray(),
                        salt,
                        ITERATION,
                        KEY_LENGTH
                    )
                ).encoded, type
        )
}