import Servidor.controller.GerenciadorGrupos;
import utils.InfoUser;

import java.util.List;

/**
 * Teste simplificado focado EXCLUSIVAMENTE nas estruturas de dados.
 * Valida a lógica de:
 * 1. Serialização do InfoUser (empacotar/desempacotar para a APDU)
 * 2. Regras de negócios do GerenciadorGrupos (join, leave, envio sem repetição,
 * limpeza)
 */
public class TesteSimples {

    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║   TESTE SIMPLIFICADO DAS ESTRUTURAS DE DADOS (SEM REDE)  ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝\n");

        GerenciadorGrupos gerenciador = new GerenciadorGrupos();

        // 1. Instanciando os usuários
        InfoUser alice = new InfoUser("Alice", "192.168.0.10", 7001);
        InfoUser bob = new InfoUser("Bob", "192.168.0.20", 7002);
        InfoUser carlos = new InfoUser("Carlos", "192.168.0.30", 7003);

        System.out.println(">> TESTE 1: Empacotamento para APDU <<");
        String pacote = alice.empacotar();
        System.out.println("Alice formatada (string para rede): " + pacote);
        InfoUser aliceDesempacotada = InfoUser.desempacotar(pacote);
        System.out.println("Alice restaurada no servidor: " + aliceDesempacotada);
        System.out.println("O equals funciona baseando-se no IP+Porta? " + alice.equals(aliceDesempacotada) + "\n");

        System.out.println(">> TESTE 2: Entrando nos grupos (JOIN) <<");
        gerenciador.join("redes2", alice);
        gerenciador.join("redes2", bob);
        gerenciador.join("lazer", alice);
        gerenciador.join("lazer", carlos);
        gerenciador.imprimirEstado();

        System.out.println(">> TESTE 3: Regra de Unicidade (JOIN Duplicado) <<");
        System.out.print("Tentando adicionar a mesma Alice no 'redes2' novamente... ");
        boolean sucessoJoin = gerenciador.join("redes2", alice);
        System.out.println("Retorno: " + sucessoJoin + " (esperado false)\n");

        System.out.println(">> TESTE 4: Filtragem de Destinatários (SEND) <<");
        System.out.println("Alice vai enviar uma mensagem no grupo 'redes2'.");
        List<InfoUser> destinatarios = gerenciador.getMembrosEnvio("redes2", alice);
        System.out.println("Quem deve receber a mensagem: " + destinatarios + " (Bob deve estar aqui, Alice não)\n");

        System.out.println(">> TESTE 5: Saída de grupo (LEAVE) <<");
        System.out.println("Bob decide sair do 'redes2'...");
        gerenciador.leave("redes2", bob);
        gerenciador.imprimirEstado();

        System.out.println(">> TESTE 6: Destruição Automática de Grupo Vazio <<");
        System.out.println("Alice e Carlos decidem sair do 'lazer'...");
        gerenciador.leave("lazer", carlos);
        gerenciador.leave("lazer", alice);
        gerenciador.imprimirEstado();
        System.out.println("O grupo 'lazer' desapareceu? " +
                (gerenciador.getMembrosEnvio("lazer", new InfoUser("Fantasma", "0", 0)).isEmpty() ? "Sim" : "Não")
                + "\n");

        System.out.println("=== FIM DO TESTE SIMPLIFICADO ===");
    }
}
