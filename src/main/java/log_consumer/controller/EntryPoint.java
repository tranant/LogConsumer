package log_consumer.controller;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import java.util.HashMap;
import java.util.Map;


public class EntryPoint extends AbstractVerticle {
    //private String kafkaServer = "localhost:9092";
    private String kafkaServer = "kafka-2ac2e185-antinywong-f578.aivencloud.com:14246";

    public void start(Future<Void> fut){
        HttpServer hs = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.get("/api/log").handler(LogController::getLog);

        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", kafkaServer);
        config.put("security.protocol", "SSL");
        config.put("ssl.truststore.location", "C:\\Users\\trana\\Desktop\\keys\\client.truststore.jks");
        config.put("ssl.truststore.password", "password");
        config.put("ssl.keystore.type", "PKCS12");
        config.put("ssl.keystore.location", "C:\\Users\\trana\\Desktop\\keys\\client.keystore.p12");
        config.put("ssl.keystore.password", "password");
        config.put("ssl.key.password", "password");
        config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("value.deserializer", "io.vertx.kafka.client.serialization.JsonObjectDeserializer");
        config.put("group.id", "my_group");
        config.put("auto.offset.reset", "latest");
        config.put("enable.auto.commit", "false");

        // Use consumer for interacting with Apache Kafka
        KafkaConsumer<String, JsonObject> consumer = KafkaConsumer.create(vertx, config);
        consumer.handler(record -> {
            LogController logController = new LogController();
            logController.sendLog(record.key(), record.value());
            System.out.println("Key: " + record.key() + " Value: " + record.value());
        });

        consumer.subscribe("Database", ar -> {
            if(ar.succeeded())
                System.out.println("Successful Subscribe.");
            else
                System.out.println("Failed Subscribe.");
        });

        hs.requestHandler(router)
                .listen(8083);
    }
}
