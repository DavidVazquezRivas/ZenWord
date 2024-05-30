package com.example.zenword;

public class Word implements Comparable<Word>{
    private final String full;
    private final String simple;

    public Word(String full, String simple) {
        this.full = full;
        this.simple = simple;
    }

    @Override
    public int compareTo(Word o) {
        return this.simple.compareTo(o.simple);
    }

    public int getLength() {
        return simple.length();
    }

    public String getFull() {
        return full;
    }

    public String getSimple() {
        return simple;
    }
}
