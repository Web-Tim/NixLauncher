package eu.nix.nixlauncher.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class WebUtil {
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    public static HttpResponse<String> get(String url)
    {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .build();
            CompletableFuture<HttpResponse<String>> response_ = HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            response_.join();
            HttpResponse<String> response = response_.get();
            if (response.statusCode() > 300)
            {
                System.out.println("Request failed: " + url + " | " + response.statusCode());
                return null;
            }
            return response;
        } catch (URISyntaxException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static HttpResponse<String> post(String url)
    {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();
            CompletableFuture<HttpResponse<String>> response_ = HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            response_.join();
            HttpResponse<String> response = response_.get();
            if (response.statusCode() > 300)
            {
                System.out.println("Request failed: " + url + " | " + response.statusCode());
                return null;
            }
            return response;
        } catch (URISyntaxException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
