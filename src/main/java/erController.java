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
        String resultadoTexto;

        if (!expressao.isEmpty()) { // Se tiver algo escrito

            if (caracteresPermitidos(expressao)) { // Verifica se existe algum caractere fora dos utilizados

                if (validaEstrutura(expressao)) { // Verifica se pertence corretamente a expressão

                    resultadoTexto = testarPalavras(expressao, textoPalavras);
                    resultado.setText(resultadoTexto); // Exibe resultado

                }
                else
                    resultado.setText("A Estrutura da expressão é inválida.");

            }
            else
                resultado.setText("A expressão possui caracteres não permitidos.");
        }
        else
            resultado.setText("Digite uma expressão regular.");

    }

    private boolean caracteresPermitidos(String expressao) {

        boolean valido = true;
        char caractere;

        for (int i = 0; i < expressao.length() && valido; i++) {

            caractere = expressao.charAt(i);
            if (!((caractere >= 'a' && caractere <= 'z') || (caractere >= 'A' && caractere <= 'Z') || (caractere >= '0' && caractere <= '9')) && caractere != '*' && caractere != '+' && caractere != '|' && caractere != '.' && caractere != '(' && caractere != ')')
                valido = false;

        }

        return valido;
    }

    private boolean validaEstrutura(String expressao) {

        boolean valido = true;
        boolean operando, ultimoFoiOperando = false;
        int quantParenteses = 0;
        char caractere;

        for (int i = 0; i < expressao.length() && valido; i++) {

            caractere = expressao.charAt(i);
            operando = (caractere >= 'a' && caractere <= 'z') || (caractere >= 'A' && caractere <= 'Z') || (caractere >= '0' && caractere <= '9'); // é letra ou número

            if (operando) {

                if (ultimoFoiOperando)
                    valido = false;

                ultimoFoiOperando = true;
            }
            else if (caractere == '(') {

                if (ultimoFoiOperando)
                    valido = false;

                quantParenteses++;
                ultimoFoiOperando = false;
            }
            else if (caractere == ')') {

                if (!ultimoFoiOperando)
                    valido = false;

                quantParenteses--;
                if (quantParenteses < 0)
                    valido = false;

                ultimoFoiOperando = true;
            }
            else if (caractere == '+' || caractere == '|' || caractere == '.') {

                if (!ultimoFoiOperando)
                    valido = false;

                ultimoFoiOperando = false;
            }
            else if (caractere == '*') {

                if (!ultimoFoiOperando)
                    valido = false;
            }
        }

        if (!ultimoFoiOperando || quantParenteses != 0)
            valido = false;

        return valido;
    }

    private String testarPalavras(String expressao, String textoPalavras) {

        String palavra = "";
        String resultadoTexto = "";
        char caractere;

        expressao = expressao.replace(".", ""); // Retira os pontos
        expressao = expressao.replace("+", "|"); // Modifica os '+' para '|'

        textoPalavras += "\n"; // Acrescenta "\n" no final para critério de parada

        for (int i = 0; i < textoPalavras.length(); i++) {

            caractere = textoPalavras.charAt(i);

            if (caractere != '\n') // Se for caractere
                palavra += caractere;
            else {
                
                if (!palavra.isEmpty()) { // Se palavra não estiver vazia

                    if (palavra.matches(expressao))
                        resultadoTexto += palavra + "\t\t-\t\tACEITA\n";
                    else
                        resultadoTexto += palavra + "\t\t-\t\tREJEITA\n";
                }

                palavra = "";
            }
        }

        return resultadoTexto;
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