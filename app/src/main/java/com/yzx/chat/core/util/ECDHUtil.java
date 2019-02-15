package com.yzx.chat.core.util;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

import javax.crypto.KeyAgreement;

/**
 * Created by YZX on 2018年12月13日.
 * 每一个不曾起舞的日子 都是对生命的辜负
 */
public class ECDHUtil {

    private static final String ECDH = "ECDH";

    public static byte[] ecdh(PrivateKey localKey, Key remoteKey) {
        if (localKey == null || remoteKey == null) {
            return null;
        }
        KeyAgreement agreement;
        try {
            agreement = KeyAgreement.getInstance(ECDH);
            agreement.init(localKey);
            agreement.doPhase(remoteKey, true);
            return agreement.generateSecret();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
    }
}
