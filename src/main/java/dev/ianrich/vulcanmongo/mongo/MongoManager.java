package dev.ianrich.vulcanmongo.mongo;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import dev.ianrich.vulcanmongo.log.Log;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static com.mongodb.client.model.Filters.eq;

public class MongoManager {

    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static MongoCollection<Document> collection;

    private static final Logger logger = Logger.getLogger(MongoManager.class.getName());

    // Connect to MongoDB
    public static void connect(String uri, String dbName, String collectionName) {
        try {
            CodecRegistry pojoCodecRegistry = CodecRegistries.fromProviders(
                    PojoCodecProvider.builder().automatic(true).build()
            );

            CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                    getDefaultCodecRegistry(),
                    pojoCodecRegistry
            );

            mongoClient = MongoClients.create(uri);
            database = mongoClient.getDatabase(dbName).withCodecRegistry(codecRegistry);
            collection = database.getCollection(collectionName);
            logger.info("Connected to MongoDB at " + uri);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to connect to MongoDB", e);
        }
    }

    // Save a single log to MongoDB (upsert by uuid + timestamp)
    public static void saveLog(Log log) {
        try {
            Document logDoc = new Document("uuid", log.getUuid().toString())
                    .append("timestamp", log.getTimestamp())
                    .append("playerName", log.getPlayerName())
                    .append("server", log.getServer())
                    .append("info", log.getInfo())
                    .append("checkName", log.getCheckName())
                    .append("checkType", log.getCheckType())
                    .append("vl", log.getVl())
                    .append("version", log.getVersion())
                    .append("ping", log.getPing())
                    .append("tps", log.getTps());

            collection.replaceOne(
                    Filters.and(
                            eq("uuid", log.getUuid().toString()),
                            eq("timestamp", log.getTimestamp())
                    ),
                    logDoc,
                    new ReplaceOptions().upsert(true)
            );
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to save log", e);
        }
    }

    // Get all logs for a specific UUID
    public static List<Log> getLogsByUUID(UUID uuid) {
        List<Log> logs = new ArrayList<>();
        try {
            FindIterable<Document> docs = collection.find(eq("uuid", uuid.toString()));
            for (Document doc : docs) {
                Log log = documentToLog(doc);
                if (log != null) {
                    logs.add(log);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to fetch logs by UUID", e);
        }
        return logs;
    }

    // Get all logs
    public static List<Log> getAllLogs() {
        List<Log> logs = new ArrayList<>();
        try {
            FindIterable<Document> docs = collection.find();
            for (Document doc : docs) {
                Log log = documentToLog(doc);
                if (log != null) {
                    logs.add(log);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to fetch all logs", e);
        }
        return logs;
    }

    // Convert Document to Log object
    private static Log documentToLog(Document doc) {
        try {
            UUID uuid = UUID.fromString(doc.getString("uuid"));
            Long timestamp = doc.getLong("timestamp") != null ? doc.getLong("timestamp") : 0L;

            return new Log(
                    uuid,
                    doc.getString("playerName"),
                    timestamp,
                    doc.getString("server"),
                    doc.getString("info"),
                    doc.getString("checkName"),
                    doc.getString("checkType"),
                    doc.getInteger("vl"),
                    doc.getString("version"),
                    doc.getInteger("ping"),
                    doc.getDouble("tps")
            );
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to convert document to log", e);
            return null;
        }
    }

    // Close MongoDB connection
    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
            logger.info("MongoDB connection closed.");
        }
    }
}
