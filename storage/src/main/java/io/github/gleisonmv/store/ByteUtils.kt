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

fun ByteArray.encode(): ByteArray {
    return Base64.encode(this, Base64.URL_SAFE)
}

fun ByteArray.decode(): ByteArray {
    return Base64.decode(this, Base64.URL_SAFE)
}

fun String.decode(): ByteArray {
    return Base64.decode(this, Base64.URL_SAFE)
}
