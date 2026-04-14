package pl.shooter.engine.ecs.components;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.steer.behaviors.PrioritySteering;
import com.badlogic.gdx.ai.steer.behaviors.Seek;
import com.badlogic.gdx.ai.steer.behaviors.Separation;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import pl.shooter.engine.ecs.Component;

public class SteeringComponent implements Component, Steerable<Vector2> {
    public TransformComponent transform;
    public VelocityComponent velocity;
    
    // Internal state buffers
    private final Vector2 position = new Vector2();
    private final Vector2 linearVelocity = new Vector2();
    
    // Persistent behaviors
    public PrioritySteering<Vector2> prioritySteering;
    public Arrive<Vector2> arriveBehavior;
    public Separation<Vector2> separationBehavior;
    public Seek<Vector2> seekBehavior;

    public SteeringBehavior<Vector2> behavior;
    private final SteeringAcceleration<Vector2> steeringOutput = new SteeringAcceleration<>(new Vector2());

    public float maxLinearSpeed = 70f;
    public float maxLinearAcceleration = 1000f;
    public float maxAngularSpeed = 300f;
    public float maxAngularAcceleration = 500f;
    
    private boolean tagged = false;
    private float zeroThreshold = 0.001f;

    public SteeringComponent(TransformComponent transform, VelocityComponent velocity) {
        this.transform = transform;
        this.velocity = velocity;
    }

    public SteeringAcceleration<Vector2> getSteeringOutput() { return steeringOutput; }

    @Override public Vector2 getPosition() {
        return position.set(transform.x, transform.y);
    }

    @Override public float getOrientation() {
        return transform.rotation * MathUtils.degreesToRadians;
    }

    @Override public void setOrientation(float orientation) {
        transform.rotation = orientation * MathUtils.radiansToDegrees;
    }

    @Override public float vectorToAngle(Vector2 vector) {
        return MathUtils.atan2(vector.y, vector.x);
    }

    @Override public Vector2 angleToVector(Vector2 outVector, float angle) {
        outVector.x = MathUtils.cos(angle);
        outVector.y = MathUtils.sin(angle);
        return outVector;
    }

    @Override public Location<Vector2> newLocation() {
        return null;
    }

    @Override public float getZeroLinearSpeedThreshold() { return zeroThreshold; }
    @Override public void setZeroLinearSpeedThreshold(float value) { this.zeroThreshold = value; }
    @Override public float getMaxLinearSpeed() { return maxLinearSpeed; }
    @Override public void setMaxLinearSpeed(float maxLinearSpeed) { this.maxLinearSpeed = maxLinearSpeed; }
    @Override public float getMaxLinearAcceleration() { return maxLinearAcceleration; }
    @Override public void setMaxLinearAcceleration(float maxLinearAcceleration) { this.maxLinearAcceleration = maxLinearAcceleration; }
    @Override public float getMaxAngularSpeed() { return maxAngularSpeed; }
    @Override public void setMaxAngularSpeed(float maxAngularSpeed) { this.maxAngularSpeed = maxAngularSpeed; }
    @Override public float getMaxAngularAcceleration() { return maxAngularAcceleration; }
    @Override public void setMaxAngularAcceleration(float maxAngularAcceleration) { this.maxAngularAcceleration = maxAngularAcceleration; }
    
    @Override public Vector2 getLinearVelocity() {
        return linearVelocity.set(velocity.vx, velocity.vy);
    }

    @Override public float getAngularVelocity() { return 0; }
    @Override public float getBoundingRadius() { return 15f; }
    @Override public boolean isTagged() { return tagged; }
    @Override public void setTagged(boolean tagged) { this.tagged = tagged; }
}
