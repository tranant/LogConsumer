package log_consumer.DAO;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.bucket.BucketType;
import com.couchbase.client.java.cluster.BucketSettings;
import com.couchbase.client.java.cluster.ClusterManager;
import com.couchbase.client.java.cluster.DefaultBucketSettings;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.couchbase.client.java.error.BucketDoesNotExistException;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import io.vertx.core.json.Json;
import log_consumer.config.DbConfig;
import log_consumer.dto.Log;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LogDao {
    public Cluster cl;
    public Bucket bucket;

    public LogDao(){
        CouchbaseEnvironment env = DefaultCouchbaseEnvironment.builder()
                //this set the IO socket timeout globally, to 45s
                .socketConnectTimeout((int) TimeUnit.SECONDS.toMillis(45))
                //this sets the connection timeout for openBucket calls globally (unless a particular call provides its own timeout)
                .connectTimeout(TimeUnit.SECONDS.toMillis(60))
                .build();
        try {

            cl = CouchbaseCluster.create(env, DbConfig.url);

            cl.authenticate(DbConfig.username, DbConfig.password);
            bucket = cl.openBucket(DbConfig.bucket);
        } catch(BucketDoesNotExistException e){
            System.out.println("Bucket doesn't exist .... creating new one");

            ClusterManager clusterManager = cl.clusterManager();
            BucketSettings bucketSettings = new DefaultBucketSettings.Builder()
                    .type(BucketType.COUCHBASE)
                    .name(DbConfig.bucket)
                    .quota(120)
                    .build();

            clusterManager.insertBucket(bucketSettings);
            bucket = cl.openBucket(DbConfig.bucket);
            bucket.query(N1qlQuery.simple("create primary index employeesLog_index on "+DbConfig.bucket));
        } catch (Exception e ){
            System.out.println("unable to access the couchbase server please check the configuration");
        }

    }

    public Map<String, Log> getLog(){
        N1qlQueryResult result = bucket.query(N1qlQuery.simple("select * from EmployeesLog"));

        return result.allRows().stream()
                .map(log->JsonObject.fromJson(log.toString()).get("EmployeesLog").toString())
                .map(log->Json.decodeValue(log, Log.class))
                .sorted(Comparator.comparing(Log::getDate).thenComparing(Log::getTime).reversed())
                .collect(Collectors.toMap(log->log.getLogId(), log->log, (log1,log2) -> log1, LinkedHashMap::new));
    }

    public void sendLog(String logId, io.vertx.core.json.JsonObject jsonObject){
        try {
            bucket.insert(JsonDocument.create(logId, JsonObject.fromJson(jsonObject.encodePrettily())));
        } catch(Exception e) {
            System.out.println("Couchbase Insert Failed.");
        }
    }
}
