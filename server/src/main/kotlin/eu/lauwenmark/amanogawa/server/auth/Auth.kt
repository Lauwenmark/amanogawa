package eu.lauwenmark.amanogawa.server.eu.lauwenmark.amanogawa.server.auth

import java.math.BigInteger
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class  User constructor(val name: String, val email: String, clearPassword: String? = null, hashPassword: String? = null) {

    val password: String
    init {
        if (clearPassword != null) {
            password = hash(clearPassword)
        } else if (hashPassword != null) {
            password = hashPassword
        } else {
            password = ""
            throw IllegalArgumentException("Empty password")
        }
    }

}

//Encryption from example at https://howtodoinjava.com/security/how-to-generate-secure-password-hash-md5-sha-pbkdf2-bcrypt-examples/
private fun salt() : ByteArray {
    val secureRandom = SecureRandom.getInstance("SHA1PRNG")
    val salt = ByteArray(16)
    secureRandom.nextBytes(salt)
    return salt
}

private fun hash(password: String, iterations: Int = 1000) : String {
    val chars = password.toCharArray()
    val salt = salt()
    val spec = PBEKeySpec(chars, salt, iterations, 64 * 8)
    val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    val hash = keyFactory.generateSecret(spec).encoded

    return "$iterations:${toHex(salt)}:${toHex(hash)}"
}

private fun toHex(array: ByteArray): String {
    val hex = BigInteger(1, array).toString(16)
    val paddingLength = (array.size * 2) - hex.length
    if (paddingLength > 0) {
        return String.format("%0"+paddingLength+"d", 0) + hex
    } else {
        return hex
    }
}

private fun fromHex(hex: String) : ByteArray {
    return BigInteger(hex, 16).toByteArray()
}

fun validate(password: String, key: String) : Boolean {
    val keyParts = key.split(":")
    val iterations = Integer.parseInt(keyParts[0])
    val salt = fromHex(keyParts[1])
    val hash = fromHex(keyParts[2])

    val spec = PBEKeySpec(password.toCharArray(), salt, iterations, 64 * 8)
    val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    val testHash = keyFactory.generateSecret(spec).encoded

    return BigInteger(hash) == BigInteger(testHash)
}