package Cliente;

import Cliente.model.Cliente;

/**
 * Ponto de entrada da aplicacao do Cliente.
 */
public class Principal {

	public static void main(String[] args) {
		
		// Configuracoes padrao apontando para o servidor local
		String ipServidor = "127.0.0.1";
		int portaServidorTCP = 5000;
		int portaServidorUDP = 5001;

		// Opcional: Se quiser permitir passar o IP do servidor por parametro no terminal
		if (args.length >= 1) {
			ipServidor = args[0];
		}

		Cliente cliente = new Cliente(ipServidor, portaServidorTCP, portaServidorUDP);
		cliente.iniciar();

	}
}