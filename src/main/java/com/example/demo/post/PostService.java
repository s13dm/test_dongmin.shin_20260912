package com.example.demo.post;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

	private final PostRepository postRepository;

	public List<Post> findAll() {
		return postRepository.findAll();
	}

	public Post findById(Long id) {
		return postRepository.findById(id)
				.orElseThrow(() -> new PostNotFoundException(id));
	}

	@Transactional
	public Post create(PostRequest request) {
		return postRepository.save(new Post(request.title(), request.content()));
	}

	@Transactional
	public Post update(Long id, PostRequest request) {
		Post post = findById(id);
		post.setTitle(request.title());
		post.setContent(request.content());
		return post;
	}

	@Transactional
	public void delete(Long id) {
		Post post = findById(id);
		postRepository.delete(post);
	}
}
