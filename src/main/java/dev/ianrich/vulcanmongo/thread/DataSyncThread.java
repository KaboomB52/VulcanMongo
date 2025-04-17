package dev.ianrich.vulcanmongo.thread;

import com.google.common.base.Stopwatch;
import dev.ianrich.vulcanmongo.log.LogManager;
import dev.ianrich.vulcanmongo.mongo.MongoManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.TimeUnit;

public class DataSyncThread extends BukkitRunnable {

    @Override
    public void run() {

        // Only save online profiles.
        Stopwatch stopwatch = Stopwatch.createUnstarted();
        stopwatch.start();

        // Save logs to MongoDB
        MongoManager.saveLogs();

        stopwatch.stop();

        // Notify players with the save status
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (player.hasPermission("basic.admin")) {
                player.sendMessage(ChatColor.BLUE + "[VulcanMongo] " + ChatColor.GRAY + "Saved " + LogManager.getTotalLogCount() + " logs and synced log data from mongo!" + ChatColor.RESET + " (" + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms)");
            }
        });
    }
}
