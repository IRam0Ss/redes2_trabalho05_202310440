package model;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Thread responsavel por funcionar como um farol na rede local.
 * Ela escuta broadcasts na porta 5001 e responde aos clientes informando
 * que o servidor E.D.E.N. esta rodando nesta maquina.
 */
public class ServidorDiscovery implements Runnable {

    private final int DISCOVERY_PORT = 5001;
    private final String DISCOVER_MESSAGE = "DISCOVER_EDEN";
    private final String RESPONSE_MESSAGE = "EDEN_HERE";
    private DatagramSocket socket;

    @Override
    public void run() {
        try {
            socket = new DatagramSocket(DISCOVERY_PORT);
            System.out.println("[DISCOVERY] Farol UDP ligado na porta " + DISCOVERY_PORT + " aguardando buscas...");

            byte[] buffer = new byte[256];

            while (!socket.isClosed()) {
                DatagramPacket pacoteRecebido = new DatagramPacket(buffer, buffer.length);
                socket.receive(pacoteRecebido);

                String mensagem = new String(pacoteRecebido.getData(), 0, pacoteRecebido.getLength(), java.nio.charset.StandardCharsets.UTF_8).trim();

                if (mensagem.equals(DISCOVER_MESSAGE)) {
                    System.out.println("[DISCOVERY] Busca recebida de: " + pacoteRecebido.getAddress().getHostAddress());
                    
                    // Responde dizendo "Estou aqui"
                    byte[] dadosResposta = RESPONSE_MESSAGE.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    DatagramPacket pacoteResposta = new DatagramPacket(
                            dadosResposta,
                            dadosResposta.length,
                            pacoteRecebido.getAddress(),
                            pacoteRecebido.getPort());
                    
                    socket.send(pacoteResposta);
                }
            }
        } catch (Exception e) {
            System.err.println("[DISCOVERY] [ERROR] Falha no farol: " + e.getMessage());
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }
}
