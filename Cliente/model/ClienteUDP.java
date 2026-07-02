package model;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import utils.APDU;
import utils.InfoUser;

/**
 * Classe responsavel por gerenciar as conexoes UDP do Cliente.
 * Cuida do envio e recebimento de mensagens e notificacoes P2P/Server.
 */
public class ClienteUDP implements Runnable {

	private int portaServidor;
	private final InetAddress IP_SERVIDOR;
	private DatagramSocket socketUDP;
	private static final int BUFFER = 4096;
	private MessageListener listener;

	/**
	 * Construtor que inicializa o socket UDP do cliente.
	 * 
	 * @param ipServidor    IP do servidor alvo
	 * @param portaServidor Porta do servidor alvo
	 * @throws UnknownHostException Caso o host nao seja encontrado
	 * @throws SocketException      Caso haja erro ao vincular a porta
	 */
	public ClienteUDP(String ipServidor, int portaServidor)
			throws UnknownHostException, SocketException {
		this.IP_SERVIDOR = InetAddress.getByName(ipServidor);
		this.portaServidor = portaServidor;

		// Construtor sem passar porta -> O Sistema Operacional escolhe uma porta livre!
		socketUDP = new DatagramSocket();

		System.out.println("[CLIENTE:UDP] [INFO] Socket vinculado a porta efemera " + socketUDP.getLocalPort());
	}

	/**
	 * Retorna a porta aleatoria que o Sistema Operacional atribuiu ao socket.
	 * 
	 * @return A porta local do socket UDP
	 */
	public int getPortaLocal() {
		return socketUDP.getLocalPort();
	}

	/**
	 * Define o listener para callbacks de mensagens recebidas.
	 * 
	 * @param listener O listener que recebera as chamadas
	 */
	public void setListener(MessageListener listener) {
		this.listener = listener;
	}

	/**
	 * Monta uma APDU do tipo SEND e envia ao servidor.
	 * 
	 * @param nomeGrupo Nome do grupo
	 * @param usuario   Informacoes de quem envia
	 * @param mensagem  Mensagem a ser enviada
	 * @throws exceptions.ConexaoException Caso ocorra erro de envio
	 */
	public void send(String nomeGrupo, InfoUser usuario, String mensagem) throws exceptions.ConexaoException {
		// montar apdu
		String apdu = APDU.montarSend(nomeGrupo, usuario, mensagem);
		byte[] dadosEnviados = apdu.getBytes(java.nio.charset.StandardCharsets.UTF_8);

		DatagramPacket pacoteEnvio = new DatagramPacket(dadosEnviados, dadosEnviados.length, IP_SERVIDOR, portaServidor);
		try {
			socketUDP.send(pacoteEnvio);
			System.out.println("[CLIENTE:UDP] [INFO] Mensagem enviada ao servidor.");
		} catch (IOException e) {
			System.out.println("[CLIENTE:UDP] [ERROR] Falha ao enviar mensagem para o servidor.");
			e.printStackTrace();
			throw new exceptions.ConexaoException("Falha ao enviar mensagem via UDP", e);
		}
	}

	/**
	 * Monta uma APDU do tipo SENDPVT e envia ao servidor.
	 * 
	 * @param nomeDestinatario Nome do destinatario
	 * @param usuario          InfoUser de quem envia
	 * @param mensagem         Mensagem a ser enviada
	 * @throws exceptions.ConexaoException Caso ocorra erro de envio
	 */
	public void sendPvt(String nomeDestinatario, InfoUser usuario, String mensagem) throws exceptions.ConexaoException {
		String apdu = APDU.montarSendPvt(nomeDestinatario, usuario, mensagem);
		byte[] dadosEnviados = apdu.getBytes(java.nio.charset.StandardCharsets.UTF_8);

		DatagramPacket pacoteEnvio = new DatagramPacket(dadosEnviados, dadosEnviados.length, IP_SERVIDOR, portaServidor);
		try {
			socketUDP.send(pacoteEnvio);
			System.out.println("[CLIENTE:UDP] [INFO] Mensagem privada enviada ao servidor.");
		} catch (IOException e) {
			System.out.println("[CLIENTE:UDP] [ERROR] Falha ao enviar mensagem privada.");
			e.printStackTrace();
			throw new exceptions.ConexaoException("Falha ao enviar mensagem privada via UDP", e);
		}
	}

	/**
	 * Thread principal responsavel pela recepcao de mensagens enviadas pelo
	 * servidor.
	 */
	public void run() {
		byte[] bufferRecepcao = new byte[BUFFER];

		while (!socketUDP.isClosed()) {
			DatagramPacket pacoteRecebido = new DatagramPacket(bufferRecepcao, bufferRecepcao.length);
			try {
				socketUDP.receive(pacoteRecebido);
				String apdu = new String(pacoteRecebido.getData(), 0, pacoteRecebido.getLength(),
						java.nio.charset.StandardCharsets.UTF_8);

				String comando = APDU.extrairComando(apdu);
				if (utils.Protocolo.SHUTDOWN.equals(comando)) {
					System.out.println("\n[SISTEMA] O servidor foi encerrado. A aplicacao sera finalizada.");
					if (listener != null)
						listener.onShutdown();
					else
						System.exit(0);
				}

				if (utils.Protocolo.UPDATE_USERS.equals(comando)) {
					System.out.println("[CLIENTE:UDP] [INFO] Recebida notificacao de atualizacao de usuarios.");
					if (listener != null)
						listener.onUpdateUsers();
					continue;
				}

				if (utils.Protocolo.SENDPVT.equals(comando)) {
					InfoUser remetente = APDU.extrairUsuario(apdu);
					String mensagemPvt = APDU.extrairMensagem(apdu);
					System.out.println("\n[MENSAGEM PRIVADA] " + remetente.getNome() + " diz: " + mensagemPvt);
					if (listener != null)
						listener.onMessageReceived(remetente.getNome(), remetente, mensagemPvt, true);
					continue; // Pula o processamento padrao de grupo abaixo
				}

				InfoUser usuario = APDU.extrairUsuario(apdu);
				String mensagem = APDU.extrairMensagem(apdu);
				String grupo = APDU.extrairGrupo(apdu);
				System.out.println("\n[CLIENTE:UDP] [INFO] Nova mensagem recebida no grupo " + grupo + ":\n"
						+ usuario.toString() + " enviou: " + mensagem);
				if (listener != null)
					listener.onMessageReceived(grupo, usuario, mensagem, false);

			} catch (SocketException e) {
				// Excecao esperada ao fechar o socket durante o receive
			} catch (IOException e) {
				System.out.println("[CLIENTE:UDP] [ERROR] Falha ao receber mensagem do servidor.");
				e.printStackTrace();
			} catch (Exception e) {
				System.out.println("[CLIENTE:UDP] [ERROR] Falha ao processar o pacote APDU recebido. Erro interno.");
				e.printStackTrace();
			}
		}

	}

	/**
	 * Fecha o socket UDP e finaliza a conexao.
	 */
	public void fecharConexao() {
		this.socketUDP.close();
	}

}
