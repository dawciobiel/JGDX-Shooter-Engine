package pl.shooter.engine.config.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Universal wrapper for data versioning.
 * Used for all JSON files in the engine to ensure future compatibility.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataWrapper<T> {
    public int version = 1;
    public T data;

    public DataWrapper() {}

    public DataWrapper(int version, T data) {
        this.version = version;
        this.data = data;
    }
}
