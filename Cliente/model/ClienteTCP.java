package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import utils.APDU;
import utils.InfoUser;
import utils.Protocolo;

/**
 * Classe responsavel por realizar a conexao TCP com o servidor
 * e enviar as mensagens de JOIN e LEAVE para o servidor.
 */
public class ClienteTCP {

	private final Socket conexaoTCP;
	private PrintWriter escritorSaida;
	private BufferedReader leitorEntrada;

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
		this.leitorEntrada = new BufferedReader(new InputStreamReader(conexaoTCP.getInputStream()));
		System.out.println("[CLIENTE:TCP] [INFO] Conectado ao servidor " + ipServidor + ":" + portaServidor);
	}

	/**
	 * Envia mensagem JOIN ao servidor para ingressar em um grupo
	 * 
	 * @param nomeGrupo Nome do grupo
	 * @param usuario   Informacoes do usuario
	 */
	public String join(String nomeGrupo, InfoUser usuario) {
		String apdu = APDU.montarJoin(nomeGrupo, usuario);
		escritorSaida.println(apdu);
		System.out.println("[CLIENTE:TCP] [INFO] JOIN enviado ao servidor: " + usuario.toString());
		try {
			return leitorEntrada.readLine();
		} catch (IOException e) {
			return "ERRO~/Falha de conexao com o servidor";
		}
	}

	/**
	 * Envia mensagem LEAVE ao servidor para sair de um grupo
	 * 
	 * @param nomeGrupo Nome do grupo
	 * @param usuario   Informacoes do usuario
	 */
	public String leave(String nomeGrupo, InfoUser usuario) {
		String apdu = APDU.montarLeave(nomeGrupo, usuario);
		escritorSaida.println(apdu);
		System.out.println("[CLIENTE:TCP] [INFO] LEAVE enviado ao servidor: " + usuario.toString());
		try {
			return leitorEntrada.readLine();
		} catch (IOException e) {
			return "ERRO~/Falha de conexao com o servidor";
		}
	}

	/**
	 * Solicita a lista de grupos ativos no servidor
	 * @return Resposta do servidor formatada
	 */
	public String list() {
		// Envia apenas o comando LIST
		escritorSaida.println(Protocolo.LIST);
		System.out.println("[CLIENTE:TCP] [INFO] LIST enviado ao servidor");
		try {
			return leitorEntrada.readLine();
		} catch (IOException e) {
			return "ERRO~/Falha de conexao com o servidor";
		}
	}

	/**
	 * Envia mensagem REGISTER ao servidor para registrar este cliente silenciosamente
	 * 
	 * @param usuario Informacoes do usuario (com a porta UDP que ele escuta)
	 */
	public void register(InfoUser usuario) {
		String apdu = Protocolo.REGISTER + Protocolo.SEPARADOR_CAMPO_APDU + "GLOBAL" + Protocolo.SEPARADOR_CAMPO_APDU + usuario.empacotar();
		escritorSaida.println(apdu);
		// Nao esperamos resposta do register
	}

	public void fecharConexao() {
		try {
			escritorSaida.close();
			conexaoTCP.close();
			System.out.println("[CLIENTE:TCP] [INFO] Conexao encerrada com o servidor.");
		} catch (IOException e) {
			System.err.println("[CLIENTE:TCP] [ERROR] Erro ao fechar conexao: " + e.getMessage());
		}
	}

	/**
	 * Obtem o IP local vinculado a esta conexao TCP.
	 * Isso evita problemas de Firewall e adaptadores virtuais (VirtualBox/Docker).
	 */
	public String getIpLocal() {
		return conexaoTCP.getLocalAddress().getHostAddress();
	}

} // fim da classe
