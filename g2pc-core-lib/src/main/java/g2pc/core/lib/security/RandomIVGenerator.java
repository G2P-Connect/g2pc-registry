package g2pc.core.lib.security;

import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;

/**
 * The type Iv generator.
 */
public class RandomIVGenerator {
    /**
     * Generate iv parameter spec.
     *
     * @return the iv parameter spec
     */
    public static IvParameterSpec generateIv() {
        byte[] iv = new byte[16]; // IV size for AES-128
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }
}