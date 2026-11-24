package io.github.bubalehich.sports.etl;

import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

class BatchSubscriber<T> extends BaseSubscriber<T> {

	private static final Logger LOG = LoggerFactory.getLogger(BatchSubscriber.class);

	private final int batchSize;
	private final Function<List<T>, Mono<Long>> batchHandler;
	private final List<T> batch = new ArrayList<>();
	private long stored;

	BatchSubscriber(int batchSize, Function<List<T>, Mono<Long>> batchHandler) {
		this.batchSize = batchSize;
		this.batchHandler = batchHandler;
	}

	@Override
	protected void hookOnSubscribe(Subscription subscription) {
		request(batchSize);
	}

	@Override
	protected void hookOnNext(T value) {
		batch.add(value);
		if (batch.size() == batchSize) {
			storeBatch(() -> request(batchSize));
		}
	}

	@Override
	protected void hookOnComplete() {
		if (!batch.isEmpty()) {
			storeBatch(() -> { });
		}
	}

	@Override
	protected void hookOnError(Throwable error) {
		LOG.error("Import failed", error);
	}

	private void storeBatch(Runnable afterStored) {
		List<T> items = List.copyOf(batch);
		batch.clear();
		batchHandler.apply(items).subscribe(
				count -> {
					stored += count;
					LOG.info("Batch of {} processed: {} new stored, {} in total", items.size(), count, stored);
				},
				error -> {
					LOG.error("Failed to store batch, cancelling import", error);
					cancel();
				},
				afterStored);
	}

}
