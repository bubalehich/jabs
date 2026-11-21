package io.github.bubalehich.ping.server;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;

import java.io.IOException;
import java.time.Clock;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public final class PingServer {

	private static final Logger LOG = Logger.getLogger(PingServer.class.getName());
	private static final int PORT = 8080;

	private PingServer() {
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		Server server = Grpc.newServerBuilderForPort(PORT, InsecureServerCredentials.create())
				.addService(new PingService(Clock.systemUTC()))
				.build()
				.start();
		LOG.info(() -> "gRPC server listening on port " + server.getPort());

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			server.shutdown();
			try {
				server.awaitTermination(5, TimeUnit.SECONDS);
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}));
		server.awaitTermination();
	}

}
