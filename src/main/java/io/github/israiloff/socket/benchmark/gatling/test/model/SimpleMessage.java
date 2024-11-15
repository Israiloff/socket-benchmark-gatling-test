package io.github.israiloff.socket.benchmark.gatling.test.model;

import java.io.Serializable;

/**
 * Simple message model.
 *
 * @param data Plain text message.
 */
public record SimpleMessage(String data) implements Serializable {
}
