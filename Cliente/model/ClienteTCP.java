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
	 * Construtor que inicializa a conexao TCP com o servidor.
	 * 
	 * @param ipServidor IP do servidor
	 * @param portaServidor Porta do servidor
	 * @throws UnknownHostException Caso o host nao seja encontrado
	 * @throws IOException Erro de entrada/saida na conexao
	 */
	public ClienteTCP(String ipServidor, int portaServidor) throws UnknownHostException, IOException {
		this.conexaoTCP = new Socket(ipServidor, portaServidor);
		this.escritorSaida = new PrintWriter(new java.io.OutputStreamWriter(conexaoTCP.getOutputStream(), java.nio.charset.StandardCharsets.UTF_8), true);
		this.leitorEntrada = new BufferedReader(new InputStreamReader(conexaoTCP.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
		System.out.println("[CLIENTE:TCP] [INFO] Conectado ao servidor " + ipServidor + ":" + portaServidor);
	}

	/**
	 * Envia mensagem JOIN ao servidor para ingressar em um grupo.
	 * 
	 * @param nomeGrupo Nome do grupo
	 * @param usuario   Informacoes do usuario
	 * @return Resposta do servidor
	 */
	public synchronized String join(String nomeGrupo, InfoUser usuario) {
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
	 * Envia mensagem LEAVE ao servidor para sair de um grupo.
	 * 
	 * @param nomeGrupo Nome do grupo
	 * @param usuario   Informacoes do usuario
	 * @return Resposta do servidor
	 */
	public synchronized String leave(String nomeGrupo, InfoUser usuario) {
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
	public synchronized String list() {
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
	 * Envia mensagem REGISTER ao servidor para registrar este cliente
	 * e validar o nome de usuario.
	 * 
	 * @param usuario Informacoes do usuario (com a porta UDP que ele escuta)
	 * @return Resposta do servidor
	 */
	public synchronized String register(InfoUser usuario) {
		String apdu = Protocolo.REGISTER + Protocolo.SEPARADOR_CAMPO_APDU + "GLOBAL" + Protocolo.SEPARADOR_CAMPO_APDU + usuario.empacotar();
		escritorSaida.println(apdu);
		try {
			return leitorEntrada.readLine();
		} catch (IOException e) {
			return "ERRO~/Falha de conexao com o servidor";
		}
	}

	/**
	 * Solicita a lista de usuarios conectados ao servidor
	 * @return Resposta do servidor com os nomes separados por virgula
	 */
	public synchronized String listUsers() {
		escritorSaida.println(Protocolo.LISTUSERS);
		System.out.println("[CLIENTE:TCP] [INFO] LISTUSERS enviado ao servidor");
		try {
			return leitorEntrada.readLine();
		} catch (IOException e) {
			return "ERRO~/Falha de conexao com o servidor";
		}
	}

	/**
	 * Solicita a lista de membros de um grupo
	 * @param nomeGrupo Nome do grupo
	 * @return Resposta do servidor com os nomes separados por virgula
	 */
	public synchronized String listMembers(String nomeGrupo) {
		String safeGrupo = nomeGrupo;
		try {
			safeGrupo = java.net.URLEncoder.encode(nomeGrupo, "UTF-8");
		} catch (Exception e) {}
		String apdu = Protocolo.LISTMEMBERS + Protocolo.SEPARADOR_CAMPO_APDU + safeGrupo + Protocolo.SEPARADOR_CAMPO_APDU + "dummy";
		escritorSaida.println(apdu);
		System.out.println("[CLIENTE:TCP] [INFO] LISTMEMBERS enviado ao servidor para grupo: " + nomeGrupo);
		try {
			return leitorEntrada.readLine();
		} catch (IOException e) {
			return "ERRO~/Falha de conexao com o servidor";
		}
	}

	/**
	 * Encerra a conexao TCP com o servidor.
	 */
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
	 * 
	 * @return IP local da conexao
	 */
	public String getIpLocal() {
		return conexaoTCP.getLocalAddress().getHostAddress();
	}

} // fim da classe
