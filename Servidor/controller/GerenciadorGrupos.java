package Servidor.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.InfoUser;

/**
 * Gerencia os grupos de mensagens e seus respectivos membros.
 * Utiliza estruturas thread-safe para suportar multiplos clientes simultaneos.
 */
public class GerenciadorGrupos {

	private Map<String, List<InfoUser>> gruposExistentes;

	/**
	 * Construtor padrao. Inicializa a estrutura de dados.
	 */
	public GerenciadorGrupos() {
		gruposExistentes = new HashMap<>();
	}

	/**
	 * Adiciona um usuario a um grupo. Se o grupo nao existir, ele e criado
	 * automaticamente.
	 * 
	 * @param nomeGrupo Nome do grupo desejado.
	 * @param usuario   Objeto InfoUser contendo os dados do usuario.
	 * @return true se o usuario foi adicionado com sucesso, false se ele ja estava
	 *         no grupo.
	 */
	public synchronized boolean join(String nomeGrupo, InfoUser usuario) {

		if (!gruposExistentes.containsKey(nomeGrupo)) { // caso o grupo nao exista, criar o grupo e adicionar o membro
			gruposExistentes.put(nomeGrupo, new ArrayList<>());
		}

		List<InfoUser> membros = gruposExistentes.get(nomeGrupo); // lista todos os membros do grupo

		if (membros.contains(usuario)) {
			System.out.println("Membro ja faz parte do grupo.");
			return false;
		}
		membros.add(usuario); // adiciona o novo membro
		return true;
	} // end join

	/**
	 * Remove um usuario de um grupo. Se o grupo ficar vazio apos a remocao, ele e
	 * deletado
	 * para evitar acumulo de grupos fantasmas na memoria.
	 * 
	 * @param nomeGrupo Nome do grupo.
	 * @param usuario   Objeto InfoUser contendo os dados do usuario (Apenas IP e
	 *                  Porta sao necessarios para a remocao).
	 * @return true se removido com sucesso, false se o grupo nao existir ou usuario
	 *         nao pertencer a ele.
	 */
	public synchronized boolean leave(String nomeGrupo, InfoUser usuario) {
		if (!gruposExistentes.containsKey(nomeGrupo)) {
			System.out.println("Grupo nao existe.");
			return false;
		}
		List<InfoUser> membros = gruposExistentes.get(nomeGrupo);

		if (!membros.contains(usuario)) {
			System.out.println("Usuario nao esta no grupo.");
			return false;
		}
		membros.remove(usuario);

		if (membros.isEmpty()) { // se todos os membros sairam, deleta o grupo
			gruposExistentes.remove(nomeGrupo);
		}
		return true;
	} // fim do leave

	/**
	 * Retorna a lista de membros de um grupo, excluindo o remetente.
	 * E utilizado na operacao SEND para encaminhar a mensagem para todos os membros
	 * corretos.
	 * 
	 * @param nomeGrupo        Nome do grupo.
	 * @param usuarioRemetente Usuario que esta enviando a mensagem.
	 * @return Lista de usuarios destinatarios. Retorna uma lista vazia caso o grupo
	 *         nao exista ou o remetente nao pertenca a ele.
	 */
	public synchronized List<InfoUser> getMembrosEnvio(String nomeGrupo, InfoUser usuarioRemetente) {

		if (!gruposExistentes.containsKey(nomeGrupo)) {
			System.out.println("Grupo inexistente para envio.");
			return new ArrayList<>();
		}

		List<InfoUser> listaMembros = new ArrayList<>(gruposExistentes.get(nomeGrupo));

		if (!listaMembros.contains(usuarioRemetente)) {
			System.out.println("Usuario remetente nao esta no grupo.");
			return new ArrayList<>();
		}

		listaMembros.remove(usuarioRemetente);
		return listaMembros;
	} // fim do getMembrosEnvio

	/**
	 * Imprime o estado atual de todos os grupos e seus respectivos membros no
	 * console.
	 */
	public synchronized void imprimirEstado() {
		System.out.println("\n══════ ESTADO DA ED ══════");
		for (Map.Entry<String, List<InfoUser>> entry : gruposExistentes.entrySet()) {
			System.out.println("  [" + entry.getKey() + "] → " + entry.getValue());
		}
		System.out.println("══════════════════════════\n");
	} // fim imprimirEstado

}// fim da class
