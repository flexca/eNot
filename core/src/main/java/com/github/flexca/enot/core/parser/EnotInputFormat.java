package com.github.flexca.enot.core.parser;

public enum EnotInputFormat {

    JSON("json"), YAML("yaml"), UNSUPPORTED("unsupported");

    private final String name;

    private EnotInputFormat(String name) {
        this.name = name;
    }
}
