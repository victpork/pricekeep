package io.equalink.pricekeep.service.pricefetch.woolworths;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

public class WoolworthsAreaDeserializer extends JsonDeserializer<WoolworthsStoreArea> {
    @Override
    public WoolworthsStoreArea deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectMapper om = (ObjectMapper) p.getCodec();
        JsonNode node = om.readTree(p);
        if (node.isArray() && !node.isEmpty()) {
            for (JsonNode area : node) {
                if (area.get("name").asText().equals("All Pick up locations")) {
                    List<WoolworthsStoreAddress> storeList = om.convertValue(area.get("storeAddresses"), new TypeReference<>() {
                    });
                    storeList.forEach(s -> {
                        s.setName(s.getName().trim());
                        s.setAddress(s.getAddress().trim());
                    });
                    storeList = storeList.stream().filter(s -> s.getName().startsWith("Woolworths")).toList();
                    return WoolworthsStoreArea.builder()
                               .id(area.get("id").asLong())
                               .storeAddresses(storeList)
                               .build();
                }
            }
        }
        return null;
    }

}
