package controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import utils.APDU;
import utils.InfoUser;
import utils.Protocolo;

/**
 * Classe responsavel por atender clientes individuais, garantindo que o
 * servidor consiga atender multiplos clientes. Cada APDU eh processada
 * individualmente em uma thread
 */
public class AtendimentoCliente implements Runnable {

	private Socket conexao;
	private GerenciadorGrupos gerenciador;

	public AtendimentoCliente(Socket conexao, GerenciadorGrupos gerenciador) {
		this.conexao = conexao;
		this.gerenciador = gerenciador;
	}

	@Override
	public void run() {
		try {
			BufferedReader tradutorBytesStr = new BufferedReader(new InputStreamReader(this.conexao.getInputStream()));
			String apduRecebida;

			// le e trata as apdus recebidas
			while ((apduRecebida = tradutorBytesStr.readLine()) != null) {
				processarAPDU(apduRecebida);
			}
		} catch (Exception e) {
			System.err.println("[ATENDIMENTO] [INFO] Conexao encerrada com o cliente.");
		} finally {
			try {
				this.conexao.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * processa a apdu recebida pelo servidor e designa o porcessamento para o
	 * gerenciador
	 * 
	 * @param apdu apdu recebida
	 */
	private void processarAPDU(String apdu) {
		String comando = APDU.extrairComando(apdu);

		System.out.println("[ATENDIMENTO] [INFO] Processando comando " + comando);

		if (comando == null || comando.isEmpty()) {
			return;
		}

		switch (comando) {
			case Protocolo.JOIN:
				String grupoJoin = APDU.extrairGrupo(apdu);
				InfoUser usuarioJoin = APDU.extrairUsuario(apdu);
				boolean checkJoin = this.gerenciador.join(grupoJoin, usuarioJoin);
				if (checkJoin) {
					System.out.println("[ATENDIMENTO] [INFO] Processamento de JOIN de '" + usuarioJoin.getNome() + "' concluido.");
				} else {
					System.out
							.println("[ATENDIMENTO] [WARNING] Processamento de JOIN de '" + usuarioJoin.getNome() + "' falhou.");
				}
				break;

			case Protocolo.LEAVE:
				String grupoLeave = APDU.extrairGrupo(apdu);
				InfoUser usuarioLeave = APDU.extrairUsuario(apdu);
				boolean checkLeave = this.gerenciador.leave(grupoLeave, usuarioLeave);
				if (checkLeave) {
					System.out.println("[ATENDIMENTO] [INFO] Processamento de LEAVE de '" + usuarioLeave.getNome() + "' concluido.");
				} else {
					System.out
							.println("[ATENDIMENTO] [WARNING] Processamento de LEAVE de '" + usuarioLeave.getNome() + "' falhou.");
				}
				break;

			case Protocolo.SEND:
				System.out.println("[ATENDIMENTO] [WARNING] APDU SEND recebida via TCP (ignorando).");
				break;

			default:
				System.err.println("[ATENDIMENTO] [ERROR] Comando desconhecido: " + comando);
				break;
		}// fim do switch
	} // fim do metodo processarAPDU
} // fim classe
