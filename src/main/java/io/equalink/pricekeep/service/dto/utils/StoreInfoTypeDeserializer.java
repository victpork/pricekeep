package io.equalink.pricekeep.service.dto.utils;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.equalink.pricekeep.service.dto.BaseEntity;
import io.equalink.pricekeep.service.dto.StoreInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StoreInfoTypeDeserializer extends StdDeserializer<BaseEntity<StoreInfo>> {

    protected StoreInfoTypeDeserializer(Class<?> vc) {
        super(vc);
    }

    public StoreInfoTypeDeserializer() {
        this(null);
    }

    @Override
    public BaseEntity<StoreInfo> deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException, IOException {
        JsonNode n = ctxt.readTree(p);
        var fieldNames = n.fieldNames();
        List<String> fieldList = new ArrayList<>();
        while (fieldNames.hasNext()) {
            fieldList.add(fieldNames.next());
        }
        if (fieldList.size() == 1 && fieldList.getFirst().equals("id")) {
            return ctxt.readTreeAsValue(n, BaseEntity.WithId.class);
        } else {
            var innerValue = ctxt.readTreeAsValue(n, StoreInfo.class);
            return new BaseEntity.WithDetail<>(innerValue);
        }

    }
}
