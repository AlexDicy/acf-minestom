package co.aikar.commands;

import java.lang.reflect.Method;

public class MinestomRegisteredCommand extends RegisteredCommand<MinestomCommandExecutionContext> {

    MinestomRegisteredCommand(BaseCommand scope, String command, Method method, String prefSubCommand) {
        super(scope, command, method, prefSubCommand);
    }

}
