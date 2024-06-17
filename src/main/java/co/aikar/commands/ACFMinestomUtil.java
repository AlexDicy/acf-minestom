package co.aikar.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ACFMinestomUtil {

    public static Component color(String message) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }

    public static Player findPlayerSmart(CommandIssuer issuer, String search) {
        CommandSender requester = issuer.getIssuer();
        if (search == null) {
            return null;
        }
        String name = ACFUtil.replace(search, ":confirm", "");

        if (!isValidName(name)) {
            issuer.sendError(MinecraftMessageKeys.IS_NOT_A_VALID_NAME, "{name}", name);
            return null;
        }

        List<Player> matches = matchPlayer(name);

        if (matches.size() > 1) {
            String allMatches = matches.stream().map(Player::getUsername).collect(Collectors.joining(", "));
            issuer.sendError(MinecraftMessageKeys.MULTIPLE_PLAYERS_MATCH,
                    "{search}", name, "{all}", allMatches);
            return null;
        }

        //noinspection Duplicates
        if (matches.isEmpty()) {
            issuer.sendError(MinecraftMessageKeys.NO_PLAYER_FOUND_SERVER, "{search}", name);
            return null;
        }

        return matches.get(0);
    }

    private static List<Player> matchPlayer(String query) {
        List<Player> players = new ArrayList<>();
        for (Player player : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (player.getUsername().contains(query)) {
                players.add(player);
            }
        }
        return players;
    }

    public static boolean isValidName(String name) {
        return name != null && !name.isEmpty() && ACFPatterns.VALID_NAME_PATTERN.matcher(name).matches();
    }

    static boolean isValidItem(ItemStack item) {
        return item != null && item.material() != Material.AIR && item.amount() > 0;
    }

    static NamedTextColor[] getAllChatColors() {
        return new NamedTextColor[] {NamedTextColor.BLACK, NamedTextColor.GREEN, NamedTextColor.BLUE, NamedTextColor.AQUA
                , NamedTextColor.DARK_BLUE, NamedTextColor.DARK_AQUA, NamedTextColor.DARK_GRAY, NamedTextColor.DARK_GREEN, NamedTextColor.DARK_RED
                , NamedTextColor.GOLD, NamedTextColor.GRAY, NamedTextColor.LIGHT_PURPLE
                , NamedTextColor.DARK_PURPLE, NamedTextColor.RED, NamedTextColor.WHITE, NamedTextColor.YELLOW};
    }

}
