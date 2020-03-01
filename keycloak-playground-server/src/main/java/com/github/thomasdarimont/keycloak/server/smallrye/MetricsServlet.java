package com.github.thomasdarimont.keycloak.server.smallrye;

import io.smallrye.metrics.MetricsRequestHandler;
import io.smallrye.metrics.exporters.OpenMetricsExporter;
import org.eclipse.microprofile.metrics.MetricRegistry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

public class MetricsServlet extends HttpServlet {

    private static final MetricsRequestHandler HANDLER = new MetricsRequestHandler();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String requestURI = req.getRequestURI();

        String method = req.getMethod();
        Stream<String> acceptHeaders = Arrays.stream(req.getHeader("Accept").split(","));

        HANDLER.handleRequest(requestURI, method, acceptHeaders, (status, message, headers) -> {

            resp.setStatus(status);

            for (Map.Entry<String, String> entry : headers.entrySet()) {
                resp.setHeader(entry.getKey(), entry.getValue());
            }

            OpenMetricsExporter exporter = new OpenMetricsExporter();
            StringBuilder stringBuilder = exporter.exportOneScope(MetricRegistry.Type.APPLICATION);

            resp.getWriter().println(stringBuilder);
        });
    }
}
