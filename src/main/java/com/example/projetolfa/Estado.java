package com.example.projetolfa;

import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;

/**
 * Representa um estado do autômato finito, tanto os dados (nome, inicial, final)
 * quanto o desenho correspondente no canvas.
 */
public class Estado {

    public static final double RAIO = 30.0;

    private String nome;
    private boolean inicial;
    private boolean finalEstado;

    private final StackPane visual; // Círculo(s) + texto + marcador de inicial, tudo junto para mover em bloco
    private final Circle circuloPrincipal;
    private final Circle circuloFinal; // Círculo interno, só fica visível quando o estado é final
    private final Text rotulo;
    private final Polygon marcadorInicial; // Setinha que aponta para o estado quando ele é inicial

    public Estado(String nome, StackPane visual, Circle circuloPrincipal, Circle circuloFinal, Text rotulo, Polygon marcadorInicial) {
        this.nome = nome;
        this.visual = visual;
        this.circuloPrincipal = circuloPrincipal;
        this.circuloFinal = circuloFinal;
        this.rotulo = rotulo;
        this.marcadorInicial = marcadorInicial;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
        rotulo.setText(nome);
    }

    public boolean isInicial() {
        return inicial;
    }

    public void setInicial(boolean inicial) {
        this.inicial = inicial;

        if (inicial) {
            marcadorInicial.setVisible(true);
        } else {
            marcadorInicial.setVisible(false);
        }
    }

    public boolean isFinalEstado() {
        return finalEstado;
    }

    public void setFinalEstado(boolean finalEstado) {
        this.finalEstado = finalEstado;

        if (finalEstado) {
            circuloFinal.setVisible(true);
        } else {
            circuloFinal.setVisible(false);
        }
    }

    public StackPane getVisual() {
        return visual;
    }

    public Circle getCirculoPrincipal() {
        return circuloPrincipal;
    }

    public double getCentroX() {
        return visual.getLayoutX() + RAIO;
    }

    public double getCentroY() {
        return visual.getLayoutY() + RAIO;
    }

    @Override
    public String toString() {
        return nome;
    }
}
