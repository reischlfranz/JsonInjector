package at.franzreischl.dke.jsoninjector;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.ws.rs.core.Response;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class JsonInjectorModel {

  private JSONArray jsonObjects;
  private String containerUrl;
  private RestClient dataInputClient;
  private RestClient statusReadClient;
  private PrintWriter pw;
  private StatusChecker statusChecker;
  DateTimeFormatter fileDateFormat;
  DateTimeFormatter logDateFormat;
  private int targetMinutesPerBatch;
  private List<Object> jsonObjectsRemaining;

  private Thread statusCheckerThread;

  public JsonInjectorModel() throws IOException {
    fileDateFormat = DateTimeFormatter.ofPattern("YYYYMMdd-HHmmss").localizedBy(Locale.getDefault()).withZone(TimeZone.getDefault().toZoneId());
    logDateFormat = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss.nn").localizedBy(Locale.getDefault()).withZone(TimeZone.getDefault().toZoneId());
    //System.out.println(fileDateFormat.format(ZonedDateTime.now()));

    BufferedWriter bw;
    bw = Files.newBufferedWriter(Paths.get(String.format("injectlog%s.txt",fileDateFormat.format(ZonedDateTime.now()))));
    pw = new PrintWriter(bw);

    log("Application started");
  }

  public String getContainerUrl() {
    return containerUrl;
  }

  public void setContainerUrl(String containerUrl) {
    try {
      statusReadClient = new RestClient(containerUrl, "active");
      if(!isContainerReady()){
        statusReadClient = null;
      }else{
        dataInputClient = new RestClient(containerUrl, "data");
        statusChecker = new StatusChecker(containerUrl);
        statusCheckerThread = new Thread(statusChecker);
        statusCheckerThread.start();

        this.containerUrl = containerUrl;
      }
    } catch (MalformedURLException e) {
      log("URL format not correct!");

    }

  }

  public String getContainerStatus(){
    Response r = statusReadClient.doGet(null);
    JSONObject jo = new JSONObject(r.readEntity(String.class));
    if(r.getStatus() == 404) return "Offline";
    if( jo.getBoolean("active")){
      return "Active";
    }else{
      return "Busy";
    }
  }

  public boolean isContainerReady(){
    return statusReadClient != null && getContainerStatus() == "Active";
  }

  public void close(){
    System.out.println("Gracefully shutting down the application");
    log("Application shutdown");
//    pw.println(String.format("[%s] Application shutdown",logDateFormat.format(ZonedDateTime.now())));
    pw.flush();
    pw.close();
  }

  private void log(String s){
    pw.println(String.format("[%s] ",logDateFormat.format(ZonedDateTime.now())) + s);
  }

  public void setJsonData(JSONArray objects) {
    this.jsonObjects = objects;
    log(objects.length() + " JSON objects entered");
  }

  public JSONArray getJsonData() {
    return this.jsonObjects;
  }


}
