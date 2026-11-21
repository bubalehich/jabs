package io.github.bubalehich.ping.client;

import com.google.protobuf.Timestamp;
import io.github.bubalehich.ping.api.PingRequest;
import io.github.bubalehich.ping.api.PingServiceGrpc;
import io.github.bubalehich.ping.api.PongResponse;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public final class PingClient {

	private static final String DEFAULT_TARGET = "localhost:8080";

	private PingClient() {
	}

	public static void main(String[] args) throws InterruptedException {
		String target = args.length > 0 ? args[0] : DEFAULT_TARGET;
		ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create()).build();
		try {
			PongResponse response = PingServiceGrpc.newBlockingStub(channel)
					.withDeadlineAfter(5, TimeUnit.SECONDS)
					.ping(PingRequest.newBuilder().setMessage("Ping").setClient("java-client").build());
			System.out.printf("Received: message=%s, timestamp=%s%n", response.getMessage(),
					toInstant(response.getTimestamp()));
		}
		finally {
			channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
		}
	}

	private static Instant toInstant(Timestamp timestamp) {
		return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
	}

}
