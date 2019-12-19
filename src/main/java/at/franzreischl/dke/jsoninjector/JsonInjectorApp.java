package at.franzreischl.dke.jsoninjector;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class JsonInjectorApp extends Application implements Runnable{

  private Scene scene;
  private Stage stage;
  private JsonInjectorModel model;


  @Override
  public void run() {
    launch();
  }


  @Override
  public void start(Stage stage) throws IOException {

    this.stage = stage;
    model = new JsonInjectorModel();
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        System.out.println("Running Shutdown Hook");
        model.close();
      }
    });


    String javaVersion = System.getProperty("java.version");
    String javafxVersion = System.getProperty("javafx.version");
    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(this.getClass().getResource("JsonInjectorUi.fxml"));
    //scene = new Scene(loadFXML("JsonInjectorUi"));
    Parent root = loader.load();
    JsonInjectorUi controller = loader.getController();
    controller.setModel(model);

    stage.setTitle("JSON Injector");

    scene = new Scene(root, 1200, 800);
    stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
      @Override public void handle(WindowEvent t) {
        System.out.println("CLOSING");
        model.close();
        //Runtime.getRuntime().runFinalization();
        Runtime.getRuntime().halt(0);
      }
    });
    stage.setScene(scene);
    stage.show();
  }

   void setRoot(String fxml) throws IOException {
    scene.setRoot(loadFXML(fxml));
  }

  public static Parent loadFXML(String fxml) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(JsonInjectorUi.class.getResource(fxml + ".fxml"));
    return fxmlLoader.load();
  }

  public static void main(String[] args) throws Exception {
    launch();
  }

}
