package at.franzreischl.dke.jsoninjector;

import javafx.beans.property.*;
import org.json.JSONObject;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class JsonInjectorModel {

  private Thread injectorThread;
  private JsonInjectorController controller;



  private String containerUrl;
  RestClient dataInputClient;
  private RestClient statusReadClient;
  private PrintWriter pw;
  private StatusChecker statusChecker;
  DateTimeFormatter fileDateFormat;
  DateTimeFormatter logDateFormat;
  int targetMinutesPerBatch;

  ArrayList<Object> dataList;
  ArrayList<BatchDataInjector> batches;


  private Thread statusCheckerThread;




  public void setTargetMinutesPerBatch(int targetMinutesPerBatch) {
    this.targetMinutesPerBatch = targetMinutesPerBatch;
  }

  public JsonInjectorController getController() {
    return controller;
  }

  public void setController(JsonInjectorController controller) {
    this.controller = controller;
  }

  /**Constructor
   *
   * @throws IOException
   */
  public JsonInjectorModel() throws IOException {
    batches = new ArrayList<>();
    fileDateFormat = DateTimeFormatter.ofPattern("YYYYMMdd-HHmmss").localizedBy(Locale.getDefault()).withZone(TimeZone.getDefault().toZoneId());
    logDateFormat = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss.nn").localizedBy(Locale.getDefault()).withZone(TimeZone.getDefault().toZoneId());
    //System.out.println(fileDateFormat.format(ZonedDateTime.now()));

    BufferedWriter bw;
    bw = Files.newBufferedWriter(Paths.get(String.format("injectlog%s.txt",fileDateFormat.format(ZonedDateTime.now()))));
    pw = new PrintWriter(bw);

    log("Application started");
    InjectorHelper.getInstance().setModel(this);
  }

  public String getContainerUrl() {
    return containerUrl;
  }

  public void setContainerUrl(String containerUrl) {
    try {
      log("set container URL to "+containerUrl);
      statusReadClient = new RestClient(containerUrl, "active");
      if(!isContainerReady()){
        statusReadClient = null;
      }else{
        dataInputClient = new RestClient(containerUrl, "data");
        statusChecker = new StatusChecker(containerUrl);
        statusCheckerThread = new Thread(statusChecker);
        //statusCheckerThread.start();

        this.containerUrl = containerUrl;
        log("Container Connection OK");
      }
    } catch (MalformedURLException e) {
      log("URL format not correct!");

    }

  }

  public String getContainerStatus(){
    Response r;
    try {
      r = statusReadClient.doGet(null);
    }catch (ProcessingException e){
      e.printStackTrace();
      return "Offline";
    }
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
    log("Gracefully shutting down the application");
    // Closing the File writer for log file
    pw.close();
  }

  void log(String s){
    pw.println(String.format("[%s] ",logDateFormat.format(ZonedDateTime.now())) + s);
    System.out.println(String.format("[%s] ",logDateFormat.format(ZonedDateTime.now())) + s);
    pw.flush();
  }

  public void setDataList(List<Object> objects) {
    if(objects == null) return;
    dataList = new ArrayList<>(objects);
    controller.remainingObjectsProperty.set(objects.size());
    controller.totalObjectsProperty.set(objects.size());
    log(objects.size() + " JSON objects entered");
  }

  public void startInjection() {

    if(injectorThread == null || !injectorThread.isAlive()) {
      injectorThread = new Thread(InjectorHelper.getInstance());
      injectorThread.start();
    }

  }

  SimpleLongProperty testProp = new SimpleLongProperty(0);


  public void setPropNextBatchSize(long val){
//    controller.nextBatchSizeProperty.set(val);
    testProp.set(val);
  }

  public long getDoneObjects(){
    return batches.stream().map(x -> x.size()).reduce(0L,(x,y)->x+y);
  }
  public long getDoneDurationMillis(){
    return batches.stream().map(x -> x.getBatchDurationMillis()).reduce(0L,(x,y)->x+y);
  }

  public double getTotalOpm(){
    if(batches.isEmpty()) return -1.0;
    return getDoneObjects() / ((double) getDoneDurationMillis() / 1000 / 60);
  }

  public double getLastBatchOpm(){
    if(batches.isEmpty()) return -1.0;
    return batches.get(batches.size()-1).getOpm();
  }

  public double getAvgBatchSize(){
    if(batches.isEmpty()) return 0.0;
    return (double) getDoneObjects() / batches.size();
  }


  public void pauseInjection() {
    if(injectorThread.isAlive()){
      InjectorHelper.getInstance().stop();

    }


  }

  public void setNextObjectIndex(long nextObject) throws IndexOutOfBoundsException{
    System.out.println("Setting next Object index to " + nextObject);
    InjectorHelper.getInstance().setNextObject(nextObject);

  }
}
