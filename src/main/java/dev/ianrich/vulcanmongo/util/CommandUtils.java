package dev.ianrich.vulcanmongo.util;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.PluginManager;

import java.lang.reflect.Field;
import java.util.Map;

public class CommandUtils {

    public static void unregisterCommand(String commandName) {
        try {
            PluginManager pluginManager = Bukkit.getServer().getPluginManager();

            // Get the command map from the plugin manager
            Field commandMapField = pluginManager.getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            SimpleCommandMap commandMap = (SimpleCommandMap) commandMapField.get(pluginManager);

            // Get knownCommands map from the command map
            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Command> knownCommands = (Map<String, Command>) knownCommandsField.get(commandMap);

            // Remove the command and any aliases
            knownCommands.keySet().removeIf(key ->
                    key.equalsIgnoreCase(commandName) || key.startsWith(commandName + ":"));

            Bukkit.getLogger().info("Successfully unregistered command: /" + commandName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
