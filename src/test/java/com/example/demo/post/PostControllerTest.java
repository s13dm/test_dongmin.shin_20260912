package com.example.demo.post;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PostControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void createAndFindPost() throws Exception {
		mockMvc.perform(post("/api/posts")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"title\":\"hello\",\"content\":\"world\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title", is("hello")));

		mockMvc.perform(get("/api/posts"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].title", is("hello")));
	}

	@Test
	void findMissingPostReturnsNotFound() throws Exception {
		mockMvc.perform(get("/api/posts/999"))
				.andExpect(status().isNotFound());
	}
}
