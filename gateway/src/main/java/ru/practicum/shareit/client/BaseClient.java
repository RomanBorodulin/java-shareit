package ru.practicum.shareit.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class BaseClient {
    protected final RestTemplate rest;

    public BaseClient(RestTemplate rest) {
        this.rest = rest;
    }

    protected <E> E get(String path, Class<E> type) {
        return get(path, null, null, type);
    }

    protected <E> E get(String path, long userId, Class<E> type) {
        return get(path, userId, null, type);
    }

    protected <E> E get(String path, Long userId, @Nullable Map<String, Object> parameters,
                        Class<E> type) {
        return makeAndSendRequest(HttpMethod.GET, path, userId, parameters, null, type);
    }

    protected <T, E> E post(String path, T body, Class<E> type) {
        return post(path, null, null, body, type);
    }

    protected <T, E> E post(String path, long userId, T body, Class<E> type) {
        return post(path, userId, null, body, type);
    }

    protected <T, E> E post(String path, Long userId, @Nullable Map<String, Object> parameters, T body,
                            Class<E> type) {
        return makeAndSendRequest(HttpMethod.POST, path, userId, parameters, body, type);
    }

    protected <T, E> E patch(String path, T body, Class<E> type) {
        return patch(path, null, null, body, type);
    }

    protected <E> E patch(String path, long userId, Class<E> type) {
        return patch(path, userId, null, null, type);
    }

    protected <T, E> E patch(String path, long userId, T body, Class<E> type) {
        return patch(path, userId, null, body, type);
    }

    protected <T, E> E patch(String path, Long userId, @Nullable Map<String, Object> parameters, T body,
                             Class<E> type) {
        return makeAndSendRequest(HttpMethod.PATCH, path, userId, parameters, body, type);
    }

    protected void delete(String path) {
        delete(path, null, null);
    }

    protected void delete(String path, long userId) {
        delete(path, userId, null);
    }

    protected void delete(String path, Long userId, @Nullable Map<String, Object> parameters) {
        makeAndSendRequest(HttpMethod.DELETE, path, userId, parameters, null, Void.class);
    }

    private <T, E> E makeAndSendRequest(HttpMethod method, String path, Long userId,
                                        @Nullable Map<String, Object> parameters, @Nullable T body, Class<E> type) {
        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders(userId));

        ResponseEntity<E> shareitServerResponse;

        if (parameters != null) {
            shareitServerResponse = rest.exchange(path, method, requestEntity, type, parameters);
        } else {
            shareitServerResponse = rest.exchange(path, method, requestEntity, type);
        }
        return shareitServerResponse.getBody();
    }

    private HttpHeaders defaultHeaders(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (userId != null) {
            headers.set("X-Sharer-User-Id", String.valueOf(userId));
        }
        return headers;
    }
}
