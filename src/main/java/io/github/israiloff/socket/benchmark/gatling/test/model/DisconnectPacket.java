package io.github.israiloff.socket.benchmark.gatling.test.model;

import java.io.Serializable;

public record DisconnectPacket(int type, String nsp) implements Serializable {
    public DisconnectPacket(String nsp) {
        this(1, nsp);
    }
}
