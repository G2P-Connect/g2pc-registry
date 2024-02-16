package g2pc.ref.mno.regsvc.service;


import g2pc.core.lib.dto.search.message.request.QueryDTO;
import java.io.IOException;
import java.util.List;

public interface MobileResponseBuilderService {

    List<String> getRegMobileRecords(List<QueryDTO> queryDTOList) throws IOException;
}
