package model;

import java.net.InetAddress;
import java.util.Scanner;

import utils.InfoUser;

/**
 * Classe responsavel por orquestrar o cliente.
 * Ela instancia as conexoes TCP e UDP, gerencia a thread de escuta
 * e controla o loop interativo com o usuario.
 */
public class Cliente {

    private String ipServidor;
    private int portaServidorTCP;
    private int portaServidorUDP;

    public Cliente(String ipServidor, int portaServidorTCP, int portaServidorUDP) {
        this.ipServidor = ipServidor;
        this.portaServidorTCP = portaServidorTCP;
        this.portaServidorUDP = portaServidorUDP;
    }

    public void iniciar() {
        Scanner scanner = new Scanner(System.in);

        try {
            // 1. Configuracoes Iniciais
            System.out.println("==========================================");
            System.out.println("            BEM-VINDO AO CHAT             ");
            System.out.println("==========================================");
            System.out.print("Digite seu nome: ");
            String nome = scanner.nextLine();

            System.out.print("Digite a porta UDP que voce vai usar (ex: 7001): ");
            int minhaPortaUDP = Integer.parseInt(scanner.nextLine());

            // Pega o IP local da maquina automaticamente
            String meuIp = InetAddress.getLocalHost().getHostAddress();
            InfoUser eu = new InfoUser(nome, meuIp, minhaPortaUDP);

            // 2. Inicializando as conexoes com o Servidor
            ClienteTCP tcp = new ClienteTCP(ipServidor, portaServidorTCP);
            ClienteUDP udp = new ClienteUDP(ipServidor, portaServidorUDP, minhaPortaUDP);

            // 3. Iniciando a thread que escuta mensagens recebidas via UDP
            Thread threadRecepcao = new Thread(udp);
            threadRecepcao.start();

            System.out.println("\nComandos disponiveis:");
            System.out.println("  /join <grupo>            - Entrar em um grupo");
            System.out.println("  /leave <grupo>           - Sair de um grupo");
            System.out.println("  /send <grupo> <mensagem> - Enviar mensagem");
            System.out.println("  /sair                    - Encerrar aplicativo\n");

            // 4. Loop de interacao com o usuario
            boolean rodando = true;
            while (rodando) {
                String input = scanner.nextLine();
                if (input.trim().isEmpty())
                    continue;

                String[] partes = input.split(" ", 3); // quebra o comando em ate 3 partes
                String comando = partes[0].toLowerCase();

                switch (comando) {
                    case "/join":
                        if (partes.length >= 2)
                            tcp.join(partes[1], eu);
                        else
                            System.out.println("Uso correto: /join <grupo>");
                        break;

                    case "/leave":
                        if (partes.length >= 2)
                            tcp.leave(partes[1], eu);
                        else
                            System.out.println("Uso correto: /leave <grupo>");
                        break;

                    case "/send":
                        if (partes.length >= 3)
                            udp.send(partes[1], eu, partes[2]);
                        else
                            System.out.println("Uso correto: /send <grupo> <mensagem>");
                        break;

                    case "/sair":
                        tcp.fecharConexao();
                        udp.fecharConexao();
                        rodando = false;
                        System.out.println("Saindo...");
                        break;

                    default:
                        System.out.println("Comando desconhecido.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scanner.close();
            System.exit(0);
        }
    }
}
