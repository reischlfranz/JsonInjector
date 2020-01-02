package at.franzreischl.dke.jsoninjector;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.JSONArray;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class JsonInjectorController {

  private JsonInjectorModel model;

  @FXML
  private AnchorPane mainPane;

  @FXML
  private CheckBox chkRestUrl;

  @FXML
  private Button restUrlBtn;

  @FXML
  private CheckBox chkJsonFile;

  @FXML
  private Label fileNameLabel;

  @FXML
  private Button filePickerButton;

  @FXML
  private Label remainLblTotal;

  @FXML
  private Button startBtn;

  @FXML
  private Button pauseBtn;

  @FXML
  private ProgressBar progrBarBatch;

  @FXML
  private Label totalLblBatch;

  @FXML
  private Label curBatchTimeLbl;

  @FXML
  private Label curBatchOPMLbl;

  @FXML
  private Pane batchProcessingWaitPane;

  @FXML
  private ProgressIndicator batchProcessingProgr;

  @FXML
  private Label remainLblBatch;

  @FXML
  private Pane batchLoadingWaitPane;

  @FXML
  private ProgressIndicator batchLoadingProgr;

  @FXML
  private Label curBatchIndexLbl;

  @FXML
  private ProgressBar progrBarTotal;

  @FXML
  private Label lastBatchOPMLbl;

  @FXML
  private Label totalOPMLbl;

  @FXML
  private Label objectsDoneLblTotal;

  @FXML
  private Label objectsLblTotal;

  @FXML
  private Label avgBatchSizeLbl;

  @FXML
  private Spinner<Integer> nextBatchTargetField;

  @FXML
  private Label nextBatchObjCntLabel;

  @FXML
  private Label nextBatchIndexLbl;

  @FXML
  private TextField restUrlField;


  // Properties for View Controller
  // Remaining Objects pane
  SimpleLongProperty remainingObjectsProperty = new SimpleLongProperty(0);
  SimpleLongProperty totalObjectsProperty = new SimpleLongProperty(0);

  // Next batch pane
  SimpleLongProperty nextBatchIndexProperty = new SimpleLongProperty(-1);
  SimpleLongProperty nextBatchSizeProperty = new SimpleLongProperty(0);

  // Current batch pane
  SimpleLongProperty currentBatchSizeProperty = new SimpleLongProperty(0);
  SimpleLongProperty currentBatchRemainProperty = new SimpleLongProperty(0);
  SimpleLongProperty currentBatchIndexProperty = new SimpleLongProperty(-1);
  SimpleDoubleProperty currentBatchProcessingProgressProperty = new SimpleDoubleProperty(0.25);
  SimpleDoubleProperty currentBatchLoadingProgressProperty = new SimpleDoubleProperty(0.25);
  SimpleBooleanProperty currentBatchIsLoadingProperty = new SimpleBooleanProperty(false);
  SimpleBooleanProperty currentBatchIsProcessingProperty = new SimpleBooleanProperty(false);

  // Objects done pane
  SimpleDoubleProperty opmLastBatchProperty = new SimpleDoubleProperty(0.0);
  SimpleDoubleProperty opmTotalProperty = new SimpleDoubleProperty(0.0);
  SimpleDoubleProperty totalProgressProperty = new SimpleDoubleProperty(-1.0);
  SimpleLongProperty totalObjectsDoneProperty = new SimpleLongProperty(0);
  SimpleDoubleProperty avgBatchSizeProperty = new SimpleDoubleProperty(0.0);



  public JsonInjectorController() {
  }

  public void initialize(){
    ObservableList<String> ss = mainPane.getStylesheets();
    String s1 = ss.get(0);
    System.out.println(s1);
    mainPane.getChildren().stream()
            .filter( x -> x.getStyleClass().contains("mainContentPane"))
            .forEach( x -> {
                x.getStyleClass().removeAll("isVisible");
                x.getStyleClass().add("isHidden");
           });
    batchProcessingWaitPane.getStyleClass().add("isHidden");

    // Change target minutes...?
    nextBatchTargetField.valueProperty().addListener((obs,oldVal,newVal)->{
      model.setTargetMinutesPerBatch(newVal);
    });
  }

  public JsonInjectorModel getModel() {
    return model;
  }

  public void setModel(JsonInjectorModel model) {
    this.model = model;
    model.setController(this);
    // Bind value properties for UI
    // Objects remaining
    remainLblTotal.textProperty()               .bind(remainingObjectsProperty.asString() );
    remainLblBatch.textProperty()               .bind(currentBatchRemainProperty.asString() );

    // Next batch
    nextBatchObjCntLabel.textProperty()         .bind(nextBatchSizeProperty.asString());
    nextBatchIndexLbl.textProperty()            .bind(nextBatchIndexProperty.asString());

    // Current Batch
    totalLblBatch.textProperty()                .bind(currentBatchSizeProperty.asString() );
    batchProcessingWaitPane.visibleProperty()   .bind(currentBatchIsProcessingProperty);
    batchLoadingWaitPane.visibleProperty()      .bind(currentBatchIsLoadingProperty);
    curBatchIndexLbl.textProperty()             .bind(currentBatchIndexProperty.asString());

    batchLoadingProgr.progressProperty()        .bind(currentBatchLoadingProgressProperty);
    batchProcessingProgr.progressProperty()     .bind(currentBatchProcessingProgressProperty);

    // Objects done
    objectsLblTotal.textProperty()              .bind(totalObjectsProperty.asString() );
    objectsDoneLblTotal.textProperty()          .bind(totalObjectsDoneProperty.asString());
    progrBarTotal.progressProperty()            .bind(totalProgressProperty);
    lastBatchOPMLbl.textProperty()              .bind(opmLastBatchProperty.asString("%.2f"));
    totalOPMLbl.textProperty()                  .bind(opmTotalProperty.asString("%.2f"));
    avgBatchSizeLbl.textProperty()              .bind(avgBatchSizeProperty.asString("%.2f"));


    // set initial model values
    model.setTargetMinutesPerBatch( nextBatchTargetField.getValue());

  }

  @FXML
  private void enterContainerUrl(ActionEvent actionEvent) {
    setContainerUrl(restUrlField.getText());
  }

  @FXML
  private void openJsonFileButton(ActionEvent actionEvent) throws IOException {
    FileChooser fc = new FileChooser();
    fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(".json","*.json"));
    File jsonFile = fc.showOpenDialog(new Stage());
    if(jsonFile != null) {
      openJsonFile(jsonFile);
    }
  }

  private void openJsonFile(File jsonFile) throws IOException{
    model.log("Opening JSON file "+ jsonFile.getAbsolutePath());
    FileReader fr = new FileReader(jsonFile);
    BufferedReader br = new BufferedReader(fr);
    StringBuilder sb = new StringBuilder();


    FileInputStream inputStream = null;
    Scanner sc = null;


    try {
      inputStream = new FileInputStream(jsonFile);
      sc = new Scanner(inputStream, "UTF-8");
      while (sc.hasNextLine()) {
        String line = sc.nextLine();
        sb.append(line);
      }
      // note that Scanner suppresses exceptions
      if (sc.ioException() != null) {
        throw sc.ioException();
      }
    } finally {
      if (inputStream != null) {
        inputStream.close();
      }
      if (sc != null) {
        sc.close();
      }
    }
    //  System.out.println(s);
    fileNameLabel.setText(jsonFile.getAbsolutePath());
    model.setDataList(new JSONArray(sb.toString()).toList());
    sb = null;
    br.close();
    fr.close();

    this.chkJsonFile.setSelected(true);
    this.filePickerButton.setDisable(true);
    checkReqs();

  }

  private void setContainerUrl(String url) {
    restUrlBtn.setDisable(true);
    restUrlField.setDisable(true);
    restUrlField.setText(url);
    model.setContainerUrl(url);
    if(model.isContainerReady()){
      chkRestUrl.setSelected(true);
    }else{
      restUrlBtn.setDisable(false);
      restUrlField.setDisable(false);
    }
    checkReqs();
  }

  private void checkReqs(){
    if(chkRestUrl.isSelected() && chkJsonFile.isSelected()){
      mainPane.getChildren().stream()
              .filter( x -> x.getStyleClass().contains("mainContentPane"))
              .forEach( x -> {
                x.getStyleClass().removeAll("isHidden");
                x.getStyleClass().add("isVisible");
              });
    }

  }

  @FXML
  private void startInjection(){
    startBtn.setDisable(true);
    model.log("Start button pressed!");
    model.startInjection();
  }



}
