package Servidor.model;

import Servidor.controller.GerenciadorGrupos;

/**
 * Classe responsavel por iniciar o servidor, gerenciando as threads do
 * protocolo TCP e UDP
 */
public class Servidor {

	private static final int PORTA_TCP = 5000;
	private static final int PORTA_UDP = 5001;

	/**
	 * Inicializa as threads TCP e UDP do servidor
	 */
	public void iniciar() {
		GerenciadorGrupos gerenciador = new GerenciadorGrupos();

		Thread tcp = new Thread(new ServidorTCP(PORTA_TCP, gerenciador));
		Thread udp = new Thread(new ServidorUDP(PORTA_UDP, gerenciador));

		tcp.start();
		udp.start();

		System.out.println("=== Servidor IM iniciado ===");
		System.out.println("TCP (JOIN/LEAVE) -> porta " + PORTA_TCP);
		System.out.println("UDP (SEND)       -> porta " + PORTA_UDP);
	}

}
