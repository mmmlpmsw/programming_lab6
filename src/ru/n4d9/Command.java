package ru.n4d9;

public class Command {
    private String name;
    private String argument;

    public Command(String command) {
        int spaceIndex = command.indexOf(' ');
        if (spaceIndex == -1)
            name = command;
        else {
            name = command.substring(0, spaceIndex);
            argument = command.substring(spaceIndex + 1);
        }
    }

    public String getName() {
        return name;
    }

    public String getArgument() {
        return argument;
    }
}
