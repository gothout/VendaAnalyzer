import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.ArrayList;

public class ProcessadorVendas {
    private static final String DIRETORIO_PROJECT = "com.lchaves.vendaAnalyzer";
    private static final String DIRETORIO_COLETOR = "coletor";
    private static final String DIRETORIO_SRC = "src";

    private static final String ARQUIVO_CSV = DIRETORIO_PROJECT + File.separator + File.separator + DIRETORIO_SRC + File.separator + DIRETORIO_COLETOR + File.separator + "vendas.csv";

    public static void main(String[] args) {
        String caminhoArquivo = obterCaminhoArquivo();
        //System.out.println("Procurando arquivo no seguinte caminho: " + caminhoArquivo);
        if (!arquivoExiste(caminhoArquivo)) {
            System.out.println("Arquivo vendas.csv não encontrado no diretório /coletor.");
            System.out.println("Por favor, informe o caminho completo do arquivo CSV:");
            Scanner scanner = new Scanner(System.in);
            caminhoArquivo = scanner.nextLine();
        }

        try {
            List<Venda> vendas = lerDadosCSV(ARQUIVO_CSV);

            // Total de vendas
            double totalVendas = vendas.stream()
                    .mapToDouble(Venda::getValor)
                    .sum();
            System.out.println("Total de vendas: " + totalVendas);

            // Média de vendas por produto
            double mediaVendasPorProduto = vendas.stream()
                    .collect(Collectors.groupingBy(Venda::getProduto, Collectors.summingDouble(Venda::getValor)))
                    .values()
                    .stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0);
            System.out.println("Média de vendas por produto: " + mediaVendasPorProduto);

            // Produto mais vendido
            Optional<String> produtoMaisVendido = vendas.stream()
                    .collect(Collectors.groupingBy(Venda::getProduto, Collectors.summingDouble(Venda::getValor)))
                    .entrySet()
                    .stream()
                    .max(Comparator.comparingDouble(entry -> entry.getValue()))
                    .map(entry -> entry.getKey());
            produtoMaisVendido.ifPresent(produto -> System.out.println("Produto mais vendido: " + produto));
        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo CSV: " + e.getMessage());
        }
    }

    private static String obterCaminhoArquivo() {
        String separadorDeDiretorio = File.separator;
        String caminhoArquivo = System.getProperty("user.dir") +separadorDeDiretorio+ ARQUIVO_CSV;
        //System.out.printf("Caminho do arquivo: %s%n", caminhoArquivo);
        return caminhoArquivo;
    }

    private static boolean arquivoExiste(String caminhoArquivo) {
        return new File(caminhoArquivo).exists();
    }

    private static List<Venda> lerDadosCSV(String arquivo) throws IOException {
        List<Venda> vendas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            // Verificar o cabeçalho
            String header = br.readLine();
            if (!header.equals("Produto,Valor")) {
                System.err.println("Erro: Cabeçalho incorreto encontrado no arquivo CSV.");
                return vendas; // Retorna lista vazia se o cabeçalho estiver incorreto
            }

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2 && validarValor(parts[1])) {
                    vendas.add(new Venda(parts[0], Double.parseDouble(parts[1])));
                } else {
                    System.err.println("Erro: Valor inválido encontrado no arquivo CSV: " + line);
                }
            }
        }
        return vendas;
    }

    private static boolean validarValor(String valor) {
        // Verificar se o valor é numérico
        return valor.matches("-?\\d+(\\.\\d+)?");
    }
}