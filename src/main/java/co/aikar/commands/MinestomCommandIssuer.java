package co.aikar.commands;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;
import net.minestom.server.network.ConnectionManager;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiPredicate;

public class MinestomCommandIssuer implements CommandIssuer {

    private final MinestomCommandManager manager;
    private final CommandSender sender;
    private final ConnectionManager connectionManager;

    MinestomCommandIssuer(MinestomCommandManager manager, CommandSender sender) {
        this.manager = manager;
        this.sender = sender;
        this.connectionManager = MinecraftServer.getConnectionManager();
    }

    @Override
    public boolean isPlayer() {
        return sender instanceof Player;
    }

    @Override
    public CommandSender getIssuer() {
        return sender;
    }

    public Player getPlayer() {
        return isPlayer() ? (Player) sender : null;
    }

    @Override
    public @NotNull UUID getUniqueId() {
        if (isPlayer()) {
            Player player = (Player) sender;
            return connectionManager.getPlayerConnectionUuid(player.getPlayerConnection(), player.getUsername());
        }
        return UUID.nameUUIDFromBytes("ConsoleSender".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public CommandManager getManager() {
        return manager;
    }

    @Override
    public void sendMessageInternal(String message) {
        sender.sendMessage(ACFMinestomUtil.color(message));
    }

    @Override
    public boolean hasPermission(String permission) {
        BiPredicate<CommandSender, String> customCheck = ((MinestomCommandManager) MinestomCommandManager.getCurrentCommandManager())
                .getCustomPermissionCheck();

        if (customCheck != null) {
            return customCheck.test(sender, permission);
        }

        return sender.hasPermission(permission);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MinestomCommandIssuer that = (MinestomCommandIssuer) o;
        return Objects.equals(sender, that.sender);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender);
    }

}
