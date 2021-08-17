package co.aikar.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class MinestomMessageFormatter extends MessageFormatter<TextColor> {

    public MinestomMessageFormatter(TextColor... colors) {
        super(colors);
    }

    @Override
    String format(TextColor color, String message) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(Component.text(message).color(color));
    }

}
