package co.aikar.commands;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextFormat;
import net.minestom.server.command.CommandSender;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.Material;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MinestomCommandContexts extends CommandContexts<MinestomCommandExecutionContext> {

    MinestomCommandContexts(MinestomCommandManager manager) {
        super(manager);

        registerIssuerAwareContext(CommandSender.class, MinestomCommandExecutionContext::getSender);
        registerIssuerAwareContext(Player.class, (c) -> {
            boolean isOptional = c.isOptional();
            CommandSender sender = c.getSender();
            boolean isPlayerSender = sender instanceof Player;
            if (!c.hasFlag("other")) {
                Player player = isPlayerSender ? (Player) sender : null;
                if (player == null && !isOptional) {
                    throw new InvalidCommandArgument(MessageKeys.NOT_ALLOWED_ON_CONSOLE, false);
                }
                PlayerInventory inventory = player != null ? player.getInventory() : null;
                if (inventory != null && c.hasFlag("itemheld") && !ACFMinestomUtil.isValidItem(inventory.getItemStack(player.getHeldSlot()))) {
                    throw new InvalidCommandArgument(MinecraftMessageKeys.YOU_MUST_BE_HOLDING_ITEM, false);
                }
                return player;
            } else {
                String arg = c.popFirstArg();
                if (arg == null && isOptional) {
                    if (c.hasFlag("defaultself")) {
                        if (isPlayerSender) {
                            return (Player) sender;
                        } else {
                            throw new InvalidCommandArgument(MessageKeys.NOT_ALLOWED_ON_CONSOLE, false);
                        }
                    } else {
                        return null;
                    }
                } else if (arg == null) {
                    throw new InvalidCommandArgument();
                }

                return getPlayer(c.getIssuer(), arg, isOptional);
            }
        });
        registerContext(TextFormat.class, c -> {
            String first = c.popFirstArg();
            Stream<? extends TextFormat> colors = NamedTextColor.NAMES.values().stream();
            if (!c.hasFlag("colorsonly")) {
                colors = Stream.concat(colors, Stream.of(TextDecoration.values()));
            }
            String filter = c.getFlagValue("filter", (String) null);
            if (filter != null) {
                filter = ACFUtil.simplifyString(filter);
                String finalFilter = filter;
                colors = colors.filter(color -> finalFilter.equals(ACFUtil.simplifyString(color.toString())));
            }

            TextColor match = NamedTextColor.NAMES.value(first);
            if (match == null) {
                String valid = colors.map(color -> "<c2>" + ACFUtil.simplifyString(color.toString()) + "</c2>")
                        .collect(Collectors.joining("<c1>,</c1> "));

                throw new InvalidCommandArgument(MessageKeys.PLEASE_SPECIFY_ONE_OF, "{valid}", valid);
            }
            return match;
        });
        registerContext(Pos.class, c -> {
            String input = c.popFirstArg();
            CommandSender sender = c.getSender();
            Pos sourceLoc = null;
            if (sender instanceof Player) {
                sourceLoc = ((Player) sender).getPosition();
            }

            boolean rel = input.startsWith("~");
            String[] split = ACFPatterns.COMMA.split(rel ? input.substring(1) : input);
            if (split.length < 3) {
                throw new InvalidCommandArgument(MinecraftMessageKeys.LOCATION_PLEASE_SPECIFY_XYZ);
            }

            Double x = ACFUtil.parseDouble(split[0]);
            Double y = ACFUtil.parseDouble(split[1]);
            Double z = ACFUtil.parseDouble(split[2]);

            if (sourceLoc != null && rel) {
                x += sourceLoc.x();
                y += sourceLoc.y();
                z += sourceLoc.z();
            } else if (rel) {
                throw new InvalidCommandArgument(MinecraftMessageKeys.LOCATION_CONSOLE_NOT_RELATIVE);
            }

            if (x == null || y == null || z == null) {
                throw new InvalidCommandArgument(MinecraftMessageKeys.LOCATION_PLEASE_SPECIFY_XYZ);
            }

            if (split.length >= 5) {
                Float yaw = ACFUtil.parseFloat(split[3]);
                Float pitch = ACFUtil.parseFloat(split[4]);

                if (pitch == null || yaw == null) {
                    throw new InvalidCommandArgument(MinecraftMessageKeys.LOCATION_PLEASE_SPECIFY_XYZ);
                }
                return new Pos(x, y, z, yaw, pitch);
            } else {
                return new Pos(x, y, z);
            }
        });
        registerContext(Point.class, c -> {
            String input = c.popFirstArg();
            CommandSender sender = c.getSender();
            String[] split = ACFPatterns.COMMA.split(input);
            if (split.length < 3) {
                throw new InvalidCommandArgument(MinecraftMessageKeys.LOCATION_PLEASE_SPECIFY_XYZ);
            }

            Integer x = ACFUtil.parseInt(split[0]);
            Integer y = ACFUtil.parseInt(split[1]);
            Integer z = ACFUtil.parseInt(split[2]);

            if (x == null || y == null || z == null) {
                throw new InvalidCommandArgument(MinecraftMessageKeys.LOCATION_PLEASE_SPECIFY_XYZ);
            }

            return new Vec(x, y, z);
        });
        registerContext(EntityType.class, c -> {
            String first = c.popFirstArg();
            Stream<? extends EntityType> entities = EntityType.values().stream();
            String filter = c.getFlagValue("filter", (String) null);
            if (filter != null) {
                filter = ACFUtil.simplifyString(filter);
                String finalFilter = filter;
                entities = entities.filter(e -> finalFilter.equals(ACFUtil.simplifyString(e.toString())));
            }

            List<? extends EntityType> filteredEntities = entities.toList();

            Optional<? extends EntityType> match = filteredEntities.stream().filter(entityType -> {
                return entityType.name().equalsIgnoreCase(first) || entityType.key().value().equalsIgnoreCase(first);
            }).findFirst();
            if (match.isEmpty()) {
                String valid = filteredEntities.stream().map(e -> "<c2>" + e.toString() + "</c2>")
                        .collect(Collectors.joining("<c1>,</c1> "));

                throw new InvalidCommandArgument(MessageKeys.PLEASE_SPECIFY_ONE_OF, "{valid}", valid);
            }
            return match.get();
        });
        registerContext(Material.class, c -> {
            String first = c.popFirstArg();
            Stream<? extends Material> materials = Material.values().stream();
            String filter = c.getFlagValue("filter", (String) null);
            if (filter != null) {
                filter = ACFUtil.simplifyString(filter);
                String finalFilter = filter;
                materials = materials.filter(m -> finalFilter.equals(ACFUtil.simplifyString(m.toString())));
            }

            List<? extends Material> filteredMaterials = materials.toList();

            Optional<? extends Material> match = filteredMaterials.stream().filter(material -> {
                return material.name().equalsIgnoreCase(first) || material.key().value().equalsIgnoreCase(first);
            }).findFirst();
            if (match.isEmpty()) {
                String valid = filteredMaterials.stream().map(e -> "<c2>" + e.toString() + "</c2>")
                        .collect(Collectors.joining("<c1>,</c1> "));

                throw new InvalidCommandArgument(MessageKeys.PLEASE_SPECIFY_ONE_OF, "{valid}", valid);
            }
            return match.get();
        });
    }


    Player getPlayer(MinestomCommandIssuer issuer, String lookup, boolean allowMissing) throws InvalidCommandArgument {
        if (issuer.isPlayer() && (lookup.equalsIgnoreCase("@s") || lookup.equalsIgnoreCase("@p"))) {
            return issuer.getPlayer();
        }

        Player player = ACFMinestomUtil.findPlayerSmart(issuer, lookup);
        if (player == null) {
            if (allowMissing) {
                return null;
            }
            throw new InvalidCommandArgument(false);
        }
        return player;
    }
}
