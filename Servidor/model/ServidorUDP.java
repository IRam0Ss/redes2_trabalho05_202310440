package model;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;

import controller.GerenciadorGrupos;
import utils.APDU;
import utils.InfoUser;
import utils.Protocolo;

/**
 * Classe responsvel por administrar o servidor UDP, recebendo e enviando
 * pacotes UDP.
 */
public class ServidorUDP implements Runnable {

	private int porta;
	private GerenciadorGrupos gerenciador;
	private static final int BUFFER = 4096; // valor em bytes para evitar o truncamento de mensagens com pacote UDP

	public ServidorUDP(int porta, GerenciadorGrupos gerenciador) {
		this.porta = porta;
		this.gerenciador = gerenciador;
	}

	@Override
	public void run() {
		try (DatagramSocket conexaoUDP = new DatagramSocket(this.porta)) {

			System.out.println("[UDP] escutando a porta " + porta);
			byte[] dadosEntrada = new byte[BUFFER];

			while (true) {
				DatagramPacket pacoteDadoRecebido = new DatagramPacket(dadosEntrada, dadosEntrada.length);
				conexaoUDP.receive(pacoteDadoRecebido);

				String apdu = new String(pacoteDadoRecebido.getData(), 0, pacoteDadoRecebido.getLength());
				processarAPDU(conexaoUDP, apdu);

			}

		} catch (Exception e) {
			System.err.println("[UDP] Erro: " + e.getMessage());
		}
	}

	/**
	 * Processa o APDU recebido pelo servidor.
	 * 
	 * @param conexaoUDP Socket UDP para enviar respostas.
	 * @param apdu       APDU recebido.
	 */
	private void processarAPDU(DatagramSocket conexaoUDP, String apdu) {

		String comando = APDU.extrairComando(apdu);
		if (comando == null || comando.isEmpty()) {
			return;
		}

		switch (comando) {
			case Protocolo.SEND:
				String nomeGrupo = APDU.extrairGrupo(apdu);
				InfoUser usuarioRemetente = APDU.extrairUsuario(apdu);
				String mensagem = APDU.extrairMensagem(apdu);

				// montar a string que os clientes alvos vao receber
				String apduEnviada = APDU.montarSend(nomeGrupo, usuarioRemetente, mensagem);

				byte[] dadosEnviados = apduEnviada.getBytes();

				List<InfoUser> destinatarios = gerenciador.getMembrosEnvio(nomeGrupo, usuarioRemetente);

				for (InfoUser membroDestinatario : destinatarios) {
					try {

						InetAddress ipDestinatario = InetAddress.getByName(membroDestinatario.getIp());
						DatagramPacket pacoteEnvio = new DatagramPacket(dadosEnviados, dadosEnviados.length, ipDestinatario,
								membroDestinatario.getPorta());

						conexaoUDP.send(pacoteEnvio);
						System.out.println("[SEND] → " + membroDestinatario.getNome() + ": " + mensagem);

					} catch (Exception e) {
						System.err.println("[SEND] Falha ao enviar para " + membroDestinatario.getNome());
					}
				}

				break;

			default:
				System.out.println("[ERRO] Comando desconhecido: " + comando);
				break;
		}

	} // fim do processarAPDU

} // fim ServidorUDP
