package at.franzreischl.dke.jsoninjector;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.JSONArray;

import java.io.*;

public class JsonInjectorView {

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
  private Label remainLblBatch;

  @FXML
  private Label curBatchTimeLbl;

  @FXML
  private Label curBatchOPMLbl;

  @FXML
  private ProgressBar progrBarTotal;

  @FXML
  private Label lastBatchOPMLbl;

  @FXML
  private Label totalOPMLbl;

  @FXML
  private Label objectsDoneLblTotal;

  @FXML
  private Spinner<Integer> nextBatchTargetField;

  @FXML
  private Label nextBatchObjCntLabel;

  @FXML
  private TextField restUrlField;

  @FXML
  private Pane batchProcessingWaitPane;

  public JsonInjectorView() {
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
  }

  public JsonInjectorModel getModel() {
    return model;
  }

  public void setModel(JsonInjectorModel model) {
    this.model = model;
  }

  @FXML
  private void enterContainerUrl(ActionEvent actionEvent) {
    restUrlBtn.setDisable(true);
    restUrlField.setDisable(true);
    model.setContainerUrl(restUrlField.getText());
    if(model.isContainerReady()){
      chkRestUrl.setSelected(true);
    }else{
      restUrlBtn.setDisable(false);
      restUrlField.setDisable(false);
    }
    checkReqs();
    System.out.println(restUrlField.getText());
  }

  @FXML
  private void openJsonFile(ActionEvent actionEvent) throws IOException {
    FileChooser fc = new FileChooser();
    fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(".json","*.json"));
    File jsonFile = fc.showOpenDialog(new Stage());
    if(jsonFile != null){
      FileReader fr = new FileReader(jsonFile);
      BufferedReader br = new BufferedReader(fr);

      String s = br.lines().reduce(String::concat).orElse("[]");
      System.out.println(s);

      model.setJsonData(new JSONArray(s));

      this.remainLblTotal.setText(model.getJsonData().length() + " JSON objects remaining");
      this.chkJsonFile.setSelected(true);
      this.filePickerButton.setDisable(true);

      System.out.println(jsonFile.getAbsolutePath());
      System.out.println(jsonFile.getCanonicalPath());

      fileNameLabel.setText(jsonFile.getAbsolutePath());
    }
    checkReqs();
  }


  @FXML
  void setNextBatchMinutes(InputMethodEvent event) {

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

}
