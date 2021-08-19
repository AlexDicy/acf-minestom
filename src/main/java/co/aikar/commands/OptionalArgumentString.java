package co.aikar.commands;

/**
 * Argument which will take a string and is optional
 * <p>
 * Example: Hey I am a string
 */
public class OptionalArgumentString extends GreedyArgumentString {
    public OptionalArgumentString(String id) {
        super(id);
    }

    @Override
    public boolean isOptional() {
        return true;
    }

    @Override
    public String toString() {
        return String.format("OptionalString<%s>", getId());
    }
}
