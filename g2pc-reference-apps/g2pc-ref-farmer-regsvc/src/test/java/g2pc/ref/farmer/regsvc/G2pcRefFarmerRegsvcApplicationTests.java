package g2pc.ref.farmer.regsvc;

import g2pc.core.lib.security.service.G2pTokenService;
import g2pc.core.lib.service.ElasticsearchService;
import g2pc.ref.farmer.regsvc.scheduler.Scheduler;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;

@SpringBootTest
@Slf4j
class G2pcRefFarmerRegsvcApplicationTests {

    @Autowired
    private Scheduler scheduler;

    @Autowired
    G2pTokenService g2pTokenService;

    @Test
    void contextLoads() {
    }

    @Test
    void testResponseScheduler() throws Exception {
        scheduler.responseScheduler();
    }

    @Test
    void testObjectMapper(){
        String jsonString = "{\"version\":\"1.0.0\",\"message_id\":\"M446-2727-0859-1434-3027\",\"message_ts\":\"2024-01-29T12:17:43+05:30\",\"action\":\"search\",\"status\":\"\",\"status_reason_code\":\"\",\"status_reason_message\":\"\",\"total_count\":0,\"completed_count\":0,\"sender_id\":\"spp.example.org\",\"receiver_id\":\"pymts.example.org\",\"is_msg_encrypted\":false,\"meta\":\"\",\"transaction_id\":\"T272-0195-3439-6563-8238\",\"correlation_id\":\"\",\"registry_type\":\"ns:FARMER_REGISTRY\",\"protocol\":\"https\",\"payload_filename\":\"file\",\"inbound_filename\":\"\",\"outbound_filename\":\"\",\"created_date\":\"2024-01-29T12:17:44+05:30\",\"last_updated_date\":\"2024-01-29T12:17:44+05:30\",\"osOwner\":[\"anonymous\"],\"osid\":\"1-a4640192-341e-4446-aa37-51e613a58e9e\"}";

        // Create an ObjectMapper instance
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Convert JSON string to HashMap<String, String>
            Map<String, Object> resultMap = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});

            String osid = (String) resultMap.get("osid");
            // Convert Object values to String
            Map<String, String> stringMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                stringMap.put(entry.getKey(), entry.getValue().toString());
            }

            // Print the result
            System.out.println(stringMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
