package com.example;

import java.io.Serializable;
import java.util.Objects;

public class ConversionUnit implements Serializable {
    private final String id;
    private final String name;
    private final String symbol;
    private final UnitCategory category;
    private final double factorToBase;

    public ConversionUnit(String id, String name, String symbol, UnitCategory category, double factorToBase) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        this.category = category;
        this.factorToBase = factorToBase;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public UnitCategory getCategory() {
        return category;
    }

    public double getFactorToBase() {
        return factorToBase;
    }

    public String getDisplayName() {
        return name + " (" + symbol + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConversionUnit that = (ConversionUnit) o;
        return Objects.equals(id, that.id) && category == that.category;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, category);
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
