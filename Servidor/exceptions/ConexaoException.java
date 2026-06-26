package exceptions;

/**
 * Excecao lancada quando ocorrem erros na conexao TCP ou UDP no servidor.
 */
public class ConexaoException extends EDENSysException {
    public ConexaoException(String message) {
        super(message);
    }

    public ConexaoException(String message, Throwable cause) {
        super(message, cause);
    }
}
