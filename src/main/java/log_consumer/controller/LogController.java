package log_consumer.controller;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import log_consumer.dto.Log;
import log_consumer.service.LogService;

import java.util.Map;

public class LogController {

    private static LogService logService = new LogService();

    public static void getLog(RoutingContext routingContext){
        Map<String, Log> log = logService.getLog();

        routingContext.response()
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(log));
    }

    public void sendLog(String logId, JsonObject jsonObject){
        logService.sendLog(logId, jsonObject);
    }


}
