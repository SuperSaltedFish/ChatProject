package com.yzx.chat.core.util;

import android.annotation.SuppressLint;
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
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;


public class AESUtil {
    private final static String AES;
    private final static String BLOCK_MODE;
    private final static String ENCRYPTION_PADDING;
    private final static String PROVIDER;
    private final static int DEFAULT_KEY_SIZE;
    private final static String ALGORITHM;
    private final static byte[] IV_BYTES;

    static {
        PROVIDER = "AndroidKeyStore";
        DEFAULT_KEY_SIZE = 192;
        IV_BYTES = new byte[16];
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            AES = "AES";
            BLOCK_MODE = "CBC";
            ENCRYPTION_PADDING = "PKCS7Padding";
        } else {
            AES = KeyProperties.KEY_ALGORITHM_AES;
            BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC;
            ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7;
        }
        ALGORITHM = String.format("%s/%s/%s", AES, BLOCK_MODE, ENCRYPTION_PADDING);
    }

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
            LogUtil.d(e.toString(),e);
        }
        return null;
    }


    @Nullable
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static Key generateAESKeyInAndroidKeyStoreApi23(String keyAlias) {
        return generateAESKeyInAndroidKeyStoreApi23(keyAlias, DEFAULT_KEY_SIZE);
    }

    @SuppressLint("WrongConstant")
    @Nullable
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static Key generateAESKeyInAndroidKeyStoreApi23(String keyAlias, int keySize) {
        if (sKeyStore == null && !initKeyStore()) {
            return null;
        }
        try {
            Key key = sKeyStore.getKey(keyAlias, null);
            if (key == null) {
                KeyGenerator generator = KeyGenerator.getInstance(AES, PROVIDER);
                KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(
                        keyAlias,
                        KeyProperties.PURPOSE_DECRYPT | KeyProperties.PURPOSE_ENCRYPT);
                builder.setBlockModes(BLOCK_MODE);
                builder.setEncryptionPaddings(ENCRYPTION_PADDING);
                builder.setKeySize(keySize);
                builder.setCertificateSubject(new X500Principal("CN=" + keyAlias));
                generator.init(builder.build());
                key = generator.generateKey();
            }
            return key;
        } catch (KeyStoreException
                | NoSuchAlgorithmException
                | UnrecoverableKeyException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException e) {
            LogUtil.d(e.toString(),e);
        }
        return null;
    }

    public static SecretKey loadKey(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, AES);
    }

    @Nullable
    public static byte[] encrypt(byte[] content, byte[] keyBytes) {
        return encrypt(content, loadKey(keyBytes));
    }

    @Nullable
    public static byte[] encrypt(byte[] content, SecretKey key) {
        if (content == null || key == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(IV_BYTES));
//            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(content);
        } catch (NoSuchAlgorithmException
                | NoSuchPaddingException
                | InvalidKeyException
                | BadPaddingException
                | InvalidAlgorithmParameterException
                | IllegalBlockSizeException e) {
            LogUtil.d(e.toString(),e);
        }
        return null;
    }

    @Nullable
    public static byte[] decrypt(byte[] content, byte[] keyBytes) {
        return decrypt(content, loadKey(keyBytes));
    }

    @Nullable
    public static byte[] decrypt(byte[] content, SecretKey key) {
        if (content == null || key == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
//            cipher.init(Cipher.DECRYPT_MODE, key);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(IV_BYTES));
            return cipher.doFinal(content);
        } catch (NoSuchAlgorithmException
                | NoSuchPaddingException
                | IllegalBlockSizeException
                | InvalidKeyException
                | InvalidAlgorithmParameterException
                | BadPaddingException e) {
            LogUtil.d(e.toString(),e);
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
            LogUtil.d(e.toString(),e);
            sKeyStore = null;
            return false;
        }
        return true;
    }

}
