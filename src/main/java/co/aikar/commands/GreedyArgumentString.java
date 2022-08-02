package co.aikar.commands;

import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Argument which will take a string.
 * <p>
 * Example: Hey I am a string
 */
public class GreedyArgumentString extends Argument<String> {

    public GreedyArgumentString(String id) {
        super(id, true, true);
    }

    @NotNull
    @Override
    public String parse(@NotNull String input) throws ArgumentSyntaxException {
        return input;
    }

    @Override
    public String parser() {
        return "brigadier:string";
    }

    @Override
    public byte @Nullable [] nodeProperties() {
        return BinaryWriter.makeArray(packetWriter -> {
            packetWriter.writeVarInt(2); // Greedy phrase
        });
    }

    @Override
    public String toString() {
        return String.format("GreedyString<%s>", getId());
    }
}
