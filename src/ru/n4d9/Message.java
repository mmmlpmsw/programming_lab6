package ru.n4d9;

import java.io.*;

/**
 * Инкапсулирует сообщение, содержащее текст и объект для прикрепления.
 * Сообщение можно сериализовать, объект тоже должен быть сериализуемым.
 */
public class Message implements Serializable {
    private String text;
    private Serializable attachment;
    private int sourcePort;

    /**
     * Создаёт сообщение с указанным текстом без вложенного объекта
     * @param text текст сообщения
     */
    public Message(String text) {
        this.text = text;
    }

    /**
     * Создаёт сообщение с указанным текстом и вложенным объектом
     * @param text текст сообщения
     * @param attachment вложенный объект
     */
    public Message(String text, Serializable attachment) {
        this.text = text;
        this.attachment = attachment;
    }

    /**
     * @return текст сообщения
     */
    public String getText() {
        return text;
    }

    /**
     * Устанавливает текст сообщения
     * @param text текст сообщения
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return вложенный объект
     */
    public Serializable getAttachment() {
        return attachment;
    }

    /**
     * Вкладывает объект в сообщение
     * @param attachment объект, который надо вложить
     */
    public void setAttachment(Serializable attachment) {
        this.attachment = attachment;
    }

    /**
     * @return true, если сообщение содержит вложенный объект
     */
    public boolean hasAttachment() {
        return attachment != null;
    }

    public Message set(String message) {setText(message); return this;}

    public int getSourcePort() {
        return sourcePort;
    }

    public void setSourcePort(int sourcePort) {
        this.sourcePort = sourcePort;
    }

    /**
     * Сериализует это сообщение
     * @return сообщение в сериализованном виде
     */
    public byte[] serialize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(this);
        return baos.toByteArray();
    }

    public static Message deserialize(byte[] content) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(content);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return (Message) ois.readObject();
    }

}
