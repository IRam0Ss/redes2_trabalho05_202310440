package exceptions;

/**
 * Excecao lancada quando ocorrem erros na conexao TCP ou UDP no cliente.
 */
public class ConexaoException extends EDENSysException {
    /**
     * Construtor com mensagem de erro.
     * 
     * @param message A mensagem de erro detalhada
     */
    public ConexaoException(String message) {
        super(message);
    }

    /**
     * Construtor com mensagem e causa raiz.
     * 
     * @param message A mensagem de erro detalhada
     * @param cause A excecao original que causou o erro
     */
    public ConexaoException(String message, Throwable cause) {
        super(message, cause);
    }
}
