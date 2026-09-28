package org.nezxenka.auth.telegram.api;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import org.nezxenka.auth.telegram.api.model.AnswerCallbackQueryRequest;
import org.nezxenka.auth.telegram.api.model.ApiResponse;
import org.nezxenka.auth.telegram.api.model.SendMessageRequest;
import org.nezxenka.auth.telegram.api.model.Update;

public final class TelegramApiClient {

    private static final String API_URL = "https://api.telegram.org/bot";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);
    private static final int HTTP_OK = 200;
    private static final Type UPDATES_TYPE = new TypeToken<ApiResponse<List<Update>>>() {}.getType();

    private final HttpClient http;
    private final Gson gson;
    private final String baseUrl;
    private final Logger logger;

    public TelegramApiClient(String token, Logger logger) {
        this.http = HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build();
        this.gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        this.baseUrl = API_URL + token + "/";
        this.logger = logger;
    }

    public List<Update> getUpdates(long offset, int timeoutSeconds) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest
            .newBuilder(URI.create(baseUrl + "getUpdates?offset=" + offset + "&timeout=" + timeoutSeconds))
            .timeout(Duration.ofSeconds(timeoutSeconds).plus(REQUEST_TIMEOUT))
            .GET()
            .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != HTTP_OK) {
            throw new TelegramApiException("getUpdates failed with HTTP " + response.statusCode());
        }
        ApiResponse<List<Update>> body = gson.fromJson(response.body(), UPDATES_TYPE);
        if (body == null || !body.isOk()) {
            String reason = body == null ? "empty response" : body.getDescription();
            throw new TelegramApiException("getUpdates returned an error: " + reason);
        }
        return body.getResult() != null ? body.getResult() : List.of();
    }

    public CompletableFuture<Void> sendMessage(SendMessageRequest request) {
        return post("sendMessage", request);
    }

    public CompletableFuture<Void> answerCallbackQuery(AnswerCallbackQueryRequest request) {
        return post("answerCallbackQuery", request);
    }

    private CompletableFuture<Void> post(String method, Object payload) {
        HttpRequest request = HttpRequest
            .newBuilder(URI.create(baseUrl + method))
            .timeout(REQUEST_TIMEOUT)
            .header("Content-Type", "application/json; charset=UTF-8")
            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload), StandardCharsets.UTF_8))
            .build();
        return http
            .sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
            .thenAccept(response -> {
                if (response.statusCode() != HTTP_OK) {
                    logger.warning("Telegram " + method + " failed with HTTP " + response.statusCode() + ": " + response.body());
                }
            });
    }
}
