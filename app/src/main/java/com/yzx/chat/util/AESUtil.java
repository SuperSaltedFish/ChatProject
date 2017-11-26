package com.yzx.chat.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;


public class AESUtil {

    private final static String AES = "AES";
    private final static String PROVIDER = "AndroidKeyStore";
    private final static String ALGORITHM = "AES/CBC/PKCS7Padding";
    private final static String CERTIFICATE_SUBJECT = "CN=YeZhiXing";
    private final static int DEFAULT_KEY_SIZE = 192;
    private final static byte[] IV_BYTES =new byte[16];

    private static KeyStore sKeyStore;

    public static byte[] generateAESKey() {
        return generateAESKey(DEFAULT_KEY_SIZE);
    }

    public static byte[] generateAESKey(int keySize) {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES);
            keyGenerator.init(keySize);
            return keyGenerator.generateKey().getEncoded();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static Key generateAESKeyInAndroidKeyStore(String keyAlias) {
        return generateAESKeyInAndroidKeyStore(keyAlias, DEFAULT_KEY_SIZE);
    }

    @Nullable
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static Key generateAESKeyInAndroidKeyStore(String keyAlias, int keySize) {
        if (sKeyStore == null && !initKeyStore()) {
            return null;
        }
        try {
            Key key = sKeyStore.getKey(keyAlias, null);
            if (key == null) {
                KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER);
                KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_DECRYPT | KeyProperties.PURPOSE_ENCRYPT);
                builder.setBlockModes(KeyProperties.BLOCK_MODE_CBC);
                builder.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);
                builder.setKeySize(keySize);
                builder.setCertificateSubject(new X500Principal(CERTIFICATE_SUBJECT));
                generator.init(builder.build());
                key = generator.generateKey();
            }
            return key;
        } catch (KeyStoreException
                | NoSuchAlgorithmException
                | UnrecoverableKeyException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Nullable
    public static byte[] encrypt(byte[] content, byte[] keyBytes) {
        return encrypt(content, new SecretKeySpec(keyBytes, AES));
    }

    @Nullable
    public static byte[] encrypt(byte[] content, Key key) {
        if (content == null || key == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(IV_BYTES));
            return cipher.doFinal(content);
        } catch (NoSuchAlgorithmException
                | NoSuchPaddingException
                | InvalidKeyException
                | BadPaddingException
                | IllegalBlockSizeException
                | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public static byte[] decrypt(byte[] content, byte[] keyBytes) {
        return decrypt(content, new SecretKeySpec(keyBytes, AES));
    }

    @Nullable
    public static byte[] decrypt(byte[] content, Key key) {
        if (content == null || key == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(IV_BYTES));
            return cipher.doFinal(content);
        } catch (NoSuchAlgorithmException
                | NoSuchPaddingException
                | IllegalBlockSizeException
                | InvalidKeyException
                | BadPaddingException
                | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }


    private static boolean initKeyStore() {
        try {
            sKeyStore = KeyStore.getInstance(PROVIDER);
            sKeyStore.load(null);
        } catch (KeyStoreException
                | CertificateException
                | NoSuchAlgorithmException
                | IOException e) {
            e.printStackTrace();
            sKeyStore = null;
            return false;
        }
        return true;
    }

}
