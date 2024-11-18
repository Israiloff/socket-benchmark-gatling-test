package io.github.israiloff.socket.benchmark.gatling.test.scenario;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.bodyBytes;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.ws;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.core.CheckBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import io.github.israiloff.socket.benchmark.gatling.test.model.DisconnectPacket;
import io.github.israiloff.socket.benchmark.gatling.test.model.Packet;
import io.github.israiloff.socket.benchmark.gatling.test.model.SimpleMessage;

public class SimpleSocketIOSimulation extends Simulation {

    private static final String RESULT_EVENT_NAME = "echoResult";

    HttpProtocolBuilder httpProtocol = http.wsBaseUrl("ws://localhost:5555").wsReconnect()
            .wsMaxReconnects(5).wsAutoReplySocketIo4();

    byte[] packConnectBytes;
    byte[] packDisconnectBytes;
    byte[] packSendBytes;

    ObjectMapper objectMapper = new ObjectMapper();

    {
        try {
            var message = objectMapper.writeValueAsString(new SimpleMessage("Hello, world!"));
            packConnectBytes = objectMapper.writeValueAsBytes(new Packet(0, "/", Arrays.asList()));
            packSendBytes = objectMapper
                    .writeValueAsBytes(new Packet(2, "/", Arrays.asList("echo", message)));
            packDisconnectBytes = objectMapper.writeValueAsBytes(new DisconnectPacket("/"));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    CheckBuilder checkBroadcastEventSaveMessage = bodyBytes().transform(bytes -> {
        Packet packet = null;

        try {
            packet = objectMapper.readValue(bytes, Packet.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize incoming bytes to Packet object. ",
                    e);
        }

        String eventName = packet.data().get(0);

        if (!eventName.equals(RESULT_EVENT_NAME)) {
            throw new RuntimeException(MessageFormat.format("Expected '{}' event, got '{}' instead",
                    RESULT_EVENT_NAME, eventName));
        }

        String message = packet.data().get(1);
        return message;
    }).saveAs("response");

    ScenarioBuilder scene = scenario("WebSocket")
            .exec(ws("Connect WS").connect("/socket.io/?EIO=4&transport=websocket"))
            .exec(ws("Connect to Socket.IO").sendBytes(packConnectBytes)).pause(1)

            .exec(ws("Say hi").sendBytes(packSendBytes).await(30).on(
                    ws.checkBinaryMessage("checkMessage").check(checkBroadcastEventSaveMessage)))
            .exec(session -> {
                System.out.println("response: " + session.get("response"));
                return session;
            })

            .exec(ws("Disconnect from Socket.IO").sendBytes(packDisconnectBytes))
            .exec(ws("Close WS").close());

    {

        setUp(scene.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
    }
}
