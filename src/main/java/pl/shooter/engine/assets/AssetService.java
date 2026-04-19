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

    public GameConfig getConfig() {
        return config;
    }

    public void setCurrentMapFolder(String folderPath) {
        this.currentMapFolder = folderPath;
    }

    public float getProgress() {
        return manager.getProgress();
    }

    public boolean update() {
        return manager.update();
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
        String basePrefix = config.paths.baseAssetsPrefix + "/";
        String cleanPath = originalPath.replace(coreAssetsPrefix, "").replace(basePrefix, "");
        
        if (subfolder != null && !subfolder.isEmpty()) {
            if (cleanPath.startsWith(subfolder + "/")) {
                cleanPath = cleanPath.substring(subfolder.length() + 1);
            }
        }

        // 2. TRY MAP FOLDER (Priority)
        if (currentMapFolder != null) {
            String subPath = (subfolder == null || subfolder.isEmpty()) ? "" : subfolder + "/";
            String mapPath = currentMapFolder + "/" + subPath + cleanPath;
            if (Gdx.files.internal(mapPath).exists()) return mapPath;
            
            String mapDirect = currentMapFolder + "/" + cleanPath;
            if (Gdx.files.internal(mapDirect).exists()) return mapDirect;
        }

        // 3. TRY CORE FOLDER
        String subPath = (subfolder == null || subfolder.isEmpty()) ? "" : subfolder + "/";
        String corePath = config.paths.coreAssets + "/" + subPath + cleanPath;
        if (Gdx.files.internal(corePath).exists()) return corePath;
        
        String coreDirect = config.paths.coreAssets + "/" + cleanPath;
        if (Gdx.files.internal(coreDirect).exists()) return coreDirect;

        // 4. ABSOLUTE FALLBACK
        if (Gdx.files.internal(originalPath).exists()) return originalPath;

        return originalPath;
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
