package com.example.demo.post;

import jakarta.validation.constraints.NotBlank;

public record PostRequest(
		@NotBlank String title,
		@NotBlank String content) {
}
