/**
 * Classe principal do Servidor.
 * Responsavel por inicializar os modulos de rede e logica do servidor E.D.E.N.
 * 
 * Autor: Iury Ramos Sodre
 * Matricula: 202310440
 * Inicio: 15/06/2026
 * Ultima alteracao: 26/06/2026
 * Nome: Sistema de Comunicacao Interno da E.D.E.N
 * Funcao: Uma aplicacao de instant messages entre usuarios de uma rede.
 */
import model.Servidor;

/**
 * Ponto de entrada da aplicacao do servidor.
 */
public class Principal {

  /**
   * Metodo principal que inicia o servidor.
   * 
   * @param args Argumentos de linha de comando
   */
  public static void main(String[] args) {
    Servidor servidor = new Servidor();
    servidor.iniciar();
  }

}
