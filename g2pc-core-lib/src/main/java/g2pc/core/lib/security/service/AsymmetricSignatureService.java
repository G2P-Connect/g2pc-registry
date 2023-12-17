package g2pc.core.lib.security.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;


@Service
public interface AsymmetricSignatureService {

    public byte[] sign(String data, InputStream fis ,  String password) throws InvalidKeyException, Exception;

    public PrivateKey getPrivate(InputStream fis ,  String password) throws Exception ;

    public PublicKey getPublic(InputStream fis ,  String password) throws Exception ;

    public boolean verifySignature(byte[] data, byte[] signature , InputStream fis ,  String password) throws Exception ;

    public KeyStore.PrivateKeyEntry extractP12Certificate(InputStream fis ,  String password) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableEntryException;
}
