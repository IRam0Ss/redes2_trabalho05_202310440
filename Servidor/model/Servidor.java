package model;

import controller.GerenciadorGrupos;

import utils.Protocolo;

/**
 * Classe responsavel por iniciar o servidor, gerenciando as threads do
 * protocolo TCP e UDP
 */
public class Servidor {

	/**
	 * Inicializa as threads TCP e UDP do servidor
	 */
	public void iniciar() {
		GerenciadorGrupos gerenciador = new GerenciadorGrupos();

		Thread tcp = new Thread(new ServidorTCP(Protocolo.PORTA_SERVIDOR, gerenciador));
		Thread udp = new Thread(new ServidorUDP(Protocolo.PORTA_SERVIDOR, gerenciador));

		tcp.start();
		udp.start();

		System.out.println("=== Servidor IM iniciado ===");
		System.out.println("TCP e UDP rodando na porta " + Protocolo.PORTA_SERVIDOR);
	}

}
