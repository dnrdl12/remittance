package io.dnrdl12.remittance.comm.crypto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * packageName    : io.dnrdl12.remittance.comm.crypto
 * fileName       : CryptoUtil
 * author         : JW.CHOI
 * date           : 2025-11-12
 * description    :  HMAC-SHA256 암복구화
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025-11-12        JW.CHOI              최초 생성
 */

@Component
@RequiredArgsConstructor
public class CryptoUtil {

    private final CryptoProperties props;
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    public String encrypt(String plainText) {
        if (plainText == null) return null;

        try {
            byte[] key = Base64.getDecoder().decode(props.getAesKeyBase64());
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            GCMParameterSpec gcm = new GCMParameterSpec(TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcm);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            ByteBuffer bb = ByteBuffer.allocate(iv.length + cipherText.length);
            bb.put(iv);
            bb.put(cipherText);
            return Base64.getEncoder().encodeToString(bb.array());
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    public String decrypt(String base64IvCipherText) {
        if (base64IvCipherText == null) return null;

        try {
            byte[] key = Base64.getDecoder().decode(props.getAesKeyBase64());
            byte[] ivCt = Base64.getDecoder().decode(base64IvCipherText);

            byte[] iv = Arrays.copyOfRange(ivCt, 0, IV_LENGTH);
            byte[] ct = Arrays.copyOfRange(ivCt, IV_LENGTH, ivCt.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            GCMParameterSpec gcm = new GCMParameterSpec(TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcm);

            byte[] plain = cipher.doFinal(ct);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Decryption failed", e);
        }
    }

    public String hmacHash(String value) {
        if (value == null) return null;
        try {
            var mac = javax.crypto.Mac.getInstance("HmacSHA256");
            byte[] key = java.util.Base64.getDecoder().decode(props.getAesKeyBase64()); // 키 별도 분리 권장
            mac.init(new javax.crypto.spec.SecretKeySpec(key, "HmacSHA256"));
            byte[] out = mac.doFinal(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(out); // VARCHAR(88) 가정
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("HMAC failed", e);
        }
    }

}