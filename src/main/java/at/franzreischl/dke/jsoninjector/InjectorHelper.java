package at.franzreischl.dke.jsoninjector;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;

import java.util.List;
import java.util.spi.CurrencyNameProvider;

public class InjectorHelper implements Runnable {
  private static InjectorHelper instance;
  SimpleBooleanProperty isRunningProperty = new SimpleBooleanProperty(false);
  SimpleLongProperty nextObjectIndexProperty = new SimpleLongProperty(0);

  private JsonInjectorModel model;
  private long batchIndex = 0;
  private BatchDataInjector lastBatch, nextBatch, currentBatch;

  public static InjectorHelper getInstance() {
    if(instance == null) instance = new InjectorHelper();
    return instance;
  }

  public void setNextObject(long nextObject) throws IndexOutOfBoundsException {
    if(nextObject < nextObjectIndexProperty.getValue()){
      nextObjectIndexProperty.notify();
      throw new IndexOutOfBoundsException("Only larger values than previous are allowed!");
    }
    nextObjectIndexProperty.set(nextObject);
  }

  public long getNextObject() {
    return nextObjectIndexProperty.getValue();
  }





  private InjectorHelper(){

  }


  public void setModel(JsonInjectorModel model) {
    this.model = model;
  }

  @Override
  public void run() {
    if(nextObjectIndexProperty.getValue()<0) return;
    isRunningProperty.set(true);
    while(isRunningProperty.getValue()){
      model.log("Data injection ongoing...");

      currentBatch = nextBatch;
      nextBatch = null;


      prepareNextBatch();

      if(currentBatch == null && nextBatch != null) continue;
      if(currentBatch == null && nextBatch == null) {
        isRunningProperty.set(false);
        break;
      }



      Platform.runLater(()->model.getController().currentBatchIndexProperty.set(currentBatch.getBatchIndex()));
      Platform.runLater(()->model.getController().currentBatchSizeProperty.set(currentBatch.size()));
      Platform.runLater(()->model.getController().currentBatchIsLoadingProperty.set(true));
      Platform.runLater(()->model.getController().currentBatchIsProcessingProperty.set(false));

      try {
        currentBatch.startInject();
      } catch (IllegalAccessException | InterruptedException e) {
        e.printStackTrace();
        nextBatch = currentBatch;
        currentBatch = null;
        isRunningProperty.set(false);
        model.log("ERROR occured during batch "+batchIndex);
        model.log(e.getMessage());
        return;
      }
      model.batches.add(currentBatch);

      // bind properties for current batch
      Platform.runLater(()->model.getController().currentBatchIsLoadingProperty.set(true));
      Platform.runLater(()->model.getController().currentBatchIsProcessingProperty.set(true));

      Platform.runLater(()->model.getController().opmLastBatchProperty.set(model.getLastBatchOpm()));
      Platform.runLater(()->model.getController().opmTotalProperty.set(model.getTotalOpm()));
      Platform.runLater(()->model.getController().remainingObjectsProperty.set(
              (nextObjectIndexProperty.getValue() < 0)? 0 :
              model.dataList.size() - nextObjectIndexProperty.getValue()));
      Platform.runLater(()->model.getController().totalObjectsDoneProperty.set(model.getDoneObjects()));
      Platform.runLater(()->model.getController().avgBatchSizeProperty.set(model.getAvgBatchSize()));


      double progress;
      if(model.dataList.size() == 0) progress = -1.0;
      else progress = (double) (model.getDoneObjects()) / model.dataList.size();
      model.log("Progress: "+progress);
      Platform.runLater(()->model.getController().totalProgressProperty.set(progress));


      lastBatch = currentBatch;
      currentBatch = null;

    }
    model.log("All done: " + BatchDataInjector.batchIndexCounter + " Batches done!" );



  }

  public void stop() {
    isRunningProperty.set(false);
  }

  void prepareNextBatch(){
    if(nextObjectIndexProperty.getValue() >= model.dataList.size()) throw new IndexOutOfBoundsException("Index bigger than object list!");

    boolean isLastBatch = false;
    long nextBatchSize;
    BatchDataInjector sizeRefBatch = (currentBatch==null) ? lastBatch:currentBatch;

    if(nextObjectIndexProperty.getValue() < 0) {
      nextBatch = null;
      Platform.runLater(()->model.getController().nextBatchIndexProperty.set(-1));
      Platform.runLater(()->model.getController().nextBatchSizeProperty.set(-1));
      return;
    }

    if (nextObjectIndexProperty.getValue() == 0 || lastBatch == null) {
      nextBatchSize = 1;
    }else if(model.targetMinutesPerBatch * lastBatch.getOpm() < sizeRefBatch.size()
             || Math.abs( model.targetMinutesPerBatch * lastBatch.getOpm() - sizeRefBatch.size()) < 2){
      nextBatchSize = (long) (model.targetMinutesPerBatch * lastBatch.getOpm());
    }else{
      nextBatchSize = (long) (( model.targetMinutesPerBatch * lastBatch.getOpm() - sizeRefBatch.size()) *  0.60
                              +  model.targetMinutesPerBatch * lastBatch.getOpm());
    }

    // check if this is the last batch - no more objects remaining after that
    if(nextObjectIndexProperty.getValue() + nextBatchSize > model.dataList.size() -1 ){
      nextBatchSize = model.dataList.size() - nextObjectIndexProperty.getValue();
      isLastBatch = true;
    }

    List<Object> nextBatchData = model.dataList.subList(nextObjectIndexProperty.intValue(), (int)(nextObjectIndexProperty.intValue() + nextBatchSize));


    nextBatch = new BatchDataInjector(model.dataInputClient, nextBatchData, model);
    nextObjectIndexProperty.set(nextObjectIndexProperty.getValue() + nextBatchSize);

    model.log("Next batch prepared, size: " + nextBatch.size());
    if(isLastBatch){
      nextObjectIndexProperty.set(-1);
    }

    // bind properties for next batch
    Platform.runLater(()->model.getController().nextBatchIndexProperty.set(nextBatch.getBatchIndex()));
    Platform.runLater(()->model.getController().nextBatchSizeProperty.set(nextBatch.size()));


  }

}
