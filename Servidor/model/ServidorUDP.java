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
 * Classe responsavel por administrar o servidor UDP, recebendo e enviando
 * pacotes UDP.
 */
public class ServidorUDP implements Runnable {

	private int porta;
	private GerenciadorGrupos gerenciador;
	private static final int BUFFER = 4096; // valor em bytes para evitar o truncamento de mensagens com pacote UDP

	/**
	 * Construtor do ServidorUDP.
	 * 
	 * @param porta       A porta UDP a ser escutada
	 * @param gerenciador O gerenciador de grupos e usuarios
	 */
	public ServidorUDP(int porta, GerenciadorGrupos gerenciador) {
		this.porta = porta;
		this.gerenciador = gerenciador;
	}

	@Override
	public void run() {
		try (DatagramSocket conexaoUDP = new DatagramSocket(this.porta)) {

			System.out.println("[SERVIDOR:UDP] [INFO] Escutando na porta " + porta);
			byte[] dadosEntrada = new byte[BUFFER];

			while (true) {
				DatagramPacket pacoteDadoRecebido = new DatagramPacket(dadosEntrada, dadosEntrada.length);
				conexaoUDP.receive(pacoteDadoRecebido);

				String apdu = new String(pacoteDadoRecebido.getData(), 0, pacoteDadoRecebido.getLength(),
						java.nio.charset.StandardCharsets.UTF_8);
				processarAPDU(conexaoUDP, apdu);

			}

		} catch (Exception e) {
			System.err.println("[SERVIDOR:UDP] [FATAL ERROR] Erro critico no socket UDP: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Processa a APDU recebida pelo servidor via UDP.
	 * 
	 * @param conexaoUDP Socket UDP para enviar respostas
	 * @param apdu       APDU recebida
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

				System.out.println("[SERVIDOR:UDP] [INFO] Recebido comando SEND do usuario '" + usuarioRemetente.getNome()
						+ "' para o grupo '" + nomeGrupo + "'");

				// montar a string que os clientes alvos vao receber
				String apduEnviada = APDU.montarSend(nomeGrupo, usuarioRemetente, mensagem);

				byte[] dadosEnviados = apduEnviada.getBytes(java.nio.charset.StandardCharsets.UTF_8);

				List<InfoUser> destinatarios = gerenciador.getMembrosEnvio(nomeGrupo, usuarioRemetente);

				for (InfoUser membroDestinatario : destinatarios) {
					try {

						InetAddress ipDestinatario = InetAddress.getByName(membroDestinatario.getIp());
						DatagramPacket pacoteEnvio = new DatagramPacket(dadosEnviados, dadosEnviados.length, ipDestinatario,
								membroDestinatario.getPorta());

						conexaoUDP.send(pacoteEnvio);
						System.out
								.println("[SERVIDOR:UDP] [INFO] Encaminhando APDU SEND para '" + membroDestinatario.getNome() + "'");

					} catch (Exception e) {
						System.err.println("[SERVIDOR:UDP] [ERROR] Falha ao enviar para '" + membroDestinatario.getNome() + "' - "
								+ e.getMessage());
						// Como e um loop de envio, apenas logamos, nao paramos o envio para os outros
						// membros
					}
				}

				break;

			case Protocolo.SENDPVT:
				String nomeDestino = APDU.extrairGrupo(apdu);
				InfoUser remetentePvt = APDU.extrairUsuario(apdu);

				System.out.println("[SERVIDOR:UDP] [INFO] Recebido comando SENDPVT de '" + remetentePvt.getNome() + "' para '"
						+ nomeDestino + "'");

				InfoUser destinoInfo = gerenciador.buscarUsuarioPorNome(nomeDestino);

				if (destinoInfo != null) {
					try {
						// Repassamos a apdu exata (que o cliente ja montou) para o destino
						byte[] dadosPvt = apdu.getBytes(java.nio.charset.StandardCharsets.UTF_8);
						InetAddress ipDest = InetAddress.getByName(destinoInfo.getIp());
						DatagramPacket pacotePvt = new DatagramPacket(dadosPvt, dadosPvt.length, ipDest, destinoInfo.getPorta());
						conexaoUDP.send(pacotePvt);
						System.out.println("[SERVIDOR:UDP] [INFO] SENDPVT encaminhado com sucesso para '" + nomeDestino + "'");
					} catch (Exception e) {
						System.err.println("[SERVIDOR:UDP] [ERROR] Falha ao encaminhar SENDPVT: " + e.getMessage());
					}
				} else {
					System.out.println("[SERVIDOR:UDP] [WARNING] Destinatario '" + nomeDestino + "' nao encontrado online.");
				}
				break;

			default:
				System.out.println("[SERVIDOR:UDP] [WARNING] Comando desconhecido: " + comando);
				break;
		}

	} // fim do processarAPDU

} // fim ServidorUDP
