package com.example.projetolfa;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.QuadCurve;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class afController {

    @FXML
    private Circle iconeNovoEstado; // Bolinha da barra lateral, usada só como "molde" para arrastar

    @FXML
    private Pane canvasDiagrama;

    @FXML
    private ToggleButton botaoModoTransicao;

    @FXML
    private Label labelTipoAutomato;

    @FXML
    private TextField campoPalavraUnica;

    @FXML
    private Label resultadoPalavraUnica;

    @FXML
    private TextArea campoPalavrasMultiplas;

    @FXML
    private TextArea resultadoPalavrasMultiplas;

    private int quantidadeEstados = 0;

    private double offsetX; // Distância do clique até o canto do estado, usada para não "pular" ao arrastar
    private double offsetY;
    private boolean estadoFoiArrastado; // Evita que um arrasto seja interpretado como clique ao soltar o mouse

    private final List<Estado> estados = new ArrayList<>();
    private final List<Transicao> transicoes = new ArrayList<>();
    private Estado origemSelecionada; // Estado de origem já escolhido ao criar uma nova transição

    // Campos usados pela janela de simulação passo a passo
    private String palavraSimulada;
    private int posicaoSimulada;
    private List<Estado> estadosAtivosSimulados = new ArrayList<>();
    private Label labelProgressoPasso;
    private Label labelEstadosAtivosPasso;
    private Label labelResultadoPasso;
    private Button botaoProximoPasso;

    @FXML
    private void initialize() {

        iconeNovoEstado.setOnDragDetected(this::onIniciarArrasto);

        canvasDiagrama.setOnDragOver(this::onArrastarSobreCanvas);
        canvasDiagrama.setOnDragDropped(this::onSoltarNoCanvas);

        botaoModoTransicao.selectedProperty().addListener((observavel, antigo, novo) -> {
            if (!novo && origemSelecionada != null) { // Cancela seleção pendente ao sair do modo
                destacarEstado(origemSelecionada, false);
                origemSelecionada = null;
            }
        });

        atualizarTipoAutomato();
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

    // ===================== Criação e edição de estados =====================

    private void criarEstado(double x, double y) {

        TextInputDialog dialogo = new TextInputDialog("q" + quantidadeEstados);
        dialogo.setTitle("Novo Estado");
        dialogo.setHeaderText(null);
        dialogo.setContentText("Nome do estado:");

        Optional<String> resultado = dialogo.showAndWait();
        if (resultado.isEmpty()) {
            return;
        }

        String nome = resultado.get().trim();
        if (nome.isEmpty()) {
            return;
        }

        boolean nomeRepetido = false;
        for (Estado existente : estados) {
            if (existente.getNome().equals(nome)) {
                nomeRepetido = true;
            }
        }
        if (nomeRepetido) {
            mostrarAlerta("Já existe um estado com esse nome.");
            return;
        }

        Circle circuloPrincipal = new Circle(Estado.RAIO); // Desenha o círculo do estado
        circuloPrincipal.setFill(Color.LIGHTGRAY);
        circuloPrincipal.setStroke(Color.BLACK);

        Circle circuloFinal = new Circle(Estado.RAIO - 6); // Círculo interno, só some quando o estado é final
        circuloFinal.setFill(Color.TRANSPARENT);
        circuloFinal.setStroke(Color.BLACK);
        circuloFinal.setVisible(false);
        circuloFinal.setMouseTransparent(true);

        Text rotulo = new Text(nome); // Escreve o nome do estado dentro do círculo
        rotulo.setFill(Color.BLACK);

        Polygon marcadorInicial = new Polygon(0.0, 0.0, -18.0, -8.0, -18.0, 8.0); // Seta que indica o estado inicial
        marcadorInicial.setFill(Color.BLACK);
        marcadorInicial.setTranslateX(-Estado.RAIO);
        marcadorInicial.setVisible(false);
        marcadorInicial.setMouseTransparent(true);

        StackPane painel = new StackPane(circuloPrincipal, circuloFinal, marcadorInicial, rotulo);
        painel.setLayoutX(x - Estado.RAIO); // Centraliza o círculo no ponto onde foi solto
        painel.setLayoutY(y - Estado.RAIO);

        Estado estado = new Estado(nome, painel, circuloPrincipal, circuloFinal, rotulo, marcadorInicial);
        estados.add(estado);

        habilitarInteracao(estado); // Deixa o estado livre para ser arrastado, editado e usado em transições

        canvasDiagrama.getChildren().add(painel);
        quantidadeEstados++;
    }

    private void habilitarInteracao(Estado estado) {

        StackPane painel = estado.getVisual();
        painel.setOnMousePressed(evento -> onEstadoPressionado(evento, estado));
        painel.setOnMouseDragged(evento -> onEstadoArrastado(evento, estado));
        painel.setOnMouseClicked(evento -> onEstadoClicado(evento, estado));

        ContextMenu menu = new ContextMenu();
        MenuItem itemExcluir = new MenuItem("Excluir Estado");
        itemExcluir.setOnAction(evento -> confirmarExclusaoEstado(estado));
        menu.getItems().add(itemExcluir);

        painel.setOnContextMenuRequested(evento -> menu.show(painel, evento.getScreenX(), evento.getScreenY()));
    }

    private void onEstadoPressionado(MouseEvent evento, Estado estado) {

        offsetX = evento.getSceneX() - estado.getVisual().getLayoutX();
        offsetY = evento.getSceneY() - estado.getVisual().getLayoutY();
        estadoFoiArrastado = false;

        estado.getVisual().toFront(); // Traz o estado clicado para cima dos demais
    }

    private void onEstadoArrastado(MouseEvent evento, Estado estado) {

        double novoX = evento.getSceneX() - offsetX;
        double novoY = evento.getSceneY() - offsetY;

        // Mantém o estado dentro dos limites do canvas
        novoX = Math.max(0, Math.min(novoX, canvasDiagrama.getWidth() - estado.getVisual().getBoundsInLocal().getWidth()));
        novoY = Math.max(0, Math.min(novoY, canvasDiagrama.getHeight() - estado.getVisual().getBoundsInLocal().getHeight()));

        estadoFoiArrastado = true;
        estado.getVisual().setLayoutX(novoX);
        estado.getVisual().setLayoutY(novoY);

        atualizarTransicoesDoEstado(estado); // Redesenha as setas ligadas a esse estado na nova posição
    }

    private void onEstadoClicado(MouseEvent evento, Estado estado) {

        if (estadoFoiArrastado) { // Não trata o fim de um arrasto como um clique
            estadoFoiArrastado = false;
            return;
        }

        if (evento.getClickCount() == 2) {
            abrirEdicaoEstado(estado);
        } else if (botaoModoTransicao.isSelected()) {
            tratarSelecaoParaTransicao(estado);
        }
    }

    private void abrirEdicaoEstado(Estado estado) {

        // 1) Pergunta o novo nome
        TextInputDialog dialogoNome = new TextInputDialog(estado.getNome());
        dialogoNome.setTitle("Editar Estado");
        dialogoNome.setHeaderText(null);
        dialogoNome.setContentText("Nome do estado:");

        Optional<String> resultadoNome = dialogoNome.showAndWait();
        if (resultadoNome.isEmpty()) {
            return;
        }

        String novoNome = resultadoNome.get().trim();
        if (novoNome.isEmpty()) {
            mostrarAlerta("O nome do estado não pode ser vazio.");
            return;
        }

        boolean nomeRepetido = false;
        for (Estado outro : estados) {
            if (outro != estado && outro.getNome().equals(novoNome)) {
                nomeRepetido = true;
            }
        }
        if (nomeRepetido) {
            mostrarAlerta("Já existe um estado com esse nome.");
            return;
        }

        estado.setNome(novoNome);

        // 2) Pergunta se é o estado inicial
        Alert perguntaInicial = new Alert(Alert.AlertType.CONFIRMATION, "Este estado é o estado inicial?", ButtonType.YES, ButtonType.NO);
        perguntaInicial.setTitle("Estado Inicial");
        perguntaInicial.setHeaderText(null);

        Optional<ButtonType> respostaInicial = perguntaInicial.showAndWait();
        if (respostaInicial.isPresent() && respostaInicial.get() == ButtonType.YES) {
            for (Estado outro : estados) { // Só pode existir um estado inicial por vez
                if (outro != estado) {
                    outro.setInicial(false);
                }
            }
            estado.setInicial(true);
        } else {
            estado.setInicial(false);
        }

        // 3) Pergunta se é um estado final
        Alert perguntaFinal = new Alert(Alert.AlertType.CONFIRMATION, "Este estado é um estado final (de aceitação)?", ButtonType.YES, ButtonType.NO);
        perguntaFinal.setTitle("Estado Final");
        perguntaFinal.setHeaderText(null);

        Optional<ButtonType> respostaFinal = perguntaFinal.showAndWait();
        if (respostaFinal.isPresent() && respostaFinal.get() == ButtonType.YES) {
            estado.setFinalEstado(true);
        } else {
            estado.setFinalEstado(false);
        }

        atualizarTipoAutomato();
    }

    private void confirmarExclusaoEstado(Estado estado) {

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                "Excluir o estado \"" + estado.getNome() + "\" e todas as transições ligadas a ele?",
                ButtonType.YES, ButtonType.NO);
        confirmacao.setTitle("Excluir Estado");
        confirmacao.setHeaderText(null);

        Optional<ButtonType> resultado = confirmacao.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.YES) {
            removerEstado(estado);
        }
    }

    private void removerEstado(Estado estado) {

        int indice = 0;
        while (indice < transicoes.size()) {
            Transicao transicao = transicoes.get(indice);

            if (transicao.getOrigem() == estado || transicao.getDestino() == estado) {
                canvasDiagrama.getChildren().remove(transicao.getVisual());
                transicoes.remove(indice);
            } else {
                indice++;
            }
        }

        canvasDiagrama.getChildren().remove(estado.getVisual());
        estados.remove(estado);

        if (origemSelecionada == estado) {
            origemSelecionada = null;
        }

        atualizarTipoAutomato();
    }

    // ===================== Criação e edição de transições =====================

    private void destacarEstado(Estado estado, boolean selecionado) {

        if (selecionado) {
            estado.getCirculoPrincipal().setStroke(Color.DODGERBLUE);
            estado.getCirculoPrincipal().setStrokeWidth(3);
        } else {
            estado.getCirculoPrincipal().setStroke(Color.BLACK);
            estado.getCirculoPrincipal().setStrokeWidth(1);
        }
    }

    private void tratarSelecaoParaTransicao(Estado clicado) {

        if (origemSelecionada == null) {
            origemSelecionada = clicado;
            destacarEstado(clicado, true);
        } else {
            Estado origem = origemSelecionada;
            destacarEstado(origem, false);
            origemSelecionada = null;
            abrirCriacaoOuEdicaoTransicao(origem, clicado);
        }
    }

    private void abrirCriacaoOuEdicaoTransicao(Estado origem, Estado destino) {

        Transicao existente = null;
        for (Transicao transicao : transicoes) {
            if (transicao.getOrigem() == origem && transicao.getDestino() == destino) {
                existente = transicao;
            }
        }

        String textoInicial;
        if (existente != null) {
            textoInicial = existente.getSimbolosTexto();
        } else {
            textoInicial = "";
        }

        TextInputDialog dialogo = new TextInputDialog(textoInicial);
        dialogo.setTitle("Transição");
        dialogo.setHeaderText(null);
        dialogo.setContentText("Símbolos (separados por vírgula; use \"" + Transicao.EPSILON + "\" para vazio/epsilon):\n"
                + origem.getNome() + " -> " + destino.getNome());

        Optional<String> resultado = dialogo.showAndWait();
        if (resultado.isEmpty()) {
            return;
        }

        List<String> simbolos = lerSimbolos(resultado.get());
        if (simbolos == null) { // Mensagem de erro já exibida em lerSimbolos
            return;
        }
        if (simbolos.isEmpty()) {
            mostrarAlerta("Informe ao menos um símbolo.");
            return;
        }

        Transicao transicao = existente;
        if (transicao == null) {
            Group grupo = new Group();
            transicao = new Transicao(origem, destino, grupo);
            transicoes.add(transicao);
            canvasDiagrama.getChildren().add(grupo);
            configurarInteracaoTransicao(transicao);
        }

        transicao.getSimbolos().clear();
        transicao.getSimbolos().addAll(simbolos);

        redesenharTodasTransicoes(); // Reorganiza também o eventual par recíproco
        atualizarTipoAutomato();
    }

    private void configurarInteracaoTransicao(Transicao transicao) {

        Group grupo = transicao.getVisual();

        grupo.setOnMouseClicked(evento -> {
            if (evento.getClickCount() == 2) {
                abrirEdicaoTransicao(transicao);
            }
        });

        ContextMenu menu = new ContextMenu();
        MenuItem itemEditar = new MenuItem("Editar Símbolos");
        itemEditar.setOnAction(evento -> abrirEdicaoTransicao(transicao));
        MenuItem itemExcluir = new MenuItem("Excluir Transição");
        itemExcluir.setOnAction(evento -> removerTransicao(transicao));
        menu.getItems().addAll(itemEditar, itemExcluir);

        grupo.setOnContextMenuRequested(evento -> menu.show(grupo, evento.getScreenX(), evento.getScreenY()));
    }

    private void abrirEdicaoTransicao(Transicao transicao) {

        TextInputDialog dialogo = new TextInputDialog(transicao.getSimbolosTexto());
        dialogo.setTitle("Editar Transição");
        dialogo.setHeaderText(null);
        dialogo.setContentText("Símbolos (separados por vírgula; use \"" + Transicao.EPSILON + "\" para vazio/epsilon):\n"
                + transicao.getOrigem().getNome() + " -> " + transicao.getDestino().getNome());

        Optional<String> resultado = dialogo.showAndWait();
        if (resultado.isEmpty()) {
            return;
        }

        List<String> simbolos = lerSimbolos(resultado.get());
        if (simbolos == null) {
            return;
        }
        if (simbolos.isEmpty()) {
            mostrarAlerta("Informe ao menos um símbolo.");
            return;
        }

        transicao.getSimbolos().clear();
        transicao.getSimbolos().addAll(simbolos);

        redesenharTransicao(transicao);
        atualizarTipoAutomato();
    }

    private void removerTransicao(Transicao transicao) {

        canvasDiagrama.getChildren().remove(transicao.getVisual());
        transicoes.remove(transicao);

        redesenharTodasTransicoes(); // O par recíproco (se existir) volta a ficar centralizado
        atualizarTipoAutomato();
    }

    private List<String> lerSimbolos(String texto) {

        List<String> simbolos = new ArrayList<>();
        String[] partes = texto.split(",");

        for (String parte : partes) {
            String simbolo = parte.trim();

            if (simbolo.isEmpty()) {
                continue;
            }
            if (simbolo.equals("ε")) {
                simbolo = Transicao.EPSILON;
            }
            if (simbolo.length() != 1) {
                mostrarAlerta("Cada símbolo deve ter um único caractere (use \"" + Transicao.EPSILON + "\" para o vazio).");
                return null;
            }
            if (!simbolos.contains(simbolo)) {
                simbolos.add(simbolo);
            }
        }

        return simbolos;
    }

    // ===================== Desenho das transições =====================

    private void atualizarTransicoesDoEstado(Estado estado) {
        for (Transicao transicao : transicoes) {
            if (transicao.getOrigem() == estado || transicao.getDestino() == estado) {
                redesenharTransicao(transicao);
            }
        }
    }

    private void redesenharTodasTransicoes() {
        for (Transicao transicao : transicoes) {
            redesenharTransicao(transicao);
        }
    }

    private void redesenharTransicao(Transicao transicao) {

        Group grupo = transicao.getVisual();
        grupo.getChildren().clear();

        if (transicao.isLaco()) {
            desenharLaco(transicao, grupo);
        } else {
            desenharAresta(transicao, grupo);
        }
    }

    private void desenharAresta(Transicao transicao, Group grupo) {

        Estado origem = transicao.getOrigem();
        Estado destino = transicao.getDestino();

        double cx1 = origem.getCentroX(), cy1 = origem.getCentroY();
        double cx2 = destino.getCentroX(), cy2 = destino.getCentroY();

        double meioX = (cx1 + cx2) / 2;
        double meioY = (cy1 + cy2) / 2;

        // O vetor perpendicular (normal) e o sinal do lado da curva são calculados sempre a
        // partir do par de estados em uma ordem fixa (não da direção desta transição em si).
        // Se calculássemos a partir de origem->destino, a curva de A->B e a de B->A cairiam
        // exatamente uma em cima da outra: a inversão da direção e a inversão do sinal se
        // cancelariam, "sobrescrevendo" o desenho da primeira transição.
        Estado referencia;
        if (origem.getNome().compareTo(destino.getNome()) < 0) {
            referencia = origem;
        } else {
            referencia = destino;
        }

        Estado outraPonta;
        if (referencia == origem) {
            outraPonta = destino;
        } else {
            outraPonta = origem;
        }

        double dx = outraPonta.getCentroX() - referencia.getCentroX();
        double dy = outraPonta.getCentroY() - referencia.getCentroY();
        double comprimento = Math.hypot(dx, dy);
        double normalX = -dy / comprimento;
        double normalY = dx / comprimento;

        double sinalLado;
        if (origem == referencia) {
            sinalLado = 1;
        } else {
            sinalLado = -1;
        }

        // Se existir a transição recíproca (destino -> origem), a curva se afasta bastante para
        // um lado; sem par, uma leve curva evita que o rótulo fique em cima da própria linha
        double curvatura;
        if (existePar(transicao)) {
            curvatura = 45 * sinalLado;
        } else {
            curvatura = 18 * sinalLado;
        }

        double ctrlX = meioX + normalX * curvatura;
        double ctrlY = meioY + normalY * curvatura;

        // Ponto inicial/final na borda do círculo, na direção do ponto de controle, para a curva
        // sair/entrar tangente ao arco em vez de "cortar" por dentro do estado
        double anguloInicio = Math.atan2(ctrlY - cy1, ctrlX - cx1);
        double xInicio = cx1 + Estado.RAIO * Math.cos(anguloInicio);
        double yInicio = cy1 + Estado.RAIO * Math.sin(anguloInicio);

        double anguloFim = Math.atan2(ctrlY - cy2, ctrlX - cx2);
        double xFim = cx2 + Estado.RAIO * Math.cos(anguloFim);
        double yFim = cy2 + Estado.RAIO * Math.sin(anguloFim);

        QuadCurve curva = new QuadCurve(xInicio, yInicio, ctrlX, ctrlY, xFim, yFim);
        curva.setFill(Color.TRANSPARENT);
        curva.setStroke(Color.BLACK);

        // A seta acompanha a tangente da curva no ponto final (direção controle -> fim)
        double anguloSeta = Math.atan2(yFim - ctrlY, xFim - ctrlX);
        Polygon seta = criarSeta(xFim, yFim, anguloSeta);

        // O rótulo fica no meio da curva e um pouco mais afastado ainda, do lado de fora do arco,
        // para que as duas setas (ida e volta) nunca tenham seus textos sobrepostos
        double meioCurvaX = 0.25 * xInicio + 0.5 * ctrlX + 0.25 * xFim;
        double meioCurvaY = 0.25 * yInicio + 0.5 * ctrlY + 0.25 * yFim;

        Label rotulo = new Label(transicao.getSimbolosTexto());
        rotulo.setStyle("-fx-background-color: white; -fx-font-size: 11px; -fx-padding: 1 3 1 3;");
        rotulo.setMouseTransparent(true);
        rotulo.setLayoutX(meioCurvaX + normalX * 12 * sinalLado - 8);
        rotulo.setLayoutY(meioCurvaY + normalY * 12 * sinalLado - 8);
        rotulo.toFront();

        grupo.getChildren().addAll(curva, seta, rotulo);
    }

    private void desenharLaco(Transicao transicao, Group grupo) {

        Estado estado = transicao.getOrigem();
        double cx = estado.getCentroX();
        double topoY = estado.getCentroY() - Estado.RAIO;

        double xEsq = cx - 14, xDir = cx + 14;
        double ctrlY = topoY - 45;

        CubicCurve curva = new CubicCurve(xEsq, topoY, cx - 22, ctrlY, cx + 22, ctrlY, xDir, topoY);
        curva.setFill(Color.TRANSPARENT);
        curva.setStroke(Color.BLACK);

        double anguloSeta = Math.atan2(topoY - ctrlY, xDir - (cx + 22));
        Polygon seta = criarSeta(xDir, topoY, anguloSeta);

        Label rotulo = new Label(transicao.getSimbolosTexto());
        rotulo.setStyle("-fx-background-color: white; -fx-font-size: 11px;");
        rotulo.setLayoutX(cx - 10);
        rotulo.setLayoutY(topoY - 65);

        grupo.getChildren().addAll(curva, seta, rotulo);
    }

    private Polygon criarSeta(double pontaX, double pontaY, double angulo) {

        double comprimento = 12;
        double abertura = Math.toRadians(22);

        double x1 = pontaX - comprimento * Math.cos(angulo - abertura);
        double y1 = pontaY - comprimento * Math.sin(angulo - abertura);
        double x2 = pontaX - comprimento * Math.cos(angulo + abertura);
        double y2 = pontaY - comprimento * Math.sin(angulo + abertura);

        Polygon seta = new Polygon(pontaX, pontaY, x1, y1, x2, y2);
        seta.setFill(Color.BLACK);
        return seta;
    }

    private boolean existePar(Transicao transicao) {
        for (Transicao outra : transicoes) {
            if (outra != transicao && outra.getOrigem() == transicao.getDestino() && outra.getDestino() == transicao.getOrigem()) {
                return true;
            }
        }
        return false;
    }

    // ===================== Determinismo (AFD x AFND) =====================

    private void atualizarTipoAutomato() {

        boolean deterministico = true;

        for (Transicao transicao : transicoes) {
            if (transicao.getSimbolos().contains(Transicao.EPSILON)) {
                deterministico = false; // Transição vazia/epsilon só existe em AFND
            }
        }

        for (int i = 0; i < transicoes.size(); i++) {
            Transicao t1 = transicoes.get(i);

            for (int j = i + 1; j < transicoes.size(); j++) {
                Transicao t2 = transicoes.get(j);

                if (t1.getOrigem() == t2.getOrigem()) {
                    for (String simbolo : t1.getSimbolos()) {
                        if (!simbolo.equals(Transicao.EPSILON) && t2.getSimbolos().contains(simbolo)) {
                            deterministico = false; // Mesmo símbolo saindo do mesmo estado para destinos diferentes
                        }
                    }
                }
            }
        }

        if (deterministico) {
            labelTipoAutomato.setText("Tipo: AFD");
        } else {
            labelTipoAutomato.setText("Tipo: AFND");
        }
    }

    // ===================== Simulação / reconhecimento de palavras =====================

    private Estado obterEstadoInicial() {
        for (Estado estado : estados) {
            if (estado.isInicial()) {
                return estado;
            }
        }
        return null;
    }

    private List<Estado> fechoEpsilon(List<Estado> conjuntoInicial) {

        List<Estado> fecho = new ArrayList<>();
        for (Estado estado : conjuntoInicial) {
            if (!fecho.contains(estado)) {
                fecho.add(estado);
            }
        }

        boolean mudou = true;
        while (mudou) {
            mudou = false;

            for (Transicao transicao : transicoes) {
                if (fecho.contains(transicao.getOrigem()) && transicao.getSimbolos().contains(Transicao.EPSILON)) {
                    if (!fecho.contains(transicao.getDestino())) {
                        fecho.add(transicao.getDestino());
                        mudou = true;
                    }
                }
            }
        }

        return fecho;
    }

    private List<Estado> avancar(List<Estado> atuais, String simbolo) {

        List<Estado> proximos = new ArrayList<>();

        for (Estado estado : atuais) {
            for (Transicao transicao : transicoes) {
                if (transicao.getOrigem() == estado && transicao.getSimbolos().contains(simbolo)) {
                    if (!proximos.contains(transicao.getDestino())) {
                        proximos.add(transicao.getDestino());
                    }
                }
            }
        }

        return fechoEpsilon(proximos);
    }

    private boolean simular(String palavra) {

        List<Estado> conjuntoInicial = new ArrayList<>();
        conjuntoInicial.add(obterEstadoInicial());

        List<Estado> atuais = fechoEpsilon(conjuntoInicial);

        for (int i = 0; i < palavra.length(); i++) {
            if (atuais.isEmpty()) {
                break;
            }
            atuais = avancar(atuais, String.valueOf(palavra.charAt(i)));
        }

        boolean aceita = false;
        for (Estado estado : atuais) {
            if (estado.isFinalEstado()) {
                aceita = true;
            }
        }

        return aceita;
    }

    @FXML
    private void testarPalavraUnica() {

        if (obterEstadoInicial() == null) {
            resultadoPalavraUnica.setText("Defina um estado inicial.");
            return;
        }

        String palavra = campoPalavraUnica.getText();
        if (palavra == null) {
            palavra = "";
        }

        boolean aceita = simular(palavra);

        String textoPalavra;
        if (palavra.isEmpty()) {
            textoPalavra = "(vazia)";
        } else {
            textoPalavra = palavra;
        }

        if (aceita) {
            resultadoPalavraUnica.setText(textoPalavra + " - ACEITA");
            resultadoPalavraUnica.setTextFill(Color.GREEN);
        } else {
            resultadoPalavraUnica.setText(textoPalavra + " - REJEITA");
            resultadoPalavraUnica.setTextFill(Color.RED);
        }
    }

    @FXML
    private void testarPalavrasMultiplas() {

        if (obterEstadoInicial() == null) {
            resultadoPalavrasMultiplas.setText("Defina um estado inicial.");
            return;
        }

        String textoPalavras = campoPalavrasMultiplas.getText();
        if (textoPalavras == null) {
            textoPalavras = "";
        }

        String[] linhas = textoPalavras.split("\n", -1);
        StringBuilder resultadoTexto = new StringBuilder();

        for (String linha : linhas) {
            String palavra = linha.trim();
            if (palavra.isEmpty()) {
                continue;
            }

            boolean aceita = simular(palavra);
            if (aceita) {
                resultadoTexto.append(palavra).append("\t\t-\t\tACEITA\n");
            } else {
                resultadoTexto.append(palavra).append("\t\t-\t\tREJEITA\n");
            }
        }

        resultadoPalavrasMultiplas.setText(resultadoTexto.toString());
    }

    // ===================== Reconhecimento passo a passo =====================

    @FXML
    private void abrirPassoAPasso() {

        if (obterEstadoInicial() == null) {
            mostrarAlerta("Defina um estado inicial antes de simular.");
            return;
        }

        String palavraAtual = campoPalavraUnica.getText();
        if (palavraAtual == null) {
            palavraAtual = "";
        }

        TextInputDialog dialogoPalavra = new TextInputDialog(palavraAtual);
        dialogoPalavra.setTitle("Passo a Passo");
        dialogoPalavra.setHeaderText(null);
        dialogoPalavra.setContentText("Palavra a reconhecer:");

        Optional<String> resultado = dialogoPalavra.showAndWait();
        if (resultado.isEmpty()) {
            return;
        }

        abrirJanelaPassoAPasso(resultado.get());
    }

    private void abrirJanelaPassoAPasso(String palavra) {

        palavraSimulada = palavra;

        Stage janela = new Stage();
        janela.setTitle("Reconhecimento Passo a Passo");

        labelProgressoPasso = new Label();
        labelEstadosAtivosPasso = new Label();
        labelResultadoPasso = new Label();
        labelResultadoPasso.setStyle("-fx-font-weight: bold;");

        botaoProximoPasso = new Button("Próximo");
        Button botaoReiniciar = new Button("Reiniciar");
        Button botaoFechar = new Button("Fechar");

        botaoProximoPasso.setOnAction(evento -> avancarPassoAPasso());
        botaoReiniciar.setOnAction(evento -> reiniciarPassoAPasso());
        botaoFechar.setOnAction(evento -> janela.close());
        janela.setOnHidden(evento -> limparDestaqueEstados());

        String textoPalavra;
        if (palavra.isEmpty()) {
            textoPalavra = "(vazia)";
        } else {
            textoPalavra = palavra;
        }

        VBox raiz = new VBox(12,
                new Label("Palavra: " + textoPalavra),
                labelProgressoPasso,
                labelEstadosAtivosPasso,
                labelResultadoPasso,
                new HBox(10, botaoProximoPasso, botaoReiniciar, botaoFechar));
        raiz.setPadding(new Insets(20));
        raiz.setAlignment(Pos.CENTER_LEFT);

        janela.setScene(new Scene(raiz, 400, 260));
        reiniciarPassoAPasso();
        janela.show();
    }

    private void reiniciarPassoAPasso() {

        posicaoSimulada = 0;

        List<Estado> conjuntoInicial = new ArrayList<>();
        conjuntoInicial.add(obterEstadoInicial());
        estadosAtivosSimulados = fechoEpsilon(conjuntoInicial);

        atualizarTelaPassoAPasso();
    }

    private void avancarPassoAPasso() {

        if (posicaoSimulada < palavraSimulada.length()) {
            char simbolo = palavraSimulada.charAt(posicaoSimulada);
            estadosAtivosSimulados = avancar(estadosAtivosSimulados, String.valueOf(simbolo));
            posicaoSimulada++;
            atualizarTelaPassoAPasso();
        }
    }

    private void atualizarTelaPassoAPasso() {

        String consumida = palavraSimulada.substring(0, posicaoSimulada);
        String restante = palavraSimulada.substring(posicaoSimulada);
        labelProgressoPasso.setText("Consumido: \"" + consumida + "\"    Restante: \"" + restante + "\"");

        destacarEstadosAtivos(estadosAtivosSimulados);

        String nomes = "";
        for (int i = 0; i < estadosAtivosSimulados.size(); i++) {
            if (i > 0) {
                nomes = nomes + ", ";
            }
            nomes = nomes + estadosAtivosSimulados.get(i).getNome();
        }
        if (nomes.isEmpty()) {
            nomes = "nenhum";
        }
        labelEstadosAtivosPasso.setText("Estados ativos: " + nomes);

        if (posicaoSimulada >= palavraSimulada.length()) {
            boolean aceita = false;
            for (Estado estado : estadosAtivosSimulados) {
                if (estado.isFinalEstado()) {
                    aceita = true;
                }
            }

            if (aceita) {
                labelResultadoPasso.setText("PALAVRA ACEITA");
                labelResultadoPasso.setTextFill(Color.GREEN);
            } else {
                labelResultadoPasso.setText("PALAVRA REJEITADA");
                labelResultadoPasso.setTextFill(Color.RED);
            }
            botaoProximoPasso.setDisable(true);
        } else {
            labelResultadoPasso.setText("");
            botaoProximoPasso.setDisable(estadosAtivosSimulados.isEmpty());
        }
    }

    private void destacarEstadosAtivos(List<Estado> ativos) {
        for (Estado estado : estados) {
            if (ativos.contains(estado)) {
                estado.getCirculoPrincipal().setFill(Color.LIGHTBLUE);
            } else {
                estado.getCirculoPrincipal().setFill(Color.LIGHTGRAY);
            }
        }
    }

    private void limparDestaqueEstados() {
        for (Estado estado : estados) {
            estado.getCirculoPrincipal().setFill(Color.LIGHTGRAY);
        }
    }

    // ===================== Utilidades =====================

    private void mostrarAlerta(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.ERROR, mensagem, ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    @FXML
    private void limparDiagrama() {

        canvasDiagrama.getChildren().clear();
        estados.clear();
        transicoes.clear();
        origemSelecionada = null;
        quantidadeEstados = 0;

        atualizarTipoAutomato();
        resultadoPalavraUnica.setText("");
        resultadoPalavrasMultiplas.clear();
    }
}
