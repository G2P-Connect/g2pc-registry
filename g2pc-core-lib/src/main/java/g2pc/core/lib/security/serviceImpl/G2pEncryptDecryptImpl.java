package g2pc.core.lib.security.serviceImpl;
import g2pc.core.lib.security.RandomIVGenerator;
import g2pc.core.lib.security.service.G2pEncryptDecrypt;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;


@Service
public class G2pEncryptDecryptImpl implements G2pEncryptDecrypt {

    /**
     * This method is used to encrypt the data in symmetric method
     * @param data string to encrypt
     * @param key public key
     * @return
     * @throws Exception
     */
    @Override
    public  String g2pEncrypt(String data, String key) throws Exception {
        IvParameterSpec ivParameterSpec = RandomIVGenerator.generateIv();

        byte[] keyBytes = key.getBytes("UTF-8");
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

        Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aesCipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);
        byte[] encryptedData = aesCipher.doFinal(data.getBytes("UTF-8"));

        String ivStr = Base64.getEncoder().encodeToString(ivParameterSpec.getIV());
        String encryptedDataStr = Base64.getEncoder().encodeToString(encryptedData);

        return ivStr + ":" + encryptedDataStr;
    }

    /**
     * This method is used to decrypt the encrypted string in symmetric method
     * @param encryptedData string to decrypt
     * @param key public key
     * @return
     * @throws Exception
     */
    @Override
    public  String g2pDecrypt(String encryptedData, String key) throws Exception {
        String[] parts = encryptedData.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid encrypted data format");
        }

        String ivStr = parts[0];
        String encryptedDataStr = parts[1];

        byte[] ivBytes = Base64.getDecoder().decode(ivStr);
        byte[] encryptedDataBytes = Base64.getDecoder().decode(encryptedDataStr);

        byte[] keyBytes = key.getBytes("UTF-8");
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

        Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aesCipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(ivBytes));
        byte[] decryptedDataBytes = aesCipher.doFinal(encryptedDataBytes);

        return new String(decryptedDataBytes, "UTF-8");
    }

    /**
     * Used to apply hashing algorithm to signature
     * @param data data to get hashed
     * @return
     * @throws NoSuchAlgorithmException
     */
    @Override
    public String sha256Hashing(String data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance( "SHA-256" ) ;
        byte[ ] hash = md.digest( data.getBytes( StandardCharsets.UTF_8 ) ) ;
        BigInteger number = new BigInteger( 1, hash ) ;
        StringBuilder hexString = new StringBuilder( number.toString( 16 ) ) ;
        while ( hexString.length( ) < 32 )
        {
            hexString.insert( 0,  " 0 " ) ;
        }
        return hexString.toString( ) ;
    }
}
