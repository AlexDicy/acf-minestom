package co.aikar.commands;

import net.kyori.adventure.text.format.TextColor;

public class MinestomMessageFormatter extends MessageFormatter<TextColor> {

    public MinestomMessageFormatter(TextColor... colors) {
        super(colors);
    }

    @Override
    String format(TextColor color, String message) {
        return color + message;
    }

}
