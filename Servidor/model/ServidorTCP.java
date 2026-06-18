package model;

import java.net.ServerSocket;
import java.net.Socket;

import controller.GerenciadorGrupos;
import controller.AtendimentoCliente;

/**
 * Classe responsavel por gerar a conexao TCP entre cliente e servidor.
 * Ela eh responsavel por criar o socket e ficar escutando por novas conexoes e
 * gerar uma thread de atendimento unico para o cliente que se conectar,
 * permitindo multiplos atendimentos
 */
public class ServidorTCP implements Runnable {

	private int porta;
	private GerenciadorGrupos gerenciador;

	public ServidorTCP(int porta, GerenciadorGrupos gerenciador) {
		this.porta = porta;
		this.gerenciador = gerenciador;
	}

	@Override
	public void run() {
		try (ServerSocket servidorTCP = new ServerSocket(porta)) {
			System.out.println("[SERVIDOR:TCP] [INFO] Escutando na porta " + porta);

			while (true) { // mantem a conexao existente sempre esperando clientes se conectarem
				Socket conexaoClienteTCP = servidorTCP.accept(); // conexao do cliente com o servidor
				System.out.println("[SERVIDOR:TCP] [INFO] Nova conexao recebida de " + conexaoClienteTCP.getInetAddress().getHostAddress());

				// criar uma Thread do servidor para cada cliente unico
				Thread threadAtendimentoCliente = new Thread(new AtendimentoCliente(conexaoClienteTCP, gerenciador));
				threadAtendimentoCliente.start();
			} // fim while

		} catch (Exception e) {
			System.err.println("[SERVIDOR:TCP] [ERROR] " + e.getMessage());
		}
	}

}
