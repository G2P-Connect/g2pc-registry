package g2pc.core.lib.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * The type Common utils.
 */
@Service
@Slf4j
public class CommonUtils {

    /**
     * Gets current time stamp.
     *
     * @return the current time stamp
     */
    public static String getCurrentTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        Date date = new Date();
        return dateFormat.format(date);
    }


    /**
     * Get request header string input stream.
     *
     * @return the input stream
     */
    public InputStream getRequestHeaderString() {
        return CommonUtils.class.getClassLoader()
                .getResourceAsStream("schema/HeaderSchema.json");
    }

    /**
     * Get request message string input stream.
     *
     * @return the input stream
     */
    public InputStream getRequestMessageString() {
        return CommonUtils.class.getClassLoader()
                .getResourceAsStream("schema/MessageSchema.json");
    }

    /**
     * Get response header string input stream.
     *
     * @return the input stream
     */
    public InputStream getResponseHeaderString() {
        return CommonUtils.class.getClassLoader()
                .getResourceAsStream("schema/ResponseHeaderSchema.json");
    }

    /**
     * Get response message string input stream.
     *
     * @return the input stream
     */
    public InputStream getResponseMessageString() {
        return CommonUtils.class.getClassLoader()
                .getResourceAsStream("schema/ResponseMessageSchema.json");
    }

    /**
     *  Get status request message string input stream.
     * @return
     */
    public InputStream getStatusRequestMessageString(){
        return CommonUtils.class.getClassLoader()
                .getResourceAsStream("schema/StatusRequestMessageSchema.json");
    }

    /**
     *  Get status response message string input stream.
     * @return
     */
    public InputStream getStatusResponseMessageString(){
        return CommonUtils.class.getClassLoader()
                .getResourceAsStream("schema/StatusResponseMessageSchema.json");
    }

    /**
     * Generate unique ID
     *
     * @param idType whether transactionId, correlationId or referenceId
     * @return unique ID
     */
    public static String generateUniqueId(String idType) {
        String uniqueNumString = Long.toString(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
        return idType + uniqueNumString.substring(0, 3) +
                "-" + uniqueNumString.substring(3, 7) +
                "-" + uniqueNumString.substring(7, 11) +
                "-" + uniqueNumString.substring(11, 15) +
                "-" + uniqueNumString.substring(15);
    }

    /**
     * Format a string
     *
     * @param data required
     * @return formatted string
     */
    public static String formatString(String data) {
        return data.replace("\\", "").
                replace("\"{", "{").
                replace("}\"", "}");
    }
}
