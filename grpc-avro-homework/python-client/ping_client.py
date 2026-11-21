import sys

import grpc

import ping_pb2
import ping_pb2_grpc

DEFAULT_TARGET = "localhost:8080"


def main() -> None:
    target = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_TARGET
    with grpc.insecure_channel(target) as channel:
        stub = ping_pb2_grpc.PingServiceStub(channel)
        response = stub.Ping(ping_pb2.PingRequest(message="Ping", client="python-client"), timeout=5)
    print(f"Received: message={response.message}, timestamp={response.timestamp.ToJsonString()}")


if __name__ == "__main__":
    main()
