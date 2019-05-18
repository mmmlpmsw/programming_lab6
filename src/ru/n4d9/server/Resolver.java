package ru.n4d9.server;

import ru.n4d9.Message;
import ru.n4d9.Utils.StringEntity;
import ru.n4d9.client.Room;
import ru.n4d9.transmitter.Sender;
import ru.n4d9.transmitter.SenderAdapter;

import java.io.*;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;

class Resolver implements Runnable{

    static Building building = new Building();
    Message message;
    int requestID;
    static InetAddress address;

    Resolver(Message message, int requestID, InetAddress address){
        this.address = address;
        this.message = message;
        this.requestID = requestID;
    }

    @Override
    public void run() {
        try {
            Message response = Resolver.resolve(message, requestID);
            respond(response.serialize(), message.getSourcePort(), address);
        } catch (IOException e) {}
    }
    static Message resolve(Message message, int requestID) {

        switch (message.getText()) {
            case "help":
                return m(building.getHelp());

            case "info":
                return m(building.getCollectionInfo());

            case "show":
                return m(building.getStringCollection());

            case "save":
                if (!(message.getAttachment() instanceof StringEntity)) {
                    return m("Клиент отправил запрос в неверном формате : аргумент сообщения должен быть строкой.");
                }
                try {
                    BuildingChecker.saveCollection(building, new OutputStreamWriter(new FileOutputStream(((StringEntity) message.getAttachment()).getString()), StandardCharsets.UTF_8));
                    return m("Сохранение успешно. В коллекции " + building.getSize() + " комнат.");
                } catch (AccessDeniedException e) {
                    System.out.println("Нет доступа к файлу");
                } catch (IOException e) {
                    return m("Ошибка чтения/записи.");
                }

            case "load":
                if (!message.hasAttachment()) {
                    return m("Имя не указано.\n" +
                            "Введите help для получения справки.");
                }
                try {
                    if (!(message.getAttachment() instanceof StringEntity)) {
                       return m("Клиент отправил запрос в неверном формате : аргумент сообщения должен быть строкой.");
                    }
                    BuildingChecker.getCollectionFromJSON(building, FileLoader.getFileContent(((StringEntity) message.getAttachment()).getString()));
                    return m("Загрузка успешна. В коллекции " + building.getSize() + " комнат.");
                } catch (AccessDeniedException e) {
                    return m("Ошибка: нет доступа для чтения.");
                } catch (FileNotFoundException e) {
                   return m("Ошибка: файл не найден.");
                } catch (IOException e) {
                    return m("Ошибка чтения/записи.");
                } catch (Exception ignored) {
                }

            case "import":
                if (!message.hasAttachment()) {
                    return m("Имя не указано.\n" +
                            "Введите help для получения справки.");
                }
                try {
                    if (!(message.getAttachment() instanceof StringEntity)) {
                        return m("Клиент отправил запрос в неверном формате : аргумент сообщения должен быть строкой.");
                    }
                    synchronized (building) {
                        BuildingChecker.getCollectionFromJSON(building, ((StringEntity) message.getAttachment()).getString());
                    }
                    return m("Загрузка успешна. В коллекции " + building.getSize() + " комнат.");
                } catch (Exception e) {
                }


            case "remove_first":
                synchronized (building) {
                    if (building.getSize() == 0) {
                        return m("Невозможно удалить комнату: коллекция пуста.");
                    }
                    return m("Удалена комната: " + building.removeFirst().toString());
                }

            case "remove_greater":
                try {
                    if (!message.hasAttachment()) {
                        return m("Удаление невозможно из-за отсутствия аргумента. Введите help для получения справки.");
                    }
                    if (!(message.getAttachment() instanceof Room)) {
                       return m("Клиент отправил данные в неверном формате : аргумент должен быть сериализованным объектом.");
                    }
                    synchronized (building) {
                        return m("Удалено " + building.removeGreater((Room) message.getAttachment()) + " комнат.");
                    }
                } catch (Exception e) {
                    return m(e.getMessage());
                }
            case "remove":
                try {
                    if (!message.hasAttachment()) {
                        return m("Нельзя добавить объект в коллекцию: клиент отправил данные в неверном формате. " +
                                "Введите help для получения справки.");
                    }
                    if (!(message.getAttachment() instanceof Room)) {
                        return m("Клиент отправил данные в неверном формате (аргумент должен быть сериализованным объектом)");
                    }
                    Room room = (Room) message.getAttachment();
                    synchronized (building) {
                        boolean removed = building.remove(room);
                        if (removed)
                            return m("Комната удалена.");
                        else
                            return m("Ошибка: такой комнаты нет в коллекции.");
                    }
                } catch (Exception e) {
                    return m(e.getMessage());
                }

            case "add":
                try {
                    if (!message.hasAttachment()) {
                        return m("Нельзя добавить объект в коллекцию: клиент отправил данные в неверном формате. " +
                                "Введите help для получения справки.");
                    }
                    if (!(message.getAttachment() instanceof Room)) {
                        return m("Клиент отправил данные в неверном формате : аргумент должен быть сериализованным объектом.");
                    }
                    Room room = (Room) message.getAttachment();
                    synchronized (building) {
                        building.add((Room) message.getAttachment());
                    }
                    return m("Комната " + room.getName() + " добавлена в коллекцию");
                } catch (Exception e) {
                    return m("Не получилось создать комнату: " + e.getMessage());
                }

            case "requestid":
                return m("Ваш request id: " + requestID);

                default:
                    return m("Не знаю такой команды");
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

    /**
     * Генерирует {@link Message} из текста и аргумента.
     * Использует короткое имя, чтобы вызовы не делали код большим
     *
     * @param text текст сообщения
     *
     * @param argument аргумент
     *
     * @return сообщение
     */
    private static Message m(String text, Serializable argument) {
        return new Message(text, argument);
    }

    private static Message m(String text) {
        return m(text, null);
    }


}
