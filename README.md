# secure-store
Armazenamento seguro para Android baseado em KeyStore

[![](https://jitpack.io/v/GleisonMV/secure-store.svg)](https://jitpack.io/#GleisonMV/secure-store)

# Instalação


build.gradle

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

app/build.gradle

```
dependencies {
    implementation 'com.github.GleisonMV:secure-store:0.6.0b'
}
```

# Api

```
// Verfica se a chave existe
SecureStore.contains(context: Context, key: String): Boolean

// Limpa a chave
SecureStore.clear(context: Context, key: String)

// Define os valores
SecureStore.setString(context: Context, key: String, value: String)
SecureStore.setDouble(context: Context, key: String, value: Double)
SecureStore.setLong(context: Context, key: String, value: Long)

// Retorna os valores
SecureStore.getString(context: Context, key: String, def: String? = null): String?
SecureStore.getDouble(context: Context, key: String, def: Double? = 0.0): Double?
SecureStore.getLong(context: Context, key: String, def: Long? = 0): Long?
```
