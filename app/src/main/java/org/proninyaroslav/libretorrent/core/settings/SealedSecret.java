package org.proninyaroslav.libretorrent.core.settings;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * A secret (the proxy password) sealed with a key that lives in the phone's keystore and never
 * leaves it. The sealed form is what is written to the app's settings, so a copy of that file
 * carries nothing that can be read without this phone.
 *
 * A value saved by an earlier version, in the clear, is still read, and the caller seals it
 * where it lies the first time it is read.
 */
final class SealedSecret {
    private static final String ALIAS = "honryu-settings-secret";
    private static final String SEALED = "k1:";

    private SealedSecret() {}

    private static SecretKey key() throws Exception {
        KeyStore store = KeyStore.getInstance("AndroidKeyStore");
        store.load(null);
        Key existing = store.getKey(ALIAS, null);
        if (existing instanceof SecretKey)
            return (SecretKey) existing;
        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        generator.init(new KeyGenParameterSpec.Builder(ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build());
        return generator.generateKey();
    }

    static boolean isSealed(String stored) {
        return stored != null && stored.startsWith(SEALED);
    }

    /** The sealed form of [plain]; "" for nothing, and the plain value itself if sealing fails. */
    static String seal(String plain) {
        if (plain == null || plain.isEmpty())
            return "";
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key());
            byte[] iv = cipher.getIV();
            byte[] body = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] out = Arrays.copyOf(iv, iv.length + body.length);
            System.arraycopy(body, 0, out, iv.length, body.length);
            return SEALED + Base64.encodeToString(out, Base64.NO_WRAP);
        } catch (Exception e) {
            // A keystore that will not work is no reason to lose what was typed.
            return plain;
        }
    }

    /**
     * The secret, or "" when it cannot be opened: the keystore key goes with the app and the
     * phone, so a sealed value that outlived it is typed again rather than the app failing.
     */
    static String open(String stored) {
        if (stored == null || stored.isEmpty() || !isSealed(stored))
            return stored;
        try {
            byte[] bytes = Base64.decode(stored.substring(SEALED.length()), Base64.NO_WRAP);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(128, bytes, 0, 12));
            return new String(cipher.doFinal(bytes, 12, bytes.length - 12), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }
}
