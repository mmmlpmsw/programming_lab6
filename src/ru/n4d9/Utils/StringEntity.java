package ru.n4d9.Utils;

import java.io.Serializable;

/**
 * класс для сериализации части сообщения
 */
public class StringEntity implements Serializable {
    private String string;

    public String getString() { return string; }

    public void setString(String string) { this.string = string; }

    public StringEntity set(String string) {setString(string); return this;}
}