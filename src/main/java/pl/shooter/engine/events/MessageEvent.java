package pl.shooter.engine.events;

/**
 * Triggered when a message should be displayed to the player.
 */
public class MessageEvent implements Event {
    public final String text;
    public final float duration; // How long to show the message in seconds

    public MessageEvent(String text, float duration) {
        this.text = text;
        this.duration = duration;
    }
}
