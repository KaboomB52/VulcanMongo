package dev.ianrich.vulcanmongo.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import dev.ianrich.vulcanmongo.log.LogManager;
import dev.ianrich.vulcanmongo.log.construct.Log;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@CommandAlias("logs")
@CommandPermission("vulcan.admin")
public class LogsCommand extends BaseCommand {

    private final int LOGS_PER_PAGE = 15;

    @Default
    @CommandCompletion("@players")
    @Syntax("<target> [page]")
    public void onLogsCommand(Player sender, OfflinePlayer target, @Optional Integer page) {
        UUID targetUUID = target.getUniqueId();
        List<Log> logs = LogManager.getLogs(targetUUID).stream()
                .sorted(Comparator.comparingLong(Log::getTimestamp).reversed())
                .toList();

        if (logs.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No logs found for " + target.getName());
            return;
        }

        int totalPages = (int) Math.ceil(logs.size() / (double) LOGS_PER_PAGE);
        int currentPage = (page == null || page < 1) ? 1 : Math.min(page, totalPages);

        sender.sendMessage(ChatColor.YELLOW + "Logs for " + target.getName() + " - Page " + currentPage + "/" + totalPages);

        int start = (currentPage - 1) * LOGS_PER_PAGE;
        int end = Math.min(start + LOGS_PER_PAGE, logs.size());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        for (int i = start; i < end; i++) {
            Log log = logs.get(i);
            String date = sdf.format(new Date(log.getTimestamp()));
            sender.sendMessage(ChatColor.GRAY + "[" + ChatColor.WHITE + date + ChatColor.GRAY + "] "
                    + ChatColor.GRAY + "[" + ChatColor.AQUA + log.getServer() + ChatColor.GRAY + "] "
                    + ChatColor.GOLD + log.getCheckName() + " " + log.getCheckType() + ChatColor.GRAY + " | "
                    + ChatColor.YELLOW + "VL: " + ChatColor.WHITE + log.getVl() + ChatColor.GRAY + " | "
                    + ChatColor.YELLOW + "Ping: " + ChatColor.WHITE + log.getPing() + ChatColor.GRAY + " | "
                    + ChatColor.YELLOW + "TPS: " + ChatColor.WHITE + log.getTps());
        }

        sender.sendMessage(ChatColor.YELLOW + "Use /logs " + target.getName() + " [page] to view more.");
    }
}
