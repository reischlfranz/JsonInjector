package at.franzreischl.dke.jsoninjector;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class JsonInjectorModel {

  public String getContainerUrl() {
    return containerUrl;
  }
  public void setContainerUrl(String containerUrl) {
    this.containerUrl = containerUrl;
  }


  private String containerUrl="http://localhost:4001/api";

  private RestClient dataInputClient = new RestClient(containerUrl,"data");
  private RestClient statusReadClient = new RestClient(containerUrl,"active");
  private PrintWriter pw;
  DateTimeFormatter fileDateFormat;
  DateTimeFormatter logDateFormat;



  private int targetMinutesPerBatch;
  private List<Object> jsonObjectsRemaining;

  public JsonInjectorModel() throws IOException {
    fileDateFormat = DateTimeFormatter.ofPattern("YYYYMMdd-HHmm").localizedBy(Locale.getDefault()).withZone(TimeZone.getDefault().toZoneId());
    logDateFormat = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss.nn").localizedBy(Locale.getDefault()).withZone(TimeZone.getDefault().toZoneId());
    System.out.println(fileDateFormat.format(ZonedDateTime.now()));

    StatusChecker statusChecker = new StatusChecker(containerUrl);
    Thread statusCheckerThread = new Thread(statusChecker);

    statusCheckerThread.start();
    System.out.println("Status checker thread started");


    BufferedWriter bw;
    bw = Files.newBufferedWriter(Paths.get(String.format("injectlog%s.txt",fileDateFormat.format(ZonedDateTime.now()))));
    pw = new PrintWriter(bw);
    pw.println(String.format("[%s] Application started",logDateFormat.format(ZonedDateTime.now())));


  }




  public String getContainerStatus(){
    return statusReadClient.doGet(null).readEntity(String.class);
  }

  public void close(){
    System.out.println("Gracefully shutting down the application");
    pw.println(String.format("[%s] Application shutdown",logDateFormat.format(ZonedDateTime.now())));
    pw.flush();
    pw.close();
  }

}
