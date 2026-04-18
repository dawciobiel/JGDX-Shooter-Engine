package pl.shooter.engine.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Singleton-like service providing a shared, pre-configured ObjectMapper.
 * This improves performance and memory usage by avoiding repeated object creation.
 */
public class JsonService {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        // Configure mapper for better stability
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    public static ObjectMapper getMapper() {
        return mapper;
    }
}
