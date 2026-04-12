package pl.shooter.engine.events;

public class ScoreEvent implements Event {
    public final int amount;

    public ScoreEvent(int amount) {
        this.amount = amount;
    }
}
