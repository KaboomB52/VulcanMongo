package dev.ianrich.vulcanmongo;

import dev.ianrich.vulcanmongo.commands.LogsCommand;
import dev.ianrich.vulcanmongo.listener.VulcanListener;
import dev.ianrich.vulcanmongo.mongo.MongoManager;
import dev.ianrich.vulcanmongo.util.CommandUtils;
import net.minebo.cobalt.acf.ACFCommandController;
import net.minebo.cobalt.acf.ACFManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class VulcanMongo extends JavaPlugin {

    public static VulcanMongo instance;

    public static ACFManager acfManager;

    @Override
    public void onEnable() {
        instance = this;

        acfManager = new ACFManager(this);

        removeLogsCommand();
        ACFCommandController.registerCommand(new LogsCommand());

        this.saveDefaultConfig();

        MongoManager.connect(getConfig().getString("mongo.uri"), getConfig().getString("mongo.database"), "logs");

        Bukkit.getPluginManager().registerEvents(new VulcanListener(), this);

    }

    @Override
    public void onDisable() {
        MongoManager.close();
    }

    public void removeLogsCommand(){
        CommandUtils.unregisterCommand("logs");
        CommandUtils.unregisterCommand("punishlogs");
    }

}
