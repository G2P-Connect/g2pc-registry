package g2pc.core.lib.security.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;


@Service
public interface AsymmetricSignatureService {

    public byte[] sign(String data) throws InvalidKeyException, Exception;

    public PrivateKey getPrivate() throws Exception ;

    public PublicKey getPublic() throws Exception ;

    public boolean verifySignature(byte[] data, byte[] signature) throws Exception ;

    public KeyStore.PrivateKeyEntry extractP12Certificate() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableEntryException;
}
