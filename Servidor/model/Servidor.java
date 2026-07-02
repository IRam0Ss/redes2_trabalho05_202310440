package model;

import controller.GerenciadorGrupos;

import utils.Protocolo;

/**
 * Classe responsavel por iniciar o servidor, gerenciando as threads do
 * protocolo TCP e UDP.
 */
public class Servidor {

	/**
	 * Inicializa as threads TCP, UDP e Discovery do servidor.
	 * Tambem adiciona um hook para fechar as conexoes ao encerrar.
	 */
	public void iniciar() {
		GerenciadorGrupos gerenciador = new GerenciadorGrupos();

		Thread tcp = new Thread(new ServidorTCP(Protocolo.PORTA_SERVIDOR, gerenciador));
		Thread udp = new Thread(new ServidorUDP(Protocolo.PORTA_SERVIDOR, gerenciador));
		Thread discovery = new Thread(new ServidorDiscovery());

		tcp.start();
		udp.start();
		discovery.start();

		System.out.println("=== Servidor IM iniciado ===");
		System.out.println("TCP e UDP rodando na porta " + Protocolo.PORTA_SERVIDOR);

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("\n[SERVIDOR] Recebido sinal de encerramento. Notificando clientes...");
			java.util.Set<utils.InfoUser> usuarios = gerenciador.getTodosUsuariosAtivos();
			try (java.net.DatagramSocket socketUDP = new java.net.DatagramSocket()) {
				for (utils.InfoUser u : usuarios) {
					String apdu = Protocolo.SHUTDOWN + Protocolo.SEPARADOR_CAMPO_APDU + "GLOBAL" + Protocolo.SEPARADOR_CAMPO_APDU
							+ u.empacotar() + Protocolo.SEPARADOR_CAMPO_APDU + "Desligando";
					byte[] dados = apdu.getBytes(java.nio.charset.StandardCharsets.UTF_8);
					java.net.DatagramPacket pacote = new java.net.DatagramPacket(dados, dados.length,
							java.net.InetAddress.getByName(u.getIp()), u.getPorta());
					socketUDP.send(pacote);
				}
			} catch (Exception e) {
				System.err.println("[SERVIDOR] Erro ao notificar: " + e.getMessage());
			}
		}));
	}

}
