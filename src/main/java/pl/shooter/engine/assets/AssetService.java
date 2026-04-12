package pl.shooter.engine.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

/**
 * Handles loading and retrieving game assets.
 */
public class AssetService {
    private final AssetManager manager;

    public AssetService() {
        this.manager = new AssetManager();
    }

    public void loadTexture(String path) {
        if (!manager.isLoaded(path)) {
            manager.load(path, Texture.class);
        }
    }

    public void finishLoading() {
        manager.finishLoading();
    }

    public Texture getTexture(String path) {
        if (manager.isLoaded(path)) {
            return manager.get(path, Texture.class);
        }
        return null;
    }

    public void dispose() {
        manager.dispose();
    }
}
