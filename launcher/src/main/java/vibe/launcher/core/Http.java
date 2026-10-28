package vibe.launcher.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

public final class Http {
    private static final String USER_AGENT = "VibeLauncher/" + Http.class.getPackage().getImplementationVersion();
    private static final int ATTEMPTS = 3;
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private Http() {
    }

    public static String getString(String url) throws IOException {
        HttpResponse<String> response = send(url, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return response.body();
    }

    public static JsonElement getJson(String url) throws IOException {
        return JsonParser.parseString(getString(url));
    }

    /** GET с заголовком Authorization: Bearer. */
    public static JsonElement getJson(String url, String bearer) throws IOException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .header("Authorization", "Bearer " + bearer)
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
        return JsonParser.parseString(sendRequest(request).body());
    }

    public static JsonElement postJson(String url, JsonElement body) throws IOException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();
        return JsonParser.parseString(sendRequest(request).body());
    }

    /** POST формы. Ответ возвращается даже при коде 4xx: OAuth присылает в нём описание ошибки. */
    public static JsonElement postForm(String url, java.util.Map<String, String> form) throws IOException {
        StringBuilder body = new StringBuilder();
        for (var entry : form.entrySet()) {
            if (body.length() > 0) body.append('&');
            body.append(java.net.URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)).append('=')
                    .append(java.net.URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();
        try {
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return JsonParser.parseString(response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Запрос прерван", e);
        }
    }

    private static HttpResponse<String> sendRequest(HttpRequest request) throws IOException {
        try {
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() / 100 != 2) {
                throw new HttpStatusException(response.statusCode(), request.uri().toString(), response.body());
            }
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Запрос прерван", e);
        }
    }

    public static final class HttpStatusException extends IOException {
        public final int status;
        public final String body;

        HttpStatusException(int status, String url, String body) {
            super("HTTP " + status + " для " + url);
            this.status = status;
            this.body = body;
        }
    }

    /**
     * Скачивает файл, если его ещё нет или он не совпадает по sha1/размеру.
     * sha1 может быть null, size может быть -1.
     */
    public static void download(String url, Path target, String sha1, long size) throws IOException {
        if (isValid(target, sha1, size)) return;
        Files.createDirectories(target.getParent());
        Path part = target.resolveSibling(target.getFileName() + ".part");
        IOException last = null;
        for (int attempt = 1; attempt <= ATTEMPTS; attempt++) {
            try {
                HttpResponse<InputStream> response = send(url, HttpResponse.BodyHandlers.ofInputStream());
                try (InputStream in = response.body()) {
                    Files.copy(in, part, StandardCopyOption.REPLACE_EXISTING);
                }
                if (!isValid(part, sha1, size)) {
                    throw new IOException("Файл повреждён (не совпала контрольная сумма): " + url);
                }
                Files.move(part, target, StandardCopyOption.REPLACE_EXISTING);
                return;
            } catch (IOException e) {
                last = e;
            }
        }
        Files.deleteIfExists(part);
        throw last;
    }

    public static boolean isValid(Path file, String sha1, long size) throws IOException {
        if (!Files.isRegularFile(file)) return false;
        if (size >= 0 && Files.size(file) != size) return false;
        return sha1 == null || sha1.equalsIgnoreCase(sha1(file));
    }

    public static String sha1(Path file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            try (InputStream in = Files.newInputStream(file)) {
                byte[] buffer = new byte[64 * 1024];
                int read;
                while ((read = in.read(buffer)) > 0) digest.update(buffer, 0, read);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static <T> HttpResponse<T> send(String url, HttpResponse.BodyHandler<T> handler) throws IOException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", USER_AGENT)
                .timeout(Duration.ofMinutes(2))
                .GET()
                .build();
        try {
            HttpResponse<T> response = CLIENT.send(request, handler);
            if (response.statusCode() / 100 != 2) {
                if (response.body() instanceof InputStream) ((InputStream) response.body()).close();
                throw new IOException("HTTP " + response.statusCode() + " для " + url);
            }
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Загрузка прервана", e);
        }
    }
}
