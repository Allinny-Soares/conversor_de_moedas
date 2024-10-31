import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonObject;

public class ConversorDeMoedas {
    private static final String API_KEY = "c2333f13df1754b301075522"; // Substitua pela sua chave
    private static final String API_BASE_URL = "https://v6.exchangerate-api.com/v6/";
    private static final List<String> moedasValidas = Arrays.asList("BRL", "USD", "EUR", "JPY");

    public static <JsonObject> void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        double valor = obterValor(scanner);
        String moedaOrigem = obterMoeda(scanner, "origem");
        String moedaDestino = obterMoeda(scanner, "destino");

        try {
            String jsonResponse = fazerRequisicao(moedaOrigem);
            JsonObject jsonObj = (JsonObject) parseJson(jsonResponse);
            double taxaConversao = obterTaxaConversao((com.google.gson.JsonObject) jsonObj, moedaDestino);
            double resultado = valor * taxaConversao;
            DecimalFormat df = new DecimalFormat("#.##");
            System.out.println(valor + " " + moedaOrigem + " equivale a " + df.format(resultado) + " " + moedaDestino);
        } catch (IOException e) {
            System.err.println("Erro ao conectar com a API: " + e.getMessage());
        } catch (JsonParseException e) {
            System.err.println("A resposta da API não está no formato JSON esperado. Verifique a conexão ou a API.");
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static double obterValor(Scanner scanner) {
        while (true) {
            System.out.print("Digite o valor a ser convertido: ");
            try {
                return scanner.nextDouble();
            } catch (InputMismatchException e) {
                System.out.println("Valor inválido. Por favor, digite um número.");
                scanner.nextLine();
            }
        }
    }

    private static String obterMoeda(Scanner scanner, String tipo) {
        while (true) {
            System.out.printf("Digite a moeda de %s (BRL, USD, EUR, JPY): ", tipo);
            String moeda = scanner.next().toUpperCase();

            if (moedasValidas.contains(moeda)) {
                return moeda;
            } else {
                System.out.println("Moeda inválida. Por favor, escolha entre BRL, USD, EUR ou JPY.");
            }
        }
    }

    private static String fazerRequisicao(String moedaOrigem) throws IOException, InterruptedException {
        String url = API_BASE_URL + API_KEY + "/latest/" + moedaOrigem;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Erro na requisição: " + response.statusCode());
        }
        return response.body();
    }

    private static JsonObject parseJson(String jsonResponse) throws JsonParseException {
        Gson gson = new Gson();
        return gson.fromJson(jsonResponse, JsonObject.class);
    }

    private static double obterTaxaConversao(JsonObject jsonObj, String moedaDestino) {
        JsonObject rates = jsonObj.getAsJsonObject("conversion_rates");
        if (!rates.has(moedaDestino)) {
            throw new IllegalArgumentException("Moeda de destino não encontrada na resposta da API.");
        }
        return rates.get(moedaDestino).getAsDouble();
    }
}