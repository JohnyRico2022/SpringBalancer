package org.nikita.util;

import org.nikita.model.Spring;

import java.util.ArrayList;

public class Utils {

    public static boolean validateAndShowDataTable(ArrayList<Spring> springs) {
        int count = springs.size();
        return count % 24 == 0;
    }

    /**
     * Склоняет слово "станок" по числу.
     * Примеры:
     * 1 → "1 станок"
     * 2 → "2 станка"
     * 5 → "5 станков"
     * 21 → "21 станок"
     * 22 → "22 станка"
     * 25 → "25 станков"
     */
    public static String pluralizeMachines(int count) {
        if (count % 10 == 1 && count % 100 != 11) {
            return " станок";
        } else if (count % 10 >= 2 && count % 10 <= 4 && (count % 100 < 10 || count % 100 >= 20)) {
            return " станка";
        } else {
            return " станков";
        }
    }
}