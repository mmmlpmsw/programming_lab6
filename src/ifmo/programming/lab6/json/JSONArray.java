package ifmo.programming.lab6.json;

import java.util.ArrayList;
import java.util.Arrays;

public class JSONArray extends JSONEntity {
    private ArrayList<JSONEntity> items;

    {
        type = JSONType.ARRAY;
    }

    public JSONArray(JSONEntity... items) {
        this.items = new ArrayList<>(Arrays.asList(items));
    }

    public JSONArray(Object... items) {
        this.items = new ArrayList<>();
        for (Object item : items) {
            if (item instanceof JSONEntity)
                this.items.add((JSONEntity)item);
            else if (item instanceof Number)
                this.items.add(new JSONNumber((Double)item));
            else if (item instanceof String)
                this.items.add(new JSONString((String)item));
            else if (item == null)
                this.items.add(null);
            else
                this.items.add(new JSONString(item.toString()));
        }
    }

    public int size() {
        return items.size();
    }

    public JSONArray() {
        this.items = new ArrayList<>();
    }

    public JSONEntity getItem(int index) {
        if (index < 0 || index >= items.size())
            return null;
        else
            return items.get(index);
    }

    public ArrayList<JSONEntity> getItems() {
        return items;
    }

    public void removeItem(int index) {
        if (index >= 0 && index < items.size())
            items.remove(index);
    }

    public void addItem(JSONEntity entity) {
        items.add(entity);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for (int i = 0; i < items.size(); i++) {
            builder.append(items.get(i));
            if (i + 1 < items.size())
                builder.append(", ");
        }
        builder.append(']');
        return builder.toString();
    }

    @Override
    public String toFormattedString(int tabSize, int depth) {
        StringBuilder builder = new StringBuilder();
        String padding = getPaddingString(tabSize, depth);

        builder.append(padding).append("[\n");
        for (int i = 0; i < items.size(); i++) {
            builder.append(items.get(i).toFormattedString(tabSize, depth+1));
            if (i + 1 < items.size())
                builder.append(",\n");
        }
        builder.append('\n').append(padding).append(']');
        return builder.toString();
    }
}
