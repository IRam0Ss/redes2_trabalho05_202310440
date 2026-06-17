import model.Servidor;

/**
 * Ponto de entrada da aplicacao do servidor
 */
public class Principal {

  public static void main(String[] args) {
    Servidor servidor = new Servidor();
    servidor.iniciar();
  }

}
