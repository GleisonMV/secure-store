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

import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.spec.AlgorithmParameterSpec
import java.util.*
import javax.crypto.Cipher
import javax.security.auth.x500.X500Principal


object RSA {

    private const val PROVIDER = "AndroidKeyStore"
    private const val TRANSFORMATION = "RSA/ECB/PKCS1Padding"
    private const val ALGORITHM = "RSA"

    private lateinit var alias: String

    private var keyStore: KeyStore? = null

    fun init(context: Context) {
        if (keyStore != null) {
            return
        }
        alias = context.packageName + ".SecureBox"
        keyStore = KeyStore.getInstance(PROVIDER)
        keyStore?.load(null)
        keyStore?.getKey(alias, null) ?: genKeyPair(context)
    }

    @Suppress("DEPRECATION")
    private fun keyPairSpec(context: Context): AlgorithmParameterSpec {
        val spec = KeyPairGeneratorSpec.Builder(context)
        val calendar = Calendar.getInstance()

        spec.setAlias(alias)
        spec.setSubject(X500Principal("CN=$alias"))
        spec.setStartDate(calendar.time)

        calendar.add(Calendar.YEAR, 25)
        spec.setEndDate(calendar.time)

        spec.setSerialNumber(BigInteger.valueOf(4))
        return spec.build()
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun keyPairSpec(setIsStrongBoxBacked: Boolean): AlgorithmParameterSpec {

        val param = KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT
        val spec = KeyGenParameterSpec.Builder(alias, param)
        val calendar = Calendar.getInstance()

        spec.setCertificateSubject(X500Principal("CN=$alias"))
        spec.setCertificateNotBefore(calendar.time)

        calendar.add(Calendar.YEAR, 25)
        spec.setCertificateNotAfter(calendar.time)

        spec.setCertificateSerialNumber(BigInteger.valueOf(4))
        spec.setDigests(KeyProperties.DIGEST_SHA256)
        spec.setBlockModes(KeyProperties.BLOCK_MODE_ECB)
        spec.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)

        if (setIsStrongBoxBacked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            spec.setIsStrongBoxBacked(true)
        }
        return spec.build()
    }

    private fun genKeyPair(context: Context) {
        var algorithmParameterSpec = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            keyPairSpec(context)
        } else {
            keyPairSpec(true)
        }
        val keyPairGen = KeyPairGenerator.getInstance(ALGORITHM, PROVIDER)
        try {
            keyPairGen.initialize(algorithmParameterSpec)
            keyPairGen.genKeyPair()
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                algorithmParameterSpec = keyPairSpec(false)
            }
            keyPairGen.initialize(algorithmParameterSpec)
            keyPairGen.generateKeyPair()
        }
    }

    fun encrypt(message: ByteArray): ByteArray {
        val cipher = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            Cipher.getInstance(TRANSFORMATION, "AndroidOpenSSL")
        else Cipher.getInstance(TRANSFORMATION, "AndroidKeyStoreBCWorkaround")
        cipher.init(Cipher.ENCRYPT_MODE, keyStore!!.getCertificate(alias))
        return cipher.doFinal(message)
    }

    fun decrypt(message: ByteArray): ByteArray {
        val cipher = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            Cipher.getInstance(TRANSFORMATION, "AndroidOpenSSL")
        else Cipher.getInstance(TRANSFORMATION, "AndroidKeyStoreBCWorkaround")
        cipher.init(Cipher.DECRYPT_MODE, keyStore!!.getKey(alias, null) as PrivateKey)
        return cipher.doFinal(message)
    }

}
