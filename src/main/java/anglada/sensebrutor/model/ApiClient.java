package anglada.sensebrutor.model;

// Created by Github Copilot
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public class ApiClient {

    private final HttpClient client;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String baseUrl;
    private final String defaultBlobContainer = "dimedianetblobs";

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    private HttpRequest.Builder requestBuilder(String path, String jwt) {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(30));
        if (jwt != null && !jwt.isBlank()) {
            b.header("Authorization", "Bearer " + jwt);
        }
        return b;
    }

    public String login(String email, String password) throws Exception {
        Map<String, Object> body = Map.of("email", email, "password", password);
        String json = mapper.writeValueAsString(body);
        HttpRequest req = requestBuilder("/api/Auth/login", null)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() / 100 == 2) {
            var node = mapper.readTree(resp.body());
            String token = null;
            if (node.has("token")) {
                token = node.get("token").asText();
            } else if (node.has("access_token")) {
                token = node.get("access_token").asText();
            } else if (node.has("jwt")) {
                token = node.get("jwt").asText();
            }
            if (token != null) {
                return token;
            } else {
                return resp.body();
            }
        } else {
            throw new IOException("Login failed: " + resp.statusCode() + " -> " + resp.body());
        }
    }

    public Usuari getMe(String jwt) throws Exception {
        HttpRequest req = requestBuilder("/api/users/me", jwt).GET().build();
        HttpResponse<String> r = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() / 100 == 2) {
            return mapper.readValue(r.body(), Usuari.class);
        }
        throw new IOException("getMe failed: " + r.statusCode() + " " + r.body());
    }

    public String getNickName(int id, String jwt) throws Exception {
        HttpRequest req = requestBuilder("/api/users/" + id + "/nickname", jwt).GET().build();
        HttpResponse<String> r = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() / 100 == 2) {
            String body = r.body();
            // Try to handle JSON responses first, then fall back to raw/plain text
            try {
                JsonNode node = mapper.readTree(body);
                if (node.isTextual()) {
                    return node.asText(); // "Mike"
                }
                if (node.has("nickName")) {
                    return node.get("nickName").asText();
                }
                if (node.has("nickname")) {
                    return node.get("nickname").asText();
                }
                // if the response is some other JSON structure, return its string form
                return node.toString();
            } catch (Exception ex) {
                // Not valid JSON — assume plain text (e.g.,: Mike)
                return body;
            }
        }
        throw new IOException("getNickName failed: " + r.statusCode());
    }

    public List<Media> getAllMedia(String jwt) throws Exception {
        HttpRequest req = requestBuilder("/api/files/all", jwt).GET().build();
        HttpResponse<String> r = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() / 100 == 2) {
            return mapper.readValue(r.body(), new TypeReference<List<Media>>() {
            });
        }
        throw new IOException("getAllMedia failed: " + r.statusCode());
    }

    public List<Media> getMediaByUser(int userId, String jwt) throws Exception {
        HttpRequest req = requestBuilder("/api/files/user/" + userId, jwt).GET().build();
        HttpResponse<String> r = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() / 100 == 2) {
            return mapper.readValue(r.body(), new TypeReference<List<Media>>() {
            });
        }
        throw new IOException("getMediaByUser failed: " + r.statusCode());
    }

    public List<Media> getMyMedia(String jwt) throws Exception {
        HttpRequest req = requestBuilder("/api/files/me", jwt).GET().build();
        HttpResponse<String> r = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() / 100 == 2) {
            return mapper.readValue(r.body(), new TypeReference<List<Media>>() {
            });
        }
        throw new IOException("getMyMedia failed: " + r.statusCode());
    }

    public String listBlobs(String jwt) throws Exception {
        String path = "/api/files?container=" + URLEncoder.encode(defaultBlobContainer, "UTF-8");
        HttpRequest req = requestBuilder(path, jwt).GET().build();
        HttpResponse<String> r = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() / 100 == 2) {
            return r.body();
        }
        throw new IOException("listBlobs failed: " + r.statusCode());
    }

    // Download blob data and write to destFile
    public void download(int id, File destFile, String jwt) throws Exception {
        String path = "/api/files/" + id + "?container=" + URLEncoder.encode(defaultBlobContainer, "UTF-8");
        HttpRequest req = requestBuilder(path, jwt).GET().build();
        HttpResponse<InputStream> r = client.send(req, HttpResponse.BodyHandlers.ofInputStream());
        if (r.statusCode() / 100 == 2) {
            try (InputStream is = r.body(); OutputStream os = new FileOutputStream(destFile)) {
                is.transferTo(os);
            }
            return;
        }
        // If the server responds with 404 we can surface a more meaningful message
        if (r.statusCode() == 404) {
            throw new FileNotFoundException("Media with id " + id + " not found (404).");
        }
        throw new IOException("downloadById failed: " + r.statusCode());
    }

    // Upload file as multipart/form-data (field names: file, downloadedFromUrl, container)
    public String uploadFileMultipart(File file, String downloadedFromUrl, String jwt) throws Exception {
        String boundary = "----JavaClientBoundary" + System.currentTimeMillis();
        Map<String, String> fields = new HashMap<>();
        if (downloadedFromUrl != null) {
            fields.put("downloadedFromUrl", downloadedFromUrl);
        }
        if (defaultBlobContainer != null) {
            fields.put("container", defaultBlobContainer);
        }

        HttpRequest.BodyPublisher body = buildMultipart(file, "file", fields, boundary);
        HttpRequest req = requestBuilder("/api/files/upload", jwt)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(body)
                .build();
        HttpResponse<String> r = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() / 100 == 2) {
            return r.body();
        }
        throw new IOException("uploadFileMultipart failed: " + r.statusCode() + " " + r.body());
    }

    // Helper to build multipart body
    private static HttpRequest.BodyPublisher buildMultipart(File file, String fileFieldName, Map<String, String> fields, String boundary) throws IOException {
        var byteArrays = new ArrayList<byte[]>();
        String CRLF = "\r\n";

        for (Map.Entry<String, String> e : fields.entrySet()) {
            String part = "--" + boundary + CRLF
                    + "Content-Disposition: form-data; name=\"" + e.getKey() + "\"" + CRLF + CRLF
                    + e.getValue() + CRLF;
            byteArrays.add(part.getBytes());
        }

        String fileHeader = "--" + boundary + CRLF
                + "Content-Disposition: form-data; name=\"" + fileFieldName + "\"; filename=\"" + file.getName() + "\"" + CRLF
                + "Content-Type: " + Files.probeContentType(file.toPath()) + CRLF + CRLF;
        byteArrays.add(fileHeader.getBytes());
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        byteArrays.add(fileBytes);
        byteArrays.add(CRLF.getBytes());

        String end = "--" + boundary + "--" + CRLF;
        byteArrays.add(end.getBytes());

        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }

    public List<Media> getMediaAddedSince(OffsetDateTime from, String jwt) throws Exception {
        if (from == null) {
            throw new IllegalArgumentException("from is required");
        }
        String fromIso = from.toString(); // ISO-8601 with offset, e.g. 2025-11-18T12:00:00Z
        String path = "/api/files/added-since?from=" + URLEncoder.encode(fromIso, "UTF-8");
        if (defaultBlobContainer != null && !defaultBlobContainer.isBlank()) {
            path += "&container=" + URLEncoder.encode(defaultBlobContainer, "UTF-8");
        }

        HttpRequest req = requestBuilder(path, jwt).GET().build();
        HttpResponse<String> r = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() / 100 == 2) {
            return mapper.readValue(r.body(), new TypeReference<List<Media>>() {
            });
        }
        throw new IOException("getMediaAddedSince failed: " + r.statusCode() + " -> " + r.body());
    }

// Convenience overload accepting ISO-8601 string (e.g. "2025-11-18T12:00:00Z")
    public List<Media> getMediaAddedSince(String isoFrom, String jwt) throws Exception {
        if (isoFrom == null || isoFrom.isBlank()) {
            throw new IllegalArgumentException("isoFrom is required");
        }
        // validate by parsing
        OffsetDateTime from = OffsetDateTime.parse(isoFrom);
        return getMediaAddedSince(from, jwt);
    }
}