package br.edu.icev.aed.forense;

import java.io.*;
import java.util.*;

public class MinhaAnaliseForense implements AnaliseForenseAvancada {
    
    class Evento {
        long timestamp;
        long bytes;
        public Evento(long t, long b) { timestamp = t; bytes = b; }
    }
@Override
public List<String> desafio2_ReconstituirLinhaDoTempo(String caminhoArquivoCsv, String sessionId) {
    List<String> lista = new ArrayList<>();

    if (sessionId == null || sessionId.trim().isEmpty()) {
        return lista;
    }

    try (BufferedReader leitor = new BufferedReader(new FileReader(caminhoArquivoCsv))) {
        
        // Pula o cabeçalho
        leitor.readLine();

        String linhaAtual;
        while ((linhaAtual = leitor.readLine()) != null) {
            
            String[] coluna = linhaAtual.split(",");
            
            if (coluna.length >= 4) {
                String sessaoAtual = coluna[2].trim();    
                String tipoAcao = coluna[3].trim();       

                if (sessionId.equals(sessaoAtual)) {
                    lista.add(tipoAcao);
                }
            }
        }

    } catch (IOException e) {
        System.err.println("Erro ao ler arquivo: " + e.getMessage());
    }

    return lista;
}
