package com.example.projetolfa;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import java.util.ArrayList;
import java.util.List;

public class grController {

    @FXML
    private TextArea campoGramatica;

    @FXML
    private TextArea campoPalavras;

    @FXML
    private TextArea resultado;

    private static class Regra {
        private final String naoTerminal;
        private final List<String> producoes;

        private Regra(String naoTerminal, List<String> producoes) {
            this.naoTerminal = naoTerminal;
            this.producoes = producoes;
        }
    }

    private static class Gramatica {
        private final List<Regra> regras = new ArrayList<>();

        private void adicionarOuSubstituir(String naoTerminal, List<String> producoes) {
            boolean encontrada = false;
            for (int i = 0; i < regras.size(); i++) {
                if (regras.get(i).naoTerminal.equals(naoTerminal)) {
                    regras.set(i, new Regra(naoTerminal, producoes));
                    encontrada = true;
                    break;
                }
            }

            if (!encontrada) {
                regras.add(new Regra(naoTerminal, producoes));
            }
        }

        private Regra buscar(String naoTerminal) {
            Regra encontrada = null;
            boolean procurando = true;
            for (Regra regra : regras) {
                if (procurando && regra.naoTerminal.equals(naoTerminal)) {
                    encontrada = regra;
                    procurando = false;
                }
            }
            return encontrada;
        }

        private boolean estaVazia() {
            return regras.isEmpty();
        }
    }

    /**
     * Lê a gramática e preserva manualmente a ordem das regras.
     */
    private Gramatica parseGramatica(String texto) {
        Gramatica gramatica = new Gramatica();
        String[] linhas = texto.split("\n");
        boolean formatoValido = true;

        for (String linha : linhas) {
            linha = linha.trim();

            if (!linha.isEmpty() && formatoValido) {
                if (!linha.contains("->")) {
                    formatoValido = false;
                } else {
                    String[] partes = linha.split("->");
                    if (partes.length != 2) {
                        formatoValido = false;
                    } else {
                        String naoTerminal = partes[0].trim();
                        List<String> listaProducoes = new ArrayList<>();
                        String producoesStr = partes[1].trim();

                        int inicio = 0;
                        for (int i = 0; i < producoesStr.length(); i++) {
                            if (producoesStr.charAt(i) == '|') {
                                listaProducoes.add(producoesStr.substring(inicio, i).trim());
                                inicio = i + 1;
                            }
                        }
                        listaProducoes.add(producoesStr.substring(inicio).trim());
                        gramatica.adicionarOuSubstituir(naoTerminal, listaProducoes);
                    }
                }
            }
        }

        return formatoValido ? gramatica : null;
    }

    /**
     * Verifica se todas as regras são lineares à direita.
     */
    private boolean isGLD(Gramatica gramatica) {
        boolean valido = true;

        for (Regra regra : gramatica.regras) {
            String nt = regra.naoTerminal;

            if (valido) {
                if (nt.length() != 1 || nt.charAt(0) < 'A' || nt.charAt(0) > 'Z') {
                    valido = false;
                } else {
                    for (String prod : regra.producoes) {
                        if (valido && !prod.equals("#")) {
                            if (prod.length() == 1) {
                                char c = prod.charAt(0);
                                if (c < 'a' || c > 'z') {
                                    valido = false;
                                }
                            } else if (prod.length() == 2) {
                                char t = prod.charAt(0);
                                char n = prod.charAt(1);
                                if (t < 'a' || t > 'z' || n < 'A' || n > 'Z') {
                                    valido = false;
                                }
                            } else {
                                valido = false;
                            }
                        }
                    }
                }
            }
        }

        return valido;
    }

    /**
     * Verifica recursivamente se a palavra é gerada pela gramática.
     */
    private boolean deriva(String palavra, int i, String ntAtual, Gramatica gramatica) {
        Regra regra = gramatica.buscar(ntAtual);
        if (regra == null) {
            return false;
        }

        List<String> producoes = regra.producoes;

        if (i == palavra.length()) {
            boolean aceita = false;
            for (String prod : producoes) {
                if (!aceita && prod.equals("#")) {
                    aceita = true;
                }
            }
            return aceita;
        }

        char charAtual = palavra.charAt(i);
        boolean aceita = false;

        for (String prod : producoes) {
            if (!aceita && !prod.equals("#")) {
                if (prod.length() == 1) {
                    if (prod.charAt(0) == charAtual && i == palavra.length() - 1) {
                        aceita = true;
                    }
                } else if (prod.length() == 2 && prod.charAt(0) == charAtual) {
                    String proximoNt = String.valueOf(prod.charAt(1));
                    if (deriva(palavra, i + 1, proximoNt, gramatica)) {
                        aceita = true;
                    }
                }
            }
        }

        return aceita;
    }

    @FXML
    private void testarGramatica() {
        resultado.clear();

        String textoGramatica = campoGramatica.getText();
        if (textoGramatica == null || textoGramatica.trim().isEmpty()) {
            mostrarAlerta("Erro de Entrada", "A área da gramática está vazia. Insira a gramática.");
            return;
        }

        Gramatica gramatica = parseGramatica(textoGramatica);
        if (gramatica == null || gramatica.estaVazia()) {
            mostrarAlerta("Erro de Sintaxe", "Formato de gramática inválido. Siga o formato: S -> aA | b | #");
            return;
        }

        if (!isGLD(gramatica)) {
            mostrarAlerta("Erro de Validação", "A gramática informada NÃO é uma Gramática Linear à Direita (GLD) válida.\nCertifique-se de que do lado esquerdo haja apenas uma letra maiúscula e do lado direito haja apenas produções do tipo 'a', 'aA' ou '#'.");
            return;
        }

        String textoPalavras = campoPalavras.getText();
        if (textoPalavras == null || textoPalavras.trim().isEmpty()) {
            mostrarAlerta("Aviso", "A área de palavras está vazia. Insira palavras para testar.");
            return;
        }

        String simboloInicial = gramatica.regras.get(0).naoTerminal;
        String[] palavras = textoPalavras.split("\n");
        StringBuilder sbResultado = new StringBuilder();

        for (String palavraRaw : palavras) {
            String palavra = palavraRaw.trim();
            if (!palavra.isEmpty()) {
                if (palavra.equals("#")) {
                    palavra = "";
                }

                boolean aceita = deriva(palavra, 0, simboloInicial, gramatica);
                if (aceita) {
                    sbResultado.append(palavraRaw.trim()).append("\t\t-\t\tACEITA\n");
                } else {
                    sbResultado.append(palavraRaw.trim()).append("\t\t-\t\tREJEITA\n");
                }
            }
        }

        resultado.setText(sbResultado.toString());
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    @FXML
    private void limparCampos() {
        campoGramatica.clear();
        campoPalavras.clear();
        resultado.clear();
    }
}
