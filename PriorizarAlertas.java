package br.edu.icev.aed;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

// Classe Alerta
class Alerta {
    private String usuarioID;
    private String tipoAcao;
    private String recurso;
    private int nivelGravidade;

    public Alerta(String userId, String acao, String recurso, int severityLevel) {
        this.usuarioID = userId;
        this.tipoAcao = acao;
        this.recurso = recurso;
        this.nivelGravidade = severityLevel;
    }

    // Apenas os getters que são usados
    public String getUserId() { return usuarioID; }
    public String getAcao() { return tipoAcao; }
    public String getRecurso() { return recurso; }
    public int getSeverityLevel() { return nivelGravidade; }
}

// Classe PriorizarAlertas
public class PriorizarAlertas {

    public List<Alerta> desafio3_priorizarAlertas(String caminhoArquivoCsv, int n) throws IOException {
        PriorityQueue<Alerta> filaDePrioridades = new PriorityQueue<>(
                Comparator.comparingInt((Alerta a) -> a.getSeverityLevel()).reversed()
        );

        try (BufferedReader buffRead = new BufferedReader(new FileReader(caminhoArquivoCsv))) {
            String linha;
            buffRead.readLine();

            while ((linha = buffRead.readLine()) != null) {
                String[] colunas = linha.split(",");

                // Verifica se a linha tem pelo menos 6 colunas
                if (colunas.length < 6) {
                    continue;
                }

                try {

                    String usuarioID = colunas[1].trim();
                    String tipoAcao = colunas[3].trim();
                    String recurso = colunas[4].trim();
                    int gravidade = Integer.parseInt(colunas[5].trim());


                    Alerta alerta = new Alerta(usuarioID, tipoAcao, recurso, gravidade);
                    filaDePrioridades.offer(alerta);

                } catch (NumberFormatException e) {
                    // Se houver erro na conversão de números, pula a linha
                    continue;
                }
            }
        }

        //Exibe os alertas
        List<Alerta> listaAlertasResultado = new ArrayList<>();
        for (int i = 0; i < n && !filaDePrioridades.isEmpty(); i++) {
            listaAlertasResultado.add(filaDePrioridades.poll());
        }

        return listaAlertasResultado;
    }
}