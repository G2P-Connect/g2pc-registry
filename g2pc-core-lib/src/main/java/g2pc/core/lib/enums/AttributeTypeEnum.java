package g2pc.core.lib.enums;

import g2pc.core.lib.constants.CoreConstants;

import java.io.IOException;

public enum AttributeTypeEnum {

    /**
     * "transaction_id" "reference_id_list" "correlation_id" "subscription_code_list"
     */


    TRANSACTION_ID ,

    REFERENCE_ID_LIST,

    CORRELATION_ID ,

    SUBSCRIPTION_CODE_LIST;


    public String toValue() {
        switch (this) {
            case TRANSACTION_ID:
                return "transaction_id";
            case REFERENCE_ID_LIST:
                return "reference_id_list";
            case CORRELATION_ID:
                return "correlation_id";
            case SUBSCRIPTION_CODE_LIST:
                return "subscription_code_list";
        }
        return null;
    }

    public static AttributeTypeEnum forValue(String value) throws IOException {
        if (null != value) {
            switch (value.toLowerCase()) {
                case "transaction_id":
                    return TRANSACTION_ID;
                case "reference_id_list":
                    return REFERENCE_ID_LIST;
                case "correlation_id":
                    return CORRELATION_ID;
                    case "subscription_code_list":
                    return SUBSCRIPTION_CODE_LIST;
            }
        }
        throw new IOException(CoreConstants.CANNOT_DESERIALIZE_TYPE);
    }
}
