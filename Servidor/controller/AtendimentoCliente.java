package controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
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
	private InfoUser usuarioAssociado = null;
	private List<String> gruposAssociados = new ArrayList<>();
	private PrintWriter escritorSaida;

	public AtendimentoCliente(Socket conexao, GerenciadorGrupos gerenciador) {
		this.conexao = conexao;
		this.gerenciador = gerenciador;
		try {
			this.escritorSaida = new PrintWriter(conexao.getOutputStream(), true);
		} catch (Exception e) {
			System.err.println("[ATENDIMENTO] [ERROR] Falha ao criar escritor TCP: " + e.getMessage());
		}
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
			if (this.usuarioAssociado != null) { // remove o usuario de todos os grupos quando ele sai
				for (String grupo : this.gruposAssociados) {
					this.gerenciador.leave(grupo, this.usuarioAssociado);
				}
				this.gerenciador.removerUsuario(this.usuarioAssociado);
			}
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

				if (this.usuarioAssociado == null) {
					this.usuarioAssociado = usuarioJoin;
				}

				boolean checkJoin = this.gerenciador.join(grupoJoin, usuarioJoin);
				if (checkJoin) {
					if (!this.gruposAssociados.contains(grupoJoin)) {
						this.gruposAssociados.add(grupoJoin);
					}
					escritorSaida.println("OK~/Entrou no grupo " + grupoJoin);
					System.out.println("[ATENDIMENTO] [INFO] Processamento de JOIN de '" + usuarioJoin.getNome() + "' concluido.");
				} else {
					escritorSaida.println("ERRO~/Voce ja esta no grupo " + grupoJoin);
					System.out
							.println("[ATENDIMENTO] [WARNING] Processamento de JOIN de '" + usuarioJoin.getNome()
									+ "' falhou.");
				}
				break;

			case Protocolo.REGISTER:
				InfoUser usuarioRegister = APDU.extrairUsuario(apdu);
				if (this.usuarioAssociado == null) {
					this.usuarioAssociado = usuarioRegister;
				}
				this.gerenciador.registrarUsuario(usuarioRegister);
				System.out.println("[ATENDIMENTO] [INFO] Cliente '" + usuarioRegister.getNome() + "' registrado silenciosamente no servidor.");
				break;

			case Protocolo.LEAVE:
				String grupoLeave = APDU.extrairGrupo(apdu);
				InfoUser usuarioLeave = APDU.extrairUsuario(apdu);
				boolean checkLeave = this.gerenciador.leave(grupoLeave, usuarioLeave);
				if (checkLeave) {
					this.gruposAssociados.remove(grupoLeave);
					escritorSaida.println("OK~/Saiu do grupo " + grupoLeave);
					System.out.println("[ATENDIMENTO] [INFO] Processamento de LEAVE de '" + usuarioLeave.getNome()
							+ "' concluido.");
				} else {
					escritorSaida.println("ERRO~/Voce nao esta no grupo " + grupoLeave);
					System.out
							.println("[ATENDIMENTO] [WARNING] Processamento de LEAVE de '" + usuarioLeave.getNome()
									+ "' falhou.");
				}
				break;

			case Protocolo.LIST:
				List<String> grupos = this.gerenciador.listarGrupos();
				if (grupos.isEmpty()) {
					escritorSaida.println("OK~/Nenhum grupo ativo no momento.");
				} else {
					escritorSaida.println("OK~/" + String.join(", ", grupos));
				}
				System.out.println("[ATENDIMENTO] [INFO] Processamento de LIST concluido.");
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
