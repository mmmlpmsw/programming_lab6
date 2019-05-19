package ifmo.programming.lab6.server;

import ifmo.programming.lab6.Utilities;
import ifmo.programming.lab6.client.Room;

import java.util.Date;
import java.util.concurrent.LinkedBlockingDeque;


/**
 * Хрущёвка
 */
public class Building {
    private LinkedBlockingDeque<Room> collection = new LinkedBlockingDeque <>();
    private Date creationDate = new Date();
    private boolean hasChanged = false;

    /**
     * Добавляет команту в здание
     * @param room комната, которую нужно добавить
     */
    public void add(Room room) {
        collection.push(room);
        hasChanged = true;
    }


    /**
     * Удаляет комнату из здания
     * @param room комната, которую нужно удалить
     * @return true, если комната удалена
     */
    public boolean remove(Room room) {
        if (!(collection.contains(room)))
            return false;
        else {
            collection.remove(room);
            hasChanged = true;
        }
        return true;

    }

    /**
     * Удаляет каждую комнату, превыщающую указанную
     * @param room Комната, с которой происходит сравнение
     * @return Количество удалённых комнат
     */
    public int removeGreater(Room room) {
        int sizeBefore = collection.size();
        collection.stream().filter(current -> current.compareTo(room) > 0).forEach(current -> collection.remove(current));
        int removed = sizeBefore - collection.size();
        if (removed > 0)
            hasChanged = true;
        return removed;
    }

    /**
     * Удаляет первый элемент
     * @return удалённый элемент (вернет null, если коллекция пуста)
     */
    public Room removeFirst() {
        /*Stack*/LinkedBlockingDeque<Room> snew = new /*Stack*/LinkedBlockingDeque<>();
        while(collection.size() > 1)
            snew.push(collection.pop());

        if (collection.size() == 1) {
            Room removed = collection.pop();
            collection.clear();
            while (snew.size() > 0)
                collection.push(snew.pop());
            hasChanged = true;
            return removed;
        }
        return null;
    }

    /**
     * геттер для получения коллекции
     * @return LinkedBlockingDeque<Room> - коллекция комнат
     */
    public LinkedBlockingDeque<Room> getCollection() {
        return collection;
    }

    /**
     * @return -  String - читабельное строковое представление об элементах коллекции
     */
    public String getStringCollection(){
        StringBuilder builder = new StringBuilder("");
        /*for (Room room : collection) {
            builder.append(room.toString());
            builder.append("\n");
        }*/
        collection.forEach(room -> builder.append(room.toString() + "\n"));
        return builder.toString();
    }


    public void setCollection(LinkedBlockingDeque<Room> collection) {
        this.collection = collection;
        hasChanged = true;
    }

    /**
     * @return int - размер коллекции
     */
    public long getSize() {
//        return collection.size();
        return collection.stream().count();
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date createdDate) {
        this.creationDate = createdDate;
        hasChanged = true;
    }

    /**
     * @return true, если содержимое коллекции было изменено
     */
    public boolean isChanged() { return hasChanged; }

    /**
     * Устанавливает состояние изменённости коллекции. Если передатть true, коллекция
     * будет обозначена как изменённая. Рекомендуется вызывать функцию с переданным false
     * после сохранения состояния коллекции в файл
     * @param hasChanged Если true, коллекция изменена
     */
    public void setChange(boolean hasChanged) { this.hasChanged = hasChanged; }

    /**
     *
     * @return читабельное строковое представление коллекции
     */
    public String getCollectionInfo() {
        long size = collection.stream().count();
        return  "Коллекция типа " + collection.getClass().getName() + ",\n" +
                "дата создания: " + creationDate + ",\n" +
                "содержит " + /*collection.size()*/ size + " элементов";
    }

    /**
     * справка по командам, реализуемым приложением
     * @return справка по командам, реализуемым приложением
     */
    public String getHelp() {
        return  Utilities.colorize("[[RED]]Оу, похоже, вам нужна помощь?" +
                "\nПриложение поддерживает выполнение следующих команд:[[RED]]" +
                "[[YELLOW]]\n\t• add {element}: добавить новый элемент в коллекцию; пример комнаты, которую можно добавить:[[YELLOW]]" +
                "[[BRIGHT_YELLOW]]{\"x\": 10, \"y\": 12, \"width\": 5, \"name\": \"хрущевка\", \"height\": 10," +
                " \n\t\"shelf\": [ { \"size\": 1, \"name\": \"flower\" }, { \"size\": 1, \"name\": \"hat\" } ] } [[BRIGHT_YELLOW]]" +
                "[[BRIGHT_GREEN]]\n\t• remove_first: удалить первый элемент из коллекции;" +
                "\n\t• remove_greater {element}: удалить из коллекции все элементы, превышающие заданный;[[BRIGHT_GREEN]]" +
                "[[CYAN]]\n\t• show: вывести в стандартный поток вывода все элементы коллекции в строковом представлении;" +
                "\n\t• info: вывести в стандартный поток вывода информацию о коллекции;[[CYAN]]" +
                "[[BLUE]]\n\t• load: перечитать коллекцию из файла;" +
                "\n\t• remove {element}: удалить элемент из коллекции по его значению;[[BLUE]]" +
                "[[PURPLE]]\n\t• import: добавить данные из файла клиента в коллекцию;" +
                "\n\t• save: сохранить состояние коллекции в файл сервера;[[PURPLE]]" +
                "[[RESET]]\n\t• help: вызов справки.[[RESET]]");

    }

    /**
     * @deprecated пережитки пятой лабы, дорогие создателю как память
     * Перечитывает коллекцию из файла
     * @return Количество загруженных элементов
     * @throws Exception когда что-то идёт не так
     */
    public int load (String filename) throws Exception {
       /* Map<String, String> env = System.getenv();
        String filename = env.get(FILENAME_ENV);*/
        int initialSize = collection.size();

        if (filename != null && !filename.isEmpty()) {
            String content = FileLoader.getFileContent(filename);
//            collection.clear(); пофикшено во времена далекой пятой лабы
            BuildingChecker.getCollectionFromJSON(this, content);
        }
        return collection.size() - initialSize;
    }
}
