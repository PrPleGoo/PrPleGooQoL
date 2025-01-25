package prplegoo.regions.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ObjectMapperFactory {
    public static ObjectMapper build() {
        return new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
    }
}
