package com.bookstore.forecasting;

import com.bookstore.web.BookPriceSuggestionController;
import com.bookstore.web.WebExceptionHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.nio.file.Path;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BookPriceSuggestionControllerTest {
    @TempDir Path directory;

    @Test void trainsThenSuggestsAndValidatesHttpInputs() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new BookPriceSuggestionController(directory.toString()))
                .setControllerAdvice(new WebExceptionHandler()).build();
        String features = "{\"pageCount\":200,\"publicationAgeYears\":1}";
        mvc.perform(post("/api/pricing/suggestions").contentType(MediaType.APPLICATION_JSON).content(features))
                .andExpect(status().isBadRequest());
        var rows = new java.util.ArrayList<String>();
        for (int i = 0; i < 10; i++) rows.add("{\"features\":" + features + ",\"price\":42}");
        mvc.perform(post("/api/pricing/suggestions/train").contentType(MediaType.APPLICATION_JSON)
                .content("[" + String.join(",", rows) + "]")).andExpect(status().isNoContent());
        mvc.perform(post("/api/pricing/suggestions").contentType(MediaType.APPLICATION_JSON).content(features))
                .andExpect(status().isOk()).andExpect(jsonPath("$.suggestedPrice").value(42.0));
        mvc.perform(post("/api/pricing/suggestions").contentType(MediaType.APPLICATION_JSON)
                .content("{\"pageCount\":0,\"publicationAgeYears\":1}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/pricing/suggestions/train").contentType(MediaType.APPLICATION_JSON).content("[]"))
                .andExpect(status().isBadRequest());
    }
}
