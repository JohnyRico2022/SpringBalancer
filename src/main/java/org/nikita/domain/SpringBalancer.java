package org.nikita.domain;

import org.nikita.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class SpringBalancer {

    /// Основной метод с выбором стратегии
    public static BalancingResult balance(List<Spring> springs, int strategyIndex) {

        if (springs == null || springs.isEmpty())
            throw new IllegalArgumentException("Список пружин пуст");

        if (springs.size() % 24 != 0)
            throw new IllegalArgumentException("Количество пружин должно быть кратно 24 (текущее: " + springs.size() + ")");

        /// 1. Сортируем по возрастанию
        List<Spring> sorted = springs.stream()
                .sorted(Comparator.comparingInt(Spring::getForce))
                .collect(Collectors.toList());

        int totalSprings = springs.size();
        int nStanchions = totalSprings / 6; // 6 пружин на стойку
        int totalForce = springs.stream().mapToInt(Spring::getForce).sum();

        List<Stanchion> stanchions;
        String strategyName;

        /// Выбор стратегии
        switch (strategyIndex % 3) {
            case 1:
                stanchions = buildStanchionsStrategyB(sorted, nStanchions);
                strategyName = "B: Линейная";
                break;
            case 2:
                stanchions = buildStanchionsStrategyC(sorted, nStanchions);
                strategyName = "C: Групповая";
                break;
            default:
                stanchions = buildStanchionsStrategyA(sorted, nStanchions);
                strategyName = "A: Симметричная";
        }

        /// Улучшаем свопами
        improveBySwapping(stanchions);

        /// Формируем стороны с контролем ≤300 Н
        List<Side> sides = buildSidesWithConstraint(stanchions, 300);

        /// Формируем станки с контролем ≤500 Н
        List<Machine> machines = buildMachinesWithConstraint(sides, 500);

        return new BalancingResult(totalSprings, stanchions, sides, machines, strategyName);

    }

    /// === СТРАТЕГИЯ А: ===
    private static List<Stanchion> buildStanchionsStrategyA(List<Spring> sorted, int nStanchions) {
        List<List<Spring>> stanchionBins = new ArrayList<>();
        for (int i = 0; i < nStanchions; i++) {
            stanchionBins.add(new ArrayList<>());
        }

        int layers = 6;
        for (int layer = 0; layer < layers; layer++) {
            int start = layer * nStanchions;
            int end = start + nStanchions;
            if (layer == 0) {
                for (int i = 0; i < nStanchions / 2; i++) {
                    stanchionBins.get(i * 2).add(sorted.get(start + i));
                    stanchionBins.get(i * 2 + 1).add(sorted.get(end - 1 - i));
                }
            } else {
                for (int i = 0; i < nStanchions; i++) {
                    stanchionBins.get(i).add(sorted.get(start + i));
                }
            }
        }

        List<Stanchion> result = new ArrayList<>();
        for (int i = 0; i < nStanchions; i++) {
            result.add(new Stanchion(i + 1, stanchionBins.get(i)));
        }
        return result;
    }


    /// === СТРАТЕГИЯ B: Линейная (без свопа) ===
    private static List<Stanchion> buildStanchionsStrategyB(List<Spring> sorted, int nStanchions) {
        List<List<Spring>> stanchionBins = new ArrayList<>();
        for (int i = 0; i < nStanchions; i++) {
            stanchionBins.add(new ArrayList<>());
        }

        int layers = 6;
        for (int layer = 0; layer < layers; layer++) {
            int start = layer * nStanchions;
            for (int i = 0; i < nStanchions; i++) {
                stanchionBins.get(i).add(sorted.get(start + i));
            }
        }

        List<Stanchion> result = new ArrayList<>();
        for (int i = 0; i < nStanchions; i++) {
            result.add(new Stanchion(i + 1, stanchionBins.get(i)));
        }
        return result;
    }


    /// === СТРАТЕГИЯ C: Групповая (по третям) ===
    private static List<Stanchion> buildStanchionsStrategyC(List<Spring> sorted, int nStanchions) {
        int n = sorted.size();
        int third = n / 3;

        List<Spring> weak = sorted.subList(0, third);
        List<Spring> medium = sorted.subList(third, 2 * third);
        List<Spring> strong = sorted.subList(2 * third, n);

        // Перемешиваем внутри групп (детерминированно)
        Collections.shuffle(weak, new Random(42));
        Collections.shuffle(medium, new Random(42));
        Collections.shuffle(strong, new Random(42));

        List<Spring> reordered = new ArrayList<>();
        for (int i = 0; i < third; i++) {
            reordered.add(weak.get(i));
            reordered.add(medium.get(i));
            reordered.add(strong.get(i));
        }

        // Теперь применяем линейную стратегию к переупорядоченному списку
        return buildStanchionsStrategyB(reordered, nStanchions);
    }


    private static void improveBySwapping(List<Stanchion> stanchions) {
        boolean improved;
        do {
            improved = false;
            for (int i = 0; i < stanchions.size(); i++) {
                for (int j = i + 1; j < stanchions.size(); j++) {
                    Stanchion a = stanchions.get(i);
                    Stanchion b = stanchions.get(j);
                    for (Spring sa : a.springs) {
                        for (Spring sb : b.springs) {
                            int oldDev = Math.abs(a.sum - b.sum);
                            List<Spring> newA = new ArrayList<>(a.springs);
                            newA.remove(sa);
                            newA.add(sb);
                            List<Spring> newB = new ArrayList<>(b.springs);
                            newB.remove(sb);
                            newB.add(sa);
                            int newSumA = newA.stream().mapToInt(Spring::getForce).sum();
                            int newSumB = newB.stream().mapToInt(Spring::getForce).sum();
                            int newDev = Math.abs(newSumA - newSumB);
                            if (newDev < oldDev - 1) {
                                stanchions.set(i, new Stanchion(a.id, newA));
                                stanchions.set(j, new Stanchion(b.id, newB));
                                improved = true;
                                break;
                            }
                        }
                        if (improved) break;
                    }
                    if (improved) break;
                }
                if (improved) break;
            }
        } while (improved);
    }

    // === Формирование сторон с ограничением ≤ maxDiff ===
    private static List<Side> buildSidesWithConstraint(List<Stanchion> stanchions, int maxDiff) {
        List<Stanchion> available = new ArrayList<>(stanchions);
        List<Side> sides = new ArrayList<>();

        while (!available.isEmpty()) {
            Stanchion bestA = null, bestB = null;
            int minDeviation = Integer.MAX_VALUE;

            for (int i = 0; i < available.size(); i++) {
                for (int j = i + 1; j < available.size(); j++) {
                    Stanchion a = available.get(i);
                    Stanchion b = available.get(j);
                    if (Math.abs(a.sum - b.sum) <= maxDiff) {
                        int sideSum = a.sum + b.sum;
                        int targetSide = /* можно передать */ 0; // или игнорировать
                        int deviation = Math.abs(sideSum - targetSide); // или просто 0
                        if (deviation < minDeviation) {
                            minDeviation = deviation;
                            bestA = a;
                            bestB = b;
                        }
                    }
                }
            }

            if (bestA == null) {
                // Аварийный режим: берём первую пару
                bestA = available.remove(0);
                bestB = available.remove(0);
            } else {
                available.remove(bestA);
                available.remove(bestB);
            }
            sides.add(new Side(bestA, bestB));
        }
        return sides;
    }

    // === Формирование станков с ограничением ≤ maxDiff ===
    private static List<Machine> buildMachinesWithConstraint(List<Side> sides, int maxDiff) {
        List<Side> available = new ArrayList<>(sides);
        List<Machine> machines = new ArrayList<>();

        while (!available.isEmpty()) {
            Side bestL = null, bestR = null;
            int minDeviation = Integer.MAX_VALUE;

            for (int i = 0; i < available.size(); i++) {
                for (int j = i + 1; j < available.size(); j++) {
                    Side l = available.get(i);
                    Side r = available.get(j);
                    if (Math.abs(l.sum - r.sum) <= maxDiff) {
                        int machineSum = l.sum + r.sum;
                        int targetMachine = /* можно передать */ 0;
                        int deviation = Math.abs(machineSum - targetMachine);
                        if (deviation < minDeviation) {
                            minDeviation = deviation;
                            bestL = l;
                            bestR = r;
                        }
                    }
                }
            }

            if (bestL == null) {
                bestL = available.remove(0);
                bestR = available.remove(0);
            } else {
                available.remove(bestL);
                available.remove(bestR);
            }
            machines.add(new Machine(bestL, bestR));
        }
        return machines;
    }
}
