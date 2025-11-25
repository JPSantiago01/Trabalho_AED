package br.edu.icev.aed;

import java.io.*;
import java.util.*;
import br.edu.icev.aed.forense.Alerta;
import br.edu.icev.aed.forense.AnaliseForenseAvancada;

public class SolucaoForense implements AnaliseForenseAvancada {


    class Evento {
        long timestamp;
        long bytes;
        public Evento(long t, long b) {
            timestamp = t;
            bytes = b;
        }
    }


    @Override
    public Set<String> encontrarSessoesInvalidas(String arquivoCSV_caminho) throws IOException {
        Set<String> sessoesInvalidas = new HashSet<>();
        Map<String, Stack<String>> pilhaVigilancia = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(arquivoCSV_caminho))) {
            String linha;
            br.readLine();

            while ((linha = br.readLine()) != null) {
                String[] campos = linha.split(",");

                String tipoAcao = campos[3].trim();
                String Idsessao = campos[2].trim();
                String IdUser = campos[1].trim();

                Stack<String> pilha = pilhaVigilancia.getOrDefault(IdUser, new Stack<>());

                if ("LOGIN".equals(tipoAcao)) {
                    if (!pilha.isEmpty()) {
                        sessoesInvalidas.add(pilha.peek());
                    }
                    // Empilha a nova sessão
                    pilha.push(Idsessao);
                } else if ("LOGOUT".equals(tipoAcao)) {

                    if (pilha.isEmpty() || !pilha.peek().equals(Idsessao)) {
                        sessoesInvalidas.add(Idsessao);
                    } else {
                        pilha.pop();
                    }
                }


                pilhaVigilancia.put(IdUser, pilha);
            }
        }

        for (Stack<String> pilha : pilhaVigilancia.values()) {
            sessoesInvalidas.addAll(pilha);
        }

        return sessoesInvalidas;
    }


    @Override
    public List<String> reconstruirLinhaTempo(String caminhoArquivoCsv, String sessionId) throws IOException {
        List<String> lista = new ArrayList<>();

        if (sessionId == null || sessionId.trim().isEmpty()) {
            return lista;
        }

        try (BufferedReader leitor = new BufferedReader(new FileReader(caminhoArquivoCsv))) {


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
    public List<Alerta> priorizarAlertas(String caminhoArquivoCsv, int n) throws IOException {

        PriorityQueue<Alerta> filaDePrioridades = new PriorityQueue<>(
                (a1, a2) -> {
                    try {
                        for (var campo : Alerta.class.getDeclaredFields()) {
                            if (campo.getType() == int.class) {
                                campo.setAccessible(true);
                                int s1 = (int) campo.get(a1);
                                int s2 = (int) campo.get(a2);
                                return Integer.compare(s2, s1); // maior severidade primeiro ✅
                            }
                        }
                    } catch (Exception ignored) {}
                    return 0;
                }
        );

        try (BufferedReader buffRead = new BufferedReader(new FileReader(caminhoArquivoCsv))) {
            String linha;
            buffRead.readLine();

            while ((linha = buffRead.readLine()) != null) {
                String[] colunas = linha.split(",");
                if (colunas.length < 7) continue;

                try {
                    long timestamp = Long.parseLong(colunas[0].trim());
                    String user = colunas[1].trim();
                    String sessao = colunas[2].trim();
                    String acao = colunas[3].trim();
                    String recurso = colunas[4].trim();
                    int severidade = Integer.parseInt(colunas[5].trim());
                    long bytes = Long.parseLong(colunas[6].trim());

                    Alerta alerta = new Alerta(
                            timestamp,
                            user,
                            sessao,
                            acao,
                            recurso,
                            severidade,
                            bytes
                    );

                    filaDePrioridades.offer(alerta);

                } catch (Exception ignored) {}
            }
        }

        List<Alerta> resultado = new ArrayList<>();
        for (int i = 0; i < n && !filaDePrioridades.isEmpty(); i++) {
            resultado.add(filaDePrioridades.poll());
        }

        return resultado;
    }

    // DESAFIO 4 - Encontrar Picos de Transferência
    @Override
    public Map<Long, Long> encontrarPicosTransferencia(String caminhoArquivoCsv) throws IOException {
        Map<Long, Long> lista = new HashMap<>();
        Stack<Evento> pilha = new Stack<>();
        List<Evento> eventos = new ArrayList<>();

        try (BufferedReader leitor = new BufferedReader(new FileReader(caminhoArquivoCsv))) {
            leitor.readLine();
            String linhaAtual = leitor.readLine();

            while (linhaAtual != null) {
                String[] coluna = linhaAtual.split(",");
                if (coluna.length >= 7) {
                    try {
                        long timestamp = Long.parseLong(coluna[0].trim());
                        long bytes = Long.parseLong(coluna[6].trim());
                        if (bytes > 0) {
                            eventos.add(new Evento(timestamp, bytes));
                        }
                    } catch (NumberFormatException e) {
                    }
                }
                linhaAtual = leitor.readLine();
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
                lista.put(atual.timestamp, pilha.peek().timestamp);
            }

            pilha.push(atual);
        }

        return lista;
    }


    @Override
    public Optional<List<String>> rastrearContaminacao(String caminhoArquivoCsv, String recursoInicial, String recursoAlvo) throws IOException {

        if (recursoInicial.equals(recursoAlvo)) {
            return Optional.of(List.of(recursoInicial));
        }

        // Construção do grafo partindo do CSV
        Map<String, List<String>> grafoCentral = criacaoGrafo(caminhoArquivoCsv);


        if (!grafoCentral.containsKey(recursoInicial) || !grafoCentral.containsKey(recursoAlvo)) {
            return Optional.empty();
        }

        Queue<String> ordemInvFila = new ArrayDeque<>();
        Map<String, String> historicoRota = new HashMap<>();
        Set<String> investigados = new HashSet<>();

        // Começa a busca com o recurso inicial
        ordemInvFila.offer(recursoInicial);
        investigados.add(recursoInicial);
        historicoRota.put(recursoInicial, null);

        while (!ordemInvFila.isEmpty()) {
            String atual = ordemInvFila.poll();

            if (atual.equals(recursoAlvo)) {
                return Optional.of(reconstruirCaminho(historicoRota, recursoAlvo));
            }

            List<String> vizinhosRecAtual = grafoCentral.get(atual);
            if (vizinhosRecAtual != null) {
                for (String vizinho : vizinhosRecAtual) {
                    if (investigados.add(vizinho)) {
                        historicoRota.put(vizinho, atual);
                        ordemInvFila.offer(vizinho);
                    }
                }
            }
        }

        return Optional.empty();
    }

    // Construir o grafo de recursos
    private Map<String, List<String>> criacaoGrafo(String caminhoArquivoCsv) throws IOException {
        Map<String, List<String>> grafo = new HashMap<>();
        Map<String, String> sessaoUltimoRecurso = new HashMap<>();

        try (BufferedReader buffRead = new BufferedReader(new FileReader(caminhoArquivoCsv))) {
            String line;
            buffRead.readLine();

            while ((line = buffRead.readLine()) != null) {
                String[] colunas = line.split(",");

                String IDSessao = colunas[2].trim();
                String recurso = colunas[4].trim();

                if (sessaoUltimoRecurso.containsKey(IDSessao)) {
                    String recursoAnterior = sessaoUltimoRecurso.get(IDSessao);

                    // Apenas se for um recurso diferente cria uma aresta
                    if (!recursoAnterior.equals(recurso)) {
                        grafo.computeIfAbsent(recursoAnterior, k -> new ArrayList<>()).add(recurso);
                    }
                }

                sessaoUltimoRecurso.put(IDSessao, recurso);
            }
        }

        return grafo;
    }


    private List<String> reconstruirCaminho(Map<String, String> historicoRota, String alvo) {
        List<String> rota = new ArrayList<>();
        String atual = alvo;

        // Reconstrói do alvo até o início
        while (atual != null) {
            rota.add(atual);
            atual = historicoRota.get(atual);
        }

        // Faz a inversão pra ficar na ordem correta
        Collections.reverse(rota);
        return rota;
    }
}