package utils;

import java.util.Objects;
import java.net.URLEncoder;
import java.net.URLDecoder;

/**
 * Classe responsavel por armazenar as informacoes dos usuarios conectados
 * A identificacao unica de um usuario e dada pela combinacao de seu IP e
 * nome.
 */
public class InfoUser {

	private String nome;
	private String ip;
	private int porta;

	/**
	 * Construtor padrao da classe InfoUser.
	 * 
	 * @param nome  Nome do usuario cliente.
	 * @param ip    Endereco IP do cliente.
	 * @param porta Porta UDP onde o cliente escuta mensagens.
	 */
	public InfoUser(String nome, String ip, int porta) {
		this.nome = nome;
		this.ip = ip;
		this.porta = porta;
	}

	/**
	 * Obtem o nome do usuario.
	 * 
	 * @return O nome do usuario.
	 */
	public String getNome() {
		return nome;
	}

	/**
	 * Obtem o endereco IP do usuario.
	 * 
	 * @return O endereco IP.
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * Obtem a porta UDP onde o usuario recebe mensagens.
	 * 
	 * @return A porta UDP.
	 */
	public int getPorta() {
		return porta;
	}

	/**
	 * Empacota o objeto em uma unica string para ser enviada pela rede.
	 * O formato sera "nome;ip;porta". Assim, ele ocupa apenas UM campo na APDU
	 * separada por "|".
	 * 
	 * @return String compacta formatada para a APDU.
	 */
	public String empacotar() {
		try {
			String nomeSeguro = URLEncoder.encode(this.nome, "UTF-8");
			return nomeSeguro + ";" + this.ip + ";" + this.porta;
		} catch (Exception e) {
			return this.nome + ";" + this.ip + ";" + this.porta;
		}
	}

	/**
	 * Desempacota uma string recebida da rede e a transforma de volta em um objeto
	 * InfoUser.
	 * 
	 * @param dados String no formato "nome;ip;porta".
	 * @return Objeto InfoUser devidamente instanciado.
	 */
	public static InfoUser desempacotar(String dados) {

		if (dados == null || dados.isEmpty()) {
			throw new IllegalArgumentException("Dados invalidos na APDU: " + dados);
		}

		String[] partes = dados.split(";");
		if (partes.length < 3) {
			throw new IllegalArgumentException("Formato de usuario invalido na APDU: " + dados);
		}
		try {
			String nomeDecodificado = URLDecoder.decode(partes[0], "UTF-8");
			return new InfoUser(nomeDecodificado, partes[1], Integer.parseInt(partes[2]));
		} catch (Exception e) {
			throw new IllegalArgumentException("Porta invalida na APDU: " + partes[2]);
		}
	}

	/**
	 * Retorna uma representacao em string legivel para o terminal.
	 * 
	 * @return String contendo nome, IP e porta do usuario.
	 */
	@Override
	public String toString() {
		return "[Nome: " + nome + " | IP: " + ip + " | Porta: " + porta + "]";
	}

	/**
	 * Compara se dois usuarios sao iguais baseando-se em seus IPs e Nomes.
	 * Esta e a chave de unicidade dentro da estrutura de dados.
	 * 
	 * @param obj Objeto a ser comparado.
	 * @return true se tiverem o mesmo IP e Nome, false caso contrario.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof InfoUser)) {
			return false;
		}
		InfoUser comparado = (InfoUser) obj;
		return this.ip.equals(comparado.ip) && this.nome.equals(comparado.nome);
	}

	/**
	 * Gera um codigo hash unico para o usuario, combinando IP e Porta.
	 * Obrigatorio para o funcionamento do map e set
	 * 
	 * @return O codigo hash gerado.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(this.ip, this.nome);
	}

} // fim class
