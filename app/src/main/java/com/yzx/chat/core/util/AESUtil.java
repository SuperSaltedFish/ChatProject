package com.yzx.chat.core.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

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

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;


public class AESUtil {
    private final static String AES;
    private final static String BLOCK_MODE;
    private final static String ENCRYPTION_PADDING;
    private final static String PROVIDER;
    private final static int DEFAULT_KEY_SIZE;
    private final static String ALGORITHM;

    static {
        PROVIDER = "AndroidKeyStore";
        DEFAULT_KEY_SIZE = 192;
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

    public static Key generateAESKey() {
        return generateAESKey(DEFAULT_KEY_SIZE);
    }

    public static SecretKey generateAESKey(int keySize) {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES);
            keyGenerator.init(keySize);
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            LogUtil.d(e.toString(), e);
        }
        return null;
    }


    @Nullable
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static SecretKey generateAESKeyInAndroidKeyStoreApi23(String keyAlias) {
        return generateAESKeyInAndroidKeyStoreApi23(keyAlias, DEFAULT_KEY_SIZE);
    }

    @SuppressLint("WrongConstant")
    @Nullable
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static SecretKey generateAESKeyInAndroidKeyStoreApi23(String keyAlias, int keySize) {
        if (sKeyStore == null && !initKeyStore()) {
            return null;
        }
        try {
            Key key = sKeyStore.getKey(keyAlias, null);
            if (key == null) {
                KeyGenerator generator = KeyGenerator.getInstance(AES, PROVIDER);
                KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(
                        keyAlias,
                        KeyProperties.PURPOSE_DECRYPT | KeyProperties.PURPOSE_ENCRYPT)
                        .setBlockModes(BLOCK_MODE)
                        .setEncryptionPaddings(ENCRYPTION_PADDING)
                        .setKeySize(keySize)
                        .setCertificateSubject(new X500Principal("CN=" + keyAlias))
                        .setRandomizedEncryptionRequired(false);//不使用随机IV
                generator.init(builder.build());
                key = generator.generateKey();
            }
            return (SecretKey) key;
        } catch (KeyStoreException
                | NoSuchAlgorithmException
                | UnrecoverableKeyException
                | NoSuchProviderException
                | InvalidAlgorithmParameterException e) {
            LogUtil.d(e.toString(), e);
        }
        return null;
    }

    public static SecretKey loadKey(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, AES);
    }

    public static byte[] getIVFromSecretKey(SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.getIV();
        } catch (NoSuchAlgorithmException
                | NoSuchPaddingException
                | InvalidKeyException e) {
            LogUtil.d(e.toString(), e);
        }
        return null;
    }

    @Nullable
    public static byte[] encrypt(byte[] content, byte[] keyBytes, @Nullable byte[] iv) {
        return encrypt(content, loadKey(keyBytes), iv);
    }

    @Nullable
    public static byte[] encrypt(byte[] content, SecretKey key, @Nullable byte[] iv) {
        if (content == null || key == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            if (iv != null) {
                cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, key);
            }
            return cipher.doFinal(content);
        } catch (NoSuchAlgorithmException
                | NoSuchPaddingException
                | InvalidKeyException
                | BadPaddingException
                | InvalidAlgorithmParameterException
                | IllegalBlockSizeException e) {
            LogUtil.d(e.toString(), e);
        }
        return null;
    }

    @Nullable
    public static byte[] decrypt(byte[] content, byte[] keyBytes, @Nullable byte[] iv) {
        return decrypt(content, loadKey(keyBytes), iv);
    }

    @Nullable
    public static byte[] decrypt(byte[] content, SecretKey key, @Nullable byte[] iv) {
        if (content == null || key == null) {
            return null;
        }
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            if (iv != null) {
                cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
            } else {
                cipher.init(Cipher.DECRYPT_MODE, key);
            }
            return cipher.doFinal(content);
        } catch (NoSuchAlgorithmException
                | NoSuchPaddingException
                | IllegalBlockSizeException
                | InvalidKeyException
                | InvalidAlgorithmParameterException
                | BadPaddingException e) {
            LogUtil.d(e.toString(), e);
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
            LogUtil.d(e.toString(), e);
            sKeyStore = null;
            return false;
        }
        return true;
    }

}
