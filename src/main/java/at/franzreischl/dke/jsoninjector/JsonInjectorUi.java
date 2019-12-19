package at.franzreischl.dke.jsoninjector;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;

public class JsonInjectorUi {

  private JsonInjectorModel model;
  @FXML
  private CheckBox chkRestUrl;

  @FXML
  private Label fileNameLabel;

  @FXML
  private Button restUrlBtn;

  @FXML
  private CheckBox chkJsonFile;

  @FXML
  private TextField restUrlField;

  @FXML
  private Button filePickerButton;

  public JsonInjectorUi() {
  }

  public JsonInjectorModel getModel() {
    return model;
  }

  public void setModel(JsonInjectorModel model) {
    this.model = model;
  }

  @FXML
  private void enterContainerUrl(ActionEvent actionEvent) {
    //TODO
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

      System.out.println(jsonFile.getAbsolutePath());
      System.out.println(jsonFile.getCanonicalPath());

      fileNameLabel.setText(jsonFile.getAbsolutePath());
    }
    //TODO
  }


}
