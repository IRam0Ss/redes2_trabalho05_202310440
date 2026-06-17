import utils.APDU;
import utils.InfoUser;

public class TesteApduFormat {
    public static void main(String[] args) {
        String[] apduFormat = new String[4];

        apduFormat[0] = APDU.montarJoin("redes2", new InfoUser("Iury", "192.168.1.103", 6001));
        apduFormat[1] = APDU.montarLeave("redes2", new InfoUser("Kauan", "192.168.1.103", 6001));
        apduFormat[2] = APDU.montarSend("redes2", new InfoUser("Clara", "192.168.1.103", 6001), "Oi tudo ~/ bem");
        apduFormat[3] = APDU.montarLeave("redes2", new InfoUser("Angela", "192.168.1.103", 6001));

        for (int i = 0; i < apduFormat.length; i++) {
            System.out.println(apduFormat[i]);
        }
        System.out.println(APDU.extrairComando(apduFormat[0]));
        System.out.println(APDU.extrairGrupo(apduFormat[0]));
        System.out.println(APDU.extrairUsuario(apduFormat[0]));
        System.out.println(APDU.extrairMensagem(apduFormat[2]));
    }
}
