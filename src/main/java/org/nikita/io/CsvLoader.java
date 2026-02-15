package org.nikita.io;

import org.nikita.model.Spring;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class CsvLoader {

    public static ArrayList<Spring> loadCsv(String file) throws IOException {
        ArrayList<Spring> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) continue;

                /// Поддерживает: "1,6652", "1;6652", "1 6652", "1\t6652"
                String[] parts = line.split("[,;\\s]+");
                if (parts.length < 2) {
                    throw new IOException("Некорректная строка " + lineNumber + ": \"" + line + "\"");
                }
                try {
                    int id = Integer.parseInt(parts[0].trim());
                    int force = Integer.parseInt(parts[1].trim());
                    System.out.println(force);
                    list.add(new Spring(id, force));
                } catch (NumberFormatException ex) {
                    throw new IOException("Неверный формат числа в строке " + lineNumber + ": \"" + line + "\"", ex);
                }
            }
        }
        if (list.isEmpty()) {
            throw new IOException("Файл пуст или не содержит данных");
        }
        return list;
    }
}