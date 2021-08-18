package co.aikar.commands;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.command.builder.suggestion.Suggestion;
import net.minestom.server.command.builder.suggestion.SuggestionCallback;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MinestomRootCommand extends Command implements RootCommand, CommandExecutor, CommandCondition, SuggestionCallback {

    private final MinestomCommandManager manager;
    private final String name;
    private BaseCommand defCommand;
    private SetMultimap<String, RegisteredCommand> subCommands = HashMultimap.create();
    private List<BaseCommand> children = new ArrayList<>();
    boolean isRegistered = false;

    MinestomRootCommand(MinestomCommandManager manager, String name) {
        super(name);
        this.manager = manager;
        this.name = name;

        setDefaultExecutor(this);
    }

    @Override
    public void addChild(BaseCommand command) {
        if (this.defCommand == null || !command.subCommands.get(BaseCommand.DEFAULT).isEmpty()) {
            this.defCommand = command;

            for(Map.Entry<String, RegisteredCommand> entry : command.subCommands.entries()) {
                if(entry.getValue().complete.isEmpty()) {
                    if (!entry.getKey().equals(BaseCommand.DEFAULT)) {
                        addSyntax(this, ArgumentType.Literal(entry.getKey()));
                    }
                } else if(entry.getKey().equals(BaseCommand.DEFAULT)) {
                    String[] complete = entry.getValue().complete.split(" ");

                    Argument<?>[] arguments = new Argument[complete.length];

                    for(int i=0; i<arguments.length; i++) {
                        String id = complete[i].toLowerCase().replaceAll("[^a-z0-9/._-]", "");

                        if(complete[i].equalsIgnoreCase("@players")) {
                            arguments[i] = ArgumentType.Entity(id).onlyPlayers(true);
                        } else {
                            if (entry.getValue().parameters[i].isOptional()) {
                                arguments[i] = ArgumentType.String(id);
                            } else {
                                arguments[i] = new OptionalArgumentString(id);
                            }
                            arguments[i].setSuggestionCallback(this);
                        }
                    }

                    addSyntax(this, arguments);
                } else {
                    String[] complete = entry.getValue().complete.split(" ");

                    Argument<?>[] arguments = new Argument[complete.length+1];
                    arguments[0] = ArgumentType.Literal(entry.getKey());

                    for(int i=1; i<arguments.length; i++) {
                        String id = complete[i-1].toLowerCase().replaceAll("[^a-z0-9/._-]", "");

                        if(complete[i-1].equalsIgnoreCase("@players")) {
                            arguments[i] = ArgumentType.Entity(id).onlyPlayers(true);
                        } else {
                            if (entry.getValue().parameters[i].isOptional()) {
                                arguments[i] = ArgumentType.String(id);
                            } else {
                                arguments[i] = new OptionalArgumentString(id);
                            }
                            arguments[i].setSuggestionCallback(this);
                        }
                    }

                    addSyntax(this, arguments);
                }
            }
        }
        addChildShared(this.children, this.subCommands, command);
    }

    @Override
    public String getDescription() {
        RegisteredCommand command = getDefaultRegisteredCommand();

        if (command != null && !command.getHelpText().isEmpty()) {
            return command.getHelpText();
        }
        if (command != null && command.scope.description != null) {
            return command.scope.description;
        }
        return defCommand.getName();
    }

    @Override
    public String getCommandName() {
        return name;
    }

    @Nullable
    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public void apply(@NotNull CommandSender sender, @NotNull CommandContext context) {
        String[] args = context.getInput().split(" ");
        String command = context.getCommandName();
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase(command)) {
                args = Arrays.copyOfRange(args, 1, args.length);
            }
        }
        execute(manager.getCommandIssuer(sender), command, args);
    }

    @Override
    public boolean canUse(@NotNull CommandSender player, @Nullable String commandString) {
        return hasAnyPermission(manager.getCommandIssuer(player));
    }

    @Override
    public void apply(@NotNull CommandSender sender, @NotNull CommandContext context, @NotNull Suggestion suggestion) {
        String[] args = context.getInput().split(" ");
        String command = context.getCommandName();
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase(command)) {
                args = Arrays.copyOfRange(args, 1, args.length);
            }
        }

        if(context.getInput().endsWith(" ")) {
            args = Arrays.copyOf(args, args.length+1);
            args[args.length-1] = "";
            suggestion.setStart(context.getInput().length() + 1);
        }

        List<String> completions = getTabCompletions(manager.getCommandIssuer(sender), command, args);
        for(String completion : completions) {
            if(!context.getInput().endsWith(" ") && completion.startsWith("<") && completion.endsWith(">")) continue;
            suggestion.addEntry(new SuggestionEntry(completion));
        }
    }

    @Override
    public CommandManager getManager() {
        return manager;
    }

    @Override
    public SetMultimap<String, RegisteredCommand> getSubCommands() {
        return this.subCommands;
    }

    @Override
    public List<BaseCommand> getChildren() {
        return children;
    }

    @Override
    public BaseCommand getDefCommand() {
        return defCommand;
    }


}
