package Cliente.model;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import utils.APDU;
import utils.InfoUser;

/**
 * Classe responsavel por realizar a conexao TCP com o servidor
 * e enviar as mensagens de JOIN e LEAVE para o servidor.
 */
public class ClienteTCP {

	private final Socket conexaoTCP;
	private PrintWriter escritorSaida;

	/**
	 * Construtor que inicializa a conexao TCP com o servidor
	 * 
	 * @param ipServidor
	 * @param portaServidor
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public ClienteTCP(String ipServidor, int portaServidor) throws UnknownHostException, IOException {
		this.conexaoTCP = new Socket(ipServidor, portaServidor);
		this.escritorSaida = new PrintWriter(conexaoTCP.getOutputStream(), true);
		System.out.println("[C_TCP] Conectado ao servidor " + ipServidor + ":" + portaServidor);
	}

	/**
	 * Envia mensagem JOIN ao servidor para ingressar em um grupo
	 * 
	 * @param nomeGrupo Nome do grupo
	 * @param usuario   Informacoes do usuario
	 */
	public void join(String nomeGrupo, InfoUser usuario) {
		String apdu = APDU.montarJoin(nomeGrupo, usuario);
		escritorSaida.println(apdu);
		System.out.println("[C_TCP] JOIN enviado ao servidor: " + usuario.toString());
	}

	/**
	 * Envia mensagem LEAVE ao servidor para sair de um grupo
	 * 
	 * @param nomeGrupo Nome do grupo
	 * @param usuario   Informacoes do usuario
	 */
	public void leave(String nomeGrupo, InfoUser usuario) {
		String apdu = APDU.montarLeave(nomeGrupo, usuario);
		escritorSaida.println(apdu);
		System.out.println("[C_TCP] LEAVE enviado ao servidor: " + usuario.toString());
	}

	public void fecharConexao() {
		try {
			escritorSaida.close();
			conexaoTCP.close();
			System.out.println("[C_TCP] Conexao encerrada com o servidor.");
		} catch (IOException e) {
			System.err.println("[C_TCP] Erro ao fechar conexao: " + e.getMessage());
		}
	}

} // fim da classe
