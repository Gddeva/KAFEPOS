package cafe.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Hash password dengan PBKDF2 (bawaan JDK, tanpa library tambahan). Format: iterasi:salt:hash */
public final class PasswordHasher {
    private static final int ITER = 65_536, KEY_BITS = 256;
    private PasswordHasher() {}

    public static String hash(String password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return ITER + ":" + Base64.getEncoder().encodeToString(salt) + ":" +
               Base64.getEncoder().encodeToString(derive(password, salt, ITER));
    }

    public static boolean verify(String password, String stored) {
        try {
            String[] p = stored.split(":");
            byte[] expected = Base64.getDecoder().decode(p[2]);
            byte[] actual = derive(password, Base64.getDecoder().decode(p[1]), Integer.parseInt(p[0]));
            return MessageDigest.isEqual(expected, actual);
        } catch (Exception e) { return false; }
    }

    private static byte[] derive(String pw, byte[] salt, int iter) {
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(new PBEKeySpec(pw.toCharArray(), salt, iter, KEY_BITS)).getEncoded();
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
}
