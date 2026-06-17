import model.Cliente;

import utils.Protocolo;

/**
 * Ponto de entrada da aplicacao do Cliente.
 */
public class Principal {

	public static void main(String[] args) {

		// Configuracoes padrao apontando para o servidor local
		String ipServidor = "127.0.0.1";

		// Opcional: Se quiser permitir passar o IP do servidor por parametro no
		// terminal
		if (args.length >= 1) {
			ipServidor = args[0];
		}

		Cliente cliente = new Cliente(ipServidor, Protocolo.PORTA_SERVIDOR);
		cliente.iniciar();

	}
}