package dev.ianrich.vulcanmongo.log;

import dev.ianrich.vulcanmongo.log.construct.Log;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LogManager {
    public static final Map<UUID, List<Log>> logs = new ConcurrentHashMap<>();

    public static void addLog(UUID uuid, Log log) {
        logs.computeIfAbsent(uuid, k -> new ArrayList<>()).add(log);
    }

    public static List<Log> getLogs(UUID uuid) {
        return logs.getOrDefault(uuid, Collections.emptyList());
    }

    public static void clearLogs(UUID uuid) {
        logs.remove(uuid);
    }

    public static Map<UUID, List<Log>> getAllLogs() {
        return logs;
    }
}
