package com.rei.stats.gsampler.util

import java.nio.file.Files
import java.nio.file.Path
import java.security.GeneralSecurityException
import java.security.SecureRandom
import java.util.stream.Collectors

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.PBEParameterSpec

import org.apache.commons.lang.ArrayUtils
import org.apache.commons.lang.RandomStringUtils

class EncryptionService {
    private static final String ALGORITHM ="PBEWithHmacSHA256AndAES_256"
    private static final int SALT_SIZE = 8
    private static final int IV_SIZE = 16
    private static final int ITERATIONS = 1000

    private SecureRandom random = new SecureRandom()

    private char[] key;

    EncryptionService(Path keyFile) {
        if (!Files.exists(keyFile)) {
           keyFile.text = RandomStringUtils.randomAscii(1024)
        }
        key = keyFile.text.toCharArray()
    }

    String encrypt(String value) {
        byte[] iv = getRandomIV()
        byte[] salt = getRandomSalt()
        byte[] encrypted = getCipher(ALGORITHM, Cipher.ENCRYPT_MODE, iv, salt).doFinal(value.bytes)
        byte[] encryptedToken = ArrayUtils.addAll(ArrayUtils.addAll(iv, salt), encrypted)
        return b64(encryptedToken)
    }

    String decrypt(String value) {
        byte[] encryptedToken = value.decodeBase64()
        byte[] iv = Arrays.copyOf(encryptedToken, IV_SIZE);
        byte[] salt = Arrays.copyOfRange(encryptedToken, IV_SIZE, IV_SIZE + SALT_SIZE)
        byte[] encrypted = Arrays.copyOfRange(encryptedToken, IV_SIZE + SALT_SIZE, encryptedToken.length)
        return new String(getCipher(ALGORITHM, Cipher.DECRYPT_MODE, iv, salt).doFinal(encrypted))
    }

    private Cipher getCipher(String algorithm, int decryptMode, byte[] iv, byte[] salt) {
        Cipher cipher = Cipher.getInstance(algorithm)
        SecretKey secretKey = SecretKeyFactory.getInstance(algorithm).generateSecret(new PBEKeySpec(key))
        cipher.init(decryptMode, secretKey, new PBEParameterSpec(salt, ITERATIONS, new IvParameterSpec(iv)))
        return cipher
    }

    private String b64(byte[] input) {
        return input.encodeBase64().writeTo(new StringWriter()).toString()
    }

    private byte[] getRandomSalt() {
        byte[] salt = new byte[SALT_SIZE];
        random.nextBytes(salt);
        return salt;
    }

    private byte[] getRandomIV() {
        byte[] salt = new byte[IV_SIZE];
        random.nextBytes(salt);
        return salt;
    }
}
