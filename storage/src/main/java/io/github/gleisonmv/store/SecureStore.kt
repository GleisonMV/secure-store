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
import android.content.SharedPreferences
import android.util.Log

import org.json.JSONObject

object SecureStore {

    private lateinit var secret: ByteArray
    private lateinit var salt: ByteArray
    private lateinit var iv: ByteArray

    private var prefs: SharedPreferences? = null

    private fun loadDefaults(context: Context) {
        RSA.init(context)
        if (prefs == null) {
            prefs = context.getSharedPreferences("encrypt.box", Context.MODE_PRIVATE)
            val input = try {
                context.openFileInput("encrypt.key")
            } catch (e: Exception) {
                null
            }
            if (input == null) {
                secret = Secret.key().toByteArray()
                salt = Secret.random(32)
                iv = Secret.random(16)
                val obj = JSONObject()
                obj.put("secret", String(secret.encode()))
                obj.put("salt", String(salt.encode()))
                obj.put("iv", String(iv.encode()))
                context.openFileOutput("encrypt.key", Context.MODE_PRIVATE).also {
                    it.write(RSA.encrypt(obj.toString().toByteArray()).encode())
                    it.close()
                }
            } else {
                val json = RSA.decrypt(input.readBytes().decode())
                val obj = JSONObject(String(json))
                secret = obj.getString("secret").decode()
                salt = obj.getString("salt").decode()
                iv = obj.getString("iv").decode()
                input.close()
            }
        }
    }

    fun contains(context: Context, key: String): Boolean {
        loadDefaults(context)
        return prefs!!.contains(key)
    }

    fun setString(context: Context, key: String, value: String) {
        loadDefaults(context)
        val editor = prefs?.edit()
        editor?.putString(
            key,
            String(AES.encrypt(secret, iv, salt, value.toByteArray())!!.encode())
        )
        editor?.apply()
    }

    fun setDouble(context: Context, key: String, value: Double) {
        setString(context, key, value.toString())
    }

    fun setLong(context: Context, key: String, value: Long) {
        setString(context, key, value.toString())
    }

    fun clear(context: Context, key: String) {
        loadDefaults(context)
        prefs!!.edit().remove(key).apply()
    }

    fun getString(context: Context, key: String, def: String? = null): String? {
        loadDefaults(context)
        return try {
            val message = prefs!!.getString(key, null)!!.toByteArray()
            String(AES.decrypt(secret, iv, salt, message.decode())!!)
        } catch (e: Exception) {
            Log.e("Store", "Err: " + e.message)
            def
        }
    }

    fun getDouble(context: Context, key: String, def: Double? = 0.0): Double? {
        return getString(context, key, null)!!.toDoubleOrNull() ?: def
    }

    fun getLong(context: Context, key: String, def: Long? = 0): Long? {
        return getString(context, key, null)!!.toLongOrNull() ?: def
    }
}