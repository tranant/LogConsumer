package log_consumer.service;


import io.vertx.core.json.JsonObject;
import log_consumer.DAO.LogDao;
import log_consumer.dto.Log;

import java.util.Map;

public class LogService {

    private LogDao logDao = new LogDao();

    public Map<String, Log> getLog(){return logDao.getLog();}
    public void sendLog(String logId, JsonObject jsonObject){ logDao.sendLog(logId, jsonObject);}
}
