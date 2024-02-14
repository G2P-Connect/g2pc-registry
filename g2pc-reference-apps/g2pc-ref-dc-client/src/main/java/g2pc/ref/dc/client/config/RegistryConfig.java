package g2pc.ref.dc.client.config;

import g2pc.core.lib.constants.CoreConstants;
import g2pc.core.lib.constants.SftpConstants;
import g2pc.core.lib.enums.SortOrderEnum;
import g2pc.ref.dc.client.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class RegistryConfig {

    @Value("${registry.api_urls.farmer_search_api}")
    private String farmerSearchURL;

    @Value("${registry.api_urls.mobile_search_api}")
    private String mobileSearchURL;

    @Value("${dashboard.clear_dp1_db_endpoint_url}")
    private String farmerClearDbURL;

    @Value("${dashboard.clear_dp2_db_endpoint_url}")
    private String mobileClearDbURL;

    @Value("${keycloak.from_dp.farmer.clientId}")
    private String farmerClientId;

    @Value("${keycloak.from_dp.farmer.clientSecret}")
    private String farmerClientSecret;

    @Value("${keycloak.from_dp.farmer.url}")
    private String keycloakFarmerTokenUrl;

    @Value("${keycloak.from_dp.mobile.url}")
    private String keycloakMobileTokenUrl;

    @Value("${keycloak.from_dp.mobile.clientId}")
    private String mobileClientId;

    @Value("${keycloak.from_dp.mobile.clientSecret}")
    private String mobileClientSecret;

    @Value("${crypto.to_dp_farmer.support_encryption}")
    private boolean isFarmerEncrypt;

    @Value("${crypto.to_dp_farmer.support_signature}")
    private boolean isFarmerSign;

    @Value("${crypto.to_dp_mobile.support_encryption}")
    private boolean isMobileEncrypt;

    @Value("${crypto.to_dp_mobile.support_signature}")
    private boolean isMobileSign;

    @Value("${crypto.to_dp_mobile.key_path}")
    private String mobileKeyPath;

    @Value("${crypto.to_dp_farmer.key_path}")
    private String farmerKeyPath;

    @Value("${crypto.to_dp_farmer.password}")
    private String farmerKeyPassword;

    @Value("${crypto.to_dp_mobile.password}")
    private String mobileKeyPassword;

    @Value("${sftp.dp1.host}")
    private String sftpDp1Host;

    @Value("${sftp.dp1.port}")
    private int sftpDp1Port;

    @Value("${sftp.dp1.user}")
    private String sftpDp1User;

    @Value("${sftp.dp1.password}")
    private String sftpDp1Password;

    @Value("${sftp.dp1.remote.inbound_directory}")
    private String sftpDp1RemoteInboundDirectory;

    @Value("${sftp.dp2.host}")
    private String sftpDp2Host;

    @Value("${sftp.dp2.port}")
    private int sftpDp2Port;

    @Value("${sftp.dp2.user}")
    private String sftpDp2User;

    @Value("${sftp.dp2.password}")
    private String sftpDp2Password;

    @Value("${sftp.dp2.remote.inbound_directory}")
    private String sftpDp2RemoteInboundDirectory;

    @Value("${crypto.sample.password}")
    private String dummyKeyPassword;

    @Value("${crypto.sample.key.path}")
    private String dummyKeyPath;

    @Value("${registry.api_urls.farmer_status_api}")
    private String farmerStatusUrl;

    @Value("${registry.api_urls.mobile_status_api}")
    private String mobileStatusUrl;



    /**
     * Map to represent which query params are required for which registry
     *
     * @return query params specific to registry
     */
    public Map<String, Object> getQueryParamsConfig() {
        Map<String, Object> queryParamsConfig = new HashMap<>();

        Map<String, String> farmerRegistryMap = new HashMap<>();
        farmerRegistryMap.put("farmer_id", "");
        farmerRegistryMap.put("season", "");
        farmerRegistryMap.put("status", "");


        Map<String, String> mobileRegistryMap = new HashMap<>();
        mobileRegistryMap.put("mobile_number", "");
        mobileRegistryMap.put("season", "");
        mobileRegistryMap.put("status", "");

        queryParamsConfig.put(Constants.FARMER_REGISTRY, farmerRegistryMap);
        queryParamsConfig.put(Constants.MOBILE_REGISTRY, mobileRegistryMap);
        return queryParamsConfig;
    }

    /**
     * Map to represent which common values to be used to generate request for a registry
     *
     * @return Map to represent registry specific config values
     */
    public Map<String, Object> getRegistrySpecificConfig(String isSignEncrypt) {
        Map<String, Object> queryParamsConfig = new HashMap<>();

        Map<String, String> farmerRegistryMap = getFarmerRegistryMap(isSignEncrypt);
        Map<String, String> mobileRegistryMap = getMobileRegistryMap(isSignEncrypt);

        queryParamsConfig.put(Constants.FARMER_REGISTRY, farmerRegistryMap);
        queryParamsConfig.put(Constants.MOBILE_REGISTRY, mobileRegistryMap);
        return queryParamsConfig;
    }

    /**
     * Set farmer registry specific config values
     *
     * @return Map to represent registry specific config values for farmer
     */
    private Map<String, String> getFarmerRegistryMap(String isSignEncrypt) {
        Map<String, String> farmerRegistryMap = new HashMap<>();
        farmerRegistryMap.put(CoreConstants.QUERY_NAME, "paid_farmer");
        farmerRegistryMap.put(CoreConstants.REG_TYPE, "ns:FARMER_REGISTRY");
        farmerRegistryMap.put(CoreConstants.REG_SUB_TYPE, "");
        farmerRegistryMap.put(CoreConstants.QUERY_TYPE, "namedQuery");
        farmerRegistryMap.put(CoreConstants.SORT_ATTRIBUTE, "farmer_id");
        farmerRegistryMap.put(CoreConstants.SORT_ORDER, SortOrderEnum.ASC.toValue());
        farmerRegistryMap.put(CoreConstants.PAGE_NUMBER, "1");
        farmerRegistryMap.put(CoreConstants.PAGE_SIZE, "10");
        farmerRegistryMap.put(CoreConstants.KEYCLOAK_URL, keycloakFarmerTokenUrl);
        farmerRegistryMap.put(CoreConstants.KEYCLOAK_CLIENT_ID, farmerClientId);
        farmerRegistryMap.put(CoreConstants.KEYCLOAK_CLIENT_SECRET, farmerClientSecret);
        farmerRegistryMap.put(CoreConstants.SUPPORT_ENCRYPTION, "" + isFarmerEncrypt);
        farmerRegistryMap.put(CoreConstants.SUPPORT_SIGNATURE, "" + isFarmerSign);
        if(isSignEncrypt.equals("2")){
            farmerRegistryMap.put(CoreConstants.KEY_PATH, dummyKeyPath);
            farmerRegistryMap.put(CoreConstants.KEY_PASSWORD, dummyKeyPassword);
        } else {
            farmerRegistryMap.put(CoreConstants.KEY_PATH, farmerKeyPath);
            farmerRegistryMap.put(CoreConstants.KEY_PASSWORD, farmerKeyPassword);
        }

        farmerRegistryMap.put(CoreConstants.DP_SEARCH_URL, farmerSearchURL);
        farmerRegistryMap.put(CoreConstants.DP_CLEAR_DB_URL, farmerClearDbURL);
        farmerRegistryMap.put(SftpConstants.SFTP_HOST, sftpDp1Host);
        farmerRegistryMap.put(SftpConstants.SFTP_PORT, String.valueOf(sftpDp1Port));
        farmerRegistryMap.put(SftpConstants.SFTP_USER, sftpDp1User);
        farmerRegistryMap.put(SftpConstants.SFTP_PASSWORD, sftpDp1Password);
        farmerRegistryMap.put(SftpConstants.SFTP_SESSION_CONFIG, "no");
        farmerRegistryMap.put(SftpConstants.SFTP_ALLOW_UNKNOWN_KEYS, String.valueOf(true));
        farmerRegistryMap.put(SftpConstants.SFTP_REMOTE_INBOUND_DIRECTORY, sftpDp1RemoteInboundDirectory);
        farmerRegistryMap.put(CoreConstants.DP_STATUS_URL , farmerStatusUrl);

        return farmerRegistryMap;
    }

    /**
     * Set mobile registry specific config values
     *
     * @return Map to represent registry specific config values for mobile
     */
    private Map<String, String> getMobileRegistryMap(String isSignEncrypt) {
        Map<String, String> mobileRegistryMap = new HashMap<>();
        mobileRegistryMap.put(CoreConstants.QUERY_NAME, "mobile_registered");
        mobileRegistryMap.put(CoreConstants.REG_TYPE, "ns:MOBILE_REGISTRY");
        mobileRegistryMap.put(CoreConstants.REG_SUB_TYPE, "");
        mobileRegistryMap.put(CoreConstants.QUERY_TYPE, "namedQuery");
        mobileRegistryMap.put(CoreConstants.SORT_ATTRIBUTE, "mobile_number");
        mobileRegistryMap.put(CoreConstants.SORT_ORDER, SortOrderEnum.ASC.toValue());
        mobileRegistryMap.put(CoreConstants.PAGE_NUMBER, "1");
        mobileRegistryMap.put(CoreConstants.PAGE_SIZE, "10");
        mobileRegistryMap.put(CoreConstants.KEYCLOAK_URL, keycloakMobileTokenUrl);
        mobileRegistryMap.put(CoreConstants.KEYCLOAK_CLIENT_ID, mobileClientId);
        mobileRegistryMap.put(CoreConstants.KEYCLOAK_CLIENT_SECRET, mobileClientSecret);
        mobileRegistryMap.put(CoreConstants.SUPPORT_ENCRYPTION, "" + isMobileEncrypt);
        mobileRegistryMap.put(CoreConstants.SUPPORT_SIGNATURE, "" + isMobileSign);
        if(isSignEncrypt.equals("2")){
            mobileRegistryMap.put(CoreConstants.KEY_PATH, dummyKeyPath);
            mobileRegistryMap.put(CoreConstants.KEY_PASSWORD, dummyKeyPassword);
        }
        else{
            mobileRegistryMap.put(CoreConstants.KEY_PATH, mobileKeyPath);
            mobileRegistryMap.put(CoreConstants.KEY_PASSWORD, mobileKeyPassword);
        }
        mobileRegistryMap.put(CoreConstants.DP_SEARCH_URL, mobileSearchURL);
        mobileRegistryMap.put(CoreConstants.DP_CLEAR_DB_URL, mobileClearDbURL);
        mobileRegistryMap.put(SftpConstants.SFTP_HOST, sftpDp2Host);
        mobileRegistryMap.put(SftpConstants.SFTP_PORT, String.valueOf(sftpDp2Port));
        mobileRegistryMap.put(SftpConstants.SFTP_USER, sftpDp2User);
        mobileRegistryMap.put(SftpConstants.SFTP_PASSWORD, sftpDp2Password);
        mobileRegistryMap.put(SftpConstants.SFTP_SESSION_CONFIG, "no");
        mobileRegistryMap.put(SftpConstants.SFTP_ALLOW_UNKNOWN_KEYS, String.valueOf(true));
        mobileRegistryMap.put(SftpConstants.SFTP_REMOTE_INBOUND_DIRECTORY, sftpDp2RemoteInboundDirectory);
        mobileRegistryMap.put(CoreConstants.DP_STATUS_URL,mobileStatusUrl);
        return mobileRegistryMap;
    }
}
