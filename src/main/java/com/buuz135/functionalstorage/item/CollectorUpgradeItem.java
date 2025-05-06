package com.buuz135.functionalstorage.item;

public class CollectorUpgradeItem extends UpgradeItem {

    int range;

    public CollectorUpgradeItem(int range) {
        super(new Properties(), Type.UTILITY);
        this.range = range;
    }

    public int getRange() {
        return this.range;
    }
}
