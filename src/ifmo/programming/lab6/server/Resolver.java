package ifmo.programming.lab6.server;


import ifmo.programming.lab6.transmitter.Sender;
import ifmo.programming.lab6.transmitter.SenderAdapter;
import ifmo.programming.lab6.Message;
import ifmo.programming.lab6.Utils.StringEntity;
import ifmo.programming.lab6.client.Room;

import java.io.*;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.util.Arrays;
import java.util.Collections;

class Resolver implements Runnable{

    private static Building building;
    private Message message;
    private int requestID;
    private static InetAddress address;
    private static final String FILE_FOR_AUTOSAVE = "autosave.json";

    Resolver(Message message, int requestID, InetAddress address, Building building){
        this.address = address;
        this.message = message;
        this.requestID = requestID;
        this.building = building;
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
		        if (building.getSize() == 0) return m("Коллекция пуста.");
                return m(building.sortCollection().getStringCollection());

            case "save":

                if (!(message.getAttachment() instanceof StringEntity)) {
                    return m("Клиент отправил запрос в неверном формате : аргумент сообщения должен быть строкой.");
                }
                try {
                    BuildingChecker.saveCollection(building, new OutputStreamWriter(new FileOutputStream(((StringEntity) message.getAttachment()).getString()), StandardCharsets.UTF_8));
                    return m("Сохранение успешно. В коллекции " + building.getSize() + " комнат.");
                }catch (NullPointerException e) {
                    return m("Не могу осуществить сохранение: не указано имя файла.");
                } catch (AccessDeniedException e) {
                    System.out.println("Нет доступа к файлу");
                } catch (FileNotFoundException e) {
                    return m("Ошибка: файл не найден.");
                }catch (IOException e) {
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
                    savestate(building);
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
                    savestate(building);
                    return m("Загрузка успешна. В коллекции " + building.getSize() + " комнат.");
                } catch (Exception e) {
                }


            case "remove_first":
                synchronized (building) {


                    if (building.getSize() == 0) {
                        return m("Невозможно удалить комнату: коллекция пуста.");
                    }
                    savestate(building);
                    Building building1 = building.sortCollection();
                    Room removing = building1.removeFirst();
                    building.remove(removing);
                    return m("Удалена комната: " + removing.toString());
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
                        int rooms = building.removeGreater((Room) message.getAttachment());
                        savestate(building);
                        return m("Удалено " + rooms + " комнат.");
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
                        if (removed) {
                            savestate(building);
                            return m("Комната удалена.");
                        } else
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
                    savestate(building);
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

    private static void savestate(Building building) {
            try {
                //System.out.println("\nСохраняю...");
                BuildingChecker.saveCollection(building, new BufferedWriter(new FileWriter(FILE_FOR_AUTOSAVE)));
                //System.out.println("Сохранено");
            } catch (NullPointerException e) {
                //System.out.println("Не могу осуществить сохранение: не указано имя файла.");
            } catch (AccessDeniedException e) {
                //System.out.println("Нет доступа к файлу");
            } catch (FileNotFoundException e) {
                //System.out.println("Ошибка: файл не найден.");
            }catch (IOException e) {
                //return m("Ошибка чтения/записи.");
            }

    }
}
