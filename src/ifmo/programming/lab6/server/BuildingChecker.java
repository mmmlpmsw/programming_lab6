package ifmo.programming.lab6.server;


import ifmo.programming.lab6.client.Room;
import ifmo.programming.lab6.json.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;

class BuildingChecker {

    /**
     * Загружает состояние тюряги из указанной строки. Строка должна содержать состояние в формате XML.
     * Обратите внимание, что имеющиеся существа не удаляются из тюряги.
     * @param building постройка, в которую надо загрузить состояние
     * @param jsonString строка, содержащая состояние в формате json
     * @throws IOException если произойдёт ошиtка ввода-вывода
     */
    static void getCollectionFromJSON(Building building, String jsonString) throws Exception {
        JSONEntity entity = JSONParser.parse(jsonString);
        JSONObject object = entity.toObject(new IllegalArgumentException("Вместо ожидаемого объекта получен элемент типа " + entity.getType().toString().toLowerCase()));
        JSONEntity createdEntity = object.getItem("created");

        if (createdEntity != null) {
            building.setCreationDate(new Date((long) (createdEntity.toNumber(new IllegalArgumentException("Вместо ожидаемого числа имеет тип " +
                    createdEntity.getType().toString().toLowerCase())).getValue())));
        }
        else {building.setCreationDate(new Date());}

        JSONEntity collectionEntity = object.getItem("collection");
        if (collectionEntity != null) {
            JSONArray collectionArray = collectionEntity.toArray(new IllegalArgumentException("Вместо ожидаемого массива имеет тип " + collectionEntity.getType().toString().toLowerCase()));
//            building.getCollection().clear();

            for (JSONEntity room : collectionArray.getItems()) {

                JSONObject roomObject = room.toObject(new IllegalArgumentException("Элементы collection должны быть объектами"));
                int width = roomObject.getItem("width").toInt(new IllegalArgumentException("Поле width элементов коллекции должно быть числом") ),
                        height = roomObject.getItem("height").toInt(new IllegalArgumentException("Поле height элементов коллекции должно быть числом, но это") );
//                        length = roomObject.getItem("length").toInt(new IllegalArgumentException("Поле length элементов коллекции collection должно быть числом") );

                int x = roomObject.getItem("x").toInt(new IllegalArgumentException("Координата x элементов коллекции должна быть числом") );
                int y = roomObject.getItem("y").toInt(new IllegalArgumentException("Координата y элементов коллекции должна быть числом") );

                String roomName = "";
                JSONEntity roomNameEntity  = roomObject.getItem("name");
                if (roomNameEntity != null) {
                    roomName = roomNameEntity.toString(new IllegalArgumentException("Поле name элементов массива collection должно быть строкой")).getContent();
                }

                Room.Thing[] things = new Room.Thing[0];

                JSONEntity JSONshelf = roomObject.getItem("shelf");

                if (JSONshelf != null) {
                    JSONArray shelfArray =  JSONshelf.toArray(
                            new IllegalArgumentException("Поле shelf элементов массива collection должно быть массивом"));
                    things = new Room.Thing[shelfArray.getItems().size()];

                    ArrayList<JSONEntity> items = shelfArray.getItems();
                    for (int i = 0; i < items.size(); i++) {
                        JSONObject thingObject = items.get(i).toObject(
                                new IllegalArgumentException("Элементы поля shelf должны быть объектами"));

                        int size;
                        String name = "";

                        JSONEntity nameEntity = thingObject.getItem("name");

                        if (nameEntity != null)
                            name = nameEntity.toString(
                                    new IllegalArgumentException("Поле name элементов поля shelf должно быть строкой")
                            ).getContent();

                        JSONEntity sizeEntity = thingObject.getItem("size");

                        if (sizeEntity == null)
                            throw new IllegalArgumentException("У элементов поля shelf должно быть поле size");

                        size = sizeEntity.toInt(
                                new IllegalArgumentException("Элементы size элементов поля shelf должны быть числами")
                        );

                        things[i] = new Room.Thing(size, name);
                    }

                }

                building.add(new Room(width, height, x, y, roomName, things));
            }

        } else { building.getCollection().clear(); }
        building.setChange(false);
    }

    /**
     * Сохраняет состояние постройки в поток в формате XML
     * @param building постройка, состояние которой надо сохранить
     * @param writer поток, в который будет записано состояние в формате XML
     * @throws IOException если произойдёт ошибка чтения-записи
     */
    static void saveCollection(Building building, OutputStreamWriter writer) throws IOException {
        JSONObject result = new JSONObject();
        result.putItem("created", building.getCreationDate().getTime());
        result.putItem("created", building.getCreationDate().getTime());
        JSONArray collection = new JSONArray();

        for (Room room : building.getCollection()) {
            JSONObject roomObject = new JSONObject();
            //     if (!room.getWallcolor().isEmpty()) { roomObject.putItem("wallcolor", new JSONString(room.getWallcolor())); }

            roomObject.putItem("width", room.getWidth());
            roomObject.putItem("height", room.getHeight());

            roomObject.putItem("x", room.getX());
            roomObject.putItem("y", room.getY());

            roomObject.putItem("name", new JSONString(room.getName()));
            //     roomObject.putItem("length", room.getLength());

            JSONArray array = new JSONArray();
            for (Room.Thing thing : room.getShelf()) {
                JSONObject object = new JSONObject();
                object.putItem("size", thing.getSize());
                object.putItem("name", new JSONString(thing.getName()));
                array.addItem(object);
            }
            roomObject.putItem("shelf", array);

            collection.addItem(roomObject);
        }

        result.putItem("collection", collection);
        writer.write(result.toFormattedString());
        writer.flush();
        building.setChange(false);
    }
}
