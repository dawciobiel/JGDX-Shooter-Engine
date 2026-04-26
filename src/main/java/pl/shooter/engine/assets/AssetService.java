package pl.shooter.engine.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import pl.shooter.engine.config.models.EngineConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Strict Asset Resolver. 
 * Only checks explicitly defined locations (Map-Local then Global).
 * Provides detailed debug output for missing files.
 */
public class AssetService implements AssetErrorListener {
    private final AssetManager manager;
    private final EngineConfig config;
    private String currentMapFolder = null;
    private final List<String> loadingErrors = new ArrayList<>();

    public AssetService(EngineConfig config) {
        this.manager = new AssetManager();
        this.manager.setErrorListener(this);
        this.config = config != null ? config : new EngineConfig();
    }

    @Override
    public void error(AssetDescriptor asset, Throwable throwable) {
        String msg = "[AssetManager] CRITICAL: File not found in internal storage: " + asset.fileName;
        System.err.println(msg);
        if (!loadingErrors.contains(msg)) loadingErrors.add(msg);
    }

    public List<String> getLoadingErrors() { return loadingErrors; }
    public void setCurrentMapFolder(String folderPath) { this.currentMapFolder = folderPath; }
    public float getProgress() { return manager.getProgress(); }
    public boolean update() { return manager.update(); }

    public void loadTexture(String path) {
        if (path == null || path.isEmpty()) return;
        String resolved = resolvePath(path, "textures");
        if (resolved != null) {
            if (!manager.isLoaded(resolved)) manager.load(resolved, Texture.class);
        } else {
            // Error already logged by resolvePath
        }
    }

    public void loadAtlas(String path) {
        if (path == null || path.isEmpty()) return;
        String resolved = resolvePath(path, "textures");
        if (resolved != null) {
            if (!manager.isLoaded(resolved)) manager.load(resolved, TextureAtlas.class);
        }
    }

    /**
     * Strict path resolution. Checks only MapLocal and Global folders.
     */
    public String resolvePath(String originalPath, String assetTypeSubfolder) {
        if (originalPath == null || originalPath.isEmpty() || originalPath.equals("null")) return null;

        // Clean path (we expect relative paths in JSON, e.g., "characters/soldier/torso.png")
        String cleanPath = originalPath.replace("assets/", "").replace("global/", "").replace("local/", "");

        // Define search order
        String mapPath = currentMapFolder != null ? currentMapFolder + "/local/" + assetTypeSubfolder + "/" + cleanPath : null;
        String globalPath = config.paths.globalRoot + "/" + assetTypeSubfolder + "/" + cleanPath;
        String absolutePath = "assets/" + originalPath;

        // 1. Try Map Local
        if (mapPath != null && Gdx.files.internal(mapPath).exists()) return mapPath;

        // 2. Try Global
        if (Gdx.files.internal(globalPath).exists()) return globalPath;

        // 3. Try fallback (raw path)
        if (Gdx.files.internal(absolutePath).exists()) return absolutePath;
        if (Gdx.files.internal(originalPath).exists()) return originalPath;

        // FAILED: Provide detailed report
        System.err.println("----------------------------------------------------------------");
        System.err.println("[AssetService] ERROR: Could not find " + assetTypeSubfolder + ": " + originalPath);
        System.err.println("[AssetService]   Checked MapLocal: " + (mapPath != null ? mapPath : "N/A"));
        System.err.println("[AssetService]   Checked Global:   " + globalPath);
        System.err.println("[AssetService]   Checked Fallback: " + absolutePath);
        System.err.println("----------------------------------------------------------------");

        String err = "MISSING: " + originalPath;
        if (!loadingErrors.contains(err)) loadingErrors.add(err);
        
        return null;
    }

    public Texture getTexture(String path) {
        if (path == null) return null;
        if (manager.isLoaded(path)) return manager.get(path, Texture.class);
        String resolved = resolvePath(path, "textures");
        if (resolved != null && manager.isLoaded(resolved)) return manager.get(resolved, Texture.class);
        return null;
    }

    public TextureAtlas getAtlas(String path) {
        if (path == null) return null;
        if (manager.isLoaded(path)) return manager.get(path, TextureAtlas.class);
        String resolved = resolvePath(path, "textures");
        if (resolved != null && manager.isLoaded(resolved)) return manager.get(resolved, TextureAtlas.class);
        return null;
    }

    public void dispose() { manager.dispose(); }
}
