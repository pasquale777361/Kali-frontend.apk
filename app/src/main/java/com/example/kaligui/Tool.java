package com.example.kaligui;

public class Tool {
    private String name;
    private String description;
    private String command;
    private boolean requiresArgument;

    public Tool(String name, String description, String command, boolean requiresArgument) {
        this.name = name;
        this.description = description;
        this.command = command;
        this.requiresArgument = requiresArgument;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCommand() {
        return command;
    }

    public boolean isRequiresArgument() {
        return requiresArgument;
    }
}