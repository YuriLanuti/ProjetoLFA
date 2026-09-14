package com.example.projetolfa;

import javafx.scene.Group;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa uma transição do autômato, ligando um estado de origem a um estado
 * de destino através de um ou mais símbolos (o símbolo "&" representa o vazio/epsilon).
 */
public class Transicao {

    public static final String EPSILON = "&";

    private final Estado origem;
    private final Estado destino;
    private final List<String> simbolos = new ArrayList<>();
    private final Group visual; // Linha/curva + seta + rótulo desenhados no canvas

    public Transicao(Estado origem, Estado destino, Group visual) {
        this.origem = origem;
        this.destino = destino;
        this.visual = visual;
    }

    public Estado getOrigem() {
        return origem;
    }

    public Estado getDestino() {
        return destino;
    }

    public List<String> getSimbolos() {
        return simbolos;
    }

    public Group getVisual() {
        return visual;
    }

    public String getSimbolosTexto() {
        String texto = "";

        for (int i = 0; i < simbolos.size(); i++) {
            if (i > 0) {
                texto = texto + ",";
            }
            texto = texto + simbolos.get(i);
        }

        return texto;
    }

    public boolean isLaco() {
        if (origem == destino) {
            return true;
        } else {
            return false;
        }
    }
}
