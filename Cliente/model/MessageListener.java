package model;

import utils.InfoUser;

public interface MessageListener {
    void onMessageReceived(InfoUser remetente, String mensagem, boolean isPrivate);
    void onShutdown();
}
