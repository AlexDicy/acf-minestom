package co.aikar.commands;

import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MinestomCommandCompletions extends CommandCompletions<MinestomCommandCompletionContext> {

    public MinestomCommandCompletions(MinestomCommandManager manager) {
        super(manager);
        registerAsyncCompletion("mobs", c -> {
            final Stream<String> normal = EntityType.values().stream()
                    .map(entityType -> ACFUtil.simplifyString(entityType.name()));
            return normal.collect(Collectors.toList());
        });
        registerAsyncCompletion("chatcolors", c -> {
            Stream<TextColor> colors = Stream.of(ACFMinestomUtil.getAllChatColors());

            String filter = c.getConfig("filter");
            if (filter != null) {
                Set<String> filters = Arrays.stream(ACFPatterns.COLON.split(filter))
                        .map(ACFUtil::simplifyString).collect(Collectors.toSet());

                colors = colors.filter(color -> filters.contains(ACFUtil.simplifyString(color.toString())));
            }

            return colors.map(color -> ACFUtil.simplifyString(color.toString())).collect(Collectors.toList());
        });
        registerCompletion("players", c -> {
            CommandSender sender = c.getSender();
            if (sender == null) {
                throw new RuntimeException("Sender cannot be null");
            }

            ArrayList<String> matchedPlayers = new ArrayList<>();
            for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                String name = player.getUsername();
                if (name.toLowerCase().startsWith(c.getInput().toLowerCase())) {
                    matchedPlayers.add(name);
                }
            }

            matchedPlayers.sort(String.CASE_INSENSITIVE_ORDER);
            matchedPlayers.addFirst("@p");
            return matchedPlayers;
        });
    }
}
