package co.aikar.commands;

import net.minestom.server.command.builder.arguments.ArgumentString;

public class OptionalArgumentString extends ArgumentString {
    public OptionalArgumentString(String id) {
        super(id);
    }

    @Override
    public boolean isOptional() {
        return true;
    }
}
