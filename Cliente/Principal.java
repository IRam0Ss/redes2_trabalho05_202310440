
/**
 * Classe principal do Cliente.
 * Responsavel por inicializar a aplicacao grafica JavaFX.
 * 
 * Autor: Iury Ramos Sodre
 * Matricula: 202310440
 * Inicio: 15/06/2026
 * Ultima alteracao: 26/06/2026
 * Nome: Sistema de Comunicacao Interno da E.D.E.N
 * Funcao: Uma aplicacao de instant messages entre usuarios de uma rede.
 */
import javafx.application.Application;
import view.ClienteGUI;

/**
 * Ponto de entrada da aplicacao do Cliente.
 */
public class Principal {

	/**
	 * Metodo principal que inicia a interface grafica.
	 * 
	 * @param args Argumentos de linha de comando
	 */
	public static void main(String[] args) {
		// Inicia a Aplicacao Grafica JavaFX
		Application.launch(ClienteGUI.class, args);
	}
}