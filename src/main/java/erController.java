package com.example.projetolfa;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class erController {

    @FXML
    private TextField campoExpressao;

    @FXML
    private TextArea campoPalavras;

    @FXML
    private TextArea resultado;

    @FXML
    private void testarExpressao() {

        String expressao = campoExpressao.getText().trim();
        String textoPalavras = campoPalavras.getText();

        if (!expressao.isEmpty()) { // Se tiver algo escrito

            if (caracteresPermitidos(expressao)) {

                if (validaEstrutura(expressao)) {

                    testarPalavras(expressao, textoPalavras);

                } else {
                    resultado.setText("ERRO: Estrutura da expressão inválida.");
                }

            } else {
                resultado.setText(
                        "ERRO: A expressão possui caracteres não permitidos."
                );
            }

        } else {
            resultado.setText("Digite uma expressão regular.");
        }
    }

    private boolean caracteresPermitidos(String expressao) {

        boolean valido = true;
        char caractere;

        for (int i = 0; i < expressao.length(); i++) {

            caractere = expressao.charAt(i);
            if (!Character.isLetterOrDigit(caractere) && caractere != '*' && caractere != '+' && caractere != '|' && caractere != '.' && caractere != '(' && caractere != ')')
                valido = false;

        }

        return valido;
    }

    private boolean validaEstrutura(String expressao) {

        boolean valido = true;
        boolean esperaOperando = true;
        boolean operando;
        char caractere;
        int parenteses = 0;

        for (int i = 0; i < expressao.length(); i++) {

            caractere = expressao.charAt(i);

            // É letra ou número
            operando = (caractere >= 'a' && caractere <= 'z') || (caractere >= 'A' && caractere <= 'Z') || (caractere >= '0' && caractere <= '9');

            if (operando) { // Se for operando

                if (!esperaOperando)
                    valido = false;

                esperaOperando = false;
            }
            else if (caractere == '(') { // Abre parêntese

                if (!esperaOperando)
                    valido = false;

                parenteses++;
                esperaOperando = true;
            }
            else if (caractere == ')') {  // Fecha parêntese

                if (esperaOperando)
                    valido = false;

                parenteses--;
                if (parenteses < 0)
                    valido = false;

                esperaOperando = false;
            }
            else if (caractere == '+' || caractere == '|' || caractere == '.') { // União ou concatenação

                if (esperaOperando)
                    valido = false;

                esperaOperando = true;
            }
            else if (caractere == '*') { // Se tiver '*'  (Fecho de Klene)

                if (esperaOperando)
                    valido = false;

                esperaOperando = false;
            }
        }

        // A expressão não pode terminar esperando um operando
        if (esperaOperando) {
            valido = false;
        }

        // Os parênteses precisam estar equilibrados
        if (parenteses != 0) {
            valido = false;
        }

        return valido;
    }

    /*
    private boolean caracteresPermitidos(String expressao) {

        boolean valido = true;

        if (!expressao.matches("[a-zA-Z0-9+|.*()]+")) {
            valido = false;
        }

        return valido;
    }
    */

    @FXML
    private void limparCampos() {

        campoExpressao.clear();
        campoPalavras.clear();
        resultado.clear();
    }
}