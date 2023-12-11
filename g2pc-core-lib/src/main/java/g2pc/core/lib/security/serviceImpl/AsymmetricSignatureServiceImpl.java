package g2pc.core.lib.security.serviceImpl;

import g2pc.core.lib.security.service.AsymmetricSignatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;


@Service
public class AsymmetricSignatureServiceImpl implements AsymmetricSignatureService {

    @Autowired
    private ResourceLoader resourceLoader;

    /**
     *
     * @param data
     * @return
     * @throws InvalidKeyException
     * @throws Exception
     */
    @Override
    public byte[] sign(String data ) throws InvalidKeyException, Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(getPrivate());
        signature.update(data.getBytes());
        return signature.sign();
    }

    /**
     *
     * @return
     * @throws Exception
     */
    @Override
    public PrivateKey getPrivate() throws Exception {
        return extractP12Certificate().getPrivateKey();
    }

    /**
     *
     * @return
     * @throws Exception
     */
    @Override
    public PublicKey getPublic() throws Exception {
        Certificate certificate = extractP12Certificate().getCertificate();
        return certificate.getPublicKey();
    }

    /**
     *
     * @param data
     * @param signature
     * @return
     * @throws Exception
     */
    @Override
    public boolean verifySignature(byte[] data, byte[] signature) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(getPublic());
        sig.update(data);

        return sig.verify(signature);
    }

    /**
     *
     * @return
     * @throws IOException
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableEntryException
     */
    @Override
    public KeyStore.PrivateKeyEntry extractP12Certificate() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableEntryException {
        Resource resource = resourceLoader.getResource("classpath:private.p12");
        InputStream fis = resource.getInputStream();

        KeyStore ks = KeyStore.getInstance("PKCS12");

        char[] password = "tekdi".toCharArray();
        ks.load(fis, password);

        KeyStore.ProtectionParameter protectionParameter = new KeyStore.PasswordProtection(password);
        KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) ks.getEntry("1", protectionParameter);

        return pkEntry;
    }
}
