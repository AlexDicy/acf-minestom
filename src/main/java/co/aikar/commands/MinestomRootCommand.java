package co.aikar.commands;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.arguments.*;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentColor;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentItemStack;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentUUID;
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentEntityType;
import net.minestom.server.command.builder.arguments.number.ArgumentDouble;
import net.minestom.server.command.builder.arguments.number.ArgumentFloat;
import net.minestom.server.command.builder.arguments.number.ArgumentInteger;
import net.minestom.server.command.builder.arguments.number.ArgumentLong;
import net.minestom.server.command.builder.suggestion.Suggestion;
import net.minestom.server.command.builder.suggestion.SuggestionCallback;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

@SuppressWarnings("rawtypes")
public class MinestomRootCommand extends Command implements RootCommand, CommandExecutor, SuggestionCallback {
    private static final Map<Class<?>, Function<String, Argument<?>>> CLASS_ARGUMENT_MAP = new HashMap<>();

    static {
        CLASS_ARGUMENT_MAP.put(boolean.class, ArgumentBoolean::new);
        CLASS_ARGUMENT_MAP.put(Boolean.class, ArgumentBoolean::new);
        CLASS_ARGUMENT_MAP.put(int.class, ArgumentInteger::new);
        CLASS_ARGUMENT_MAP.put(Integer.class, ArgumentInteger::new);
        CLASS_ARGUMENT_MAP.put(double.class, ArgumentDouble::new);
        CLASS_ARGUMENT_MAP.put(Double.class, ArgumentDouble::new);
        CLASS_ARGUMENT_MAP.put(float.class, ArgumentFloat::new);
        CLASS_ARGUMENT_MAP.put(Float.class, ArgumentFloat::new);
        CLASS_ARGUMENT_MAP.put(long.class, ArgumentLong::new);
        CLASS_ARGUMENT_MAP.put(Long.class, ArgumentLong::new);
        CLASS_ARGUMENT_MAP.put(String.class, ArgumentWord::new);
        CLASS_ARGUMENT_MAP.put(String[].class, ArgumentStringArray::new);
        CLASS_ARGUMENT_MAP.put(TextColor.class, ArgumentColor::new);
        CLASS_ARGUMENT_MAP.put(EntityType.class, ArgumentEntityType::new);

        CLASS_ARGUMENT_MAP.put(Player.class, s -> new ArgumentEntity(s).singleEntity(true).onlyPlayers(true));

        CLASS_ARGUMENT_MAP.put(ItemStack.class, ArgumentItemStack::new);
        CLASS_ARGUMENT_MAP.put(UUID.class, ArgumentUUID::new);
    }

    private final MinestomCommandManager manager;
    private final String name;
    private BaseCommand defCommand;
    private final SetMultimap<String, RegisteredCommand> subCommands = HashMultimap.create();
    private final List<BaseCommand> children = new ArrayList<>();
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

            boolean isForwardingCommand = command instanceof ForwardingCommand;

            for (Map.Entry<String, RegisteredCommand> entry : command.getSubCommands().entries()) {
                if ((BaseCommand.isSpecialSubcommand(entry.getKey())) && !isForwardingCommand || (!entry.getKey().equals("help") && entry.getValue().prefSubCommand.equals("help"))) {
                    // don't register stuff like __catchunknown and don't help command aliases
                    continue;
                }

                // handle sub commands
                List<Argument<?>> arguments = new ArrayList<>();

                if (!isForwardingCommand) {
                    String commandName = entry.getKey();
                    ArgumentLiteral commandArgument;

                    String[] split = ACFPatterns.SPACE.split(commandName);
                    for (int i = 0; i < split.length - 1; i++) {
                        arguments.add(ArgumentType.Literal(split[i]));
                    }
                    commandName = split[split.length - 1];
                    commandArgument = ArgumentType.Literal(split[split.length - 1]);
                    arguments.add(commandArgument);
                }


                CommandParameter<?>[] parameters = entry.getValue().parameters;
                for (CommandParameter<?> param : parameters) {
                    CommandParameter<?> nextParam = param.getNextParam();
                    if (param.isCommandIssuer() || (param.canExecuteWithoutInput() && nextParam != null && !nextParam.canExecuteWithoutInput())) {
                        continue;
                    }

                    Argument<?> argument;
                    if (param.getType().isEnum()) {
                        //noinspection unchecked
                        argument = ArgumentType.Enum(param.getName(), (Class<? extends Enum<?>>) param.getType())
                                .setFormat(ArgumentEnum.Format.LOWER_CASED);
                        if (param.isOptional()) {
                            argument.setDefaultValue(() -> null);
                        }
                    } else if (param.isOptional()) {
                        argument = param.consumesRest ? new OptionalArgumentString(param.getName()) : ArgumentType.Word(param.getName());
                    } else if (param.consumesRest) {
                        argument = new GreedyArgumentString(param.getName());
                    } else {
                        Function<String, Argument<?>> argumentFunction = CLASS_ARGUMENT_MAP.get(param.getType());
                        if (argumentFunction != null) {
                            argument = argumentFunction.apply(param.getName());
                        } else {
                            argument = ArgumentType.Word(param.getName());
                        }
                    }
                    String defaultValue = param.getDefaultValue();
                    if (!(defaultValue != null && defaultValue.trim().isEmpty())) {
                        if (argument instanceof ArgumentWord) {
                            ((ArgumentWord) argument).setDefaultValue(() -> defaultValue);
                        }
                        if (argument instanceof ArgumentString) {
                            ((ArgumentString) argument).setDefaultValue(() -> defaultValue);
                        }
                    }
                    if (!(argument instanceof ArgumentEnum)) {
                        argument.setSuggestionCallback(this);
                    }

                    arguments.add(argument);
                }

                addSyntax(this, arguments.toArray(new Argument[]{}));
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
        if (args.length > 0) {
            args = Arrays.copyOfRange(args, 1, args.length);
        }
        execute(manager.getCommandIssuer(sender), context.getCommandName(), args);
    }

    @Override
    public void apply(@NotNull CommandSender sender, @NotNull CommandContext context, @NotNull Suggestion suggestion) {
        String[] args = context.getInput().split(" ");
        if (args.length > 0) {
            args = Arrays.copyOfRange(args, 1, args.length);
        }

        if (context.getInput().endsWith(" ")) {
            args = Arrays.copyOf(args, args.length + 1);
            args[args.length - 1] = "";
            suggestion.setStart(context.getInput().length() + 1);
        }

        List<String> completions = getTabCompletions(manager.getCommandIssuer(sender), context.getCommandName(), args);
        for (String completion : completions) {
            if (!context.getInput().endsWith(" ") && completion.startsWith("<") && completion.endsWith(">")) continue;
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
