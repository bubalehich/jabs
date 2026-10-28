package io.github.bubalehich.events.domain.common;

import java.util.List;
import java.util.function.Function;

public record PageResult<T>(List<T> content, int page, int size, long totalElements) {

	public PageResult {
		content = List.copyOf(content);
	}

	public int totalPages() {
		return (int) Math.ceilDiv(totalElements, size);
	}

	public <R> PageResult<R> map(Function<? super T, ? extends R> mapper) {
		return new PageResult<>(content.stream().<R>map(mapper).toList(), page, size, totalElements);
	}

}
