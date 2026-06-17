package utils;

/**
 * Classe responsavel por armazenar as constantes de protocolo entre cliente e
 * servidor.
 */
public class Protocolo {
    // define o comando das apdus
    public static final String JOIN = "JOIN";
    public static final String LEAVE = "LEAVE";
    public static final String SEND = "SEND";

    // porta padrao do servidor
    public static final int PORTA_SERVIDOR = 5000;

    // separador (flags)
    public static final String SEPARADOR_CAMPO_APDU = "~/";

    // indices do comando desmembrado
    public static final int IDX_COMANDO = 0;
    public static final int IDX_GRUPO = 1;
    public static final int IDX_USUARIO = 2;
    public static final int IDX_MENSAGEM = 3; // usado para os send

}// fim Protocolo
