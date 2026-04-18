package pl.shooter.engine.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import pl.shooter.engine.config.GameConfig;

/**
 * Strict Asset Resolver. 
 * Normalizes all paths by stripping legacy prefixes and prioritizing local map data.
 * Uses GameConfig for path resolution.
 */
public class AssetService {
    private final AssetManager manager;
    private final GameConfig config;
    private String currentMapFolder = null;

    public AssetService(GameConfig config) {
        this.manager = new AssetManager();
        this.config = config != null ? config : new GameConfig();
    }

    public void setCurrentMapFolder(String folderPath) {
        this.currentMapFolder = folderPath;
    }

    public void loadTexture(String path) {
        String resolvedPath = resolvePath(path, config.paths.textures);
        if (resolvedPath != null && !manager.isLoaded(resolvedPath)) {
            manager.load(resolvedPath, Texture.class);
        }
    }

    public String resolvePath(String originalPath, String subfolder) {
        if (originalPath == null || originalPath.isEmpty()) return null;
        
        // 1. STRIP EVERYTHING EXCEPT THE REAL FILENAME/SUBPATH
        String coreAssetsPrefix = config.paths.coreAssets + "/";
        String cleanPath = originalPath.replace(coreAssetsPrefix, "").replace("assets/", "");
        
        if (subfolder != null && !subfolder.isEmpty()) {
            if (cleanPath.startsWith(subfolder + "/")) {
                cleanPath = cleanPath.substring(subfolder.length() + 1);
            }
        }

        // 2. TRY MAP FOLDER (Priority)
        if (currentMapFolder != null) {
            String subPath = (subfolder == null || subfolder.isEmpty()) ? "" : subfolder + "/";
            String mapPath = currentMapFolder + "/" + subPath + cleanPath;
            if (exists(mapPath)) return mapPath;
            
            String mapDirect = currentMapFolder + "/" + cleanPath;
            if (exists(mapDirect)) return mapDirect;
        }

        // 3. TRY CORE FOLDER
        String subPath = (subfolder == null || subfolder.isEmpty()) ? "" : subfolder + "/";
        String corePath = config.paths.coreAssets + "/" + subPath + cleanPath;
        if (exists(corePath)) return corePath;
        
        String coreDirect = config.paths.coreAssets + "/" + cleanPath;
        if (exists(coreDirect)) return coreDirect;

        // 4. ABSOLUTE FALLBACK
        if (Gdx.files.internal(originalPath).exists()) return originalPath;

        return originalPath;
    }

    private boolean exists(String path) {
        if (path == null) return false;
        return Gdx.files.internal(path).exists() || Gdx.files.internal(path + "0.png").exists();
    }

    public void finishLoading() {
        try {
            manager.finishLoading();
        } catch (Exception e) {
            Gdx.app.error("ResourceManager", "Loading failed: " + e.getMessage());
        }
    }

    public Texture getTexture(String path) {
        if (path == null) return null;
        if (manager.isLoaded(path)) return manager.get(path, Texture.class);
        String resolved = resolvePath(path, config.paths.textures);
        if (manager.isLoaded(resolved)) return manager.get(resolved, Texture.class);
        return null;
    }

    public void dispose() {
        manager.dispose();
    }
}
