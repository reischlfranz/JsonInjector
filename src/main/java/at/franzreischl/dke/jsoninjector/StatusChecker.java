package at.franzreischl.dke.jsoninjector;

import org.json.*;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class StatusChecker implements Runnable {

  public enum Status{
    OFFLINE,
    BUSY,
    OK
  }

  private RestClient rs;
  private Status status  = Status.OFFLINE;
  private java.time.LocalDateTime lastUpdate;

  //String s = JsonInjectorModel.getContainerUrl();


  public StatusChecker(String url) throws MalformedURLException {
    rs = new RestClient(url,"active");
    checkStatus();
    System.out.println(lastUpdate);
  }

  private void checkStatus(){
    Response r = rs.doGet(null);
    int responseCode = r.getStatus();
    String responseBodyString = r.readEntity(String.class);
    JSONObject jo;
    if(responseCode > 400){
      status = Status.OFFLINE;
    }else{
      jo = new JSONObject(responseBodyString);
      if(responseCode == 200 && jo.getBoolean("active") ){
        status = Status.OK;
      }else if(responseCode == 200 && !jo.getBoolean("active") ){
        status = Status.BUSY;
      }
    }
    lastUpdate = LocalDateTime.now();
  }

  public boolean isOnline(){ return status != Status.OFFLINE;}

  public boolean isOk(){ return status == Status.OK;}

  @Override
  public void run() {
    while(true) {
      try {

        Thread.sleep(10000);
        checkStatus();
        System.err.println("Container ist " +
                           (status == Status.OFFLINE ? "Offline!" :
                                   (status == Status.BUSY ? "Beschäftigt" : "Bereit!")));


      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

  }
}
