package org.nikita.model;

public class Side {

    public final Stanchion left, right;
    public final int sum;

    public Side(Stanchion left, Stanchion right) {
        this.left = left;
        this.right = right;
        this.sum = left.sum + right.sum;
    }
}

