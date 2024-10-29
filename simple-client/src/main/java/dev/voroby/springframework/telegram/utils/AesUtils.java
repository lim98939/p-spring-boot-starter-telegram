package dev.voroby.springframework.telegram.utils;


import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;


/**
 * AES加密解密
 * kilyoqiang
 * https://www.cnblogs.com/kikyoqiang/
 */
public class AesUtils {

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_MODE = "AES/CBC/PKCS5Padding";
    public static final String SECRET_KEY = "6162382d323fb399fc202a5aca55336a";

    // 加密
    public static String encrypt(String plainText, String secretKey) throws Exception {
        byte[] iv = generateIV(); // 生成初始化向量
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), AES_ALGORITHM);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance(AES_MODE);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        byte[] combined = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    private static byte[] generateIV() {
        byte[] iv = new byte[16]; // 16 字节为初始化向量长度
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    public static String decrypt(String encryptedText, String secretKey) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedText);
        byte[] iv = new byte[16]; // 16 字节为初始化向量长度
        byte[] encryptedBytes = new byte[combined.length - iv.length];

        System.arraycopy(combined, 0, iv, 0, iv.length);
        System.arraycopy(combined, iv.length, encryptedBytes, 0, encryptedBytes.length);

        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), AES_ALGORITHM);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance(AES_MODE);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes);
    }
}
