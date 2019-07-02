package log_consumer.main;


import io.vertx.core.Launcher;
import log_consumer.controller.EntryPoint;

public class Application {
    public static void main(String[] args){
      Launcher.executeCommand("run", EntryPoint.class.getName());
    }
}
