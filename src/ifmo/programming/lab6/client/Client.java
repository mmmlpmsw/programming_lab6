package ifmo.programming.lab6.client;

import ifmo.programming.lab6.transmitter.Receiver;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;


public class Client {

    private static Scanner scanner;
    private static Receiver receiver;
    private static int port;

    public static void main(String[] args) {

        try {
            // Изменение кодировки вывода для поддержки Git Bash
            System.setOut(new PrintStream(System.out, true, "UTF-8"));
        } catch (UnsupportedEncodingException ignored) {}

        scanner = new Scanner(System.in);
        System.out.print("Введите порт: ");
        try {
            port = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.err.println("Порт должен быть целым числом от 0 до 65535, клиент будет отключен.");
            System.exit(-1);
        }
        try {
            receiver = new Receiver(port, true);
            receiver.startListening();
        } catch (IOException e) {
            System.out.println("Не получилось запустить клиент: " + e.toString());
        }

        System.out.println("Добро пожаловать! \nИ помните, что в конце каждой команды должен стоять символ \";\".\n" +
                "Чтобы получить справку по командам, введите команду help.\n");
        CommandReader reader = new CommandReader(port, receiver);
        reader.IMMA_CHARGIN_MAH_LAZER();
    }

}