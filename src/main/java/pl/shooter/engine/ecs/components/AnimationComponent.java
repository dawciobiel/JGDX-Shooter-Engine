package pl.shooter.engine.ecs.components;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import pl.shooter.engine.ecs.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores multiple animations for an entity (e.g. "idle", "walk", "shoot").
 */
public class AnimationComponent implements Component {
    public enum State {
        IDLE,
        WALK,
        SHOOT,
        DIE
    }

    public Map<State, Animation<TextureRegion>> animations = new HashMap<>();
    public State currentState = State.IDLE;
    public float stateTime = 0f;
    public boolean looping = true;

    // Dimensions for rendering
    public float width = 32f;
    public float height = 32f;

    public AnimationComponent() {}
    public AnimationComponent(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public void addAnimation(State state, Animation<TextureRegion> animation) {
        animations.put(state, animation);
    }

    public TextureRegion getCurrentKeyFrame() {
        Animation<TextureRegion> anim = animations.get(currentState);
        return (anim != null) ? anim.getKeyFrame(stateTime, looping) : null;
    }
}
