import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class RastrearContaminacao {
    public Optional<List<String>> desafio5_rastrearContaminacao(String caminhoArquivoCsv, String recursoInicial, String recursoAlvo) throws IOException {
        // Early exit
        if (recursoInicial.equals(recursoAlvo)) {
            return Optional.of(List.of(recursoInicial));
        }

        // Construção do grafo partindo do CSV
        Map<String, List<String>> grafoCentral = criacaoGrafo(caminhoArquivoCsv);

        // Early exit: verifica se os recursos existem no grafo
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

    // Reconstruir o caminho a partir dos predecessores
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
