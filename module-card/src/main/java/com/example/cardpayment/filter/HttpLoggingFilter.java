package com.example.cardpayment.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class HttpLoggingFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpLoggingFilter.class);

    private final ObjectMapper objectMapper;

    public HttpLoggingFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        long startedAt = System.currentTimeMillis();

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long elapsedMs = System.currentTimeMillis() - startedAt;
            LOGGER.info("[REQUEST] method={} uri={} query={} body={}",
                    request.getMethod(), request.getRequestURI(), request.getQueryString(),
                    convertByteArrayToString(requestWrapper.getContentAsByteArray(), request.getContentType()));
            LOGGER.info("[RESPONSE] method={} uri={} status={} durationMs={} body={}",
                    request.getMethod(), request.getRequestURI(), responseWrapper.getStatus(), elapsedMs,
                    convertByteArrayToString(responseWrapper.getContentAsByteArray(), responseWrapper.getContentType()));
            responseWrapper.copyBodyToResponse();
        }
    }

    private String convertByteArrayToString(byte[] content, String contentType) {
        if (content.length == 0) {
            return "";
        }

        try {
            JsonNode root = objectMapper.readTree(content);
            return objectMapper.writeValueAsString(root);
        } catch (IOException exception) {
            return new String(content, StandardCharsets.UTF_8);
        }
    }
}
