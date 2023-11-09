package g2pc.core.lib.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.text.SimpleDateFormat;
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
    public String getCurrentTimeStamp()
    {
        return new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(new java.util.Date());
    }

    /**
     * Gets uuid.
     *
     * @return the uuid
     */
    public String getUUID()
    {
        return UUID.randomUUID().toString();
    }


    /**
     * Get request header string input stream.
     *
     * @return the input stream
     */
    public InputStream getRequestHeaderString(){
        InputStream schemaStream = CommonUtils.class.getClassLoader()
                .getResourceAsStream("schema/headerschema.json");
        return schemaStream;
    }

    /**
     * Get request message string input stream.
     *
     * @return the input stream
     */
    public InputStream getRequestMessageString(){
        InputStream schemaStream = CommonUtils.class.getClassLoader()
                .getResourceAsStream("schema/messageschema.json");
        return schemaStream;
    }

    /**
     * Get response header string input stream.
     *
     * @return the input stream
     */
    public InputStream getResponseHeaderString(){
        InputStream schemaStream = CommonUtils.class.getClassLoader()
                .getResourceAsStream("schema/ResponseHeaderschema.json");
        return schemaStream;
    }

    /**
     * Get response message string input stream.
     *
     * @return the input stream
     */
    public InputStream getResponseMessageString(){
        InputStream schemaStream = CommonUtils.class.getClassLoader()
                .getResourceAsStream("schema/ResponseMessageschema.json");
        return schemaStream;
    }




}
