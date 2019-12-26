package at.franzreischl.dke.jsoninjector;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import org.json.JSONArray;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class JsonInjectorModel {

  private long nextObject = 0, batchIndex = 0;

  SimpleLongProperty remainingObjectsProperty = new SimpleLongProperty(0);
  SimpleLongProperty totalObjectsProperty = new SimpleLongProperty(0);
  SimpleLongProperty nextBatchSizeProperty = new SimpleLongProperty(0);
  SimpleLongProperty currentBatchSizeProperty = new SimpleLongProperty(0);
  SimpleLongProperty currentBatchRemainProperty = new SimpleLongProperty(0);

  SimpleDoubleProperty currentBatchProgress = new SimpleDoubleProperty(0.25);

  Property<Boolean> runs = new SimpleBooleanProperty(false);


  private String containerUrl;
  private RestClient dataInputClient;
  private RestClient statusReadClient;
  private PrintWriter pw;
  private StatusChecker statusChecker;
  DateTimeFormatter fileDateFormat;
  DateTimeFormatter logDateFormat;
  private int targetMinutesPerBatch;

  private BatchDataInjector lastBatch, nextBatch, currentBatch;



  private ArrayList<Object> dataList;

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
      log("set container URL to "+containerUrl);
      statusReadClient = new RestClient(containerUrl, "active");
      if(!isContainerReady()){
        statusReadClient = null;
      }else{
        dataInputClient = new RestClient(containerUrl, "data");
        statusChecker = new StatusChecker(containerUrl);
        statusCheckerThread = new Thread(statusChecker);
        statusCheckerThread.start();

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
    System.out.println("Gracefully shutting down the application");
    log("Application shutdown");
//    pw.println(String.format("[%s] Application shutdown",logDateFormat.format(ZonedDateTime.now())));
    pw.flush();
    pw.close();
  }

  void log(String s){
    pw.println(String.format("[%s] ",logDateFormat.format(ZonedDateTime.now())) + s);
  }

  public void setDataList(List<Object> objects) {
    if(objects == null) return;
    dataList = new ArrayList<>(objects);
    remainingObjectsProperty.set(objects.size());
    log(objects.size() + " JSON objects entered");
  }

//  public JSONArray getJsonData() {
//    return this.jsonObjects;
//  }


  void prepareNextBatch(){
    long nextBatchSize;
    if(nextObject < 0) return;

    if (lastBatch == null ){
       nextBatchSize = 1;
     }else if(targetMinutesPerBatch * lastBatch.getOpm() < lastBatch.size()
              || Math.abs( targetMinutesPerBatch * lastBatch.getOpm() - lastBatch.size()) < 2){
       nextBatchSize = (long) (targetMinutesPerBatch * lastBatch.getOpm());
    }else{
        nextBatchSize = (long) (( targetMinutesPerBatch * lastBatch.getOpm() - lastBatch.size()) *  0.60 +  targetMinutesPerBatch * lastBatch.getOpm());
    }
    if(nextBatchSize > dataList.size() - nextObject - 1){
      nextBatchSize = dataList.size() - nextObject - 1;
      nextObject = -1;
    }

    List<Object> nextBatchData = dataList.subList((int)nextObject, (int)(nextObject + nextBatchSize));

    nextBatch = new BatchDataInjector(this.dataInputClient, nextBatchData, this);
    nextObject += nextBatchSize;

    log("Next batch prepared, size: " + nextBatch);
  }


  void startInjection(){
    runs.setValue(true);
    while(runs.getValue() && nextBatch != null){
      currentBatch = nextBatch;
      nextBatch = null;

      // bind properties for current batch



      try {
        currentBatch.startInject();
      } catch (IllegalAccessException | InterruptedException e) {
        e.printStackTrace();
        runs.setValue(false);
        return;
      }

      lastBatch = currentBatch;
      currentBatch = null;

      prepareNextBatch();
    }

  }





}
