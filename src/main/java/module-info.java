module net.javaguids.lost_and_found {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.sql;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    opens net.javaguids.lost_and_found to javafx.fxml;
    opens net.javaguids.lost_and_found.model.users to org.junit.platform.commons;
    opens net.javaguids.lost_and_found.model.enums to org.junit.platform.commons;
    opens net.javaguids.lost_and_found.database to org.junit.platform.commons;
    opens net.javaguids.lost_and_found.exceptions to org.junit.platform.commons;

    exports net.javaguids.lost_and_found.controllers to javafx.fxml;
    opens net.javaguids.lost_and_found.controllers to javafx.fxml;
    
    exports net.javaguids.lost_and_found;
    exports net.javaguids.lost_and_found.model.users;
    exports net.javaguids.lost_and_found.model.enums;
    exports net.javaguids.lost_and_found.database;
    exports net.javaguids.lost_and_found.exceptions;
}