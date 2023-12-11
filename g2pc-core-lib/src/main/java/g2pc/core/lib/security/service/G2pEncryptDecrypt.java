package g2pc.core.lib.security.service;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface G2pEncryptDecrypt {

    public  String g2pEncrypt(String data, String key) throws Exception;

    public  String g2pDecrypt(String encryptedData, String key) throws Exception;

    public String sha256Hashing(String data) throws NoSuchAlgorithmException;

    public String hmacHashing(String data , String secret) throws NoSuchAlgorithmException, InvalidKeyException;

}
