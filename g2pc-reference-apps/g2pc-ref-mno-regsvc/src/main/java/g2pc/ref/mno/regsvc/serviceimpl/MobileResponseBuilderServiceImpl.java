package g2pc.ref.mno.regsvc.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import g2pc.core.lib.dto.common.header.RequestHeaderDTO;
import g2pc.core.lib.dto.common.header.ResponseHeaderDTO;
import g2pc.core.lib.dto.common.message.request.MessageDTO;
import g2pc.core.lib.dto.common.message.response.DataDTO;
import g2pc.ref.mno.regsvc.dto.request.QueryMobileDTO;
import g2pc.ref.mno.regsvc.dto.request.QueryParamsMobileDTO;
import g2pc.ref.mno.regsvc.dto.response.RegRecordMobileDTO;
import g2pc.ref.mno.regsvc.service.MobileResponseBuilderService;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class MobileResponseBuilderServiceImpl implements MobileResponseBuilderService {

    @Value("${client.api_urls.mobile_info_api}")
    String mobileInfoURL;

    /**
     * Get farmer records information string from API
     *
     * @param messageString required
     * @return String of mobile records
     */
    @Override
    public String getRegMobileRecords(String messageString) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        //remove only use for testing
        objectMapper.registerSubtypes(RequestHeaderDTO.class,
                ResponseHeaderDTO.class,
                QueryMobileDTO.class,
                QueryParamsMobileDTO.class);
        MessageDTO messageDTO = objectMapper.readerFor(MessageDTO.class).readValue(messageString);
        String queryParams = objectMapper.writeValueAsString(messageDTO.getSearchRequest().getSearchCriteria().getQuery().getQueryParams());
        QueryParamsMobileDTO queryParamsMobileDTO = objectMapper.readValue(queryParams, QueryParamsMobileDTO.class);

        List<String> mobileNumbers = queryParamsMobileDTO.getMobileNumber();
        String season = queryParamsMobileDTO.getSeason();

        String uri = mobileInfoURL;
        HttpResponse<String> response = Unirest.post(uri)
                .body(objectMapper.writeValueAsString(queryParamsMobileDTO))
                .asString();

        List<RegRecordMobileDTO> regRecordMobileDTOList = objectMapper.readerFor(List.class).
                readValue(response.getBody());

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(regRecordMobileDTOList);
    }

    @Override
    public DataDTO buildData(String regRecordsString) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<RegRecordMobileDTO> regRecordFarmerDTOList = objectMapper.readerFor(List.class).
                readValue(regRecordsString);

        DataDTO dataDTO = new DataDTO();
        dataDTO.setVersion("1.0.0");
        dataDTO.setRegType("ns:MOBILE_REGISTRY");
        dataDTO.setRegSubType("");
        dataDTO.setRegRecordType("ns:MOBILE_REGISTRY:MOBILE");
        dataDTO.setRegRecords(regRecordFarmerDTOList);
        return dataDTO;
    }
}
