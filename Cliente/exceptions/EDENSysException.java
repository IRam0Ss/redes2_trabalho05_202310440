package exceptions;

/**
 * Excecao base para erros do sistema E.D.E.N.
 */
public class EDENSysException extends Exception {
	/**
	 * Construtor com mensagem de erro.
	 * 
	 * @param message A mensagem de erro
	 */
	public EDENSysException(String message) {
		super(message);
	}

	/**
	 * Construtor com mensagem e causa raiz.
	 * 
	 * @param message A mensagem de erro
	 * @param cause   A excecao original
	 */
	public EDENSysException(String message, Throwable cause) {
		super(message, cause);
	}
}
