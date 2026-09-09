package com.example.projetolfa;

import javafx.fxml.FXML;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.util.Optional;

public class afController {

    @FXML
    private Circle iconeNovoEstado; // Bolinha da barra lateral, usada só como "molde" para arrastar

    @FXML
    private Pane canvasDiagrama;

    private int quantidadeEstados = 0;

    private double offsetX; // Distância do clique até o canto do estado, usada para não "pular" ao arrastar
    private double offsetY;

    @FXML
    private void initialize() {

        iconeNovoEstado.setOnDragDetected(this::onIniciarArrasto);

        canvasDiagrama.setOnDragOver(this::onArrastarSobreCanvas);
        canvasDiagrama.setOnDragDropped(this::onSoltarNoCanvas);
    }

    private void onIniciarArrasto(MouseEvent evento) {

        Dragboard dragboard = iconeNovoEstado.startDragAndDrop(TransferMode.COPY);

        WritableImage imagemArrastada = iconeNovoEstado.snapshot(null, null);
        dragboard.setDragView(imagemArrastada, imagemArrastada.getWidth() / 2, imagemArrastada.getHeight() / 2);

        ClipboardContent conteudo = new ClipboardContent();
        conteudo.putString("novoEstado"); // Só um marcador para o dragboard aceitar o drop
        dragboard.setContent(conteudo);

        evento.consume();
    }

    private void onArrastarSobreCanvas(DragEvent evento) {

        if (evento.getDragboard().hasString()) {
            evento.acceptTransferModes(TransferMode.COPY);
        }
        evento.consume();
    }

    private void onSoltarNoCanvas(DragEvent evento) {

        if (evento.getDragboard().hasString()) {
            criarEstado(evento.getX(), evento.getY());
            evento.setDropCompleted(true);
        } else {
            evento.setDropCompleted(false);
        }
        evento.consume();
    }

    private void criarEstado(double x, double y) {

        TextInputDialog dialogo = new TextInputDialog("q" + quantidadeEstados);
        dialogo.setTitle("Novo Estado");
        dialogo.setHeaderText(null);
        dialogo.setContentText("Nome do estado:");

        Optional<String> resultado = dialogo.showAndWait();
        String nome = resultado.orElse("").trim();

        if (!nome.isEmpty()) { // Se tiver algo escrito

            Circle circulo = new Circle(30); // Desenha o círculo do estado
            circulo.setFill(Color.LIGHTGRAY);

            Text rotulo = new Text(nome); // Escreve o nome do estado dentro do círculo
            rotulo.setFill(Color.BLACK);

            StackPane estado = new StackPane(circulo, rotulo); // Agrupa o círculo e o texto num só desenho
            estado.setLayoutX(x - 30); // Centraliza o círculo no ponto onde foi solto
            estado.setLayoutY(y - 30);

            habilitarArrasto(estado); // Deixa o estado livre para ser reposicionado dentro do canvas

            canvasDiagrama.getChildren().add(estado);
            quantidadeEstados++;
        }
    }

    private void habilitarArrasto(StackPane estado) {

        estado.setOnMousePressed(this::onEstadoPressionado);
        estado.setOnMouseDragged(this::onEstadoArrastado);
    }

    private void onEstadoPressionado(MouseEvent evento) {

        StackPane estado = (StackPane) evento.getSource();
        offsetX = evento.getSceneX() - estado.getLayoutX();
        offsetY = evento.getSceneY() - estado.getLayoutY();

        estado.toFront(); // Traz o estado clicado para cima dos demais
    }

    private void onEstadoArrastado(MouseEvent evento) {

        StackPane estado = (StackPane) evento.getSource();
        double novoX = evento.getSceneX() - offsetX;
        double novoY = evento.getSceneY() - offsetY;

        // Mantém o estado dentro dos limites do canvas
        novoX = Math.max(0, Math.min(novoX, canvasDiagrama.getWidth() - estado.getBoundsInLocal().getWidth()));
        novoY = Math.max(0, Math.min(novoY, canvasDiagrama.getHeight() - estado.getBoundsInLocal().getHeight()));

        estado.setLayoutX(novoX);
        estado.setLayoutY(novoY);
    }

    @FXML
    private void limparDiagrama() {

        canvasDiagrama.getChildren().clear();
        quantidadeEstados = 0;
    }
}
