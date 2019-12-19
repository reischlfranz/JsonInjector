module at.franzreischl.dke.jsoninjector {
    requires javafx.fxml;
    requires javafx.graphics;
    requires jersey.client;
    requires java.ws.rs;
    requires java.desktop;
    requires org.json;

    /* For the missing org.glassfish.jersey.ExtendedConfig */
    requires jersey.common;
    requires javafx.controls;

    opens at.franzreischl.dke.jsoninjector to javafx.fxml;
    exports at.franzreischl.dke.jsoninjector;
}