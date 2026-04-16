package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import pl.shooter.engine.assets.AudioService;
import pl.shooter.engine.config.ConfigService;
import pl.shooter.engine.config.GameConfig;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.events.EventBus;
import pl.shooter.engine.events.TauntEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Monitors TauntEvents and triggers random taunt sounds.
 */
public class TauntSystem extends GameSystem {
    private final AudioService audioService;
    private final GameConfig config;
    private final List<String> tauntFiles = new ArrayList<>();
    private final Random random = new Random();

    public TauntSystem(EntityManager entityManager, EventBus eventBus, AudioService audioService) {
        super(entityManager);
        this.audioService = audioService;
        this.config = new ConfigService().getConfig();
        
        eventBus.subscribe(TauntEvent.class, event -> triggerTaunt());
        discoverTaunts();
    }

    private void discoverTaunts() {
        FileHandle dir = Gdx.files.internal(config.audio.tauntsDir);
        if (dir.exists() && dir.isDirectory()) {
            for (FileHandle file : dir.list()) {
                if (file.extension().equals("wav") || file.extension().equals("mp3")) {
                    tauntFiles.add(file.path());
                }
            }
        }
    }

    private void triggerTaunt() {
        if (tauntFiles.isEmpty()) return;
        
        String randomTaunt = tauntFiles.get(random.nextInt(tauntFiles.size()));
        // Passing sfxVolume as the second argument as required by AudioService
        audioService.playSound(randomTaunt, config.audio.sfxVolume);
        Gdx.app.log("TauntSystem", "Taunt triggered! Playing: " + randomTaunt);
    }

    @Override
    public void update(float deltaTime) {}
}
