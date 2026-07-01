package utils;

/**
 * Classe responsavel por armazenar as constantes de protocolo entre cliente e
 * servidor.
 */
public class Protocolo {
    /** Comando JOIN: entrar em grupo */
    public static final String JOIN = "JOIN";
    /** Comando LEAVE: sair de grupo */
    public static final String LEAVE = "LEAVE";
    /** Comando SEND: enviar mensagem a grupo */
    public static final String SEND = "SEND";
    /** Comando LIST: listar grupos */
    public static final String LIST = "LIST";
    /** Comando REGISTER: registrar no servidor */
    public static final String REGISTER = "REGISTER";
    /** Comando SHUTDOWN: servidor desligando */
    public static final String SHUTDOWN = "SHUTDOWN";
    /** Comando SENDPVT: mensagem privada */
    public static final String SENDPVT = "SENDPVT";
    /** Comando LISTUSERS: listar usuarios globais */
    public static final String LISTUSERS = "LISTUSERS";
    /** Comando LISTMEMBERS: listar membros de um grupo especifico */
    public static final String LISTMEMBERS = "LISTMEMBERS";
    /** Comando UPDATE_USERS: notificacao UDP de que um usuario conectou ou desconectou */
    public static final String UPDATE_USERS = "UPDATE_USERS";

    /** Porta padrao do servidor E.D.E.N */
    public static final int PORTA_SERVIDOR = 5000;

    /** Separador usado na string da APDU */
    public static final String SEPARADOR_CAMPO_APDU = "~/";

    // indices do comando desmembrado
    public static final int IDX_COMANDO = 0;
    public static final int IDX_GRUPO = 1;
    public static final int IDX_USUARIO = 2;
    public static final int IDX_MENSAGEM = 3;

}// fim Protocolo
