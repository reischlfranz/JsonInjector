package at.franzreischl.dke.jsoninjector;

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
import java.util.ArrayList;
import java.util.List;

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
  private Label remainLblBatch;

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
  private Spinner<Integer> nextBatchTargetField;

  @FXML
  private Label nextBatchObjCntLabel;

  @FXML
  private TextField restUrlField;

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

    // Set the spinner field limits for batch time target.
    nextBatchTargetField.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1,9999,1));
    nextBatchTargetField.valueProperty().addListener((obs,oldVal,newVal)->{
            model.prepareNextBatch();
    });

  }

  public JsonInjectorModel getModel() {
    return model;
  }

  public void setModel(JsonInjectorModel model) {
    this.model = model;
    // Bind value properties for UI
    // Objects remaining
    remainLblTotal.textProperty()       .bind(model.remainingObjectsProperty.asString() );
    remainLblBatch.textProperty()       .bind(model.currentBatchRemainProperty.asString() );

    // Next batch
//    lastBatchOPMLbl.textProperty()      .bind(model.y.asString() );
//    totalOPMLbl.textProperty()          .bind(model.y.asString() );
//    objectsDoneLblTotal.textProperty()  .bind(model.y.asString() );
    objectsLblTotal.textProperty()      .bind(model.totalObjectsProperty.asString() );
    nextBatchObjCntLabel.textProperty() .bind(model.nextBatchSizeProperty.asString() );
    totalLblBatch.textProperty()        .bind(model.currentBatchSizeProperty.asString() );


    // Current Batch
    progrBarBatch.progressProperty()    .bind(model.currentBatchProgress);
//    curBatchTimeLbl.textProperty()      .bind(model.y.asString() );
//    curBatchOPMLbl.textProperty()       .bind(model.y.asString() );

    // set initial model values
    model.setTargetMinutesPerBatch(nextBatchTargetField.getValue());

    // DEBUG TODO remove these lines
    try {
      openJsonFile(Paths.get("C:\\Users\\Franzi\\OneDrive\\IT-Projekt Wirtschaftsinformatik\\WP2\\seismic.json").toFile());
      this.setContainerUrl("http://localhost:4000/api");
    } catch (IOException e) {       e.printStackTrace();     }
    // DEBUG END
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

    String s = br.lines().reduce(String::concat).orElse("[]");
    //  System.out.println(s);
    fileNameLabel.setText(jsonFile.getAbsolutePath());
    JSONArray ja = new JSONArray(s);
    List l = ja.toList();
    model.setDataList(new JSONArray(s).toList());

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


  @FXML
  void setNextBatchMinutes(InputMethodEvent event) {
    model.setTargetMinutesPerBatch(nextBatchTargetField.getValue());

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
    model.log("Start button pressed!");
    model.startInjection();
  }

}
