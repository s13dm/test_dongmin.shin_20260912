package com.example.demo.post;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

	private final PostService postService;

	@GetMapping
	public List<Post> findAll() {
		return postService.findAll();
	}

	@GetMapping("/{id}")
	public Post findById(@PathVariable Long id) {
		return postService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Post create(@Valid @RequestBody PostRequest request) {
		return postService.create(request);
	}

	@PutMapping("/{id}")
	public Post update(@PathVariable Long id, @Valid @RequestBody PostRequest request) {
		return postService.update(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		postService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
