import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class EncontrarSessoesInvalidas {
    public Set<String> desafio1_encontrarSessoesInvalidas(String arquivoCSV_caminho) throws IOException {
        Set<String> sessoesInvalidas = new HashSet<>();
        Map<String, Stack<String>> pilhaVigilancia = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(arquivoCSV_caminho))) {
            String linha;
            br.readLine(); // Pula cabeçalho

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
                    // Se a pilha tiver vazia ou a sessão não for igual ao topo, é inválida
                    if (pilha.isEmpty() || !pilha.peek().equals(Idsessao)) {
                        sessoesInvalidas.add(Idsessao);
                    } else {
                        pilha.pop();
                    }
                }

                // Atualiza o mapa com a pilha modificada
                pilhaVigilancia.put(IdUser, pilha);
            }
        }

        // Adiciona todas as sessões que nunca fizeram LOGOUT
        for (Stack<String> pilha : pilhaVigilancia.values()) {
            sessoesInvalidas.addAll(pilha);
        }

        return sessoesInvalidas;
    }

}