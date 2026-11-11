package com.mindgames.backend.services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.HtmlUtils;

import com.mindgames.backend.entities.SearchContent;

// Búsqueda real "fuera de la página": consulta en vivo la API pública de Wikipedia en español.
@Service
public class WikipediaSearchService {

    private static final String WIKIPEDIA_API = "https://es.wikipedia.org/w/api.php";

    private final RestClient restClient = RestClient.create();

    @SuppressWarnings("unchecked")
    public List<SearchContent> buscar(String query, int limite) {
        List<SearchContent> resultados = new ArrayList<>();

        if (query == null || query.isBlank()) {
            return resultados;
        }

        String url = WIKIPEDIA_API
                + "?action=query&list=search&format=json&srlimit=" + limite
                + "&srsearch=" + URLEncoder.encode(query, StandardCharsets.UTF_8);

        Map<String, Object> respuesta = restClient.get()
                .uri(url)
                .header("User-Agent", "MindGames/1.0 (proyecto educativo UTEQ)")
                .retrieve()
                .body(Map.class);

        if (respuesta == null) {
            return resultados;
        }

        Object queryObj = respuesta.get("query");
        if (!(queryObj instanceof Map)) {
            return resultados;
        }

        Object searchObj = ((Map<String, Object>) queryObj).get("search");
        if (!(searchObj instanceof List)) {
            return resultados;
        }

        for (Object item : (List<Object>) searchObj) {
            Map<String, Object> hit = (Map<String, Object>) item;

            String pageId = String.valueOf(hit.get("pageid"));
            String titulo = String.valueOf(hit.get("title"));
            String snippetHtml = String.valueOf(hit.getOrDefault("snippet", ""));

            // El snippet de Wikipedia trae HTML (<span class="searchmatch">...</span>); lo limpiamos
            String descripcion = HtmlUtils.htmlUnescape(snippetHtml.replaceAll("<[^>]+>", ""));
            String articuloUrl = "https://es.wikipedia.org/wiki/" + titulo.replace(" ", "_");

            resultados.add(new SearchContent("wiki-" + pageId, titulo, descripcion, articuloUrl));
        }

        return resultados;
    }
}
