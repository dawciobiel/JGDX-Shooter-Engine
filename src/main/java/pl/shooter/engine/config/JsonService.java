package pl.shooter.engine.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;

/**
 * Provides shared, pre-configured instances of JSON and TOML mappers.
 * Standardizes serialization across the engine.
 */
public class JsonService {
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final ObjectMapper tomlMapper = new TomlMapper();

    static {
        // Configure JSON
        jsonMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        
        // Configure TOML (if specific options needed)
    }

    public static ObjectMapper getMapper() {
        return jsonMapper;
    }

    public static ObjectMapper getTomlMapper() {
        return tomlMapper;
    }
}
