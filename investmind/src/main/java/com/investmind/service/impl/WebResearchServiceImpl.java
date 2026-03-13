package com.investmind.service.impl;

import com.investmind.service.WebResearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class WebResearchServiceImpl implements WebResearchService {

    private static final Pattern HK_TICKER_PATTERN = Pattern.compile("\\b\\d{5}\\b");
    private static final Pattern DDG_RESULT_PATTERN = Pattern.compile(
            "<a[^>]*class=\"result__a\"[^>]*href=\"([^\"]+)\"[^>]*>(.*?)</a>[\\s\\S]*?<a[^>]*class=\"result__snippet\"[^>]*>(.*?)</a>",
            Pattern.CASE_INSENSITIVE);

    @Value("${investmind.ai.web-search-enabled:true}")
    private Boolean webSearchEnabled;

    @Value("${investmind.ai.web-search-base-url:https://s.jina.ai/}")
    private String webSearchBaseUrl;

    @Value("${investmind.ai.web-search-max-chars:4000}")
    private Integer webSearchMaxChars;

    @Override
    public String research(String question) {
        if (!Boolean.TRUE.equals(webSearchEnabled) || !StringUtils.hasText(question)) {
            return "";
        }

        StringBuilder merged = new StringBuilder();
        Set<String> queries = buildQueries(question);
        int successCount = 0;
        for (String q : queries) {
            String snippet = runSingleQuery(q);
            if (StringUtils.hasText(snippet)) {
                if (!merged.isEmpty()) {
                    merged.append("\n\n---\n\n");
                }
                merged.append("Query: ").append(q).append("\n").append(snippet);
                successCount++;
            }
            if (successCount >= 3) {
                break;
            }
        }

        return merged.toString();
    }

    private String runSingleQuery(String question) {
        String fromJina = queryByJina(question);
        if (StringUtils.hasText(fromJina)) {
            return fromJina;
        }
        return queryByDuckDuckGo(question);
    }

    private String queryByJina(String question) {
        try {
            String query = URLEncoder.encode(question, StandardCharsets.UTF_8);
            String base = normalizeBaseUrl(webSearchBaseUrl);
            String url = base + query;

            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Accept", "text/plain");
            httpGet.setHeader("User-Agent", "InvestMind/1.0");

            try (CloseableHttpClient client = HttpClients.createDefault();
                 CloseableHttpResponse response = client.execute(httpGet)) {
                int statusCode = response.getCode();
                String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                if (statusCode >= 200 && statusCode < 300 && StringUtils.hasText(body)) {
                    int max = webSearchMaxChars == null ? 4000 : Math.max(1000, webSearchMaxChars);
                    return body.length() > max ? body.substring(0, max) : body;
                }
                log.warn("联网检索失败，status={}", statusCode);
            }
        } catch (Exception e) {
            log.warn("联网检索异常: {}", e.getMessage());
        }
        return "";
    }

    private String queryByDuckDuckGo(String question) {
        try {
            String q = URLEncoder.encode(question, StandardCharsets.UTF_8);
            String url = "https://duckduckgo.com/html/?q=" + q;
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Accept", "text/html");
            httpGet.setHeader("User-Agent", "Mozilla/5.0 InvestMind/1.0");

            try (CloseableHttpClient client = HttpClients.createDefault();
                 CloseableHttpResponse response = client.execute(httpGet)) {
                int statusCode = response.getCode();
                if (statusCode < 200 || statusCode >= 300) {
                    log.warn("DuckDuckGo检索失败，status={}", statusCode);
                    return "";
                }
                String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                if (!StringUtils.hasText(body)) {
                    return "";
                }

                Matcher matcher = DDG_RESULT_PATTERN.matcher(body);
                StringBuilder out = new StringBuilder();
                int count = 0;
                while (matcher.find() && count < 5) {
                    String link = cleanHtml(matcher.group(1));
                    String title = cleanHtml(matcher.group(2));
                    String snippet = cleanHtml(matcher.group(3));
                    if (!StringUtils.hasText(title) && !StringUtils.hasText(snippet)) {
                        continue;
                    }
                    if (!out.isEmpty()) {
                        out.append("\n");
                    }
                    out.append("- ").append(title);
                    if (StringUtils.hasText(snippet)) {
                        out.append(" | ").append(snippet);
                    }
                    if (StringUtils.hasText(link)) {
                        out.append(" | ").append(link);
                    }
                    count++;
                }
                return out.toString();
            }
        } catch (Exception e) {
            log.warn("DuckDuckGo检索异常: {}", e.getMessage());
            return "";
        }
    }

    private Set<String> buildQueries(String question) {
        Set<String> queries = new LinkedHashSet<>();
        String q = question.trim();
        queries.add(q + " 最新 财报 估值 关键数据");
        queries.add(q + " HKEX annual results announcement pdf");
        queries.add(q + " AASTOCKS quote market cap 52 week high low");

        for (String ticker : extractTickers(q)) {
            queries.add(ticker + ".HK annual report cash and cash equivalents borrowings operating cash flow");
            queries.add(ticker + ".HK dividend yield market cap 52-week low high");
        }
        return queries;
    }

    private List<String> extractTickers(String text) {
        List<String> tickers = new ArrayList<>();
        Matcher matcher = HK_TICKER_PATTERN.matcher(text == null ? "" : text);
        while (matcher.find()) {
            tickers.add(matcher.group());
        }
        return tickers;
    }

    private String normalizeBaseUrl(String url) {
        String base = StringUtils.hasText(url) ? url.trim() : "https://s.jina.ai/";
        if (!base.endsWith("/")) {
            base += "/";
        }
        return base;
    }

    private String cleanHtml(String input) {
        if (!StringUtils.hasText(input)) {
            return "";
        }
        String text = input
                .replaceAll("<[^>]+>", " ")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#x27;", "'")
                .replace("&#39;", "'")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return text;
    }
}
