/*
 * Copyright (c) 2026 Timotej Adamec
 * SPDX-License-Identifier: MIT
 *
 * This file is part of the thesis:
 * "Multiplatform snagging system with code sharing maximisation"
 *
 * Czech Technical University in Prague
 * Faculty of Information Technology
 * Department of Software Engineering
 */

package cz.adamec.timotej.snag.authentication.fe.common.driven.internal

import org.publicvalue.multiplatform.oidc.tokenstore.SettingsStore
import java.io.File
import java.security.KeyStore
import java.util.Base64
import java.util.Properties
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val KEYSTORE_TYPE = "PKCS12"
private const val KEY_ALIAS = "snag-auth-key"
private const val KEYSTORE_PASSWORD = "snag-ks"
private const val ALGORITHM = "AES"
private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val GCM_TAG_LENGTH = 128
private const val GCM_IV_LENGTH = 12
private const val AES_KEY_SIZE = 256

internal class JvmEncryptedSettingsStore : SettingsStore {
    private val snagDir = File(System.getProperty("user.home"), ".snag").apply { mkdirs() }
    private val keystoreFile = File(snagDir, "auth.keystore")
    private val dataFile = File(snagDir, "auth-tokens.enc")

    private val secretKey: SecretKey by lazy { loadOrCreateKey() }

    override suspend fun get(key: String): String? {
        val props = loadProperties()
        val encrypted = props.getProperty(key) ?: return null
        return decrypt(encrypted)
    }

    override suspend fun put(
        key: String,
        value: String,
    ) {
        val props = loadProperties()
        props.setProperty(key, encrypt(value))
        saveProperties(props)
    }

    override suspend fun remove(key: String) {
        val props = loadProperties()
        props.remove(key)
        saveProperties(props)
    }

    override suspend fun clear() {
        if (dataFile.exists()) dataFile.delete()
    }

    private fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val combined = iv + ciphertext
        return Base64.getEncoder().encodeToString(combined)
    }

    private fun decrypt(encoded: String): String {
        val combined = Base64.getDecoder().decode(encoded)
        val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
        val ciphertext = combined.copyOfRange(GCM_IV_LENGTH, combined.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }

    private fun loadOrCreateKey(): SecretKey {
        val ks = KeyStore.getInstance(KEYSTORE_TYPE)
        if (keystoreFile.exists()) {
            keystoreFile.inputStream().use { ks.load(it, KEYSTORE_PASSWORD.toCharArray()) }
            val entry = ks.getEntry(KEY_ALIAS, KeyStore.PasswordProtection(KEYSTORE_PASSWORD.toCharArray()))
            if (entry is KeyStore.SecretKeyEntry) return entry.secretKey
        }
        ks.load(null, KEYSTORE_PASSWORD.toCharArray())
        val key = KeyGenerator.getInstance(ALGORITHM).apply { init(AES_KEY_SIZE) }.generateKey()
        ks.setEntry(
            KEY_ALIAS,
            KeyStore.SecretKeyEntry(key),
            KeyStore.PasswordProtection(KEYSTORE_PASSWORD.toCharArray()),
        )
        keystoreFile.outputStream().use { ks.store(it, KEYSTORE_PASSWORD.toCharArray()) }
        return key
    }

    private fun loadProperties(): Properties =
        Properties().apply {
            if (dataFile.exists()) {
                dataFile.inputStream().use { load(it) }
            }
        }

    private fun saveProperties(props: Properties) {
        dataFile.outputStream().use { props.store(it, null) }
    }
}
