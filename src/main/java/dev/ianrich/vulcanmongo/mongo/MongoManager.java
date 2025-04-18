package dev.ianrich.vulcanmongo.mongo;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import dev.ianrich.vulcanmongo.log.LogManager;
import dev.ianrich.vulcanmongo.log.construct.Log;
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

    // Save logs to MongoDB
    public static void saveLogs() {
        try {
            List<Document> allLogs = new ArrayList<>();

            for (UUID uuid : LogManager.getAllLogs().keySet()) {
                for (Log log : LogManager.getLogs(uuid)) {
                    Document logDoc = new Document("uuid", uuid.toString())
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
                    allLogs.add(logDoc);
                }
            }

            if (!allLogs.isEmpty()) {
                collection.insertMany(allLogs);
            }

            logger.info("Logs saved successfully.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to save logs to MongoDB", e);
        }
    }



    // Load logs from MongoDB
    public static void loadLogs() {
        try {
            FindIterable<Document> docs = collection.find();
            for (Document doc : docs) {
                UUID uuid = UUID.fromString(doc.getString("uuid"));
                Log log = new Log(
                        uuid,
                        doc.getString("playerName"),
                        doc.getLong("timestamp"),
                        doc.getString("server"),
                        doc.getString("info"),
                        doc.getString("checkName"),
                        doc.getString("checkType"),
                        doc.getInteger("vl"),
                        doc.getString("version"),
                        doc.getInteger("ping"),
                        doc.getDouble("tps")
                );
                LogManager.addLog(uuid, log);
            }
            logger.info("Logs loaded successfully.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load logs from MongoDB", e);
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
