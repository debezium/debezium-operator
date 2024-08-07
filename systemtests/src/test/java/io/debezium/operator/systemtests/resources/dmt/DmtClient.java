/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.systemtests.resources.dmt;

import static io.debezium.operator.systemtests.ConfigProperties.HTTP_POLL_INTERVAL;
import static io.debezium.operator.systemtests.ConfigProperties.HTTP_POLL_TIMEOUT;
import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.skodjob.dmt.schema.DatabaseColumnEntry;
import io.skodjob.dmt.schema.DatabaseEntry;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DmtClient {
    private static final MediaType MEDIATYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    private static final Logger LOGGER = LoggerFactory.getLogger(DmtClient.class);

    private static OkHttpClient defaultClient() {
        return new OkHttpClient.Builder()
                .writeTimeout(Duration.ofSeconds(10))
                .callTimeout(Duration.ofSeconds(10))
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(10))
                .build();
    }

    public static String readRedisOffsets(String host, int port) {
        return readRedisOffsets(host, port, "metadata:debezium:offsets");
    }

    public static String readRedisOffsets(String host, int port, String key) {
        AtomicReference<String> offset = new AtomicReference<>();
        Map<String, String> params = Map.of("hashKey", key);
        await().atMost(Duration.ofSeconds(HTTP_POLL_TIMEOUT))
                .pollInterval(Duration.ofMillis(HTTP_POLL_INTERVAL))
                .until(() -> {
                    try (Response response = DmtClient.sendGetRequest(host, port, "/Redis/readHash", params)) {
                        offset.set(response.body().string());
                        return response.isSuccessful();
                    }
                    catch (Exception e) {
                        return false;
                    }
                });
        return offset.get();
    }

    public static void resetRedis(String host, int port) {
        await().atMost(Duration.ofSeconds(HTTP_POLL_TIMEOUT))
                .pollInterval(Duration.ofMillis(HTTP_POLL_INTERVAL))
                .until(() -> {
                    try (Response response = DmtClient.sendGetRequest(host, port, "/Redis/reset")) {
                        return response.isSuccessful();
                    }
                    catch (Exception e) {
                        return false;
                    }
                });
    }

    public static void resetMysql(String host, int port) {
        await().atMost(Duration.ofSeconds(HTTP_POLL_TIMEOUT))
                .pollInterval(Duration.ofMillis(HTTP_POLL_INTERVAL))
                .until(() -> {
                    try (Response response = DmtClient.sendGetRequest(host, port, "/Main/ResetDatabase")) {
                        return response.isSuccessful();
                    }
                    catch (Exception e) {
                        return false;
                    }
                });
    }

    public static void waitForDmt(String host, int port, Duration atMost) {
        await().atMost(atMost)
                .pollInterval(Duration.ofMillis(HTTP_POLL_INTERVAL))
                .until(() -> {
                    try (Response response = DmtClient.sendGetRequest(host, port, "/")) {
                        return response.isSuccessful();
                    }
                    catch (Exception e) {
                        LOGGER.trace("DMT is not ready yet!");
                        return false;
                    }
                });
    }

    public static void waitForFilledRedis(String host, int port, Duration atMost, String channel) {
        await().atMost(atMost)
                .pollInterval(Duration.ofMillis(HTTP_POLL_INTERVAL))
                .until(() -> readRedisChannel(host, port, channel, 100).length() > 100);
    }

    public static int digStreamedData(String host, int port, int number) {
        final String CHANNEL = "inventory.inventory.operator_test";
        JSONParser parser = new JSONParser();
        String jsonRespo = readRedisChannel(host, port, CHANNEL, number);

        if (Objects.isNull(jsonRespo)) {
            return 0;
        }

        try {
            JSONArray response = (JSONArray) parser.parse(jsonRespo);
            JSONObject topic = (JSONObject) response.get(0);
            JSONArray responses = (JSONArray) topic.get(CHANNEL);
            return responses.size();
        }
        catch (ParseException e) {
            LOGGER.error("Cannot parse JSON response from DMT: {}", e.getMessage());
            return 0;
        }
    }

    public static String readRedisChannel(String host, int port, String channel, int limit) {
        List<String> channels = Collections.singletonList(channel);
        Map<String, String> params = Collections.singletonMap("max", String.valueOf(limit));
        ObjectMapper mapper = new ObjectMapper();
        try {
            String body = mapper.writeValueAsString(channels);
            return sendPostRequest(host, port, "Redis/pollMessages", params, body);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean insertTestDataToDatabase(String host, int port, int number) {
        for (int i = 0; i < number; i++) {
            DatabaseEntry entry = new DatabaseEntry("operator_test", "id");
            DatabaseColumnEntry idColumn = new DatabaseColumnEntry("" + i, "id", "INT(50)");
            DatabaseColumnEntry nameColumn = new DatabaseColumnEntry("name" + i, "name", "VARCHAR(80)");
            entry.addColumnEntry(idColumn);
            entry.addColumnEntry(nameColumn);
            Response response;
            if (i == 0) {
                response = createTableAndUpsert(host, port, entry);
            }
            else {
                response = insertDataToDatabase(host, port, entry);
            }
            if (!response.isSuccessful()) {
                response.close();
                return false;
            }
            response.close();
        }
        return true;
    }

    public static Response createTableAndUpsert(String host, int port, DatabaseEntry databaseEntry) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String body = objectMapper.writeValueAsString(databaseEntry);
            return sendPostRequest(host, port, "/Main/CreateTableAndUpsert", body);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Response insertDataToDatabase(String host, int port, DatabaseEntry databaseEntry) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String body = objectMapper.writeValueAsString(databaseEntry);
            return sendPostRequest(host, port, "/Main/Insert", body);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Response sendPostRequest(String host, int port, String command, String body) {
        OkHttpClient client = defaultClient();
        Request request = new Request.Builder()
                .url("http://" + host + ":" + port + command)
                .post(RequestBody.create(body, MEDIATYPE_JSON))
                .build();
        Call call = client.newCall(request);

        AtomicReference<Response> responseAtomicReference = new AtomicReference<>();
        await().atMost(Duration.ofSeconds(HTTP_POLL_TIMEOUT))
                .pollInterval(Duration.ofMillis(HTTP_POLL_INTERVAL))
                .until(() -> {
                    try (Response response = call.execute()) {
                        if (response.isSuccessful()) {
                            responseAtomicReference.set(response);
                            return true;
                        }
                        else {
                            return false;
                        }
                    }
                    catch (Exception e) {
                        return false;
                    }
                });
        return responseAtomicReference.get();
    }

    public static String sendPostRequest(String host, int port, String command, Map<String, String> params, String body) {
        OkHttpClient client = defaultClient();

        HttpUrl.Builder builder = new HttpUrl.Builder()
                .scheme("http")
                .host(host)
                .port(port);

        String[] urlSubPaths = command.split("/");
        for (String urlSubPath : urlSubPaths) {
            builder = builder.addPathSegment(urlSubPath);
        }

        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder = builder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        HttpUrl url = builder.build();
        RequestBody requestBody = RequestBody.create(body, MEDIATYPE_JSON);
        Request request = new Request.Builder()
                .url(url)
                .method("POST", requestBody)
                .build();
        Call call = client.newCall(request);
        AtomicReference<String> responseAtomicReference = new AtomicReference<>();
        await().atMost(Duration.ofSeconds(HTTP_POLL_TIMEOUT))
                .pollInterval(Duration.ofMillis(HTTP_POLL_INTERVAL))
                .until(() -> {
                    try (Response response = call.execute()) {
                        if (response.isSuccessful()) {
                            responseAtomicReference.set(response.body().string());
                            return true;
                        }
                        else {
                            return false;
                        }
                    }
                    catch (Exception e) {
                        return false;
                    }
                });
        return responseAtomicReference.get();
    }

    public static Response sendGetRequest(String host, int port, String command) throws IOException {
        OkHttpClient client = defaultClient();
        Request request = new Request.Builder()
                .url("http://" + host + ":" + port + command)
                .build();
        Call call = client.newCall(request);
        return call.execute();
    }

    public static Response sendGetRequest(String host, int port, String command, Map<String, String> params) throws IOException {
        OkHttpClient client = defaultClient();

        HttpUrl.Builder builder = Objects.requireNonNull(HttpUrl.parse("http://" + host + ":" + port + command))
                .newBuilder();

        if (!Objects.isNull(params)) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder = builder.addQueryParameter(entry.getKey(), entry.getValue());
            }
        }

        Request request = new Request.Builder()
                .url(builder.build())
                .build();

        Call call = client.newCall(request);
        return call.execute();
    }
}
