package com.yzx.chat.core.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

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

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;


/**
 * Created by YZX on 2018年12月13日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class ECCUtil {

    private final static String ECC = Build.VERSION.SDK_INT < Build.VERSION_CODES.M ? "EC" : KeyProperties.KEY_ALGORITHM_EC;
    private final static String DEFAULT_ELLIPTIC_CURVE = "secp256k1";
    private final static String DEFAULT_DIGEST_ALGORITHM = "SHA1withECDSA";

    @Nullable
    public static KeyPair generateECCKeyPairCompat(Context context, String keyAlias) {
        return generateECCKeyPairCompat(context, keyAlias, DEFAULT_ELLIPTIC_CURVE);
    }

    @Nullable
    public static KeyPair generateECCKeyPairCompat(Context context, String keyAlias, String ellipticCurve) {
        KeyPair keyPair = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            keyPair = ECCUtil.ECCBaseOnAndroidKeyStore.generateECCKeyPair(context, keyAlias, ellipticCurve);
        }
        if (keyPair == null) {
            keyPair = ECCUtil.ECCBaseOnBC.generateECCKeyPair(ellipticCurve);
        }
        return keyPair;
    }

    public static PublicKey loadECPublicKeyCompat(byte[] publicKey) {
        PublicKey key = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            key = ECCUtil.ECCBaseOnAndroidKeyStore.loadECPublicKey(publicKey);
        }
        if (key == null) {
            key = ECCUtil.ECCBaseOnBC.loadECPublicKey(publicKey);
        }
        return key;
    }

    public static PrivateKey loadECPrivateKeyCompat(byte[] privateKey) {
        PrivateKey key = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            key = ECCUtil.ECCBaseOnAndroidKeyStore.loadECPrivateKey(privateKey);
        }
        if (key == null) {
            key = ECCUtil.ECCBaseOnBC.loadECPrivateKey(privateKey);
        }
        return key;
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
            LogUtil.d(e.toString(), e);
        }
        return null;
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
            LogUtil.d(e.toString(), e);
        }
        return null;
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
            LogUtil.d(e.toString(), e);
        }
        return null;
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
            LogUtil.d(e.toString(), e);
        }
        return null;
    }

    public static Signature loadSignatureOfSignTypeCompat(PrivateKey privateKey) {
        Signature signature = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            signature = ECCBaseOnAndroidKeyStore.loadSignatureOfSignType(privateKey);
        }
        if (signature == null) {
            signature = ECCBaseOnBC.loadSignatureOfSignType(privateKey);
        }
        return signature;
    }


    public static Signature loadSignatureOfVerifyTypeCompat(PublicKey publicKey) {
        Signature signature = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            signature = ECCBaseOnAndroidKeyStore.loadSignatureOfVerifyType(publicKey);
        }
        if (signature == null) {
            signature = ECCBaseOnBC.loadSignatureOfVerifyType(publicKey);
        }
        return signature;
    }

    public static byte[] sign(Signature signature, byte[] data) {
        if (signature == null || data == null || data.length == 0) {
            return null;
        }
        try {
            signature.update(data);
            return signature.sign();
        } catch (SignatureException e) {
            LogUtil.d(e.toString(), e);
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
            LogUtil.d(e.toString(), e);
        }
        return false;
    }

    public static class ECCBaseOnAndroidKeyStore {

        private final static String PROVIDER = "AndroidKeyStore";

        private static KeyStore sKeyStore;

        private static boolean initAndroidKeyStore() {
            try {
                sKeyStore = KeyStore.getInstance(PROVIDER);
                sKeyStore.load(null);
            } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
                LogUtil.d(e.toString(), e);
                sKeyStore = null;
                return false;
            }
            return true;
        }


        @Nullable
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        public static KeyPair generateECCKeyPair(Context context, String keyAlias) {
            return generateECCKeyPair(context, keyAlias, DEFAULT_ELLIPTIC_CURVE);
        }

        @Nullable
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        public static KeyPair generateECCKeyPair(Context context, String keyAlias, String ellipticCurve) {
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
                LogUtil.d(e.toString(), e);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return generateECCKeyPairApi23(keyAlias, ellipticCurve);
            } else {
                return generateECCKeyPairApi18(context, keyAlias, ellipticCurve);
            }
        }

        @Nullable
        @SuppressWarnings("deprecation")
        @SuppressLint("WrongConstant")
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        private static KeyPair generateECCKeyPairApi18(Context context, String keyAlias, String ellipticCurve) {
            Calendar startTime = Calendar.getInstance();
            Calendar endTime = Calendar.getInstance();
            endTime.add(Calendar.YEAR, 10);
            try {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ECC, PROVIDER);
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
                LogUtil.d(e.toString(), e);
            }
            return null;
        }

        @Nullable
        @TargetApi(Build.VERSION_CODES.M)
        @RequiresApi(api = Build.VERSION_CODES.M)
        private static KeyPair generateECCKeyPairApi23(String keyAlias, String ellipticCurve) {
            try {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ECC, PROVIDER);
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
                LogUtil.d(e.toString(), e);
            }
            return null;
        }

        public static PublicKey loadECPublicKey(byte[] publicKey) {
            if (publicKey == null) {
                return null;
            }
            try {
                KeyFactory kf = KeyFactory.getInstance(ECC, PROVIDER);
                X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKey);
                return kf.generatePublic(x509KeySpec);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
                LogUtil.d(e.toString(), e);
            }
            return null;
        }

        public static PrivateKey loadECPrivateKey(byte[] privateKey) {
            if (privateKey == null) {
                return null;
            }
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(privateKey);
            try {
                KeyFactory kf = KeyFactory.getInstance(ECC, PROVIDER);
                return kf.generatePrivate(pkcs8KeySpec);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
                LogUtil.d(e.toString(), e);
            }
            return null;
        }


        @Nullable
        public static byte[] encryptByPublicKey(byte[] data, byte[] publicKey) {
            if (data == null || publicKey == null) {
                return null;
            }
            return ECCUtil.encryptByPublicKey(data, (ECPublicKey) loadECPublicKey(publicKey));
        }

        @Nullable
        public static byte[] encryptByPrivateKey(byte[] data, byte[] privateKey) {
            if (data == null || privateKey == null) {
                return null;
            }
            return ECCUtil.encryptByPrivateKey(data, loadECPrivateKey(privateKey));
        }

        @Nullable
        public static byte[] decryptByPublicKey(byte[] data, byte[] publicKey) {
            if (data == null || publicKey == null) {
                return null;
            }
            return ECCUtil.decryptByPublicKey(data, (ECPublicKey) loadECPublicKey(publicKey));
        }


        @Nullable
        public static byte[] decryptByPrivateKey(byte[] data, byte[] privateKey) {
            if (data == null || privateKey == null) {
                return null;
            }
            return ECCUtil.decryptByPrivateKey(data, loadECPrivateKey(privateKey));
        }


        public static Signature loadSignatureOfSignType(PrivateKey privateKey) {
            Signature signature;
            try {
                signature = Signature.getInstance(DEFAULT_DIGEST_ALGORITHM, PROVIDER);
                signature.initSign(privateKey);
                return signature;
            } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException e) {
                LogUtil.d(e.toString(), e);
            }
            return null;
        }


        public static Signature loadSignatureOfVerifyType(PublicKey publicKey) {
            Signature signature;
            try {
                signature = Signature.getInstance(DEFAULT_DIGEST_ALGORITHM, PROVIDER);
                signature.initVerify(publicKey);
                return signature;
            } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException e) {
                LogUtil.d(e.toString(), e);
            }
            return null;
        }

        public static byte[] sign(byte[] data, PrivateKey privateKey) {
            return ECCUtil.sign(loadSignatureOfSignType(privateKey), data);
        }


        public static boolean verify(byte[] data, byte[] sign, PublicKey publicKey) {
            return ECCUtil.verify(loadSignatureOfVerifyType(publicKey), data, sign);
        }
    }

    public static class ECCBaseOnBC {

        private final static String PROVIDER = "BC";

        @Nullable
        public static KeyPair generateECCKeyPair() {
            return generateECCKeyPair(DEFAULT_ELLIPTIC_CURVE);
        }

        @Nullable
        public static KeyPair generateECCKeyPair(String ellipticCurve) {
            try {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ECC, PROVIDER);
                keyPairGenerator.initialize(new ECGenParameterSpec(ellipticCurve));
                return keyPairGenerator.genKeyPair();
            } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
                LogUtil.d(e.toString(), e);
                return null;
            }
        }

        public static PublicKey loadECPublicKey(byte[] publicKey) {
            if (publicKey == null) {
                return null;
            }
            try {
                KeyFactory kf = KeyFactory.getInstance(ECC, PROVIDER);
                X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKey);
                return kf.generatePublic(x509KeySpec);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
                LogUtil.d(e.toString(), e);
            }
            return null;
        }

        public static PrivateKey loadECPrivateKey(byte[] privateKey) {
            if (privateKey == null) {
                return null;
            }
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(privateKey);
            try {
                KeyFactory kf = KeyFactory.getInstance(ECC, PROVIDER);
                return kf.generatePrivate(pkcs8KeySpec);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchProviderException e) {
                LogUtil.d(e.toString(), e);
            }
            return null;
        }


        @Nullable
        public static byte[] encryptByPublicKey(byte[] data, byte[] publicKey) {
            if (data == null || publicKey == null) {
                return null;
            }
            return ECCUtil.encryptByPublicKey(data, (ECPublicKey) loadECPublicKey(publicKey));
        }

        @Nullable
        public static byte[] encryptByPrivateKey(byte[] data, byte[] privateKey) {
            if (data == null || privateKey == null) {
                return null;
            }
            return ECCUtil.encryptByPrivateKey(data, loadECPrivateKey(privateKey));
        }

        @Nullable
        public static byte[] decryptByPublicKey(byte[] data, byte[] publicKey) {
            if (data == null || publicKey == null) {
                return null;
            }
            return ECCUtil.decryptByPublicKey(data, (ECPublicKey) loadECPublicKey(publicKey));
        }


        @Nullable
        public static byte[] decryptByPrivateKey(byte[] data, byte[] privateKey) {
            if (data == null || privateKey == null) {
                return null;
            }
            return ECCUtil.decryptByPrivateKey(data, loadECPrivateKey(privateKey));
        }


        public static Signature loadSignatureOfSignType(PrivateKey privateKey) {
            Signature signature;
            try {
                signature = Signature.getInstance(DEFAULT_DIGEST_ALGORITHM, PROVIDER);
                signature.initSign(privateKey);
                return signature;
            } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException e) {
                LogUtil.d(e.toString(), e);
            }
            return null;
        }


        public static Signature loadSignatureOfVerifyType(PublicKey publicKey) {
            Signature signature;
            try {
                signature = Signature.getInstance(DEFAULT_DIGEST_ALGORITHM, PROVIDER);
                signature.initVerify(publicKey);
                return signature;
            } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException e) {
                LogUtil.d(e.toString(), e);
            }
            return null;
        }

        public static byte[] sign(byte[] data, PrivateKey privateKey) {
            return ECCUtil.sign(loadSignatureOfSignType(privateKey), data);
        }


        public static boolean verify(byte[] data, byte[] sign, PublicKey publicKey) {
            return ECCUtil.verify(loadSignatureOfVerifyType(publicKey), data, sign);
        }
    }
}
