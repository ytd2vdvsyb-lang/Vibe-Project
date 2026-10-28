package vibe.launcher.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Вход через аккаунт Microsoft (лицензия Minecraft): device code OAuth, затем Xbox Live, XSTS и Minecraft.
 * Нужен client id своего приложения Azure, допущенного Mojang к Minecraft API.
 */
public final class MicrosoftAuth {
    private static final String MS = "https://login.microsoftonline.com/consumers/oauth2/v2.0";
    private static final String SCOPE = "XboxLive.signin offline_access";

    private final String clientId;

    public MicrosoftAuth(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException("Не задан Microsoft client id (настройка msClientId). Подробнее в README.");
        }
        this.clientId = clientId;
    }

    /** Что показать игроку: ссылку и код. */
    public record DeviceCode(String userCode, String verificationUri, String message) {
    }

    /** Результат входа. refreshToken сохраняется, чтобы не входить заново. */
    public record Session(Account account, String refreshToken) {
    }

    public Session login(Consumer<DeviceCode> show) throws IOException {
        JsonObject code = Http.postForm(MS + "/devicecode", Map.of("client_id", clientId, "scope", SCOPE)).getAsJsonObject();
        if (code.has("error")) throw new IOException(describe(code));
        show.accept(new DeviceCode(code.get("user_code").getAsString(), code.get("verification_uri").getAsString(),
                code.get("message").getAsString()));

        long interval = code.get("interval").getAsLong() * 1000;
        long deadline = System.currentTimeMillis() + code.get("expires_in").getAsLong() * 1000;
        while (System.currentTimeMillis() < deadline) {
            sleep(interval);
            JsonObject token = Http.postForm(MS + "/token", Map.of(
                    "client_id", clientId,
                    "grant_type", "urn:ietf:params:oauth:grant-type:device_code",
                    "device_code", code.get("device_code").getAsString())).getAsJsonObject();
            if (token.has("access_token")) return finish(token);
            String error = token.has("error") ? token.get("error").getAsString() : "";
            if (error.equals("slow_down")) interval += 5000;
            else if (!error.equals("authorization_pending")) throw new IOException(describe(token));
        }
        throw new IOException("Время на вход вышло, попробуй ещё раз");
    }

    public Session refresh(String refreshToken) throws IOException {
        JsonObject token = Http.postForm(MS + "/token", Map.of(
                "client_id", clientId,
                "grant_type", "refresh_token",
                "refresh_token", refreshToken,
                "scope", SCOPE)).getAsJsonObject();
        if (!token.has("access_token")) throw new IOException(describe(token));
        return finish(token);
    }

    private Session finish(JsonObject msToken) throws IOException {
        String refreshToken = msToken.get("refresh_token").getAsString();

        JsonObject xblRequest = new JsonObject();
        JsonObject xblProps = new JsonObject();
        xblProps.addProperty("AuthMethod", "RPS");
        xblProps.addProperty("SiteName", "user.auth.xboxlive.com");
        xblProps.addProperty("RpsTicket", "d=" + msToken.get("access_token").getAsString());
        xblRequest.add("Properties", xblProps);
        xblRequest.addProperty("RelyingParty", "http://auth.xboxlive.com");
        xblRequest.addProperty("TokenType", "JWT");
        JsonObject xbl = Http.postJson("https://user.auth.xboxlive.com/user/authenticate", xblRequest).getAsJsonObject();

        JsonObject xstsRequest = new JsonObject();
        JsonObject xstsProps = new JsonObject();
        xstsProps.addProperty("SandboxId", "RETAIL");
        JsonArray userTokens = new JsonArray();
        userTokens.add(xbl.get("Token").getAsString());
        xstsProps.add("UserTokens", userTokens);
        xstsRequest.add("Properties", xstsProps);
        xstsRequest.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
        xstsRequest.addProperty("TokenType", "JWT");
        JsonObject xsts;
        try {
            xsts = Http.postJson("https://xsts.auth.xboxlive.com/xsts/authorize", xstsRequest).getAsJsonObject();
        } catch (Http.HttpStatusException e) {
            throw new IOException(xstsError(e.body), e);
        }
        String userHash = xsts.getAsJsonObject("DisplayClaims").getAsJsonArray("xui").get(0)
                .getAsJsonObject().get("uhs").getAsString();

        JsonObject mcRequest = new JsonObject();
        mcRequest.addProperty("identityToken", "XBL3.0 x=" + userHash + ";" + xsts.get("Token").getAsString());
        JsonObject mc = Http.postJson("https://api.minecraftservices.com/authentication/login_with_xbox", mcRequest)
                .getAsJsonObject();
        String accessToken = mc.get("access_token").getAsString();

        JsonObject profile;
        try {
            profile = Http.getJson("https://api.minecraftservices.com/minecraft/profile", accessToken).getAsJsonObject();
        } catch (Http.HttpStatusException e) {
            if (e.status == 404) throw new IOException("На этом аккаунте Microsoft нет лицензии Minecraft", e);
            throw e;
        }
        String id = profile.get("id").getAsString();
        UUID uuid = UUID.fromString(id.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
        Account account = new Account(profile.get("name").getAsString(), uuid, accessToken, "msa", userHash);
        return new Session(account, refreshToken);
    }

    private static String describe(JsonObject error) {
        if (error.has("error_description")) return error.get("error_description").getAsString();
        return error.has("error") ? error.get("error").getAsString() : error.toString();
    }

    private static String xstsError(String body) {
        if (body != null && body.contains("2148916233")) return "У аккаунта нет профиля Xbox. Зайди один раз на xbox.com и попробуй снова";
        if (body != null && body.contains("2148916238")) return "Детский аккаунт: его нужно добавить в семью Microsoft";
        return "Xbox Live отказал во входе";
    }

    private static void sleep(long millis) throws IOException {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Вход прерван", e);
        }
    }
}
