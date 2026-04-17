package pl.shooter.engine.ecs.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import pl.shooter.engine.ecs.Entity;
import pl.shooter.engine.ecs.EntityManager;
import pl.shooter.engine.ecs.GameSystem;
import pl.shooter.engine.ecs.components.*;
import pl.shooter.engine.events.EventBus;
import pl.shooter.engine.events.InteractionEvent;

import java.util.List;

/**
 * Monitors player proximity to interactable objects and handles the interaction key/button.
 */
public class InteractionSystem extends GameSystem {
    private final EventBus eventBus;

    public InteractionSystem(EntityManager entityManager, EventBus eventBus) {
        super(entityManager);
        this.eventBus = eventBus;
    }

    @Override
    public void update(float deltaTime) {
        // Interaction triggered by SPACE, Left Control, or Right Mouse Button
        boolean interactPressed = Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || 
                                 Gdx.input.isKeyJustPressed(Input.Keys.CONTROL_LEFT) ||
                                 Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT);
        
        if (!interactPressed) return;

        List<Entity> players = entityManager.getEntitiesWithComponents(PlayerComponent.class, TransformComponent.class);
        List<Entity> interactables = entityManager.getEntitiesWithComponents(InteractableComponent.class, TransformComponent.class);

        if (players.isEmpty() || interactables.isEmpty()) return;

        for (Entity player : players) {
            TransformComponent pt = entityManager.getComponent(player, TransformComponent.class);

            for (Entity interactable : interactables) {
                InteractableComponent ic = entityManager.getComponent(interactable, InteractableComponent.class);
                if (ic.onceOnly && ic.activated) continue;

                TransformComponent it = entityManager.getComponent(interactable, TransformComponent.class);

                float dx = pt.x - it.x;
                float dy = pt.y - it.y;
                float distSq = dx * dx + dy * dy;

                if (distSq < ic.interactionRadius * ic.interactionRadius) {
                    handleInteraction(player, interactable, ic);
                    eventBus.publish(new InteractionEvent(player, interactable, ic));
                    return;
                }
            }
        }
    }

    private void handleInteraction(Entity player, Entity interactable, InteractableComponent ic) {
        Gdx.app.log("InteractionSystem", "Interaction triggered: " + ic.type + " (targetId: " + ic.targetId + ")");
        
        if (ic.targetId != null) {
            List<Entity> doorEntities = entityManager.getEntitiesWithComponents(DoorComponent.class);
            for (Entity doorEntity : doorEntities) {
                DoorComponent door = entityManager.getComponent(doorEntity, DoorComponent.class);
                if (ic.targetId.equals(door.doorId)) {
                    toggleDoor(doorEntity, door);
                }
            }
        }
        
        if (ic.onceOnly) {
            ic.activated = true;
        }
    }

    private void toggleDoor(Entity doorEntity, DoorComponent door) {
        door.isOpen = !door.isOpen;
        
        if (door.isOpen) {
            Gdx.app.log("InteractionSystem", "Door " + door.doorId + " opened!");
            entityManager.removeComponent(doorEntity, ColliderComponent.class);
            entityManager.removeComponent(doorEntity, ObstacleComponent.class);
            
            TextureComponent tc = entityManager.getComponent(doorEntity, TextureComponent.class);
            if (tc != null && door.openTexture != null) {
                tc.assetPath = door.openTexture;
            }
        } else {
            Gdx.app.log("InteractionSystem", "Door " + door.doorId + " closed!");
            // Re-adding components. Using a standard radius for now.
            entityManager.addComponent(doorEntity, new ColliderComponent(24f));
            entityManager.addComponent(doorEntity, new ObstacleComponent());
            
            TextureComponent tc = entityManager.getComponent(doorEntity, TextureComponent.class);
            if (tc != null && door.closedTexture != null) {
                tc.assetPath = door.closedTexture;
            }
        }
    }
}
