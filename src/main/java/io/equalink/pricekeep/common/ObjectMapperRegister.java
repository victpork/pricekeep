package io.equalink.pricekeep.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;

@Singleton
public class ObjectMapperRegister implements ObjectMapperCustomizer {

    @Override
    public void customize(ObjectMapper objectMapper) {
        SimpleModule animateDeserializeModule = new SimpleModule();

        objectMapper.registerModule(animateDeserializeModule);
    }

}
