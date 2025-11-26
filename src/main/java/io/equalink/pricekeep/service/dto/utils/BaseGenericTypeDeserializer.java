package io.equalink.pricekeep.service.dto.utils;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.equalink.pricekeep.service.dto.BaseEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BaseGenericTypeDeserializer<T> extends StdDeserializer<BaseEntity<T>> {


    protected BaseGenericTypeDeserializer(Class<?> vc) {
        super(vc);
    }

    public BaseGenericTypeDeserializer() {
        this(null);
    }

    @Override
    public BaseEntity<T> deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException, IOException {
        JsonNode n = ctxt.readTree(p);
        var fieldNames = n.fieldNames();
        List<String> fieldList = new ArrayList<>();
        while (fieldNames.hasNext()) {
            fieldList.add(fieldNames.next());
        }
        if (fieldList.size() == 1 && fieldList.getFirst().equals("id")) {
            return ctxt.readTreeAsValue(n, BaseEntity.WithId.class);
        } else {
            var innerValue = ctxt.readTreeAsValue(n, this._valueClass);
            return new BaseEntity.WithDetail<T>((T) innerValue);
        }

    }
}
