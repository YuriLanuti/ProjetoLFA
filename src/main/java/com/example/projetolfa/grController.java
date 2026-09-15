package com.example.projetolfa;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class grController {

    @FXML
    private TextArea campoGramatica;

    @FXML
    private TextArea campoPalavras;

    @FXML
    private TextArea resultado;

    /**
     * 1. Estrutura de Dados e Parser
     * Lê a gramática do texto de entrada e a converte para um HashMap.
     * Usamos LinkedHashMap para garantir que o primeiro Não-Terminal seja o inicial.
     */
    private LinkedHashMap<String, List<String>> parseGramatica(String texto) {
        LinkedHashMap<String, List<String>> gramatica = new LinkedHashMap<>();
        String[] linhas = texto.split("\n");
        
        for (String linha : linhas) {
            linha = linha.trim();
            if (linha.isEmpty()) continue;
            
            // Verifica se a linha contém o formato esperado com '->'
            if (!linha.contains("->")) {
                return null; // Erro de formatação
            }
            
            // Divide entre o Não-Terminal (esquerda) e as produções (direita)
            String[] partes = linha.split("->");
            if (partes.length != 2) return null;
            
            String naoTerminal = partes[0].trim();
            
            // Divide as produções usando o separador '|'
            // NOTA: Em split sem regex usamos "\\|" ou escape, 
            // mas implementando 'na unha' usaremos a quebra da string para ser seguro sem regex
            List<String> listaProducoes = new ArrayList<>();
            String producoesStr = partes[1].trim();
            
            int start = 0;
            for (int i = 0; i < producoesStr.length(); i++) {
                if (producoesStr.charAt(i) == '|') {
                    listaProducoes.add(producoesStr.substring(start, i).trim());
                    start = i + 1;
                }
            }
            // Adiciona a última parte
            listaProducoes.add(producoesStr.substring(start).trim());
            
            gramatica.put(naoTerminal, listaProducoes);
        }
        return gramatica;
    }

    /**
     * 2. Validador Sintático (GLD)
     * Verifica se todas as regras da gramática são lineares à direita.
     */
    private boolean isGLD(HashMap<String, List<String>> gramatica) {
        for (Map.Entry<String, List<String>> entry : gramatica.entrySet()) {
            String nt = entry.getKey();
            
            // Regra Básica: O lado esquerdo deve ser estritamente UM Não-Terminal (letra maiúscula)
            if (nt.length() != 1 || nt.charAt(0) < 'A' || nt.charAt(0) > 'Z') {
                return false;
            }
            
            for (String prod : entry.getValue()) {
                if (prod.equals("#")) {
                    continue; // Palavra vazia é permitida
                }
                
                if (prod.length() == 1) {
                    char c = prod.charAt(0);
                    // Deve ser um terminal minúsculo isolado
                    if (c < 'a' || c > 'z') {
                        return false;
                    }
                } else if (prod.length() == 2) {
                    char t = prod.charAt(0);
                    char n = prod.charAt(1);
                    // Deve ser um terminal minúsculo seguido por um Não-Terminal maiúsculo
                    if (t < 'a' || t > 'z' || n < 'A' || n > 'Z') {
                        return false;
                    }
                } else {
                    // Qualquer outra coisa foge da regra GLD
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 3. Motor de Simulação (Backtracking Recursivo)
     * Verifica se a palavra é gerada pela gramática a partir do Não-Terminal atual.
     */
    private boolean deriva(String palavra, int i, String ntAtual, HashMap<String, List<String>> gramatica) {
        List<String> producoes = gramatica.get(ntAtual);
        if (producoes == null) return false;

        // Condição de Aceitação: consumimos toda a palavra
        if (i == palavra.length()) {
            // Só aceita se houver transição para a palavra vazia '#'
            for (String prod : producoes) {
                if (prod.equals("#")) {
                    return true;
                }
            }
            return false; // Não terminou em um estado de aceitação
        }

        char charAtual = palavra.charAt(i);

        // Tentamos todas as produções disponíveis para o Não-Terminal atual
        for (String prod : producoes) {
            if (prod.equals("#")) {
                continue; // A palavra vazia só é verificada no final
            }
            
            if (prod.length() == 1) {
                // Produção de apenas um terminal (ex: 'a')
                if (prod.charAt(0) == charAtual && i == palavra.length() - 1) {
                    return true;
                }
            } else if (prod.length() == 2) {
                // Produção de terminal + Não-Terminal (ex: 'aA')
                if (prod.charAt(0) == charAtual) {
                    String proximoNt = String.valueOf(prod.charAt(1));
                    // Chama a recursão (Backtracking) para tentar a combinação seguinte
                    if (deriva(palavra, i + 1, proximoNt, gramatica)) {
                        return true;
                    }
                }
            }
        }
        
        // Se o laço terminar e nenhum caminho for válido, retorne falso.
        return false;
    }

    /**
     * 4. Ação da Interface Gráfica
     * Função conectada ao botão "Testar" na UI.
     */
    @FXML
    private void testarGramatica() {
        resultado.clear();
        
        String textoGramatica = campoGramatica.getText();
        if (textoGramatica == null || textoGramatica.trim().isEmpty()) {
            mostrarAlerta("Erro de Entrada", "A área da gramática está vazia. Insira a gramática.");
            return;
        }
        
        // Passo 1: Parser
        LinkedHashMap<String, List<String>> gramatica = parseGramatica(textoGramatica);
        if (gramatica == null || gramatica.isEmpty()) {
            mostrarAlerta("Erro de Sintaxe", "Formato de gramática inválido. Siga o formato: S -> aA | b | #");
            return;
        }
        
        // Passo 2: Validação
        if (!isGLD(gramatica)) {
            mostrarAlerta("Erro de Validação", "A gramática informada NÃO é uma Gramática Linear à Direita (GLD) válida.\nCertifique-se de que do lado esquerdo haja apenas uma letra maiúscula e do lado direito haja apenas produções do tipo 'a', 'aA' ou '#'.");
            return;
        }
        
        String textoPalavras = campoPalavras.getText();
        if (textoPalavras == null || textoPalavras.trim().isEmpty()) {
            mostrarAlerta("Aviso", "A área de palavras está vazia. Insira palavras para testar.");
            return;
        }
        
        // O símbolo inicial da gramática é sempre a primeira chave (primeira linha lida do LinkedHashMap)
        String simboloInicial = gramatica.keySet().iterator().next();
        
        String[] palavras = textoPalavras.split("\n");
        StringBuilder sbResultado = new StringBuilder();
        
        // Passo 3 & 4: Motor de Simulação para cada palavra
        for (String palavraRaw : palavras) {
            String palavra = palavraRaw.trim();
            if (palavra.isEmpty()) continue;
            
            // Permite que o usuário digite '#' caso queira testar explicitamente a palavra vazia (épsilon)
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
        
        resultado.setText(sbResultado.toString());
    }

    /**
     * Auxiliar para exibir os Alertas de erro/aviso do JavaFX
     */
    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
    
    /**
     * Limpa os campos visuais, ligado ao botão "Limpar".
     */
    @FXML
    private void limparCampos() {
        campoGramatica.clear();
        campoPalavras.clear();
        resultado.clear();
    }
}
