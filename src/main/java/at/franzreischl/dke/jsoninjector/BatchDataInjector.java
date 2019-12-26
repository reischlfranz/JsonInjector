package at.franzreischl.dke.jsoninjector;

import javax.ws.rs.core.Response;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Timer;

public class BatchDataInjector {
  private static int batchIndexCounter = 0;

  private RestClient rc;
  private int batchIndex;
  private List<Object> batchData;
  private int index = 0;
  private Instant startTime;
  private Instant endLoadTime;
  private Instant doneTime;
  private boolean done=false;
  private JsonInjectorModel caller;

  public BatchDataInjector(RestClient rc, List<Object> batchData, JsonInjectorModel caller){
    if(batchData == null || rc == null || caller == null) throw new IllegalArgumentException();
    this.rc = rc;
    this.batchData = batchData;
    this.caller = caller;
    batchIndex = batchIndexCounter++;
  }

  private boolean isDone(){
    return done;
  }

  public void startInject() throws IllegalAccessException, InterruptedException {
    if(done) return;
    caller.log("Batch " + batchIndex + ": Injecting " + batchData.size() + " objects into container...");
    startTime = Instant.now();
    Response r = rc.doPost(null, batchData);
    if(r.getStatus() != 200){
      throw new IllegalAccessException(r.getStatusInfo().toString());
    }
    endLoadTime = Instant.now();

    caller.log("Batch " + batchIndex + ": Data posted in " + getLoadDurationMillis() + "ms, waiting for container ready...");
    while(!caller.isContainerReady()){
      Thread.sleep(500);
    }
    doneTime = Instant.now();
    caller.log("Batch " + batchIndex + ": Data processing by container done in " + getBatchDurationMillis() + "ms");
    done = true;
  }

  int size(){
    return batchData.size();
  }

  double getOpm(){
    if(!done) return -1.0;
    return batchData.size() / ( (doneTime.toEpochMilli() - startTime.toEpochMilli()) /1000 /60);
  }

  long getLoadDurationMillis(){
    if(!done) return -1;
    else return endLoadTime.toEpochMilli() - startTime.toEpochMilli();
  }

  long getBatchDurationMillis(){
    if(!done) return -1;
    else return doneTime.toEpochMilli() - startTime.toEpochMilli();
  }

  long getProcessingDurationMillis(){
    if(!done) return -1;
    else return doneTime.toEpochMilli() - endLoadTime.toEpochMilli();
  }










}