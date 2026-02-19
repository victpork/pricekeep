package io.equalink.pricekeep.service.pricefetch.woolworths;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class WoolworthsMultibuyDeserializer extends JsonDeserializer<WoolworthsProductQuote.Multibuy> {
    @Override
    public WoolworthsProductQuote.Multibuy deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException {

        JsonNode prdTagNode = jp.getCodec().readTree(jp);

        if (prdTagNode != null && prdTagNode.has("multiBuy") && prdTagNode.get("multiBuy").isObject()) {
            JsonNode multibuyNode = prdTagNode.get("multiBuy");
            WoolworthsProductQuote.Multibuy mb = new WoolworthsProductQuote.Multibuy();
            if (multibuyNode.has("quantity")) {
                mb.setQuantity(multibuyNode.get("quantity").decimalValue());
            }
            if (multibuyNode.has("value")) {
                mb.setValue(multibuyNode.get("value").decimalValue());
            }
            return mb;
        }
        return null;
    }
}
