package io.github.bubalehich.ping.server;

import com.google.protobuf.Timestamp;
import io.github.bubalehich.ping.api.PingRequest;
import io.github.bubalehich.ping.api.PingServiceGrpc;
import io.github.bubalehich.ping.api.PongResponse;
import io.grpc.stub.StreamObserver;

import java.time.Clock;
import java.time.Instant;
import java.util.logging.Logger;

class PingService extends PingServiceGrpc.PingServiceImplBase {

	private static final Logger LOG = Logger.getLogger(PingService.class.getName());

	private final Clock clock;

	PingService(Clock clock) {
		this.clock = clock;
	}

	@Override
	public void ping(PingRequest request, StreamObserver<PongResponse> responseObserver) {
		LOG.info(() -> "Received '%s' from %s".formatted(request.getMessage(), request.getClient()));
		Instant now = clock.instant();
		PongResponse response = PongResponse.newBuilder()
				.setMessage("Pong")
				.setTimestamp(Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()))
				.build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

}
