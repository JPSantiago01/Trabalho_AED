package br.edu.icev.aed.forense;

import java.io.*;
import java.util.*;

public class MinhaAnaliseForense implements AnaliseForenseAvancada {
    
    class Evento {
        long timestamp;
        long bytes;
        public Evento(long t, long b) {
            timestamp = t;
            bytes = b;
        }
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
@Override
public Map<Long, Long> desafio4_encontrarPicosTransferencia(String caminhoArquivoCsv) {
    Map<Long, Long> resultado = new HashMap<>();
    Stack<Evento> pilha = new Stack<>();
    List<Evento> eventos = new ArrayList<>();

    try (BufferedReader leitor = new BufferedReader(new FileReader(caminhoArquivoCsv))) {
        leitor.readLine();
        String linha = leitor.readLine();
        
        while (linha != null) {
            String[] col = linha.split(",");
            if (col.length >= 7) {
                try {
                    long timestamp = Long.parseLong(col[0].trim());
                    long bytes = Long.parseLong(col[6].trim());
                    if (bytes > 0) {
                        eventos.add(new Evento(timestamp, bytes));
                    }
                } catch (NumberFormatException e) {
                }
            }
            linha = leitor.readLine();
        }
    } catch (IOException e) {
        System.err.println("Erro ao ler arquivo: " + e.getMessage());
    }
    
    for (int i = eventos.size() - 1; i >= 0; i--) {
        Evento atual = eventos.get(i);
        
        while (!pilha.isEmpty() && pilha.peek().bytes <= atual.bytes) {
            pilha.pop();
        }
        
        if (!pilha.isEmpty()) {
            resultado.put(atual.timestamp, pilha.peek().timestamp);
        }
        
        pilha.push(atual);
    }
    
    return resultado;
}
