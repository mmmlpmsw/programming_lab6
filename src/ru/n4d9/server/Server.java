package ru.n4d9.server;

import ru.n4d9.Message;
import ru.n4d9.transmitter.Receiver;
import ru.n4d9.transmitter.ReceiverListener;
import ru.n4d9.transmitter.Sender;
import ru.n4d9.transmitter.SenderAdapter;

import java.io.IOException;
import java.net.InetAddress;

public class Server {
    private static final int RECEIVING_PORT = 2222;

    public static void main(String[] args) {
        try {
            Receiver receiver = new Receiver(RECEIVING_PORT, false);
            receiver.setListener(generateListener());
            receiver.startListening();

            System.out.println("Сервер слушает порт " + RECEIVING_PORT + "...");
        } catch (IOException e) {
            System.out.println("Не получилось запустить сервер: " + e.toString());
        }
    }

    private static void processRequest(int requestID, byte[] data, InetAddress address) {
        Message message;
        try {
            message = Message.deserialize(data);
            String text = message.getText();
            System.out.println("Пришёл запрос: " + text);

            new Thread(new Resolver(message, requestID, address)).start();
            /*Message response = Resolver.resolve(message, requestID);

            respond(response.serialize(), message.getSourcePort(), address);*/
        } catch (ClassNotFoundException | IOException e) {
            System.out.println("Не получилось обработать запрос: " + e.toString());
        }
    }

    private static void respond(byte[] data, int port, InetAddress address) {
        Sender.send(data, address, port, false, new SenderAdapter() {
            @Override
            public void onError(String message) {
                System.out.println("Не получилось ответить на запрос: " + message);
            }
        });
    }

    private static ReceiverListener generateListener() {
        return new ReceiverListener() {
            @Override
            public void received(int requestID, byte[] data, InetAddress address) {
                processRequest(requestID, data, address);
            }

            @Override
            public void exceptionThrown(Exception e) {
                System.out.println("Не получилось принять запрос: " + e.toString());
            }
        };
    }
}
