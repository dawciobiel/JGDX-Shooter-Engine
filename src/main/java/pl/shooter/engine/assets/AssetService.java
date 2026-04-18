package pl.shooter.engine.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

/**
 * Strict Asset Resolver. 
 * Normalizes all paths by stripping legacy prefixes and prioritizing local map data.
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
        String resolvedPath = resolvePath(path, "graphics/textures");
        if (resolvedPath != null && !manager.isLoaded(resolvedPath)) {
            manager.load(resolvedPath, Texture.class);
        }
    }

    public String resolvePath(String originalPath, String subfolder) {
        if (originalPath == null || originalPath.isEmpty()) return null;
        
        // 1. STRIP EVERYTHING EXCEPT THE REAL FILENAME/SUBPATH
        // Remove "assets/shared/", "assets/", and the subfolder if it's already there
        String cleanPath = originalPath.replace("assets/core/", "").replace("assets/", "");
        if (subfolder != null && !subfolder.isEmpty()) {
            if (cleanPath.startsWith(subfolder + "/")) {
                cleanPath = cleanPath.substring(subfolder.length() + 1);
            }
        }

        // 2. TRY MAP FOLDER (Priority)
        if (currentMapFolder != null) {
            String mapPath = currentMapFolder + "/" + (subfolder.isEmpty() ? "" : subfolder + "/") + cleanPath;
            if (exists(mapPath)) return mapPath;
            
            String mapDirect = currentMapFolder + "/" + cleanPath;
            if (exists(mapDirect)) return mapDirect;
        }

        // 3. TRY CORE FOLDER
        String corePath = "assets/core/" + (subfolder.isEmpty() ? "" : subfolder + "/") + cleanPath;
        if (exists(corePath)) return corePath;
        
        String coreDirect = "assets/core/" + cleanPath;
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
        String resolved = resolvePath(path, "graphics/textures");
        if (manager.isLoaded(resolved)) return manager.get(resolved, Texture.class);
        return null;
    }

    public void dispose() {
        manager.dispose();
    }
}
