package model;

import utils.InfoUser;

/**
 * Interface para os eventos de mensagens recebidas.
 * Implementada pela interface grafica para atualizar o chat.
 */
public interface MessageListener {
    /**
     * Evento acionado quando uma mensagem chega.
     * 
     * @param destino O nome do grupo ou usuario alvo
     * @param remetente As informacoes do usuario que enviou a mensagem
     * @param mensagem O corpo da mensagem
     * @param isPrivate Se a mensagem e um sussurro/privada
     */
    void onMessageReceived(String destino, InfoUser remetente, String mensagem, boolean isPrivate);

    /**
     * Evento acionado quando o servidor desliga ou a conexao cai.
     */
    void onShutdown();
}
