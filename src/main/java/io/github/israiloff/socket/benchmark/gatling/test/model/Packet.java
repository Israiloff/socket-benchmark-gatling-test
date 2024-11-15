package io.github.israiloff.socket.benchmark.gatling.test.model;

import java.io.Serializable;
import java.util.List;

public record Packet(int type, String nsp, List<String> data) implements Serializable {
}
