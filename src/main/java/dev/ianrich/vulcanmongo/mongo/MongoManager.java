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
            for (UUID uuid : LogManager.getAllLogs().keySet()) {
                List<Log> playerLogs = LogManager.getLogs(uuid);
                List<Document> logDocs = new ArrayList<>();

                for (Log log : playerLogs) {
                    Document logDoc = new Document("timestamp", log.getTimestamp())
                            .append("playerName", log.getPlayerName())
                            .append("server", log.getServer())
                            .append("info", log.getInfo())
                            .append("checkName", log.getCheckName())
                            .append("checkType", log.getCheckType())
                            .append("vl", log.getVl())
                            .append("version", log.getVersion())
                            .append("ping", log.getPing())
                            .append("tps", log.getTps());
                    logDocs.add(logDoc);
                }

                // Retrieve the existing document for the player from MongoDB
                Document existingDoc = collection.find(eq("uuid", uuid.toString())).first();

                // If the document exists, merge the logs with the new ones, otherwise create a new document
                if (existingDoc != null) {
                    List<Document> existingLogs = existingDoc.getList("logs", Document.class);
                    existingLogs.addAll(logDocs); // Merge new logs with existing ones

                    // Replace the document with the merged logs
                    Document doc = new Document("uuid", uuid.toString())
                            .append("logs", existingLogs);
                    collection.replaceOne(eq("uuid", uuid.toString()), doc);
                } else {
                    // If no document exists, create a new one with the logs
                    Document doc = new Document("uuid", uuid.toString())
                            .append("logs", logDocs);
                    collection.insertOne(doc);
                }
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
                List<Document> logDocs = doc.getList("logs", Document.class);

                for (Document logDoc : logDocs) {
                    Log log = new Log(
                            uuid,
                            logDoc.getString("playerName"),
                            logDoc.getLong("timestamp"),
                            logDoc.getString("server"),
                            logDoc.getString("info"),
                            logDoc.getString("checkName"),
                            logDoc.getString("checkType"),
                            logDoc.getInteger("vl"),
                            logDoc.getString("version"),
                            logDoc.getInteger("ping"),
                            logDoc.getDouble("tps")
                    );
                    LogManager.addLog(uuid, log);
                }
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
