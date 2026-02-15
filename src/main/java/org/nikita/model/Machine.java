package org.nikita.model;

public class Machine {

    public final Side leftSide, rightSide;
    public final int sum;

    public Machine(Side leftSide, Side rightSide) {
        this.leftSide = leftSide;
        this.rightSide = rightSide;
        this.sum = leftSide.sum + rightSide.sum;
    }
}