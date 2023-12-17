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
    public byte[] sign(String data , InputStream fis ,  String password) throws InvalidKeyException, Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(getPrivate(fis, password));
        signature.update(data.getBytes());
        return signature.sign();
    }

    /**
     *
     * @return
     * @throws Exception
     */
    @Override
    public PrivateKey getPrivate(InputStream fis ,  String password) throws Exception {
        return extractP12Certificate(fis , password).getPrivateKey();
    }

    /**
     *
     * @return
     * @throws Exception
     */
    @Override
    public PublicKey getPublic(InputStream fis ,  String password) throws Exception {
        Certificate certificate = extractP12Certificate(fis , password).getCertificate();
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
    public boolean verifySignature(byte[] data, byte[] signature , InputStream fis ,  String password) throws Exception {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(getPublic(fis,password));
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
    public KeyStore.PrivateKeyEntry extractP12Certificate(InputStream fis , String password) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableEntryException {
        KeyStore ks = KeyStore.getInstance("PKCS12");

        char[] passwordInChar = password.toCharArray();
        ks.load(fis, passwordInChar);

        KeyStore.ProtectionParameter protectionParameter = new KeyStore.PasswordProtection(passwordInChar);
        KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) ks.getEntry("1", protectionParameter);

        return pkEntry;
    }
}
