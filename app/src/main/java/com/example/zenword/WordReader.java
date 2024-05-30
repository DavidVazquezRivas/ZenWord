package com.example.zenword;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WordReader {
    private final BufferedReader br;

    public WordReader(InputStream is) {
        br = new BufferedReader(new InputStreamReader(is));
    }

    public Word read() throws IOException {
        String line = br.readLine();
        if (line == null) return null;

        String[] words= line.split(";");

        if (words.length != 2) {
            throw new IOException("Word file format error: " + line);
        }

        return new Word(words[0], words[1]);
    }

    public void close() throws IOException {
        br.close();
    }
}
