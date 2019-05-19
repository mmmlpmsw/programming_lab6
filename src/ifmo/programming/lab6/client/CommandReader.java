package ifmo.programming.lab6.client;


import ifmo.programming.lab6.transmitter.ReceiverListener;
import ifmo.programming.lab6.Message;
import ifmo.programming.lab6.Utils.StringEntity;
import ifmo.programming.lab6.json.JSONParseException;
import ifmo.programming.lab6.server.FileLoader;
import ifmo.programming.lab6.transmitter.Receiver;
import ifmo.programming.lab6.transmitter.Sender;
import ifmo.programming.lab6.transmitter.SenderAdapter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.file.AccessDeniedException;

public class CommandReader {

    private static final int SENDING_PORT = 2227;
    private int port;
    private Receiver receiver;

    CommandReader (int port, Receiver receiver) {
        this.port = port;
        this.receiver = receiver;
    }

    // TODO: Нормально назвать функцию
    void IMMA_CHARGIN_MAH_LAZER() {
        System.out.print("Введите команду >>> ");
        String command;
        try {
            while ((command = getNextCommand()) != null) {
                command = command.replace("\\s{2,}", " ").trim();
                if (command.isEmpty()) {
                    System.out.println("Введите команду.");
                }

                int spaceIndex = command.indexOf(" ");
                if (spaceIndex == -1) {
                    processCommand(command, null);
                } else {
                    String name = command.substring(0, spaceIndex);
                    String arg = command.substring(spaceIndex + 1);
                    processCommand(name, arg);
                }

                System.out.print("Введите команду >>> ");
            }
            //processCommand("autosave", null);
            System.exit(0); //todo try to fix
        }catch (IOException e) {
            System.out.println("Ошибка ввода.");
        }

    }

    private void processCommand(String name, String arg) {

        switch (name){

            case "exit":
                System.exit(0);
            case "import":
                doImport(name, arg);
                return;

            case "save":
            case "load":
                doWithFilenameArgument(name, arg);
                return;

            case "add":
            case "remove":
            case "remove_greater":
                doWithRoomArgument(name, arg);
                return;

            case "remove_first":
            case "help":
            case "info":
            case "show":
            default:
                doWithRoomArgument(name, null);
        }


    }

    /**
     * формирует команду import и передает ее на сериализацию
     * @param name имя команды
     * @param filename имя файла, из которого достается содержимое
     */
    private void doImport(String name, String filename){
        try {
            String content = FileLoader.getFileContent(filename);
            Message message = new Message(name, new StringEntity().set(content));
            send(message);
        }
        catch (AccessDeniedException e) { System.out.println("Нет доступа к файлу"); }
        catch (FileNotFoundException e) { System.out.println("Ошибка: файл не найден."); }
        catch (IOException e) { System.out.println("Ошибка ввода-вывода: " + e.getMessage()); }
        catch (Exception e) { System.out.println(e.getMessage()); }

    }

    /**
     * Выполняет команду, аргумент которой
     * является json-представлением экземпляра класса Room
     * @param name имя команжы
     * @param arg аргумент команды
     */
    private void doWithRoomArgument(String name, String arg) {
        /*try {*/
        Message message = new Message(name, null);
        if (arg != null)
            try {
                message.setAttachment(RoomFactory.makeRoomFromJSON(arg));
            } catch (JSONParseException | IllegalArgumentException e) {
                System.out.println( e.getMessage());}
        send(message);
        /*} catch (IOException e) {e.getMessage();}*/
    }

    /**
     * Выполняет команду, аргументом которой является название файла
     * @param name имя команды
     * @param filename аргумент команды - название файла
     */
    private void doWithFilenameArgument(String name, String filename) {
        /* try {*/
        Message message = new Message(name, new StringEntity().set(filename));
        send(message);
        /* } catch (IOException e) { e.getMessage(); }*/
    }

    private void send(Message message){
        message.setSourcePort(port);
        try {
            Sender.send(message.serialize(), InetAddress.getByName("localhost"), SENDING_PORT, true, new SenderAdapter() {
                @Override
                public void onSuccess() {
                    System.out.println("Команда отправилась, жду ответ...");
                    waitForServerResponse();
                }

                @Override
                public void onError(String message) {
                    System.out.println("Не получилось отправить запрос: " + message);
                    IMMA_CHARGIN_MAH_LAZER();
                }
            });
        } catch (IOException e) {
            System.out.println("Не получилось сформировать запрос: " + e.getMessage());
        }

    }

    private void waitForServerResponse() {
        Thread outer = Thread.currentThread();
        receiver.setListener(new ReceiverListener() {
            @Override
            public void received(int requestID, byte[] data, InetAddress address) {
                try {
                    Message message = Message.deserialize(data);
                    System.out.println("Вот что ответил сервер: " + message.getText());
                    try {
                        Thread.sleep(30);
                        outer.interrupt();
                    } catch (InterruptedException ignored) {
                    }
                } catch (IOException | ClassNotFoundException e) {}
            }

            @Override
            public void exceptionThrown(Exception e) {
                e.printStackTrace();
                System.out.println("Не удалось получить ответ сервера: " + e.toString());
                outer.interrupt();
            }
        });

        try {
            Thread.sleep(3000);
            System.out.println("Сервер ничего не ответил");
        } catch (InterruptedException ignored) {
        } finally {
            IMMA_CHARGIN_MAH_LAZER();
        }
    }


    /**
     * читает команду со стандартного потока ввода
     * @return преобразованная в строку многострочная команда
     * @throws IOException когда что-то идёт не так
     */
    private static String getNextCommand() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder builder = new StringBuilder();
        char running;
        boolean inString = false;
        do {
            int current = reader.read();
            if (current == -1) {
                return null;
            }
            running = (char)current;

            if (running != ';' || inString) {  builder.append(running); }
            if (running == '"') { inString = !inString;}
        } while (running != ';' || inString);
        return builder.toString();
    }
}
