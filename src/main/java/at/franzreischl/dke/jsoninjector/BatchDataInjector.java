package at.franzreischl.dke.jsoninjector;

import javafx.application.Platform;
import org.json.JSONArray;

import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.List;

public class BatchDataInjector {
  static int batchIndexCounter = 0;

  private RestClient rc;

  public int getBatchIndex() {
    return batchIndex;
  }

  private int batchIndex;
  private List<Object> batchData;
  private int index = 0;
  private Instant startTime;
  private Instant endLoadTime;
  private Instant doneTime;
  private boolean done=false;
  private JsonInjectorModel model;

  public BatchDataInjector(RestClient rc, List<Object> batchData, JsonInjectorModel model){
    if(batchData == null || rc == null || model == null) throw new IllegalArgumentException();
    this.rc = rc;
    this.batchData = batchData;
    this.model = model;
    batchIndex = batchIndexCounter++;
  }

  private boolean isDone(){
    return done;
  }

  public void startInject() throws IllegalAccessException, InterruptedException {
    if(done) return;

    Platform.runLater(()-> model.getController().currentBatchIsLoadingProperty.set(true));
    Platform.runLater(()-> model.getController().currentBatchIsProcessingProperty.set(false));
    Platform.runLater(()-> model.getController().currentBatchProcessingProgressProperty.set(-1.0));
    Platform.runLater(()-> model.getController().currentBatchLoadingProgressProperty.set(-1.0));


    model.log("Batch " + batchIndex + ": Injecting " + batchData.size() + " objects into container...");
    startTime = Instant.now();
    Response r = rc.doPostJsonObject(null, new JSONArray(batchData));
    if(r.getStatus() != 200){
      throw new IllegalAccessException(r.getStatusInfo().toString());
    }
    endLoadTime = Instant.now();
    Platform.runLater(()-> model.getController().currentBatchIsLoadingProperty.set(true));
    Platform.runLater(()-> model.getController().currentBatchIsProcessingProperty.set(true));
    Platform.runLater(()-> model.getController().currentBatchLoadingProgressProperty.set(1.0));
    Platform.runLater(()-> model.getController().currentBatchProcessingProgressProperty.set(-1.0));

    model.log("Batch " + batchIndex + ": Data posted in " + getLoadDurationMillis() + "ms, waiting for container ready...");
    while(!model.isContainerReady()){
      Thread.sleep(500);
    }
    doneTime = Instant.now();
    model.log("Batch " + batchIndex + ": Data processing by container done in " + getBatchDurationMillis() + "ms");
    Platform.runLater(()-> model.getController().currentBatchIsLoadingProperty.set(true));
    Platform.runLater(()-> model.getController().currentBatchIsProcessingProperty.set(true));
    Platform.runLater(()-> model.getController().currentBatchLoadingProgressProperty.set(1.0));
    Platform.runLater(()-> model.getController().currentBatchProcessingProgressProperty.set(1.0));

    updatePanes(false, false);
    done = true;

    model.log("Batch " + batchIndex + ": Objects per minute: " + getOpm() );
  }

  private void updatePanes(boolean loading, boolean processing) {
//    caller.currentBatchIsLoading.setValue(loading);
//    caller.currentBatchIsProcessing.setValue(processing);
//    try{
//      caller.currentBatchIsLoading.notifyAll();
//    }catch (IllegalMonitorStateException e){
//      System.out.println(e.getMessage());
//    }
//    try{
//      caller.currentBatchIsProcessing.notifyAll();
//    }catch (IllegalMonitorStateException e){}
  }


  double getOpm(){
    if(!done) return -1.0;

    long d = doneTime.toEpochMilli();
    long s = startTime.toEpochMilli();
    long diff = d - s;
    if (diff <1) diff = 1;
    return batchData.size() / ( ((double)diff /1000 /60));
  }

  long getLoadDurationMillis(){
    return endLoadTime.toEpochMilli() - startTime.toEpochMilli();
  }

  long getBatchDurationMillis(){
    return doneTime.toEpochMilli() - startTime.toEpochMilli();
  }

  long getProcessingDurationMillis(){
    return doneTime.toEpochMilli() - endLoadTime.toEpochMilli();
  }

  long size(){
    if(batchData == null) return -1;
    return batchData.size();
  }










}
