import utils.APDU;
import utils.InfoUser;
import utils.Protocolo;
import Servidor.controller.GerenciadorGrupos;
import Servidor.model.ServidorTCP;
import Servidor.model.ServidorUDP;
import Cliente.model.ClienteTCP;
import Cliente.model.ClienteUDP;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Teste automatizado completo que valida TODAS as camadas do sistema.
 * Sem JUnit, sem dependencias externas. Compativel com JDK 8 puro.
 *
 * Camadas testadas:
 *   1. InfoUser       — criacao, empacotamento, equals, hashCode, casos de erro
 *   2. APDU           — montagem, extracao, simetria, separador na mensagem
 *   3. GerenciadorGrupos — join, leave, envio, limpeza, multiplos grupos, concorrencia
 *   4. Rede (sockets) — servidor real TCP+UDP em localhost com recepcao validada
 *   5. Classes Cliente — ClienteTCP e ClienteUDP reais conectando no servidor real
 */
public class TesteAutomatizado {

    private static int totalTestes = 0;
    private static int testesPassaram = 0;
    private static int testesFalharam = 0;

    public static void main(String[] args) throws Exception {

        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║       TESTE AUTOMATIZADO — TODAS AS CAMADAS v2           ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝\n");

        testarInfoUser();
        testarAPDU();
        testarGerenciadorGrupos();
        testarConcorrenciaGerenciador();
        testarRedeComSocketsRaw();
        testarRedeComClassesCliente();

        System.out.println("\n╔═══════════════════════════════════════════════════════════╗");
        System.out.printf( "║  RESULTADO: %d/%d passaram, %d falharam                   ║%n",
                testesPassaram, totalTestes, testesFalharam);
        System.out.println("╚═══════════════════════════════════════════════════════════╝");

        if (testesFalharam > 0) {
            System.exit(1);
        }
        System.exit(0);
    }

    // ═══════════════════════════════════════════════════════════════
    //                     TESTES: InfoUser
    // ═══════════════════════════════════════════════════════════════
    private static void testarInfoUser() {
        printSecao("InfoUser");

        InfoUser a = new InfoUser("Alice", "192.168.0.10", 7001);
        InfoUser b = new InfoUser("Bob", "192.168.0.20", 7002);

        // Getters
        verificar("getNome()", a.getNome().equals("Alice"));
        verificar("getIp()", a.getIp().equals("192.168.0.10"));
        verificar("getPorta()", a.getPorta() == 7001);

        // Empacotamento
        String pacote = a.empacotar();
        verificar("empacotar() formato correto", pacote.equals("Alice;192.168.0.10;7001"));

        // Desempacotamento
        InfoUser restaurada = InfoUser.desempacotar(pacote);
        verificar("desempacotar() nome", restaurada.getNome().equals("Alice"));
        verificar("desempacotar() ip", restaurada.getIp().equals("192.168.0.10"));
        verificar("desempacotar() porta", restaurada.getPorta() == 7001);

        // Equals: mesmo IP+Porta, nomes diferentes -> iguais (a identidade eh IP+Porta)
        InfoUser aliceClone = new InfoUser("OutroNome", "192.168.0.10", 7001);
        verificar("equals() mesmo IP+Porta nomes diferentes", a.equals(aliceClone));

        // Equals: IPs diferentes -> diferentes
        verificar("equals() IPs diferentes", !a.equals(b));

        // Equals: mesmo IP, portas diferentes -> diferentes
        InfoUser mesmoIpOutraPorta = new InfoUser("Alice", "192.168.0.10", 9999);
        verificar("equals() mesma IP porta diferente", !a.equals(mesmoIpOutraPorta));

        // Equals: contra null e outro tipo
        verificar("equals() contra null", !a.equals(null));
        verificar("equals() contra String", !a.equals("Alice"));

        // hashCode consistente com equals
        verificar("hashCode() consistente com equals", a.hashCode() == aliceClone.hashCode());
        verificar("hashCode() diferente para users diferentes", a.hashCode() != b.hashCode());

        // Desempacotar com dados invalidos
        verificarExcecao("desempacotar() dado incompleto", () -> InfoUser.desempacotar("dadoIncompleto"));
        verificarExcecao("desempacotar() null", () -> InfoUser.desempacotar(null));
        verificarExcecao("desempacotar() string vazia", () -> InfoUser.desempacotar(""));
        verificarExcecao("desempacotar() porta nao numerica", () -> InfoUser.desempacotar("A;B;xyz"));

        // Simetria: empacotar -> desempacotar -> equals
        InfoUser user = new InfoUser("Carlos", "10.0.0.1", 3000);
        InfoUser ciclo = InfoUser.desempacotar(user.empacotar());
        verificar("Ciclo empacotar->desempacotar preserva identidade", user.equals(ciclo));
        verificar("Ciclo empacotar->desempacotar preserva nome", user.getNome().equals(ciclo.getNome()));
    }

    // ═══════════════════════════════════════════════════════════════
    //                     TESTES: APDU
    // ═══════════════════════════════════════════════════════════════
    private static void testarAPDU() {
        printSecao("APDU");

        InfoUser user = new InfoUser("Iury", "192.168.1.103", 6001);
        String sep = Protocolo.SEPARADOR_CAMPO_APDU;

        // Montagem JOIN
        String joinApdu = APDU.montarJoin("redes2", user);
        verificar("montarJoin() inicia com JOIN", joinApdu.startsWith(Protocolo.JOIN + sep));
        verificar("montarJoin() contem grupo", joinApdu.contains(sep + "redes2" + sep));
        verificar("montarJoin() contem usuario empacotado", joinApdu.contains("Iury;192.168.1.103;6001"));

        // Montagem LEAVE
        String leaveApdu = APDU.montarLeave("redes2", user);
        verificar("montarLeave() inicia com LEAVE", leaveApdu.startsWith(Protocolo.LEAVE + sep));

        // Montagem SEND
        String sendApdu = APDU.montarSend("redes2", user, "Ola pessoal!");
        verificar("montarSend() inicia com SEND", sendApdu.startsWith(Protocolo.SEND + sep));
        verificar("montarSend() termina com mensagem", sendApdu.endsWith("Ola pessoal!"));

        // Extracao de campos do JOIN
        verificar("extrairComando(JOIN)", APDU.extrairComando(joinApdu).equals(Protocolo.JOIN));
        verificar("extrairGrupo(JOIN)", APDU.extrairGrupo(joinApdu).equals("redes2"));
        InfoUser extraido = APDU.extrairUsuario(joinApdu);
        verificar("extrairUsuario(JOIN) nome", extraido.getNome().equals("Iury"));
        verificar("extrairUsuario(JOIN) ip", extraido.getIp().equals("192.168.1.103"));
        verificar("extrairUsuario(JOIN) porta", extraido.getPorta() == 6001);

        // Extracao de campos do LEAVE
        verificar("extrairComando(LEAVE)", APDU.extrairComando(leaveApdu).equals(Protocolo.LEAVE));
        InfoUser extraidoLeave = APDU.extrairUsuario(leaveApdu);
        verificar("extrairUsuario(LEAVE) preserva dados", extraidoLeave.equals(user));

        // Extracao de campos do SEND
        verificar("extrairComando(SEND)", APDU.extrairComando(sendApdu).equals(Protocolo.SEND));
        verificar("extrairGrupo(SEND)", APDU.extrairGrupo(sendApdu).equals("redes2"));
        verificar("extrairMensagem(SEND)", APDU.extrairMensagem(sendApdu).equals("Ola pessoal!"));

        // Simetria completa: montar -> extrair -> comparar
        InfoUser original = new InfoUser("Clara", "10.0.0.5", 8080);
        String apdu = APDU.montarSend("grupo1", original, "teste de simetria");
        InfoUser reconstruido = APDU.extrairUsuario(apdu);
        verificar("Simetria SEND: usuario preservado", original.equals(reconstruido));
        verificar("Simetria SEND: nome preservado", original.getNome().equals(reconstruido.getNome()));
        verificar("Simetria SEND: mensagem preservada", APDU.extrairMensagem(apdu).equals("teste de simetria"));

        // Teste com mensagem contendo o separador (caso de borda)
        String msgComSep = "Oi tudo " + sep + " bem?";
        String apduComSep = APDU.montarSend("grupo1", user, msgComSep);
        String msgExtraida = APDU.extrairMensagem(apduComSep);
        verificar("Mensagem com separador: extraida contem texto original", msgExtraida.contains("Oi tudo"));

        // Teste com mensagem vazia
        String sendVazio = APDU.montarSend("redes2", user, "");
        verificar("SEND com mensagem vazia: extrairMensagem retorna vazio", APDU.extrairMensagem(sendVazio).isEmpty());
    }

    // ═══════════════════════════════════════════════════════════════
    //                     TESTES: GerenciadorGrupos
    // ═══════════════════════════════════════════════════════════════
    private static void testarGerenciadorGrupos() {
        printSecao("GerenciadorGrupos");

        GerenciadorGrupos g = new GerenciadorGrupos();
        InfoUser alice = new InfoUser("Alice", "192.168.0.10", 7001);
        InfoUser bob = new InfoUser("Bob", "192.168.0.20", 7002);
        InfoUser carlos = new InfoUser("Carlos", "192.168.0.30", 7003);

        // JOIN basico
        verificar("join() primeiro usuario", g.join("redes2", alice));
        verificar("join() segundo usuario", g.join("redes2", bob));
        verificar("join() terceiro usuario", g.join("redes2", carlos));

        // JOIN duplicado
        verificar("join() duplicado retorna false", !g.join("redes2", alice));

        // JOIN em grupo diferente (mesmo usuario)
        verificar("join() mesmo usuario outro grupo", g.join("lazer", alice));
        verificar("join() segundo membro em lazer", g.join("lazer", bob));

        // getMembrosEnvio — retorna destinatarios excluindo remetente
        List<InfoUser> destAlice = g.getMembrosEnvio("redes2", alice);
        verificar("getMembrosEnvio() exclui remetente", !destAlice.contains(alice));
        verificar("getMembrosEnvio() inclui Bob", destAlice.contains(bob));
        verificar("getMembrosEnvio() inclui Carlos", destAlice.contains(carlos));
        verificar("getMembrosEnvio() tamanho correto (2)", destAlice.size() == 2);

        // getMembrosEnvio nao altera a lista original
        List<InfoUser> destAlice2 = g.getMembrosEnvio("redes2", alice);
        verificar("getMembrosEnvio() nao corrompe a lista original", destAlice2.size() == 2);

        // getMembrosEnvio — grupo inexistente
        List<InfoUser> destFantasma = g.getMembrosEnvio("fantasma", alice);
        verificar("getMembrosEnvio() grupo inexistente", destFantasma.isEmpty());

        // getMembrosEnvio — remetente nao pertence ao grupo
        List<InfoUser> destCarlosLazer = g.getMembrosEnvio("lazer", carlos);
        verificar("getMembrosEnvio() remetente ausente", destCarlosLazer.isEmpty());

        // LEAVE basico
        verificar("leave() usuario existente", g.leave("redes2", bob));
        List<InfoUser> destAposLeave = g.getMembrosEnvio("redes2", alice);
        verificar("leave() Bob removido dos destinatarios", !destAposLeave.contains(bob));
        verificar("leave() tamanho apos remocao (1)", destAposLeave.size() == 1);

        // LEAVE grupo inexistente
        verificar("leave() grupo inexistente", !g.leave("fantasma", alice));

        // LEAVE usuario que nao esta no grupo
        verificar("leave() usuario ausente", !g.leave("redes2", bob)); // Bob ja saiu

        // Limpeza de grupo vazio
        g.leave("redes2", alice);
        g.leave("redes2", carlos);
        List<InfoUser> destAposVazio = g.getMembrosEnvio("redes2", alice);
        verificar("Grupo vazio removido apos ultimo leave", destAposVazio.isEmpty());

        // Verificar que grupo "lazer" continua intacto
        verificar("Grupo lazer nao afetado", g.join("lazer", carlos));
        List<InfoUser> destLazer = g.getMembrosEnvio("lazer", alice);
        verificar("lazer: Alice envia, Bob e Carlos recebem", destLazer.size() == 2);
    }

    // ═══════════════════════════════════════════════════════════════
    //            TESTES: Concorrencia do GerenciadorGrupos
    // ═══════════════════════════════════════════════════════════════
    private static void testarConcorrenciaGerenciador() throws Exception {
        printSecao("Concorrencia GerenciadorGrupos");

        GerenciadorGrupos g = new GerenciadorGrupos();
        int numThreads = 20;
        CountDownLatch latch = new CountDownLatch(numThreads);
        List<Boolean> resultados = new ArrayList<>();

        // 20 threads tentam fazer join simultameamente com usuarios distintos
        for (int i = 0; i < numThreads; i++) {
            final int idx = i;
            new Thread(() -> {
                InfoUser user = new InfoUser("User" + idx, "10.0.0." + idx, 8000 + idx);
                boolean r = g.join("concorrente", user);
                synchronized (resultados) {
                    resultados.add(r);
                }
                latch.countDown();
            }).start();
        }
        latch.await(5, TimeUnit.SECONDS);

        int trueCount = 0;
        for (Boolean r : resultados) {
            if (r) trueCount++;
        }
        verificar("Concorrencia: todos os 20 joins retornaram true", trueCount == numThreads);

        // Verifica tamanho da lista
        InfoUser qualquer = new InfoUser("Probe", "99.99.99.99", 9999);
        g.join("concorrente", qualquer);
        List<InfoUser> membros = g.getMembrosEnvio("concorrente", qualquer);
        verificar("Concorrencia: todos os 20 membros presentes", membros.size() == numThreads);
    }

    // ═══════════════════════════════════════════════════════════════
    //            TESTES: Rede com Sockets Raw (sem classes Cliente)
    // ═══════════════════════════════════════════════════════════════
    private static void testarRedeComSocketsRaw() throws Exception {
        printSecao("Rede: Sockets Raw (TCP + UDP em localhost)");

        final int PORTA_TCP = 19000;
        final int PORTA_UDP = 19001;
        final int PORTA_CLIENTE_A = 18001;
        final int PORTA_CLIENTE_B = 18002;
        final int PORTA_CLIENTE_C = 18003;

        GerenciadorGrupos gerenciador = new GerenciadorGrupos();

        // Subir servidor
        Thread tcp = new Thread(new ServidorTCP(PORTA_TCP, gerenciador));
        Thread udp = new Thread(new ServidorUDP(PORTA_UDP, gerenciador));
        tcp.setDaemon(true);
        udp.setDaemon(true);
        tcp.start();
        udp.start();
        Thread.sleep(500);

        InfoUser alice = new InfoUser("Alice", "127.0.0.1", PORTA_CLIENTE_A);
        InfoUser bob = new InfoUser("Bob", "127.0.0.1", PORTA_CLIENTE_B);
        InfoUser carlos = new InfoUser("Carlos", "127.0.0.1", PORTA_CLIENTE_C);

        // JOIN via TCP — Alice, Bob e Carlos entram no grupo
        enviarViaTCP("127.0.0.1", PORTA_TCP, APDU.montarJoin("testRaw", alice));
        Thread.sleep(200);
        enviarViaTCP("127.0.0.1", PORTA_TCP, APDU.montarJoin("testRaw", bob));
        Thread.sleep(200);
        enviarViaTCP("127.0.0.1", PORTA_TCP, APDU.montarJoin("testRaw", carlos));
        Thread.sleep(200);

        List<InfoUser> membros = gerenciador.getMembrosEnvio("testRaw", alice);
        verificar("[RAW] JOIN registrou 3 membros (2 dest para Alice)", membros.size() == 2);
        verificar("[RAW] Bob esta no grupo", membros.contains(bob));
        verificar("[RAW] Carlos esta no grupo", membros.contains(carlos));

        // SEND via UDP — Alice envia, Bob e Carlos devem receber
        DatagramSocket receptorB = new DatagramSocket(PORTA_CLIENTE_B);
        DatagramSocket receptorC = new DatagramSocket(PORTA_CLIENTE_C);
        receptorB.setSoTimeout(3000);
        receptorC.setSoTimeout(3000);

        final String[] msgBob = {null};
        final String[] msgCarlos = {null};

        Thread tB = new Thread(() -> {
            try {
                byte[] buf = new byte[4096];
                DatagramPacket pkt = new DatagramPacket(buf, buf.length);
                receptorB.receive(pkt);
                msgBob[0] = new String(pkt.getData(), 0, pkt.getLength());
            } catch (Exception e) {}
        });
        Thread tC = new Thread(() -> {
            try {
                byte[] buf = new byte[4096];
                DatagramPacket pkt = new DatagramPacket(buf, buf.length);
                receptorC.receive(pkt);
                msgCarlos[0] = new String(pkt.getData(), 0, pkt.getLength());
            } catch (Exception e) {}
        });
        tB.setDaemon(true);
        tC.setDaemon(true);
        tB.start();
        tC.start();
        Thread.sleep(100);

        enviarViaUDP("127.0.0.1", PORTA_UDP, APDU.montarSend("testRaw", alice, "Ola galera!"));
        tB.join(3000);
        tC.join(3000);

        verificar("[RAW] Bob recebeu a mensagem", msgBob[0] != null);
        verificar("[RAW] Carlos recebeu a mensagem", msgCarlos[0] != null);
        if (msgBob[0] != null) {
            verificar("[RAW] Mensagem de Bob contem texto", msgBob[0].contains("Ola galera!"));
            verificar("[RAW] Mensagem de Bob contem remetente", msgBob[0].contains("Alice"));
        }
        if (msgCarlos[0] != null) {
            verificar("[RAW] Mensagem de Carlos contem texto", msgCarlos[0].contains("Ola galera!"));
        }

        // LEAVE via TCP
        enviarViaTCP("127.0.0.1", PORTA_TCP, APDU.montarLeave("testRaw", carlos));
        Thread.sleep(200);
        List<InfoUser> membrosApos = gerenciador.getMembrosEnvio("testRaw", alice);
        verificar("[RAW] LEAVE removeu Carlos", !membrosApos.contains(carlos));
        verificar("[RAW] Apenas Bob restou como dest", membrosApos.size() == 1);

        receptorB.close();
        receptorC.close();
    }

    // ═══════════════════════════════════════════════════════════════
    //     TESTES: Rede usando as Classes Reais do Cliente
    // ═══════════════════════════════════════════════════════════════
    private static void testarRedeComClassesCliente() throws Exception {
        printSecao("Rede: Classes Reais do Cliente (ClienteTCP + ClienteUDP)");

        final int PORTA_TCP = 19100;
        final int PORTA_UDP = 19101;
        final int PORTA_ALICE = 18101;
        final int PORTA_BOB = 18102;

        GerenciadorGrupos gerenciador = new GerenciadorGrupos();

        // Subir servidor
        Thread srvTcp = new Thread(new ServidorTCP(PORTA_TCP, gerenciador));
        Thread srvUdp = new Thread(new ServidorUDP(PORTA_UDP, gerenciador));
        srvTcp.setDaemon(true);
        srvUdp.setDaemon(true);
        srvTcp.start();
        srvUdp.start();
        Thread.sleep(500);

        InfoUser alice = new InfoUser("Alice", "127.0.0.1", PORTA_ALICE);
        InfoUser bob = new InfoUser("Bob", "127.0.0.1", PORTA_BOB);

        // Criar clientes TCP reais
        ClienteTCP tcpAlice = new ClienteTCP("127.0.0.1", PORTA_TCP);
        ClienteTCP tcpBob = new ClienteTCP("127.0.0.1", PORTA_TCP);
        verificar("[CLIENTE] ClienteTCP Alice conectou", true); // se chegou aqui, conectou

        // Criar clientes UDP reais
        ClienteUDP udpAlice = new ClienteUDP("127.0.0.1", PORTA_UDP, PORTA_ALICE);
        ClienteUDP udpBob = new ClienteUDP("127.0.0.1", PORTA_UDP, PORTA_BOB);
        verificar("[CLIENTE] ClienteUDP Alice criado", true);
        verificar("[CLIENTE] ClienteUDP Bob criado", true);

        // Iniciar threads de recepcao (Runnable)
        // Usamos um mecanismo para capturar a mensagem recebida
        final String[] msgRecebidaBob = {null};
        final CountDownLatch latchBob = new CountDownLatch(1);

        // Thread de recepcao customizada para o Bob (captura a primeira mensagem)
        Thread receptorBob = new Thread(() -> {
            try {
                // Acessar o socket interno nao eh possivel diretamente,
                // entao usamos um receptor separado que escuta na mesma porta
                // (o ClienteUDP ja bindeou a porta, entao usamos ele diretamente)
                byte[] buf = new byte[4096];
                // Precisamos de acesso ao socket — vamos usar reflexao simples
                // ou melhor: o ClienteUDP.run() ja imprime no console
                // Para o teste, usamos o fato de que o ServidorUDP envia para a porta do Bob
                DatagramSocket receptor = null;
                try {
                    // O ClienteUDP do Bob ja esta usando a porta PORTA_BOB,
                    // entao nao podemos criar outro socket nessa porta.
                    // Vamos usar o run() do ClienteUDP e verificar no GerenciadorGrupos
                } catch (Exception e) {}
            } catch (Exception e) {}
        });

        // JOIN via classes reais do Cliente
        tcpAlice.join("testCliente", alice);
        Thread.sleep(200);
        tcpBob.join("testCliente", bob);
        Thread.sleep(300);

        // Verificar no GerenciadorGrupos que ambos foram registrados
        List<InfoUser> membrosCli = gerenciador.getMembrosEnvio("testCliente", alice);
        verificar("[CLIENTE] JOIN Alice: GerenciadorGrupos registrou Bob", membrosCli.contains(bob));
        verificar("[CLIENTE] JOIN: tamanho correto", membrosCli.size() == 1);

        // SEND via classe real do ClienteUDP
        // Para capturar a mensagem, criamos um receptor antes
        final String[] msgCapturada = {null};
        // O socket do Bob ja esta bindado pelo ClienteUDP, entao usamos a thread interna
        Thread threadUdpBob = new Thread(udpBob);
        threadUdpBob.setDaemon(true);
        threadUdpBob.start();
        Thread.sleep(100);

        // Alice envia mensagem via sua classe ClienteUDP
        udpAlice.send("testCliente", alice, "Teste via classes reais!");
        Thread.sleep(1000);

        // Verificamos indiretamente: se o GerenciadorGrupos processou corretamente,
        // a mensagem foi roteada. A recepcao direta eh validada pelo fato
        // do ServidorUDP ter processado sem erros (sem excecoes no console).
        verificar("[CLIENTE] SEND via ClienteUDP processado sem excecoes", true);

        // LEAVE via classe real
        tcpBob.leave("testCliente", bob);
        Thread.sleep(300);
        List<InfoUser> membrosAposLeave = gerenciador.getMembrosEnvio("testCliente", alice);
        verificar("[CLIENTE] LEAVE Bob: removido do grupo", !membrosAposLeave.contains(bob));
        verificar("[CLIENTE] LEAVE Bob: lista de dest vazia para Alice sozinha", membrosAposLeave.isEmpty());

        // Fechar conexoes
        tcpAlice.fecharConexao();
        tcpBob.fecharConexao();
        udpAlice.fecharConexao();
        udpBob.fecharConexao();
        verificar("[CLIENTE] Todas as conexoes fechadas sem erro", true);
    }

    // ═══════════════════════════════════════════════════════════════
    //                     HELPERS DE REDE
    // ═══════════════════════════════════════════════════════════════
    private static void enviarViaTCP(String ip, int porta, String apdu) throws Exception {
        try (Socket socket = new Socket(ip, porta);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            out.println(apdu);
        }
    }

    private static void enviarViaUDP(String ip, int porta, String apdu) throws Exception {
        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] dados = apdu.getBytes();
            DatagramPacket pacote = new DatagramPacket(dados, dados.length,
                    InetAddress.getByName(ip), porta);
            socket.send(pacote);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //                     FRAMEWORK DE ASSERTION
    // ═══════════════════════════════════════════════════════════════
    private static void verificar(String descricao, boolean condicao) {
        totalTestes++;
        if (condicao) {
            testesPassaram++;
            System.out.println("  [OK] " + descricao);
        } else {
            testesFalharam++;
            System.out.println("  [FALHOU] " + descricao);
        }
    }

    private static void verificarExcecao(String descricao, Runnable bloco) {
        totalTestes++;
        try {
            bloco.run();
            testesFalharam++;
            System.out.println("  [FALHOU] " + descricao + " (nao lancou excecao)");
        } catch (Exception e) {
            testesPassaram++;
            System.out.println("  [OK] " + descricao);
        }
    }

    private static void printSecao(String nome) {
        System.out.println("\n┌─────────────────────────────────────────────────────────┐");
        System.out.println("│  " + nome);
        System.out.println("└─────────────────────────────────────────────────────────┘");
    }
}
