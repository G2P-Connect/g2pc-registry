package g2pc.ref.mno.regsvc.serviceimpl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.config.G2pUnirestHelper;
import g2pc.core.lib.dto.common.message.request.QueryDTO;
import g2pc.ref.mno.regsvc.dto.response.RegRecordMobileDTO;
import g2pc.ref.mno.regsvc.service.MobileResponseBuilderService;
import kong.unirest.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MobileResponseBuilderServiceImpl implements MobileResponseBuilderService {

    @Value("${client.api_urls.mno_info_url}")
    String mobileInfoURL;

    @Autowired
    G2pUnirestHelper g2pUnirestHelper;

    /**
     * Get farmer records information string from API
     *
     * @param queryDTOList required
     * @return list of mobile records
     */
    @Override
    public List<String> getRegMobileRecords(List<QueryDTO> queryDTOList) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> regMnoRecordsList = new ArrayList<>();
        String uri = mobileInfoURL;
        HttpResponse<String> response = g2pUnirestHelper.g2pPost(uri)
                .body(objectMapper.writeValueAsString(queryDTOList))
                .asString();
        List<RegRecordMobileDTO> regRecordMobileDTOList = objectMapper.readValue(response.getBody(),
                new TypeReference<>() {});
        for (RegRecordMobileDTO regRecordMobileDTO : regRecordMobileDTOList) {
            regMnoRecordsList.add(objectMapper.writeValueAsString(regRecordMobileDTO));
        }
        return regMnoRecordsList;
    }
}
