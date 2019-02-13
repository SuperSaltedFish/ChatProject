package com.yzx.chat.core.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.ECKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NullCipher;
import javax.security.auth.x500.X500Principal;



/**
 * Created by YZX on 2018年12月13日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class ECCUtil {

    private final static String ECC = Build.VERSION.SDK_INT < Build.VERSION_CODES.M ? "EC" : KeyProperties.KEY_ALGORITHM_EC;
    private final static String PROVIDER_ANDROID_KEY_STORE = "AndroidKeyStore";
    private final static String PROVIDER_BC = "BC";
    private final static String DEFAULT_ELLIPTIC_CURVE = "secp256k1";
    private final static String DEFAULT_DIGEST_ALGORITHM = "SHA1withECDSA";

    private static KeyStore sKeyStore;

    private static boolean initAndroidKeyStore() {
        try {
            sKeyStore = KeyStore.getInstance(PROVIDER_ANDROID_KEY_STORE);
            sKeyStore.load(null);
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            LogUtil.d(e.toString(),e);
            sKeyStore = null;
            return false;
        }
        return true;
    }

    @Nullable
    public static KeyPair generateECCKeyPairByBC() {
        return generateECCKeyPairByBC(DEFAULT_ELLIPTIC_CURVE);
    }

    @Nullable
    public static KeyPair generateECCKeyPairByBC(String ellipticCurve) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ECC, PROVIDER_BC);
            keyPairGenerator.initialize(new ECGenParameterSpec(ellipticCurve));
            return keyPairGenerator.genKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            LogUtil.d(e.toString(),e);
            return null;
        }
    }

    @Nullable
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static KeyPair generateECCKeyPairInAndroidKeyStore(Context context, String keyAlias) {
        return generateECCKeyPairInAndroidKeyStore(context, keyAlias, DEFAULT_ELLIPTIC_CURVE);
    }

    @Nullable
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static KeyPair generateECCKeyPairInAndroidKeyStore(Context context, String keyAlias, String ellipticCurve) {
        if (sKeyStore == null && !initAndroidKeyStore()) {
            return null;
        }
        try {
            Certificate mCertificate = sKeyStore.getCertificate(keyAlias);
            PrivateKey privateKey = (PrivateKey) sKeyStore.getKey(keyAlias, null);
            if (mCertificate != null && privateKey != null) {
                return new KeyPair(mCertificate.getPublicKey(), privateKey);
            }
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            LogUtil.d(e.toString(),e);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return generateECCKeyPairInAndroidKeyStoreApi23(keyAlias, ellipticCurve);
        } else {
            return generateECCKeyPairInAndroidKeyStoreApi18(context, keyAlias, ellipticCurve);
        }
    }

    @Nullable
    @SuppressWarnings("deprecation")
    @SuppressLint("WrongConstant")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private static KeyPair generateECCKeyPairInAndroidKeyStoreApi18(Context context, String keyAlias, String ellipticCurve) {
        Calendar startTime = Calendar.getInstance();
        Calendar endTime = Calendar.getInstance();
        endTime.add(Calendar.YEAR, 10);
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ECC, PROVIDER_ANDROID_KEY_STORE);
            KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                    .setAlgorithmParameterSpec(new ECGenParameterSpec(ellipticCurve))
                    .setSubject(new X500Principal("CN=" + keyAlias))
                    .setAlias(keyAlias)
                    .setKeyType(ECC)
                    .setSerialNumber(BigInteger.ONE)
                    .setStartDate(startTime.getTime())
                    .setEndDate(endTime.getTime())
                    .build();
            keyPairGenerator.initialize(spec);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            LogUtil.d(e.toString(),e);
        }
        return null;
    }

    @Nullable
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    private static KeyPair generateECCKeyPairInAndroidKeyStoreApi23(String keyAlias, String ellipticCurve) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ECC, PROVIDER_ANDROID_KEY_STORE);
            KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                    keyAlias,
                    KeyProperties.PURPOSE_DECRYPT | KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_SIGN)
                    .setCertificateSubject(new X500Principal("CN=" + keyAlias))
                    .setDigests(KeyProperties.DIGEST_SHA1, KeyProperties.DIGEST_SHA256)
                    .setAlgorithmParameterSpec(new ECGenParameterSpec(ellipticCurve))
                    .build();
            keyPairGenerator.initialize(spec);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            LogUtil.d(e.toString(),e);
        }
        return null;
    }

    public static PublicKey loadECPublicKeyFromBC(byte[] publicKey) {
        if (publicKey == null) {
            return null;
        }
        try {
            KeyFactory kf = KeyFactory.getInstance(ECC, PROVIDER_BC);
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKey);
            return kf.generatePublic(x509KeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
            LogUtil.d(e.toString(),e);
        }
        return null;
    }

    public static PrivateKey loadECPrivateKeyFromBC(byte[] privateKey) {
        if (privateKey == null) {
            return null;
        }
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(privateKey);
        try {
            KeyFactory kf = KeyFactory.getInstance(ECC, PROVIDER_BC);
            return kf.generatePrivate(pkcs8KeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
            LogUtil.d(e.toString(),e);
        }
        return null;
    }

    public static PublicKey loadECPublicKeyFromAndroidKeyStore(byte[] publicKey) {
        if (publicKey == null) {
            return null;
        }
        try {
            KeyFactory kf = KeyFactory.getInstance(ECC, PROVIDER_ANDROID_KEY_STORE);
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKey);
            return kf.generatePublic(x509KeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
            LogUtil.d(e.toString(),e);
        }
        return null;
    }

    public static PrivateKey loadECPrivateKeyFromAndroidKeyStore(byte[] privateKey) {
        if (privateKey == null) {
            return null;
        }
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(privateKey);
        try {
            KeyFactory kf = KeyFactory.getInstance(ECC, PROVIDER_ANDROID_KEY_STORE);
            return kf.generatePrivate(pkcs8KeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
            LogUtil.d(e.toString(),e);
        }
        return null;
    }


    @Nullable
    public static byte[] encryptByBCPublicKey(byte[] data, byte[] publicKey) {
        if (data == null || publicKey == null) {
            return null;
        }
        return encryptByPublicKey(data, (ECPublicKey) loadECPublicKeyFromBC(publicKey));
    }

    @Nullable
    public static byte[] encryptByAndroidKeyStorePublicKey(byte[] data, byte[] publicKey) {
        if (data == null || publicKey == null) {
            return null;
        }
        return encryptByPublicKey(data, (ECPublicKey) loadECPublicKeyFromAndroidKeyStore(publicKey));
    }


    @Nullable
    public static byte[] encryptByPublicKey(byte[] data, ECPublicKey publicKey) {
        if (data == null || publicKey == null) {
            return null;
        }
        Cipher cipher = new NullCipher();
        try {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey, publicKey.getParams());
            return cipher.doFinal(data);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            LogUtil.d(e.toString(),e);
        }
        return null;
    }


    @Nullable
    public static byte[] encryptByBCPrivateKey(byte[] data, byte[] privateKey) {
        if (data == null || privateKey == null) {
            return null;
        }
        return encryptByPrivateKey(data, loadECPrivateKeyFromBC(privateKey));
    }

    @Nullable
    public static byte[] encryptByAndroidKeyStorePrivateKey(byte[] data, byte[] privateKey) {
        if (data == null || privateKey == null) {
            return null;
        }
        return encryptByPrivateKey(data, loadECPrivateKeyFromAndroidKeyStore(privateKey));
    }

    @Nullable
    public static byte[] encryptByPrivateKey(byte[] data, PrivateKey privateKey) {
        if (data == null || !(privateKey instanceof ECKey)) {
            return null;
        }
        Cipher cipher = new NullCipher();
        try {
            cipher.init(Cipher.ENCRYPT_MODE, privateKey, ((ECKey) privateKey).getParams());
            return cipher.doFinal(data);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            LogUtil.d(e.toString(),e);
        }
        return null;
    }


    @Nullable
    public static byte[] decryptByBCPublicKey(byte[] data, byte[] publicKey) {
        if (data == null || publicKey == null) {
            return null;
        }
        return decryptByPublicKey(data, (ECPublicKey) loadECPublicKeyFromBC(publicKey));
    }

    @Nullable
    public static byte[] decryptByAndroidKeyStorePublicKey(byte[] data, byte[] publicKey) {
        if (data == null || publicKey == null) {
            return null;
        }
        return decryptByPublicKey(data, (ECPublicKey) loadECPublicKeyFromAndroidKeyStore(publicKey));
    }

    @Nullable
    public static byte[] decryptByPublicKey(byte[] data, ECPublicKey publicKey) {
        if (data == null || publicKey == null) {
            return null;
        }
        Cipher cipher = new NullCipher();
        try {
            cipher.init(Cipher.DECRYPT_MODE, publicKey, publicKey.getParams());
            return cipher.doFinal(data);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            LogUtil.d(e.toString(),e);
        }
        return null;
    }


    @Nullable
    public static byte[] decryptByBCPrivateKey(byte[] data, byte[] privateKey) {
        if (data == null || privateKey == null) {
            return null;
        }
        return decryptByPrivateKey(data, loadECPrivateKeyFromBC(privateKey));
    }

    @Nullable
    public static byte[] decryptByAndroidKeyStorePrivateKey(byte[] data, byte[] privateKey) {
        if (data == null || privateKey == null) {
            return null;
        }
        return decryptByPrivateKey(data, loadECPrivateKeyFromAndroidKeyStore(privateKey));
    }

    @Nullable
    public static byte[] decryptByPrivateKey(byte[] data, PrivateKey privateKey) {
        if (data == null || !(privateKey instanceof ECKey)) {
            return null;
        }
        Cipher cipher = new NullCipher();
        try {
            cipher.init(Cipher.DECRYPT_MODE, privateKey, ((ECKey) privateKey).getParams());
            return cipher.doFinal(data);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e) {
            LogUtil.d(e.toString(),e);
        }
        return null;
    }

    public static byte[] signByAndroidKeyStorePublicKey(byte[] data, PublicKey publicKey) {
        Signature signature;
        try {
            signature = Signature.getInstance(DEFAULT_DIGEST_ALGORITHM, PROVIDER_ANDROID_KEY_STORE);
            signature.initVerify(publicKey);
            signature.update(data);
            return signature.sign();
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException | NoSuchProviderException e) {
            LogUtil.d(e.toString(),e);
        }
        return null;
    }

    public static Signature loadSignatureOfSignType(PrivateKey privateKey) {
        Signature signature;
        try {
            signature = Signature.getInstance(DEFAULT_DIGEST_ALGORITHM, PROVIDER_BC);
            signature.initSign(privateKey);
            return signature;
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException e) {
            LogUtil.d(e.toString(),e);
        }
        return null;
    }

    public static Signature loadSignatureOfVerifyType(PublicKey publicKey) {
        Signature signature;
        try {
            signature = Signature.getInstance(DEFAULT_DIGEST_ALGORITHM, PROVIDER_BC);
            signature.initVerify(publicKey);
            return signature;
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException e) {
            LogUtil.d(e.toString(),e);
        }
        return null;
    }

    public static byte[] sign(byte[] data, PrivateKey privateKey) {
        return sign(loadSignatureOfSignType(privateKey), data);
    }

    public static boolean verify(byte[] data, byte[] sign, PublicKey publicKey) {
        return verify(loadSignatureOfVerifyType(publicKey), data, sign);
    }

    public static byte[] sign(Signature signature, byte[] data) {
        if (signature == null || data == null || data.length == 0) {
            return null;
        }
        try {
            signature.update(data);
            return signature.sign();
        } catch (SignatureException e) {
            LogUtil.d(e.toString(),e);
        }
        return null;
    }

    public static boolean verify(Signature signature, byte[] data, byte[] sign) {
        if (signature == null || data == null || sign == null || data.length == 0 || sign.length == 0) {
            return false;
        }
        try {
            signature.update(data);
            return signature.verify(sign);
        } catch (SignatureException e) {
            LogUtil.d(e.toString(),e);
        }
        return false;
    }


}
