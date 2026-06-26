package exceptions;

/**
 * Excecao base para erros do sistema E.D.E.N. (Servidor).
 */
public class EDENSysException extends Exception {
    public EDENSysException(String message) {
        super(message);
    }

    public EDENSysException(String message, Throwable cause) {
        super(message, cause);
    }
}
