package org.nikita.model;

import java.util.ArrayList;
import java.util.List;

public class BalancingResult {

    public final Statistics stats;
    public final List<Stanchion> stanchions;
    public final List<Side> sides;
    public final List<Machine> machines;
    public final String strategyName;

    public BalancingResult(int totalSprings, List<Stanchion> stanchions, List<Side> sides, List<Machine> machines, String strategyName) {
        this.stats = new Statistics(totalSprings, stanchions, sides, machines);
        this.stanchions = new ArrayList<>(stanchions);  // защита от изменений
        this.sides = new ArrayList<>(sides);
        this.machines = new ArrayList<>(machines);
        this.strategyName = strategyName;
    }
}