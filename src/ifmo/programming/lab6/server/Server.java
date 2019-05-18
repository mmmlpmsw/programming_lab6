package ifmo.programming.lab6.server;



import ifmo.programming.lab6.transmitter.Receiver;
import ifmo.programming.lab6.transmitter.ReceiverListener;
import ifmo.programming.lab6.transmitter.Sender;
import ifmo.programming.lab6.transmitter.SenderAdapter;
import ifmo.programming.lab6.Message;

import java.io.IOException;
import java.net.InetAddress;

public class Server {
    private static final int RECEIVING_PORT = 2222;
    static Building building = new Building();

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

            new Thread(new Resolver(message, requestID, address, building)).start();
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
