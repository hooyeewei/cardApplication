package com.example.cardpayment.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HttpLoggingFilterTest {

    private final HttpLoggingFilter filter = new HttpLoggingFilter(new ObjectMapper());

    @Test
    void doFilter_whenResponseIsJson_returnPreservedJsonResponseBody() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/card-payment/create");
        request.setContentType("application/json");
        request.setContent("{\"merchantReference\":\"M1\"}".getBytes(StandardCharsets.UTF_8));

        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> {
            HttpServletResponse servletResponse = (HttpServletResponse) res;
            servletResponse.setStatus(400);
            servletResponse.setContentType("application/json");
            servletResponse.getWriter().write("{\"message\":\"bad request\"}");
        };

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString()).isEqualTo("{\"message\":\"bad request\"}");
    }

    @Test
    void doFilter_whenResponseIsNonJson_returnPreservedRawResponseBody() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/card-payment/1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> {
            HttpServletResponse servletResponse = (HttpServletResponse) res;
            servletResponse.setStatus(500);
            servletResponse.setContentType("text/plain");
            servletResponse.getWriter().write("plain error");
        };

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getContentAsString()).isEqualTo("plain error");
    }

    @Test
    void doFilter_whenFilterChainThrowsException_returnRethrownException() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/card-payment/1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, res) -> {
            throw new ServletException("boom");
        };

        assertThatThrownBy(() -> filter.doFilter(request, response, chain))
                .isInstanceOf(ServletException.class)
                .hasMessageContaining("boom");
    }
}
