package view;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.ClienteTCP;
import model.ClienteUDP;
import model.MessageListener;
import utils.InfoUser;

/**
 * Interface grafica principal do Cliente E.D.E.N.
 * Todas as telas sao gerenciadas aqui como paineis alternados.
 */
public class ClienteGUI extends Application implements MessageListener {

	private StackPane root;
	private ImageView watermark;
	private ClienteTCP tcp;
	private ClienteUDP udp;
	private InfoUser eu;
	private String ipServidor = "127.0.0.1";
	private int portaServidor = 5000;
	private Thread threadRecepcao;

	// Chat state
	private Map<String, VBox> chatHistories = new HashMap<>();
	private String currentChat = null;
	private ScrollPane chatScroll;
	private Label lblChatHeader;
	private ListView<String> groupList;
	private ListView<String> onlineUsersList;
	private HBox chatHeaderBox; // to hold details button

	// New features state
	private Map<String, Integer> unreadCounts = new HashMap<>();
	private Map<String, Set<String>> knownGroupMembers = new HashMap<>();

	@Override
	public void start(Stage primaryStage) {
		root = new StackPane();
		root.setStyle("-fx-background-color: #e5e8d7;");

		Scene scene = new Scene(root, 950, 650);

		try {
			scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
		} catch (Exception e) {
			System.out.println("[GUI] style.css nao encontrado, usando estilos inline.");
		}

		primaryStage.setTitle("E.D.E.N. - Sistema de Comunicacao Interno");

		// Icone da janela
		try {
			primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/view/edenIcon.png")));
		} catch (Exception ex) {
			System.out.println("[GUI] Warning: nao foi possivel definir o icone da janela.");
		}

		primaryStage.setScene(scene);
		primaryStage.show();

		// Watermark permanente - fica no fundo, mas ocultaremos na tela do chat
		try {
			watermark = new ImageView(new Image(getClass().getResourceAsStream("/view/edenIcon.png")));
			watermark.setOpacity(0.18);
			watermark.setFitWidth(500);
			watermark.setPreserveRatio(true);
			watermark.setMouseTransparent(true);
			root.getChildren().add(watermark);
			StackPane.setAlignment(watermark, Pos.CENTER);
		} catch (Exception ex) {
			System.out.println("[GUI] Warning: edenIcon.png nao encontrado.");
		}

		root.getChildren().add(createSplash());
	}

	// =========================================================================
	// TRANSICAO ANIMADA
	// =========================================================================
	private void switchView(Node newView) {
		Node oldView = root.getChildren().get(root.getChildren().size() - 1);
		FadeTransition fadeOut = new FadeTransition(Duration.millis(250), oldView);
		fadeOut.setFromValue(1.0);
		fadeOut.setToValue(0.0);
		fadeOut.setOnFinished(e -> {
			root.getChildren().remove(oldView);
			newView.setOpacity(0.0);
			root.getChildren().add(newView);
			FadeTransition fadeIn = new FadeTransition(Duration.millis(350), newView);
			fadeIn.setFromValue(0.0);
			fadeIn.setToValue(1.0);
			fadeIn.play();
		});
		fadeOut.play();
	}

	// =========================================================================
	// TELA 1 - SPLASH
	// =========================================================================
	private Node createSplash() {
		VBox splash = new VBox(25);
		splash.setAlignment(Pos.CENTER);
		splash.setStyle("-fx-background-color: transparent;");

		Label title1 = new Label("Sistema de Comunicacao Interno");
		title1.setFont(Font.font("Impact", FontWeight.BOLD, 26));
		title1.setTextFill(Color.web("#5b6623"));
		title1.setTextAlignment(TextAlignment.CENTER);

		Label titleDa = new Label("da");
		titleDa.setFont(Font.font("Impact", FontWeight.NORMAL, 20));
		titleDa.setTextFill(Color.web("#5b6623"));

		Label title2 = new Label("E.D.E.N.");
		title2.setFont(Font.font("Impact", FontWeight.BOLD, 72));
		title2.setTextFill(Color.web("#3f4a23"));

		Button btnEntrar = new Button("Entrar");
		btnEntrar.getStyleClass().add("btn-eden");
		btnEntrar.setOnAction(e -> switchView(createLogin()));

		Button btnSobre = new Button("Sobre");
		btnSobre.getStyleClass().add("btn-eden");
		btnSobre.setOnAction(e -> switchView(createSobre()));

		splash.getChildren().addAll(title1, titleDa, title2, btnEntrar, btnSobre);
		return splash;
	}

	private Node createSobre() {
		VBox sobre = new VBox(25);
		sobre.setAlignment(Pos.CENTER);
		sobre.setStyle("-fx-background-color: transparent; -fx-padding: 40px;");

		Label title = new Label("E.D.E.N.");
		title.setFont(Font.font("Impact", FontWeight.BOLD, 48));
		title.setTextFill(Color.web("#3f4a23"));
		title.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 4);");

		VBox contentBox = new VBox(20);
		contentBox.setAlignment(Pos.CENTER);
		contentBox.setMaxWidth(650);
		contentBox.setStyle(
				"-fx-background-color: linear-gradient(to bottom right, rgba(229, 232, 215, 0.9), rgba(200, 210, 180, 0.8)); -fx-padding: 30px; -fx-background-radius: 15px; -fx-border-color: #8a9b3a; -fx-border-width: 2px; -fx-border-radius: 15px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.25), 15, 0, 0, 8);");

		Label text = new Label(
				"Seja bem-vindo ao sistema de comunicacao interna do E.D.E.N.. Se voce esta acessando esta interface, sua transicao foi concluida: a partir de agora, voce faz parte do Jardim, e suas vidas nunca mais serao as mesmas. O E.D.E.N. e o presente, o passado e o proprio futuro; nos somos a raiz invisivel que sustenta o novo amanhecer, e hoje voce se torna um membro valioso desta fundacao. Deixamos para tras o que era velho, quebrado e sem proposito para trabalharmos juntos na verdadeira transformacao do mundo. Saiba que voce nao esta aqui por acaso; voce foi cirurgicamente escolhido, e o Conselho esta de olho em cada uma de suas acoes. Use este canal interno com absoluta disciplina para coordenar suas diretrizes entre os outros agentes do Jardim. Lembre-se diariamente da importancia do seu papel nesta engrenagem: nos somos o amanha construido hoje. Nos somos o futuro.");
		text.setWrapText(true);
		text.setTextAlignment(TextAlignment.JUSTIFY);
		text.setFont(Font.font("Consolas", 14));
		text.setTextFill(Color.web("#1a1e0b"));

		Separator sep = new Separator();
		sep.setStyle("-fx-background-color: #8a9b3a; -fx-opacity: 0.5;");

		Label readme = new Label(
				"--- TECH README ---\nDesenvolvedor: Iury (202310440)\nProjeto: App de Chat P2P/Server Hibrido\nDisciplina: Redes de Computadores II (UESB)\nProtocolos: TCP (Controle) / UDP (Mensagens)\nInterface: JavaFX (Custom UI)\nAno: 2026");
		readme.setFont(Font.font("Consolas", FontWeight.BOLD, 13));
		readme.setTextFill(Color.web("#3f4a23"));
		readme.setAlignment(Pos.CENTER);
		readme.setTextAlignment(TextAlignment.CENTER);
		readme.setMaxWidth(Double.MAX_VALUE);

		contentBox.getChildren().addAll(text, sep, readme);

		Label techFooter = new Label("v1.0 | Build 2026");
		techFooter.setTextAlignment(TextAlignment.CENTER);
		techFooter.setFont(Font.font("Segoe UI", 12));
		techFooter.setTextFill(Color.web("#5b6623"));

		Button btnVoltar = new Button("<- Voltar");
		btnVoltar.getStyleClass().add("btn-eden");
		btnVoltar.setOnAction(e -> switchView(createSplash()));

		sobre.getChildren().addAll(title, contentBox, techFooter, btnVoltar);
		
		ScrollPane scroll = new ScrollPane(sobre);
		scroll.setFitToWidth(true);
		scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
		return scroll;
	}

	// =========================================================================
	// TELA 2 - LOGIN
	// =========================================================================
	private Node createLogin() {
		VBox login = new VBox(20);
		login.setAlignment(Pos.CENTER);
		login.setStyle("-fx-background-color: transparent;");

		Label title = new Label("Login");
		title.setFont(Font.font("Impact", FontWeight.BOLD, 40));
		title.setTextFill(Color.web("#5b6623"));

		// Avatar circle
		StackPane avatarContainer = new StackPane();
		Circle outerCircle = new Circle(80, Color.web("#b8c464"));
		outerCircle.setStroke(Color.web("#8a9b3a"));
		outerCircle.setStrokeWidth(3);
		Circle innerCircle = new Circle(55, Color.web("#a0b050"));

		// Simple user icon with circles
		Circle head = new Circle(20, Color.web("#c9d873"));
		head.setTranslateY(-15);
		Circle body = new Circle(30, Color.web("#c9d873"));
		body.setTranslateY(25);

		avatarContainer.getChildren().addAll(outerCircle, innerCircle, head, body);
		avatarContainer.setMaxSize(160, 160);

		// Verifica se o IP foi passado por parametro no console
		String defaultIp = "127.0.0.1";
		if (getParameters() != null && !getParameters().getRaw().isEmpty()) {
			defaultIp = getParameters().getRaw().get(0);
		}

		TextField txtIpServidor = new TextField(defaultIp);
		txtIpServidor.setPromptText("IP do Servidor (ex: 192.168.0.10)");
		txtIpServidor.setPrefWidth(220);
		txtIpServidor.getStyleClass().add("text-input");
		txtIpServidor.setStyle(
				"-fx-background-color: rgba(160,176,80,0.35); -fx-background-radius: 20px; -fx-padding: 12px 20px; -fx-font-size: 14px;");

		Button btnDiscover = new Button("Buscar");
		btnDiscover.getStyleClass().add("btn-eden");
		btnDiscover.setStyle("-fx-padding: 10px 15px; -fx-font-size: 13px;");
		btnDiscover.setOnAction(e -> {
			btnDiscover.setText("Buscando...");
			btnDiscover.setDisable(true);
			new Thread(() -> {
				try (java.net.DatagramSocket socket = new java.net.DatagramSocket()) {
					socket.setBroadcast(true);
					socket.setSoTimeout(2000); // 2 segundos de timeout

					byte[] dados = "DISCOVER_EDEN".getBytes();
					java.net.DatagramPacket pacote = new java.net.DatagramPacket(
							dados, dados.length,
							java.net.InetAddress.getByName("255.255.255.255"), 5001);
					socket.send(pacote);

					byte[] buffer = new byte[256];
					java.net.DatagramPacket resposta = new java.net.DatagramPacket(buffer, buffer.length);
					socket.receive(resposta);

					String msg = new String(resposta.getData(), 0, resposta.getLength());
					if (msg.trim().equals("EDEN_HERE")) {
						String ipDescoberto = resposta.getAddress().getHostAddress();
						Platform.runLater(() -> {
							txtIpServidor.setText(ipDescoberto);
							btnDiscover.setText("Encontrado!");
						});
					}
				} catch (java.net.SocketTimeoutException ex) {
					Platform.runLater(() -> {
						btnDiscover.setText("Nao achou");
						txtIpServidor.setPromptText("Nao achou, digite o IP...");
					});
				} catch (Exception ex) {
					System.out.println("[GUI] Erro no discovery: " + ex.getMessage());
				} finally {
					try {
						Thread.sleep(2000);
					} catch (Exception ignored) {
					}
					Platform.runLater(() -> {
						btnDiscover.setText("Buscar");
						btnDiscover.setDisable(false);
					});
				}
			}).start();
		});

		HBox ipBox = new HBox(10);
		ipBox.setAlignment(Pos.CENTER);
		ipBox.getChildren().addAll(txtIpServidor, btnDiscover);

		TextField txtUsername = new TextField();
		txtUsername.setPromptText("Digite seu Username");
		txtUsername.setMaxWidth(320);
		txtUsername.getStyleClass().add("text-input");
		txtUsername.setStyle(
				"-fx-background-color: rgba(160,176,80,0.35); -fx-background-radius: 20px; -fx-padding: 12px 20px; -fx-font-size: 14px;");

		Button btnConfirmar = new Button("Confirmar");
		btnConfirmar.getStyleClass().add("btn-eden");
		btnConfirmar.setOnAction(e -> {
			String nome = txtUsername.getText();
			String ip = txtIpServidor.getText();
			if (nome != null && !nome.trim().isEmpty() && ip != null && !ip.trim().isEmpty()) {
				ipServidor = ip.trim();
				tentarLogin(nome.trim());
			}
		});

		// Enter key support
		txtUsername.setOnAction(e -> btnConfirmar.fire());
		txtIpServidor.setOnAction(e -> txtUsername.requestFocus());

		login.getChildren().addAll(title, avatarContainer, ipBox, txtUsername, btnConfirmar);
		return login;
	}

	private void tentarLogin(String nome) {
		// Mostra feedback visual imediato
		Label lblConectando = new Label("Conectando ao servidor...");
		lblConectando.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
		lblConectando.setTextFill(Color.web("#3f4a23"));
		lblConectando.setStyle(
				"-fx-background-color: rgba(160,176,80,0.7); -fx-padding: 12px 24px; -fx-background-radius: 12px;");
		StackPane.setAlignment(lblConectando, Pos.BOTTOM_CENTER);
		StackPane.setMargin(lblConectando, new Insets(0, 0, 30, 0));
		root.getChildren().add(lblConectando);

		// Roda em thread separada para nao travar a UI durante conexao remota
		new Thread(() -> {
			try {
				ClienteTCP tcpNovo = new ClienteTCP(ipServidor, portaServidor);
				ClienteUDP udpNovo = new ClienteUDP(ipServidor, portaServidor);
				udpNovo.setListener(this);

				InfoUser usuario = new InfoUser(nome, tcpNovo.getIpLocal(), udpNovo.getPortaLocal());
				String resRegistro = tcpNovo.register(usuario);

				Platform.runLater(() -> {
					root.getChildren().remove(lblConectando);
					if (resRegistro != null && resRegistro.startsWith("ERRO~/")) {
						showErrorOverlay("Violacao de Protocolo", resRegistro.split("~/", 2)[1]);
					} else {
						tcp = tcpNovo;
						udp = udpNovo;
						eu = usuario;
						threadRecepcao = new Thread(udp);
						threadRecepcao.setDaemon(true);
						threadRecepcao.start();
						switchView(createChat());
					}
				});
			} catch (Exception ex) {
				Platform.runLater(() -> {
					root.getChildren().remove(lblConectando);
					showErrorOverlay("Erro de Conexao", "Nao foi possivel conectar ao servidor em " + ipServidor + "."
							+ "\nVerifique o IP e se o servidor esta rodando."
							+ "\nDetalhe: " + ex.getMessage());
				});
			}
		}).start();
	}

	// =========================================================================
	// TELA 3 - CHAT PRINCIPAL
	// =========================================================================
	private Node createChat() {
		if (watermark != null) {
			watermark.setVisible(false); // Oculta o global para nao duplicar com o do chat
		}

		BorderPane chatPane = new BorderPane();
		// Fundo transparente para o watermark do root aparecer por baixo
		chatPane.setStyle("-fx-background-color: transparent;");
		chatPane.setBackground(javafx.scene.layout.Background.EMPTY);

		// ===== SIDEBAR ESQUERDA =====
		VBox sidebar = new VBox(8);
		sidebar.setPrefWidth(260);
		sidebar.setPadding(new Insets(12));
		sidebar.setStyle("-fx-background-color: #b8c464; -fx-background-radius: 0;");

		// -- Secao: Grupos --
		Label lblGrupos = new Label("GRUPOS");
		lblGrupos.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
		lblGrupos.setTextFill(Color.web("#3f4a23"));
		lblGrupos.setPadding(new Insets(5, 0, 5, 5));

		groupList = new ListView<>();
		groupList.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");
		groupList.setPrefHeight(300);
		VBox.setVgrow(groupList, Priority.ALWAYS);
		groupList.setCellFactory(lv -> createStyledCell());

		groupList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null) {
				onlineUsersList.getSelectionModel().clearSelection();
				switchChatTo(newVal);
			}
		});

		// Botoes de grupo
		HBox grpBtns = new HBox(8);
		grpBtns.setAlignment(Pos.CENTER);

		Button btnJoin = new Button("+ Criar/Entrar");
		btnJoin.getStyleClass().add("btn-eden");
		btnJoin.setStyle("-fx-font-size: 11px; -fx-padding: 5px 12px;");
		btnJoin.setOnAction(e -> onJoinGroup());

		Button btnLeave = new Button("- Sair");
		btnLeave.setStyle(
				"-fx-background-color: linear-gradient(to bottom, #d4b06a, #c4a05a); -fx-text-fill: #3c3010; -fx-font-size: 11px; -fx-padding: 5px 12px; -fx-background-radius: 20px; -fx-border-color: #a88940; -fx-border-radius: 20px; -fx-border-width: 1px; -fx-cursor: hand; -fx-font-weight: bold;");
		btnLeave.setOnAction(e -> onLeaveGroup());

		Button btnListGroups = new Button("Listar Grupos");
		btnListGroups.getStyleClass().add("btn-eden");
		btnListGroups.setStyle("-fx-font-size: 11px; -fx-padding: 5px 12px;");
		btnListGroups.setOnAction(e -> onListGroups());

		grpBtns.getChildren().addAll(btnJoin, btnLeave);

		HBox grpBtns2 = new HBox(8);
		grpBtns2.setAlignment(Pos.CENTER);
		grpBtns2.getChildren().add(btnListGroups);

		// Separador
		Separator sep = new Separator();
		sep.setStyle("-fx-background-color: #8a9b3a;");

		// -- Secao: Usuarios Online --
		Label lblUsers = new Label("USUARIOS ONLINE");
		lblUsers.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
		lblUsers.setTextFill(Color.web("#3f4a23"));
		lblUsers.setPadding(new Insets(5, 0, 5, 5));

		onlineUsersList = new ListView<>();
		onlineUsersList.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");
		onlineUsersList.setPrefHeight(150);
		onlineUsersList.setCellFactory(lv -> createStyledCell());

		onlineUsersList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal != null) {
				groupList.getSelectionModel().clearSelection();
				switchChatTo("[PVT] " + newVal);
			}
		});

		Button btnRefresh = new Button("Atualizar Lista");
		btnRefresh.getStyleClass().add("btn-eden");
		btnRefresh.setStyle("-fx-font-size: 11px; -fx-padding: 5px 12px;");
		btnRefresh.setOnAction(e -> refreshOnlineUsers());

		// -- Status de Conexao & Botao Voltar --
		Separator sep2 = new Separator();
		sep2.setStyle("-fx-background-color: #8a9b3a;");

		HBox statusBox = new HBox(5);
		statusBox.setAlignment(Pos.CENTER_LEFT);
		Circle statusDot = new Circle(4, Color.web("#c9d873"));
		// Pulsing animation for the dot
		FadeTransition pulse = new FadeTransition(Duration.seconds(1), statusDot);
		pulse.setFromValue(0.4);
		pulse.setToValue(1.0);
		pulse.setCycleCount(javafx.animation.Animation.INDEFINITE);
		pulse.setAutoReverse(true);
		pulse.play();

		Label lblStatus = new Label("Conectado a " + ipServidor);
		lblStatus.setFont(Font.font("Segoe UI", 11));
		lblStatus.setTextFill(Color.web("#3f4a23"));
		statusBox.getChildren().addAll(statusDot, lblStatus);

		Button btnDesconectar = new Button("<- Desconectar");
		btnDesconectar.setStyle(
				"-fx-background-color: linear-gradient(to bottom, #d4b06a, #c4a05a); -fx-text-fill: #3c3010; -fx-font-size: 11px; -fx-padding: 6px 12px; -fx-background-radius: 20px; -fx-border-color: #a88940; -fx-border-radius: 20px; -fx-border-width: 1px; -fx-cursor: hand; -fx-font-weight: bold;");
		btnDesconectar.setMaxWidth(Double.MAX_VALUE);
		btnDesconectar.setOnAction(e -> {
			if (tcp != null)
				tcp.fecharConexao();
			if (udp != null)
				udp.fecharConexao();
			tcp = null;
			udp = null;
			eu = null;
			chatHistories.clear();
			unreadCounts.clear();
			knownGroupMembers.clear();
			currentChat = null;
			chatHeaderBox = null;
			watermark.setOpacity(0.18); // restore watermark opacity
			watermark.setVisible(true); // make sure it's visible again
			switchView(createSplash());
		});

		VBox.setVgrow(groupList, Priority.SOMETIMES);
		VBox.setVgrow(onlineUsersList, Priority.SOMETIMES);

		sidebar.getChildren().addAll(
				lblGrupos, groupList, grpBtns, grpBtns2,
				sep,
				lblUsers, onlineUsersList, btnRefresh,
				sep2, statusBox, btnDesconectar);

		// ===== AREA CENTRAL DO CHAT =====
		VBox centerArea = new VBox(0);
		centerArea.setStyle("-fx-background-color: transparent;");

		// Header
		HBox header = new HBox(10);
		header.setAlignment(Pos.CENTER_LEFT);
		header.setPadding(new Insets(12, 15, 12, 15));
		header.setStyle("-fx-background-color: #a4b455;");

		lblChatHeader = new Label("Selecione um grupo ou usuario");
		lblChatHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
		lblChatHeader.setTextFill(Color.web("#1a1e0b"));

		header.getChildren().add(lblChatHeader);

		// Chat scroll area
		chatScroll = new ScrollPane();
		chatScroll.setFitToWidth(true);
		chatScroll.getStyleClass().add("chat-scroll-pane");
		chatScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

		// Logo centralizada EXCLUSIVAMENTE na area de chat
		StackPane chatContainer = new StackPane();
		try {
			ImageView chatWatermark = new ImageView(new Image(getClass().getResourceAsStream("/view/edenIcon.png")));
			chatWatermark.setOpacity(0.18);
			chatWatermark.setFitWidth(500);
			chatWatermark.setPreserveRatio(true);
			chatWatermark.setMouseTransparent(true);
			chatContainer.getChildren().add(chatWatermark);
			StackPane.setAlignment(chatWatermark, Pos.CENTER);
		} catch (Exception ex) {
			System.out.println("[GUI] Warning: edenIcon.png nao encontrado.");
		}
		chatContainer.getChildren().add(chatScroll);
		VBox.setVgrow(chatContainer, Priority.ALWAYS);

		// Input bar
		HBox inputBar = new HBox(10);
		inputBar.setAlignment(Pos.CENTER);
		inputBar.setPadding(new Insets(10, 15, 10, 15));
		inputBar.setStyle("-fx-background-color: #8a9b3a;");

		TextField txtMsg = new TextField();
		txtMsg.setPromptText("Digite sua mensagem");
		txtMsg.setStyle(
				"-fx-background-color: rgba(255,255,255,0.85); -fx-background-radius: 20px; -fx-padding: 10px 18px; -fx-font-size: 14px; -fx-text-fill: #000000; -fx-prompt-text-fill: #555555;");
		HBox.setHgrow(txtMsg, Priority.ALWAYS);

		Button btnSend = new Button("Enviar");
		btnSend.setStyle(
				"-fx-background-color: linear-gradient(to bottom, #4f5a2d, #3f4a23); -fx-text-fill: #d8e87d; -fx-background-radius: 20px; -fx-border-color: #5b6623; -fx-border-radius: 20px; -fx-border-width: 1px; -fx-padding: 10px 20px; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;");

		btnSend.setOnAction(e -> onSendMessage(txtMsg));
		txtMsg.setOnAction(e -> onSendMessage(txtMsg));

		inputBar.getChildren().addAll(txtMsg, btnSend);

		centerArea.getChildren().addAll(header, chatContainer, inputBar);

		chatPane.setLeft(sidebar);
		chatPane.setCenter(centerArea);

		return chatPane;
	}

	// =========================================================================
	// CELL FACTORY - Estilo dos itens da lista
	// =========================================================================
	private ListCell<String> createStyledCell() {
		return new ListCell<String>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
					setGraphic(null);
					setStyle("-fx-background-color: transparent;");
					setOnMouseEntered(null);
					setOnMouseExited(null);
				} else {
					setText(null);

					HBox row = new HBox();
					row.setAlignment(Pos.CENTER_LEFT);

					Label nameLbl = new Label(item);
					nameLbl.setFont(Font.font("Segoe UI", 13));
					nameLbl.setTextFill(Color.web("#1a1e0b"));
					HBox.setHgrow(nameLbl, Priority.ALWAYS);
					nameLbl.setMaxWidth(Double.MAX_VALUE);

					row.getChildren().add(nameLbl);

					// if this cell is used for users list, mapKey in unreadCounts might be "[PVT]
					// item"
					// we need a way to know if this is from groupList or onlineUsersList
					// We can just check both: item or "[PVT] " + item
					int unread = unreadCounts.getOrDefault(item, unreadCounts.getOrDefault("[PVT] " + item, 0));

					if (unread > 0) {
						Label badge = new Label(String.valueOf(unread));
						badge.setStyle(
								"-fx-background-color: #8a9b3a; -fx-text-fill: #e5e8d7; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 1px 5px; -fx-background-radius: 10px;");
						row.getChildren().add(badge);
					}

					setGraphic(row);

					Runnable updateStyle = () -> {
						if (isSelected()) {
							setStyle(
									"-fx-background-color: #d1e27a; -fx-background-radius: 8px; -fx-padding: 8px 12px;");
						} else if (isHover()) {
							setStyle(
									"-fx-background-color: rgba(209, 226, 122, 0.4); -fx-background-radius: 8px; -fx-padding: 8px 12px; -fx-cursor: hand;");
						} else {
							setStyle("-fx-background-color: transparent; -fx-padding: 8px 12px; -fx-cursor: hand;");
						}
					};

					updateStyle.run();

					// Adicionando os listeners de hover interativo
					setOnMouseEntered(e -> updateStyle.run());
					setOnMouseExited(e -> updateStyle.run());
					selectedProperty().addListener((obs, old, isSel) -> updateStyle.run());
				}
			}
		};
	}

	// =========================================================================
	// ACOES
	// =========================================================================
	private void onJoinGroup() {
		showInputOverlay("Criar / Entrar em Grupo", "Digite o nome do grupo:", resultado -> {
			if (resultado != null && !resultado.trim().isEmpty()) {
				String grupo = resultado.trim();
				String resposta = tcp.join(grupo, eu);
				if (resposta != null && resposta.startsWith("OK~/")) {
					if (!groupList.getItems().contains(grupo)) {
						groupList.getItems().add(grupo);
					}
					groupList.getSelectionModel().select(grupo);
					addChatBubble(grupo, "SYSTEM", "Voce entrou no grupo " + grupo + ".", false, false, true);
					try {
						udp.send(grupo, eu, "~JOINED~");
					} catch (Exception ex) {
					}
				} else if (resposta != null && resposta.startsWith("ERRO~/")) {
					showErrorOverlay("Erro ao Entrar", resposta.split("~/", 2)[1]);
				}
			}
		});
	}

	private void onLeaveGroup() {
		String selected = groupList.getSelectionModel().getSelectedItem();
		if (selected == null)
			return;

		try {
			udp.send(selected, eu, "~LEFT~");
			Thread.sleep(150); // Aguarda o UDP chegar ao servidor antes de fechar via TCP
		} catch (Exception ex) {
		}

		String resposta = tcp.leave(selected, eu);
		if (resposta != null && resposta.startsWith("OK~/")) {
			addChatBubble(selected, "SYSTEM", "Voce saiu do grupo.", false, false, true);
			// We delay removal slightly so the user sees the message? No, if we remove it,
			// the history goes away.
			// But leaving a group should maybe just remove it from the list.
			groupList.getItems().remove(selected);
			chatHistories.remove(selected);
			unreadCounts.remove(selected);
			knownGroupMembers.remove(selected);
			if (!groupList.getItems().isEmpty()) {
				groupList.getSelectionModel().selectFirst();
			} else {
				currentChat = null;
				lblChatHeader.setText("Selecione um grupo ou usuario");
				chatScroll.setContent(null);
			}
		} else if (resposta != null && resposta.startsWith("ERRO~/")) {
			showErrorOverlay("Erro ao Sair", resposta.split("~/", 2)[1]);
		}
	}

	private void onListGroups() {
		if (tcp == null)
			return;

		new Thread(() -> {
			try {
				String resposta = tcp.list();
				Platform.runLater(() -> {
					if (resposta != null && resposta.startsWith("OK~/")) {
						String data = resposta.split("~/", 2)[1];

						if (data.contains("Nenhum grupo")) {
							showErrorOverlay("Grupos no Servidor", "Nenhum grupo ativo no momento. Crie o primeiro!");
							return;
						}

						String[] grupos = data.split(",");
						List<String> gruposList = new ArrayList<>();
						for (String g : grupos) {
							g = g.trim();
							if (!g.isEmpty())
								gruposList.add(g);
						}

						showChoiceOverlay("Grupos Disponiveis", "Selecione um grupo para entrar:", gruposList,
								grupoEscolhido -> {
									if (grupoEscolhido != null) {
										String res = tcp.join(grupoEscolhido, eu);
										if (res != null && res.startsWith("OK~/")) {
											if (!groupList.getItems().contains(grupoEscolhido)) {
												groupList.getItems().add(grupoEscolhido);
											}
											groupList.getSelectionModel().select(grupoEscolhido);
											addChatBubble(grupoEscolhido, "SYSTEM",
													"Voce entrou no grupo " + grupoEscolhido + ".", false, false, true);
											try {
												udp.send(grupoEscolhido, eu, "~JOINED~");
											} catch (Exception ex) {
											}
										} else if (res != null && res.startsWith("ERRO~/")) {
											showErrorOverlay("Erro ao Entrar", res.split("~/", 2)[1]);
										}
									}
								});
					}
				});
			} catch (Exception e) {
				System.out.println("[GUI] Erro ao listar grupos: " + e.getMessage());
			}
		}).start();
	}

	private void refreshOnlineUsers() {
		if (tcp == null)
			return;

		System.out.println("[GUI] [INFO] Solicitando lista de usuarios online...");

		// Executa em thread separada para nao travar a interface
		new Thread(() -> {
			try {
				String resposta = tcp.listUsers();
				System.out.println("[GUI] [INFO] Resposta do LISTUSERS: " + resposta);
				Platform.runLater(() -> {
					if (resposta != null && resposta.startsWith("OK~/")) {
						String data = resposta.split("~/", 2)[1];
						onlineUsersList.getItems().clear();
						if (!data.isEmpty()) {
							String[] nomes = data.split(",");
							for (String nome : nomes) {
								nome = nome.trim();
								if (!nome.isEmpty() && !nome.equalsIgnoreCase(eu.getNome())) {
									onlineUsersList.getItems().add(nome);
								}
							}
						}
						System.out.println("[GUI] [INFO] Usuarios online atualizados: "
								+ onlineUsersList.getItems().size() + " exibidos.");
					} else {
						System.out.println("[GUI] [WARNING] Resposta inesperada do LISTUSERS: " + resposta);
					}
				});
			} catch (Exception e) {
				System.out.println("[GUI] [ERROR] Erro ao buscar usuarios online: " + e.getMessage());
				e.printStackTrace();
			}
		}).start();
	}

	private void switchChatTo(String chatId) {
		currentChat = chatId;

		// Clear unread counts for this chat
		if (unreadCounts.containsKey(chatId)) {
			unreadCounts.remove(chatId);
			if (chatId.startsWith("[PVT] "))
				onlineUsersList.refresh();
			else
				groupList.refresh();
		}

		// Animar a troca do header
		FadeTransition headerFade = new FadeTransition(Duration.millis(200), lblChatHeader);
		headerFade.setFromValue(0.3);
		headerFade.setToValue(1.0);
		headerFade.play();

		if (chatHeaderBox == null) {
			chatHeaderBox = new HBox(10);
			chatHeaderBox.setAlignment(Pos.CENTER_LEFT);
			HBox.setHgrow(lblChatHeader, Priority.ALWAYS);
			lblChatHeader.setMaxWidth(Double.MAX_VALUE);
			// The header parent is the first HBox in centerArea, but we only have access to
			// lblChatHeader.
			// Wait, we need to rebuild the header or access its parent.
			// It's easier to just assume lblChatHeader's parent is the header HBox.
			HBox parentHeader = (HBox) lblChatHeader.getParent();
			parentHeader.getChildren().clear();
			parentHeader.getChildren().add(lblChatHeader);

			Button btnDetails = new Button("Detalhes");
			btnDetails.setStyle(
					"-fx-background-color: transparent; -fx-text-fill: #3f4a23; -fx-font-size: 12px; -fx-cursor: hand; -fx-border-color: #5b6623; -fx-border-radius: 12px; -fx-padding: 4px 10px;");
			btnDetails.setOnAction(e -> {
				if (currentChat != null && !currentChat.startsWith("[PVT] ")) {
					showGroupDetailsOverlay(currentChat);
				}
			});
			parentHeader.getChildren().add(btnDetails);
		}

		// Enable or disable details button based on if it's a group
		HBox parentHeader = (HBox) lblChatHeader.getParent();
		if (parentHeader.getChildren().size() > 1) {
			parentHeader.getChildren().get(1).setVisible(!chatId.startsWith("[PVT] "));
		}

		if (chatId.startsWith("[PVT] ")) {
			lblChatHeader.setText("Mensagem Privada: " + chatId.substring(6));
		} else {
			lblChatHeader.setText("Grupo: " + chatId);
		}

		// Buscar ou criar historico
		if (!chatHistories.containsKey(chatId)) {
			VBox newHistory = new VBox(10);
			newHistory.setPadding(new Insets(15));
			chatHistories.put(chatId, newHistory);
		}

		VBox history = chatHistories.get(chatId);
		chatScroll.setContent(history);

		// Animar entrada do chat
		history.setOpacity(0);
		TranslateTransition slide = new TranslateTransition(Duration.millis(200), history);
		slide.setFromY(15);
		slide.setToY(0);
		slide.play();

		FadeTransition chatFade = new FadeTransition(Duration.millis(250), history);
		chatFade.setFromValue(0.0);
		chatFade.setToValue(1.0);
		chatFade.play();
	}

	private void onSendMessage(TextField txtMsg) {
		String msg = txtMsg.getText();
		if (msg == null || msg.trim().isEmpty() || currentChat == null)
			return;

		try {
			if (currentChat.startsWith("[PVT] ")) {
				String destino = currentChat.substring(6);
				udp.sendPvt(destino, eu, msg);
			} else {
				udp.send(currentChat, eu, msg);
			}

			addChatBubble(currentChat, eu.getNome(), msg, true, false, false);
			txtMsg.clear();
		} catch (exceptions.ConexaoException e) {
			showErrorOverlay("Erro de Envio", "Falha ao enviar mensagem: " + e.getMessage());
		}
	}

	// =========================================================================
	// BOLHAS DE CHAT E DETALHES
	// =========================================================================
	private Color getColorForName(String name) {
		int hash = name.hashCode();
		// Generate a deterministic color in the EDEN palette range
		int[] palette = {
				0xc4a05a, 0x8c9e5e, 0x5b6623, 0x79874c, 0xc9d873, 0xa0b050, 0x8a9b3a
		};
		int colorHex = palette[Math.abs(hash) % palette.length];
		return Color.web(String.format("#%06x", colorHex));
	}

	private void addChatBubble(String chatId, String senderName, String text, boolean sentByMe, boolean isPrivate,
			boolean isSystem) {
		if (!chatHistories.containsKey(chatId)) {
			VBox newHistory = new VBox(10);
			newHistory.setPadding(new Insets(15));
			chatHistories.put(chatId, newHistory);
		}

		VBox history = chatHistories.get(chatId);

		HBox row = new HBox();
		row.setPadding(new Insets(4, 0, 4, 0));

		if (isSystem) {
			Label lblSys = new Label("-- " + text + " --");
			lblSys.setFont(Font.font("Consolas", 11));
			lblSys.setTextFill(Color.web("#8a9b3a"));
			lblSys.setAlignment(Pos.CENTER);
			row.setAlignment(Pos.CENTER);
			row.getChildren().add(lblSys);
		} else {
			VBox bubbleContainer = new VBox(4);
			bubbleContainer.setMaxWidth(400);

			// Avatar and Name header
			HBox header = new HBox(6);
			header.setAlignment(sentByMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

			Circle avatar = new Circle(10, getColorForName(senderName));
			Label initial = new Label(senderName.substring(0, 1).toUpperCase());
			initial.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
			initial.setTextFill(Color.web("#1a1e0b"));
			StackPane avatarStack = new StackPane(avatar, initial);

			Label nameLbl = new Label(senderName);
			nameLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
			nameLbl.setTextFill(Color.web("#3f4a23"));

			String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
			Label timeLbl = new Label(timestamp);
			timeLbl.setFont(Font.font("Consolas", 10));
			timeLbl.setTextFill(Color.web("#8a9b3a"));

			if (sentByMe) {
				header.getChildren().addAll(timeLbl, nameLbl, avatarStack);
				bubbleContainer.setAlignment(Pos.CENTER_RIGHT);
			} else {
				header.getChildren().addAll(avatarStack, nameLbl, timeLbl);
				bubbleContainer.setAlignment(Pos.CENTER_LEFT);
			}

			Label lblMsg = new Label(text);
			lblMsg.setWrapText(true);
			lblMsg.setFont(Font.font("Segoe UI", 13));

			if (sentByMe) {
				lblMsg.setStyle(
						"-fx-background-color: rgba(91, 102, 35, 0.9); -fx-text-fill: #e5e8d7; -fx-padding: 10px 14px; -fx-background-radius: 15px 0px 15px 15px; -fx-border-color: #c9d873; -fx-border-width: 0 3px 0 0; -fx-border-radius: 15px 0px 15px 15px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 4, 0, -2, 2);");
			} else {
				lblMsg.setStyle(
						"-fx-background-color: rgba(140, 158, 94, 0.9); -fx-text-fill: #1a1e0b; -fx-padding: 10px 14px; -fx-background-radius: 0px 15px 15px 15px; -fx-border-color: #5b6623; -fx-border-width: 0 0 0 3px; -fx-border-radius: 0px 15px 15px 15px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 4, 0, 2, 2);");
				if (isPrivate) {
					lblMsg.setStyle(
							"-fx-background-color: rgba(122, 143, 74, 0.9); -fx-text-fill: #e5e8d7; -fx-padding: 10px 14px; -fx-background-radius: 0px 15px 15px 15px; -fx-border-color: #c9d873; -fx-border-width: 1.5px; -fx-border-radius: 0px 15px 15px 15px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 4, 0, 2, 2);");
				}
			}

			bubbleContainer.getChildren().addAll(header, lblMsg);

			row.getChildren().add(bubbleContainer);
			if (sentByMe)
				row.setAlignment(Pos.CENTER_RIGHT);
			else
				row.setAlignment(Pos.CENTER_LEFT);
		}

		// Animate entrance
		row.setOpacity(0);
		row.setTranslateY(10);

		history.getChildren().add(row);

		FadeTransition fade = new FadeTransition(Duration.millis(200), row);
		fade.setFromValue(0);
		fade.setToValue(1);
		fade.play();

		TranslateTransition slide = new TranslateTransition(Duration.millis(200), row);
		slide.setFromY(10);
		slide.setToY(0);
		slide.play();
	}

	// =========================================================================
	// OVERLAY DE ERRO
	// =========================================================================
	// OVERLAY DE DETALHES DO GRUPO
	// =========================================================================
	private void showGroupDetailsOverlay(String grupo) {
		VBox box = new VBox(15);
		box.setAlignment(Pos.TOP_CENTER);
		box.setMaxSize(350, 400);
		box.setStyle(
				"-fx-background-color: #e3e6d8;" +
						"-fx-border-color: #5b6623; -fx-border-width: 2.5px;" +
						"-fx-background-radius: 12px; -fx-border-radius: 12px;" +
						"-fx-padding: 30px;" +
						"-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 15, 0, 0, 5);");

		Label lblTitle = new Label("Detalhes do Grupo");
		lblTitle.setFont(Font.font("Impact", FontWeight.BOLD, 22));
		lblTitle.setTextFill(Color.web("#5b6623"));

		Label lblGroup = new Label(grupo);
		lblGroup.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
		lblGroup.setTextFill(Color.web("#3f4a23"));

		Label lblMembers = new Label("Membros Ativos Conhecidos:");
		lblMembers.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
		lblMembers.setTextFill(Color.web("#5b6623"));

		ListView<String> membersList = new ListView<>();
		membersList.setStyle("-fx-background-color: rgba(160,176,80,0.2); -fx-background-radius: 8px;");
		membersList.setCellFactory(lv -> createStyledCell());

		Set<String> members = knownGroupMembers.getOrDefault(grupo, new HashSet<>());
		membersList.getItems().add(eu.getNome() + " (Voce)");
		for (String m : members) {
			if (!m.equals(eu.getNome()))
				membersList.getItems().add(m);
		}

		HBox buttons = new HBox(12);
		buttons.setAlignment(Pos.CENTER);

		Button btnClose = new Button("Fechar");
		btnClose.getStyleClass().add("btn-eden");

		Button btnLeave = new Button("Sair do Grupo");
		btnLeave.setStyle(
				"-fx-background-color: linear-gradient(to bottom, #d4b06a, #c4a05a); -fx-text-fill: #3c3010; -fx-background-radius: 20px; -fx-border-color: #a88940; -fx-border-radius: 20px; -fx-border-width: 1px; -fx-padding: 8px 20px; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;");

		buttons.getChildren().addAll(btnClose, btnLeave);

		box.getChildren().addAll(lblTitle, lblGroup, lblMembers, membersList, buttons);

		StackPane overlay = new StackPane(box);
		overlay.setStyle("-fx-background-color: rgba(50, 60, 20, 0.55);");

		Runnable closeOverlay = () -> {
			FadeTransition fadeOut = new FadeTransition(Duration.millis(200), overlay);
			fadeOut.setFromValue(1);
			fadeOut.setToValue(0);
			fadeOut.setOnFinished(ev -> root.getChildren().remove(overlay));
			fadeOut.play();
		};

		btnClose.setOnAction(e -> closeOverlay.run());
		btnLeave.setOnAction(e -> {
			closeOverlay.run();
			// Reuse the existing leave logic
			if (groupList.getItems().contains(grupo)) {
				groupList.getSelectionModel().select(grupo);
				onLeaveGroup();
			}
		});

		overlay.setOpacity(0);
		root.getChildren().add(overlay);
		FadeTransition fadeIn = new FadeTransition(Duration.millis(200), overlay);
		fadeIn.setFromValue(0);
		fadeIn.setToValue(1);
		fadeIn.play();
	}

	// =========================================================================
	// OVERLAY DE ERRO (substitui Alert)
	// =========================================================================
	private void showErrorOverlay(String title, String message) {
		VBox errorBox = new VBox(15);
		errorBox.setAlignment(Pos.CENTER);
		errorBox.setMaxSize(420, 260);
		errorBox.setStyle(
				"-fx-background-color: #e3e6d8;" +
						"-fx-border-color: #5b6623; -fx-border-width: 2.5px;" +
						"-fx-background-radius: 12px; -fx-border-radius: 12px;" +
						"-fx-padding: 30px;" +
						"-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 15, 0, 0, 5);");

		Label lblTitle = new Label("[!] " + title);
		lblTitle.setFont(Font.font("Impact", FontWeight.BOLD, 26));
		lblTitle.setTextFill(Color.web("#5b6623"));

		Label lblMsg = new Label(message);
		lblMsg.setFont(Font.font("Segoe UI", 15));
		lblMsg.setTextFill(Color.web("#3f4a23"));
		lblMsg.setWrapText(true);
		lblMsg.setTextAlignment(TextAlignment.CENTER);

		Button btnOk = new Button("Reconhecer");
		btnOk.getStyleClass().add("btn-eden");

		errorBox.getChildren().addAll(lblTitle, lblMsg, btnOk);

		StackPane overlay = new StackPane(errorBox);
		overlay.setStyle("-fx-background-color: rgba(50, 60, 20, 0.55);");

		btnOk.setOnAction(e -> {
			FadeTransition fadeOut = new FadeTransition(Duration.millis(200), overlay);
			fadeOut.setFromValue(1);
			fadeOut.setToValue(0);
			fadeOut.setOnFinished(ev -> root.getChildren().remove(overlay));
			fadeOut.play();
		});

		overlay.setOpacity(0);
		root.getChildren().add(overlay);
		FadeTransition fadeIn = new FadeTransition(Duration.millis(200), overlay);
		fadeIn.setFromValue(0);
		fadeIn.setToValue(1);
		fadeIn.play();
	}

	// =========================================================================
	// OVERLAY DE INPUT CUSTOMIZADO (substitui TextInputDialog)
	// =========================================================================
	private void showInputOverlay(String title, String prompt, java.util.function.Consumer<String> onConfirm) {
		VBox box = new VBox(15);
		box.setAlignment(Pos.CENTER);
		box.setMaxSize(440, 280);
		box.setStyle(
				"-fx-background-color: #e3e6d8;" +
						"-fx-border-color: #5b6623; -fx-border-width: 2.5px;" +
						"-fx-background-radius: 12px; -fx-border-radius: 12px;" +
						"-fx-padding: 30px;" +
						"-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 15, 0, 0, 5);");

		Label lblTitle = new Label(title);
		lblTitle.setFont(Font.font("Impact", FontWeight.BOLD, 24));
		lblTitle.setTextFill(Color.web("#5b6623"));

		Label lblPrompt = new Label(prompt);
		lblPrompt.setFont(Font.font("Segoe UI", 14));
		lblPrompt.setTextFill(Color.web("#3f4a23"));

		TextField txtInput = new TextField();
		txtInput.setMaxWidth(300);
		txtInput.setStyle(
				"-fx-background-color: rgba(160,176,80,0.35); -fx-background-radius: 20px; -fx-padding: 10px 18px; -fx-font-size: 14px;");

		HBox buttons = new HBox(12);
		buttons.setAlignment(Pos.CENTER);

		Button btnOk = new Button("Confirmar");
		btnOk.getStyleClass().add("btn-eden");

		Button btnCancel = new Button("Cancelar");
		btnCancel.setStyle(
				"-fx-background-color: linear-gradient(to bottom, #d4b06a, #c4a05a); -fx-text-fill: #3c3010; -fx-background-radius: 20px; -fx-border-color: #a88940; -fx-border-radius: 20px; -fx-border-width: 1px; -fx-padding: 8px 20px; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;");

		buttons.getChildren().addAll(btnOk, btnCancel);
		box.getChildren().addAll(lblTitle, lblPrompt, txtInput, buttons);

		StackPane overlay = new StackPane(box);
		overlay.setStyle("-fx-background-color: rgba(50, 60, 20, 0.55);");

		Runnable closeOverlay = () -> {
			FadeTransition fadeOut = new FadeTransition(Duration.millis(200), overlay);
			fadeOut.setFromValue(1);
			fadeOut.setToValue(0);
			fadeOut.setOnFinished(ev -> root.getChildren().remove(overlay));
			fadeOut.play();
		};

		btnOk.setOnAction(e -> {
			String val = txtInput.getText();
			closeOverlay.run();
			onConfirm.accept(val);
		});
		txtInput.setOnAction(e -> btnOk.fire());
		btnCancel.setOnAction(e -> closeOverlay.run());

		overlay.setOpacity(0);
		root.getChildren().add(overlay);
		FadeTransition fadeIn = new FadeTransition(Duration.millis(200), overlay);
		fadeIn.setFromValue(0);
		fadeIn.setToValue(1);
		fadeIn.play();

		txtInput.requestFocus();
	}

	// =========================================================================
	// OVERLAY DE ESCOLHA CUSTOMIZADO (substitui ChoiceDialog)
	// =========================================================================
	private void showChoiceOverlay(String title, String prompt, List<String> options,
			java.util.function.Consumer<String> onConfirm) {
		VBox box = new VBox(15);
		box.setAlignment(Pos.CENTER);
		box.setMaxSize(440, 380);
		box.setStyle(
				"-fx-background-color: #e3e6d8;" +
						"-fx-border-color: #5b6623; -fx-border-width: 2.5px;" +
						"-fx-background-radius: 12px; -fx-border-radius: 12px;" +
						"-fx-padding: 30px;" +
						"-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 15, 0, 0, 5);");

		Label lblTitle = new Label(title);
		lblTitle.setFont(Font.font("Impact", FontWeight.BOLD, 24));
		lblTitle.setTextFill(Color.web("#5b6623"));

		Label lblPrompt = new Label(prompt);
		lblPrompt.setFont(Font.font("Segoe UI", 14));
		lblPrompt.setTextFill(Color.web("#3f4a23"));

		ListView<String> listOptions = new ListView<>();
		listOptions.getItems().addAll(options);
		listOptions.setMaxHeight(160);
		listOptions.setMaxWidth(300);
		listOptions.setStyle("-fx-background-color: rgba(160,176,80,0.2); -fx-background-radius: 8px;");
		listOptions.setCellFactory(lv -> createStyledCell());
		listOptions.getSelectionModel().selectFirst();

		HBox buttons = new HBox(12);
		buttons.setAlignment(Pos.CENTER);

		Button btnOk = new Button("Entrar");
		btnOk.getStyleClass().add("btn-eden");

		Button btnCancel = new Button("Cancelar");
		btnCancel.setStyle(
				"-fx-background-color: linear-gradient(to bottom, #d4b06a, #c4a05a); -fx-text-fill: #3c3010; -fx-background-radius: 20px; -fx-border-color: #a88940; -fx-border-radius: 20px; -fx-border-width: 1px; -fx-padding: 8px 20px; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;");

		buttons.getChildren().addAll(btnOk, btnCancel);
		box.getChildren().addAll(lblTitle, lblPrompt, listOptions, buttons);

		StackPane overlay = new StackPane(box);
		overlay.setStyle("-fx-background-color: rgba(50, 60, 20, 0.55);");

		Runnable closeOverlay = () -> {
			FadeTransition fadeOut = new FadeTransition(Duration.millis(200), overlay);
			fadeOut.setFromValue(1);
			fadeOut.setToValue(0);
			fadeOut.setOnFinished(ev -> root.getChildren().remove(overlay));
			fadeOut.play();
		};

		btnOk.setOnAction(e -> {
			String selected = listOptions.getSelectionModel().getSelectedItem();
			closeOverlay.run();
			if (selected != null)
				onConfirm.accept(selected);
		});
		btnCancel.setOnAction(e -> closeOverlay.run());

		// Duplo clique para entrar direto
		listOptions.setOnMouseClicked(e -> {
			if (e.getClickCount() == 2)
				btnOk.fire();
		});

		overlay.setOpacity(0);
		root.getChildren().add(overlay);
		FadeTransition fadeIn = new FadeTransition(Duration.millis(200), overlay);
		fadeIn.setFromValue(0);
		fadeIn.setToValue(1);
		fadeIn.play();
	}

	// =========================================================================
	// CALLBACKS DO LISTENER (Thread UDP -> GUI)
	// =========================================================================
	@Override
	public void onMessageReceived(String destino, InfoUser remetente, String mensagem, boolean isPrivate) {
		Platform.runLater(() -> {
			String chatId;
			if (isPrivate) {
				chatId = "[PVT] " + remetente.getNome();
				if (!onlineUsersList.getItems().contains(remetente.getNome())) {
					onlineUsersList.getItems().add(remetente.getNome());
				}
			} else {
				chatId = destino;
				if (chatId != null && !chatId.trim().isEmpty()) {
					if (!groupList.getItems().contains(chatId)) {
						groupList.getItems().add(chatId);
					}
					// Adiciona o usuario a lista de conhecidos do grupo
					knownGroupMembers.putIfAbsent(chatId, new HashSet<>());
					knownGroupMembers.get(chatId).add(remetente.getNome());
				}
			}

			if (chatId != null && !chatId.trim().isEmpty()) {
				if (mensagem.equals("~JOINED~")) {
					addChatBubble(chatId, "SYSTEM", remetente.getNome() + " entrou no grupo.", false, false, true);
				} else if (mensagem.equals("~LEFT~")) {
					addChatBubble(chatId, "SYSTEM", remetente.getNome() + " saiu do grupo.", false, false, true);
					if (knownGroupMembers.containsKey(chatId)) {
						knownGroupMembers.get(chatId).remove(remetente.getNome());
					}
				} else {
					addChatBubble(chatId, remetente.getNome(), mensagem, false, isPrivate, false);
				}

				// Unread messages indicator
				if (!chatId.equals(currentChat)) {
					unreadCounts.put(chatId, unreadCounts.getOrDefault(chatId, 0) + 1);
					if (isPrivate)
						onlineUsersList.refresh();
					else
						groupList.refresh();
				}
			}
		});
	}

	@Override
	public void onShutdown() {
		Platform.runLater(() -> {
			showErrorOverlay("Servidor Encerrado", "O servidor foi desligado. A aplicacao sera fechada.");
		});
	}

	@Override
	public void stop() {
		if (tcp != null)
			tcp.fecharConexao();
		if (udp != null)
			udp.fecharConexao();
		System.exit(0);
	}
}
