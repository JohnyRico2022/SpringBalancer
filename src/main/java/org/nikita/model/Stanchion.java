package org.nikita.model;

import java.util.ArrayList;
import java.util.List;

public class Stanchion {

    public final int id;
    public final List<Spring> springs;
    public final int sum;

    public Stanchion(int id, List<Spring> springs) {
        this.id = id;
        this.springs = new ArrayList<>(springs);
        this.sum = springs.stream().mapToInt(Spring::getForce).sum();
    }

    @Override
    public String toString() {
        return "Stanchion{" +
                "id=" + id +
                ", springs=" + springs +
                ", sum=" + sum +
                '}';
    }
}