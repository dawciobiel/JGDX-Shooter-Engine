package pl.shooter.engine.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

/**
 * Strict Asset Resolver. 
 * Optimized to handle both short and absolute paths without corruption.
 */
public class AssetService {
    private final AssetManager manager;
    private String currentMapFolder = null;

    public AssetService() {
        this.manager = new AssetManager();
    }

    public void setCurrentMapFolder(String folderPath) {
        this.currentMapFolder = folderPath;
    }

    public void loadTexture(String path) {
        if (path == null) return;
        String resolvedPath = resolvePath(path, "graphics/textures");
        if (resolvedPath != null && !manager.isLoaded(resolvedPath)) {
            manager.load(resolvedPath, Texture.class);
        }
    }

    public String resolvePath(String originalPath, String subfolder) {
        if (originalPath == null || originalPath.isEmpty()) return null;
        
        // 1. If it's already a full valid path that EXISTS, return it immediately
        if (originalPath.startsWith("assets/") && Gdx.files.internal(originalPath).exists()) {
            return originalPath;
        }

        // 2. Cleanup: remove legacy/redundant prefixes for search
        String cleanPath = originalPath.replace("assets/shared/", "").replace("assets/", "");

        // 3. Try LOCAL MAP FOLDER (Priority)
        if (currentMapFolder != null) {
            String mapPath = currentMapFolder + "/" + subfolder + "/" + cleanPath;
            if (exists(mapPath)) return mapPath;
            
            String mapDirect = currentMapFolder + "/" + cleanPath;
            if (exists(mapDirect)) return mapDirect;
        }

        // 4. Try CORE FOLDER (Fallback)
        String corePath = "assets/core/" + subfolder + "/" + cleanPath;
        if (exists(corePath)) return corePath;
        
        String coreDirect = "assets/core/" + cleanPath;
        if (exists(coreDirect)) return coreDirect;

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
            Gdx.app.error("AssetService", "Loading failed: " + e.getMessage());
        }
    }

    public Texture getTexture(String path) {
        if (path == null) return null;
        
        // Try direct first (in case it's already a full resolved path)
        if (manager.isLoaded(path)) return manager.get(path, Texture.class);
        
        // Try resolving
        String resolved = resolvePath(path, "graphics/textures");
        if (resolved != null && manager.isLoaded(resolved)) {
            return manager.get(resolved, Texture.class);
        }
        
        // If not loaded yet, queue it for next time
        loadTexture(path);
        return null;
    }

    public void dispose() {
        manager.dispose();
    }
}
