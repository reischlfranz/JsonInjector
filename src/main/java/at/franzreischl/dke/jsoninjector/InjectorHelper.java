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

  private JsonInjectorModel model;
  private boolean isRunning = false;
  private long nextObject = 0, batchIndex = 0;
  private BatchDataInjector lastBatch, nextBatch, currentBatch;

  public static InjectorHelper getInstance() {
    if(instance == null) instance = new InjectorHelper();
    return instance;
  }


  private InjectorHelper(){

  }


  public void setModel(JsonInjectorModel model) {
    this.model = model;
  }

  @Override
  public void run() {
    if(nextObject<0) return;
    isRunning = true;
    while(isRunning){
      model.log("Data injection ongoing...");

      currentBatch = nextBatch;
      nextBatch = null;

      // bind properties for current batch
      prepareNextBatch();

      if(currentBatch == null && nextBatch != null) continue;
      if(currentBatch == null && nextBatch == null) {
        isRunning = false;
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
        isRunning = false;
        return;
      }
      Platform.runLater(()->model.getController().currentBatchIsLoadingProperty.set(true));
      Platform.runLater(()->model.getController().currentBatchIsProcessingProperty.set(true));

      Platform.runLater(()->model.getController().remainingObjectsProperty.set(
              (nextObject<0)?0:model.dataList.size() - nextObject-1));


      lastBatch = currentBatch;
      currentBatch = null;

    }
    model.log("All done: " + BatchDataInjector.batchIndexCounter + " Batches done!" );



  }

  public void stop() {
    isRunning = false;
  }

  void prepareNextBatch(){
    if(nextObject >= model.dataList.size()) throw new IndexOutOfBoundsException("Index bigger than object list!");

    boolean isLastBatch = false;
    long nextBatchSize;
    BatchDataInjector sizeRefBatch = (currentBatch==null) ? lastBatch:currentBatch;

    if(nextObject < 0) {
      nextBatch = null;
      Platform.runLater(()->model.getController().nextBatchIndexProperty.set(-1));
      Platform.runLater(()->model.getController().nextBatchSizeProperty.set(-1));
      return;
    }

    if (nextObject == 0 || lastBatch == null) {
      nextBatchSize = 1;
    }else if(model.targetMinutesPerBatch * lastBatch.getOpm() < sizeRefBatch.size()
             || Math.abs( model.targetMinutesPerBatch * lastBatch.getOpm() - sizeRefBatch.size()) < 2){
      nextBatchSize = (long) (model.targetMinutesPerBatch * lastBatch.getOpm());
    }else{
      nextBatchSize = (long) (( model.targetMinutesPerBatch * lastBatch.getOpm() - sizeRefBatch.size()) *  0.60
                              +  model.targetMinutesPerBatch * lastBatch.getOpm());
    }

    // check if this is the last batch - no more objects remaining after that
    if(nextObject + nextBatchSize > model.dataList.size() -1 ){
      nextBatchSize = model.dataList.size() - nextObject;
      isLastBatch = true;
    }

    List<Object> nextBatchData = model.dataList.subList((int)nextObject, (int)(nextObject + nextBatchSize));


    nextBatch = new BatchDataInjector(model.dataInputClient, nextBatchData, model);
    nextObject += nextBatchSize;

    model.log("Next batch prepared, size: " + nextBatch.size());
    if(isLastBatch){
      nextObject = -1;
    }

    // bind properties for next batch
    Platform.runLater(()->model.getController().nextBatchIndexProperty.set(nextBatch.getBatchIndex()));
    Platform.runLater(()->model.getController().nextBatchSizeProperty.set(nextBatch.size()));


  }

}
