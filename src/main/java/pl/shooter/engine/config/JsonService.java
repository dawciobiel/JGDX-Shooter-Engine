package pl.shooter.engine.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Provides a shared, pre-configured ObjectMapper instance for the entire engine.
 */
public class JsonService {
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        // Configure standard options
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    }

    public static ObjectMapper getMapper() {
        return mapper;
    }
}
