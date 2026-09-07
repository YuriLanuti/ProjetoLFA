module com.example.projetolfa {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.projetolfa to javafx.fxml;
    exports com.example.projetolfa;
}