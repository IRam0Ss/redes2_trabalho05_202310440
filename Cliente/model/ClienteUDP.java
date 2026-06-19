package model;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import utils.APDU;
import utils.InfoUser;

public class ClienteUDP implements Runnable {

	private int portaServidor;
	private final InetAddress IP_SERVIDOR;
	private DatagramSocket socketUDP;
	private static final int BUFFER = 4096;

	public ClienteUDP(String ipServidor, int portaServidor)
			throws UnknownHostException, SocketException {
		this.IP_SERVIDOR = InetAddress.getByName(ipServidor);
		this.portaServidor = portaServidor;

		// Construtor sem passar porta -> O Sistema Operacional escolhe uma porta livre!
		socketUDP = new DatagramSocket();

		System.out.println("[CLIENTE:UDP] [INFO] Socket vinculado a porta efemera " + socketUDP.getLocalPort());
	}

	/**
	 * Retorna a porta aleatoria que o Sistema Operacional atribuiu ao socket
	 */
	public int getPortaLocal() {
		return socketUDP.getLocalPort();
	}

	/**
	 * Monta uma APDU do tipo SEND e envia ao servidor.
	 * 
	 * @param nomeGrupo Nome do grupo.
	 * @param mensagem  Mensagem a ser enviada.
	 */
	public void send(String nomeGrupo, InfoUser usuario, String mensagem) {
		// montar apdu
		String apdu = APDU.montarSend(nomeGrupo, usuario, mensagem);
		byte[] dadosEnviados = apdu.getBytes();

		DatagramPacket pacoteEnvio = new DatagramPacket(dadosEnviados, dadosEnviados.length, IP_SERVIDOR, portaServidor);
		try {
			socketUDP.send(pacoteEnvio);
			System.out.println("[CLIENTE:UDP] [INFO] Mensagem enviada ao servidor.");
		} catch (IOException e) {
			System.out.println("[CLIENTE:UDP] [ERROR] Falha ao enviar mensagem para o servidor.");
			e.printStackTrace();
		}
	}

	/**
	 * Responsavel pela recepcao de mensagens enviadas pelo servidor
	 */
	public void run() {
		byte[] bufferRecepcao = new byte[BUFFER];

		while (!socketUDP.isClosed()) {
			DatagramPacket pacoteRecebido = new DatagramPacket(bufferRecepcao, bufferRecepcao.length);
			try {
				socketUDP.receive(pacoteRecebido);
				String apdu = new String(pacoteRecebido.getData(), 0, pacoteRecebido.getLength());
				
				String comando = APDU.extrairComando(apdu);
				if (utils.Protocolo.SHUTDOWN.equals(comando)) {
					System.out.println("\n[SISTEMA] O servidor foi encerrado. A aplicacao sera finalizada.");
					System.exit(0);
				}

				InfoUser usuario = APDU.extrairUsuario(apdu);
				String mensagem = APDU.extrairMensagem(apdu);
				System.out.println("\n[CLIENTE:UDP] [INFO] Nova mensagem recebida:\n" + usuario.toString() + " enviou: " + mensagem);

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

	public void fecharConexao() {
		this.socketUDP.close();
	}

}
