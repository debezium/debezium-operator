/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package io.debezium.operator.systemtests.resources.dmt;

import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    public static Response resetRedis(String host, int port) {
        return sendGetRequest(host, port, "/Redis/reset");
    }

    public static Response resetMysql(String host, int port) {
        return sendGetRequest(host, port, "/Main/ResetDatabase");
    }

    public static void waitForDmt(String host, int port, Duration atMost) {
        await().atMost(atMost)
                .pollInterval(Duration.ofMillis(100))
                .until(() -> {
                    try (Response response = DmtClient.sendGetRequest(host, port, "/")) {
                        return response.isSuccessful();
                    }
                });
    }

    public static void waitForFilledRedis(String host, int port, Duration atMost, String channel) {
        await().atMost(atMost)
                .pollInterval(Duration.ofMillis(200))
                .until(() -> readRedisChannel(host, port, channel, 100).length() > 100);
    }

    public static int digStreamedData(String host, int port, int number) {
        String jsonRespo = readRedisChannel(host, port, "inventory.inventory.operator_test", number);
        if (Objects.isNull(jsonRespo)) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < number; i++) {
            if (jsonRespo.contains("name" + i)) {
                count++;
            }
        }
        return count;
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
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://" + host + ":" + port + command)
                .post(RequestBody.create(body, MEDIATYPE_JSON))
                .build();
        Call call = client.newCall(request);
        try (Response response = call.execute()) {
            return response;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String sendPostRequest(String host, int port, String command, Map<String, String> params, String body) {
        OkHttpClient client = new OkHttpClient();

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
        try (Response response = call.execute()) {
            if (response.isSuccessful()) {
                return response.body().string();
            }
            return null;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Response sendGetRequest(String host, int port, String command) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://" + host + ":" + port + command)
                .build();
        Call call = client.newCall(request);
        try (Response response = call.execute()) {
            return response;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
