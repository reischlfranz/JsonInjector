package at.franzreischl.dke.jsoninjector;

import javax.swing.*;
import java.awt.*;

public class JsonInjectorView extends JFrame implements Runnable {


  private JsonInjectorModel model;


  public JsonInjectorView(){
    this.setSize(720, 480);
    this.setMinimumSize(new Dimension(300,200));
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        System.out.println("Running Shutdown Hook");
        model.close();
      }
    });
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);

  }

  @Override
  public void run() {
    this.setVisible(true);
  }

  public void setModel(JsonInjectorModel jsonInjectorModel) {
    this.model = jsonInjectorModel;
  }

}
