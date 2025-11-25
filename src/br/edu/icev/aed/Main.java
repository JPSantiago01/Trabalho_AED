package br.edu.icev.aed;

import java.io.IOException;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {

        String caminho = "arquivo_logs.csv";
        SolucaoForense solucao = new SolucaoForense();

        solucao.encontrarSessoesInvalidas(caminho);
        solucao.reconstruirLinhaTempo(caminho, "session-a-01");
        solucao.priorizarAlertas(caminho, 3);
        solucao.encontrarPicosTransferencia(caminho);
        solucao.rastrearContaminacao(caminho, "/opt/app/application.properties", "/var/log/syslog");

    }
}
