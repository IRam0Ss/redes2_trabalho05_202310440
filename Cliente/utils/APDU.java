package utils;

public class APDU {

	// montagem da apdu
	/**
	 * Monta a APDU de join seguindo o protocolo
	 * 
	 * @param nomeGrupo
	 * @param usuario
	 * @return
	 */
	public static String montarJoin(String nomeGrupo, InfoUser usuario) {
		String join = Protocolo.JOIN + Protocolo.SEPARADOR_CAMPO_APDU + nomeGrupo + Protocolo.SEPARADOR_CAMPO_APDU
				+ usuario.empacotar();

		return join;
	}

	/**
	 * Monta a APDU de leave seguindo o protocolo
	 * 
	 * @param nomeGrupo
	 * @param usuario
	 * @return
	 */
	public static String montarLeave(String nomeGrupo, InfoUser usuario) {
		String leave = Protocolo.LEAVE + Protocolo.SEPARADOR_CAMPO_APDU + nomeGrupo + Protocolo.SEPARADOR_CAMPO_APDU
				+ usuario.empacotar();
		return leave;
	}

	/**
	 * Monta a APDU de send seguindo o protocolo
	 * 
	 * @param nomeGrupo
	 * @param usuario
	 * @param mensagem
	 * @return
	 */
	public static String montarSend(String nomeGrupo, InfoUser usuario, String mensagem) {
		String send = Protocolo.SEND + Protocolo.SEPARADOR_CAMPO_APDU + nomeGrupo + Protocolo.SEPARADOR_CAMPO_APDU
				+ usuario.empacotar() + Protocolo.SEPARADOR_CAMPO_APDU + mensagem;
		return send;
	}

	// desempacotamento da apdu

	/**
	 * Retorna o comando da APDU
	 * 
	 * @param apdu
	 * @return
	 */
	public static String extrairComando(String apdu) {
		String comando = apdu.split(Protocolo.SEPARADOR_CAMPO_APDU)[Protocolo.IDX_COMANDO];
		return comando;
	}

	/**
	 * Retorna o nome do grupo da APDU
	 * 
	 * @param apdu
	 * @return
	 */
	public static String extrairGrupo(String apdu) {
		String grupo = apdu.split(Protocolo.SEPARADOR_CAMPO_APDU)[Protocolo.IDX_GRUPO];
		return grupo;
	}

	/**
	 * Retorna o usuário da APDU
	 * 
	 * @param apdu
	 * @return
	 */
	public static InfoUser extrairUsuario(String apdu) {
		String usuario = apdu.split(Protocolo.SEPARADOR_CAMPO_APDU)[Protocolo.IDX_USUARIO];
		return InfoUser.desempacotar(usuario);
	}

	/**
	 * Retorna a mensagem da APDU
	 * 
	 * @param apdu
	 * @return
	 */
	public static String extrairMensagem(String apdu) {
		String mensagem = apdu.split(Protocolo.SEPARADOR_CAMPO_APDU, 4)[Protocolo.IDX_MENSAGEM];
		return mensagem;
	}

}// fim APDU
