package br.edu.icev.aed.forense;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class SolucaoForense implements AnaliseForenseAvancada {

    class Evento {
        long timestamp;
        long bytes;

        public Evento(long t, long b) {
            this.timestamp = t;
            this.bytes = b;
        }
    }

    @Override
    public List<String> desafio2_ReconstituirLinhaDoTempo(String caminhoArquivoCsv, String sessionId) {

        Queue<String> fila = new LinkedList<>();

        if (sessionId == null || sessionId.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try (BufferedReader leitor = new BufferedReader(new FileReader(caminhoArquivoCsv))) {
            leitor.readLine();
            String linha;

            while ((linha = leitor.readLine()) != null) {
                String[] col = linha.split(",");

                if (col.length >= 4) {
                    String s = col[2].trim();
                    String acao = col[3].trim();

                    if (sessionId.equals(s)) {
                        fila.add(acao);
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo: " + e.getMessage());
        }

        List<String> lista = new ArrayList<>();
        while (!fila.isEmpty()) {
            lista.add(fila.poll());
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
                        long t = Long.parseLong(col[0].trim());
                        long b = Long.parseLong(col[6].trim());

                        if (b > 0) {
                            eventos.add(new Evento(t, b));
                        }

                    } catch (NumberFormatException e) {}
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
}

