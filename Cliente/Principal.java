import model.Cliente;

import utils.Protocolo;

import javafx.application.Application;
import view.ClienteGUI;

/**
 * Ponto de entrada da aplicacao do Cliente.
 */
public class Principal {

	public static void main(String[] args) {

		// Inicia a Aplicacao Grafica JavaFX
		Application.launch(ClienteGUI.class, args);

	}
}