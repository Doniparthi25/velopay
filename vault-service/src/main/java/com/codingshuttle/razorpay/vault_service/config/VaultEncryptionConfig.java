package com.codingshuttle.razorpay.vault_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;

import javax.crypto.spec.SecretKeySpec;

@Configuration
public class VaultEncryptionConfig {


    public static BytesEncryptor panEncrypter(byte[] dek) {
        SecretKeySpec deckey = new SecretKeySpec(dek, "AES");
        return new AesBytesEncryptor(deckey, KeyGenerators.secureRandom(12),
                AesBytesEncryptor.CipherAlgorithm.GCM);
    }

}
