package org.nikita.model;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class Statistics {

    public final int totalSprings, totalForce;
    public final int targetStanchion, targetSide, targetMachine;
    public final int stanchionMin, stanchionMax, stanchionAvg, stanchionStdDevRounded;
    public final int sideMin, sideMax, sideAvg, sideStdDevRounded;
    public final int machineMin, machineMax, machineAvg, machineStdDevRounded;


    public Statistics(int totalSprings, List<Stanchion> stanchions, List<Side> sides, List<Machine> machines) {
        this.totalSprings = totalSprings;
        this.totalForce = stanchions.stream().mapToInt(s -> s.sum).sum();

        int nSt = stanchions.size();
        int nSi = sides.size();
        int nMa = machines.size();

        targetStanchion = totalForce / nSt;
        targetSide = totalForce / nSi;
        targetMachine = totalForce / nMa;

        stanchionMin = stanchions.stream().mapToInt(s -> s.sum).min().orElse(0);
        stanchionMax = stanchions.stream().mapToInt(s -> s.sum).max().orElse(0);
        stanchionAvg = totalForce / nSt;
        stanchionStdDevRounded = (int) Math.round(calcStdDev(stanchions.stream().mapToDouble(s -> s.sum)));

        sideMin = sides.stream().mapToInt(s -> s.sum).min().orElse(0);
        sideMax = sides.stream().mapToInt(s -> s.sum).max().orElse(0);
        sideAvg = sides.stream().mapToInt(s -> s.sum).sum() / nSi;
        sideStdDevRounded = (int) Math.round(calcStdDev(sides.stream().mapToDouble(s -> s.sum)));

        machineMin = machines.stream().mapToInt(m -> m.sum).min().orElse(0);
        machineMax = machines.stream().mapToInt(m -> m.sum).max().orElse(0);
        machineAvg = machines.stream().mapToInt(m -> m.sum).sum() / nMa;
        machineStdDevRounded = (int) Math.round(calcStdDev(machines.stream().mapToDouble(m -> m.sum)));
    }

    private static double calcStdDev(DoubleStream values) {
        List<Double> list = values.boxed().collect(Collectors.toList());
        if (list.size() < 2) return 0.0;
        double avg = list.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        return Math.sqrt(list.stream().mapToDouble(x -> Math.pow(x - avg, 2)).sum() / (list.size() - 1));
    }

    @Override
    public String toString() {
        return "Statistics{" +
                "machineAvg=" + machineAvg +
                ", totalSprings=" + totalSprings +
                ", totalForce=" + totalForce +
                ", targetStanchion=" + targetStanchion +
                ", targetSide=" + targetSide +
                ", targetMachine=" + targetMachine +
                ", stanchionMin=" + stanchionMin +
                ", stanchionMax=" + stanchionMax +
                ", stanchionAvg=" + stanchionAvg +
                ", stanchionStdDevRounded=" + stanchionStdDevRounded +
                ", sideMin=" + sideMin +
                ", sideMax=" + sideMax +
                ", sideAvg=" + sideAvg +
                ", sideStdDevRounded=" + sideStdDevRounded +
                ", machineMin=" + machineMin +
                ", machineMax=" + machineMax +
                ", machineStdDevRounded=" + machineStdDevRounded +
                '}';
    }
}