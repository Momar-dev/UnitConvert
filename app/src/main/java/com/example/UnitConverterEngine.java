package com.example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UnitConverterEngine {

    // Absolute zero thresholds
    public static final double ABSOLUTE_ZERO_CELSIUS = -273.15;
    public static final double ABSOLUTE_ZERO_FAHRENHEIT = -459.67;
    public static final double ABSOLUTE_ZERO_KELVIN = 0.0;

    private static final Map<UnitCategory, List<ConversionUnit>> UNITS_MAP = new HashMap<>();

    static {
        // LENGTH (Base: Meter)
        List<ConversionUnit> lengthUnits = new ArrayList<>();
        lengthUnits.add(new ConversionUnit("m", "Meter", "m", UnitCategory.LENGTH, 1.0));
        lengthUnits.add(new ConversionUnit("km", "Kilometer", "km", UnitCategory.LENGTH, 1000.0));
        lengthUnits.add(new ConversionUnit("cm", "Centimeter", "cm", UnitCategory.LENGTH, 0.01));
        lengthUnits.add(new ConversionUnit("mm", "Millimeter", "mm", UnitCategory.LENGTH, 0.001));
        lengthUnits.add(new ConversionUnit("mi", "Mile", "mi", UnitCategory.LENGTH, 1609.344));
        lengthUnits.add(new ConversionUnit("ft", "Foot", "ft", UnitCategory.LENGTH, 0.3048));
        lengthUnits.add(new ConversionUnit("in", "Inch", "in", UnitCategory.LENGTH, 0.0254));
        UNITS_MAP.put(UnitCategory.LENGTH, Collections.unmodifiableList(lengthUnits));

        // WEIGHT (Base: Kilogram)
        List<ConversionUnit> weightUnits = new ArrayList<>();
        weightUnits.add(new ConversionUnit("kg", "Kilogram", "kg", UnitCategory.WEIGHT, 1.0));
        weightUnits.add(new ConversionUnit("g", "Gram", "g", UnitCategory.WEIGHT, 0.001));
        weightUnits.add(new ConversionUnit("mg", "Milligram", "mg", UnitCategory.WEIGHT, 0.000001));
        weightUnits.add(new ConversionUnit("lb", "Pound", "lb", UnitCategory.WEIGHT, 0.45359237));
        weightUnits.add(new ConversionUnit("oz", "Ounce", "oz", UnitCategory.WEIGHT, 0.028349523125));
        UNITS_MAP.put(UnitCategory.WEIGHT, Collections.unmodifiableList(weightUnits));

        // TEMPERATURE
        List<ConversionUnit> tempUnits = new ArrayList<>();
        tempUnits.add(new ConversionUnit("c", "Celsius", "°C", UnitCategory.TEMPERATURE, 1.0));
        tempUnits.add(new ConversionUnit("f", "Fahrenheit", "°F", UnitCategory.TEMPERATURE, 1.0));
        tempUnits.add(new ConversionUnit("k", "Kelvin", "K", UnitCategory.TEMPERATURE, 1.0));
        UNITS_MAP.put(UnitCategory.TEMPERATURE, Collections.unmodifiableList(tempUnits));
    }

    public static List<ConversionUnit> getUnitsForCategory(UnitCategory category) {
        List<ConversionUnit> units = UNITS_MAP.get(category);
        return units != null ? units : Collections.<ConversionUnit>emptyList();
    }

    public static ConversionUnit getDefaultFromUnit(UnitCategory category) {
        List<ConversionUnit> units = getUnitsForCategory(category);
        if (units.isEmpty()) return null;
        switch (category) {
            case LENGTH:
                // Default: Kilometer
                for (ConversionUnit u : units) {
                    if ("km".equals(u.getId())) return u;
                }
                return units.get(0);
            case WEIGHT:
                // Default: Kilogram
                for (ConversionUnit u : units) {
                    if ("kg".equals(u.getId())) return u;
                }
                return units.get(0);
            case TEMPERATURE:
                // Default: Celsius
                for (ConversionUnit u : units) {
                    if ("c".equals(u.getId())) return u;
                }
                return units.get(0);
            default:
                return units.get(0);
        }
    }

    public static ConversionUnit getDefaultToUnit(UnitCategory category) {
        List<ConversionUnit> units = getUnitsForCategory(category);
        if (units.isEmpty()) return null;
        switch (category) {
            case LENGTH:
                // Default: Meter
                for (ConversionUnit u : units) {
                    if ("m".equals(u.getId())) return u;
                }
                return units.get(1);
            case WEIGHT:
                // Default: Gram
                for (ConversionUnit u : units) {
                    if ("g".equals(u.getId())) return u;
                }
                return units.get(1);
            case TEMPERATURE:
                // Default: Fahrenheit
                for (ConversionUnit u : units) {
                    if ("f".equals(u.getId())) return u;
                }
                return units.get(1);
            default:
                return units.size() > 1 ? units.get(1) : units.get(0);
        }
    }

    public static ConversionResult convert(double inputValue, ConversionUnit from, ConversionUnit to) {
        if (from == null || to == null) {
            return ConversionResult.error("Units cannot be null", "");
        }

        if (Double.isNaN(inputValue) || Double.isInfinite(inputValue)) {
            return ConversionResult.error("Value must be a finite number", "");
        }

        String formula = getFormulaExplanation(from, to);

        // Category validation
        if (from.getCategory() != to.getCategory()) {
            return ConversionResult.error("Cannot convert across different categories", formula);
        }

        // Domain checks
        if (from.getCategory() == UnitCategory.LENGTH && inputValue < 0) {
            return ConversionResult.error("Length value cannot be negative", formula);
        }

        if (from.getCategory() == UnitCategory.WEIGHT && inputValue < 0) {
            return ConversionResult.error("Weight value cannot be negative", formula);
        }

        if (from.getCategory() == UnitCategory.TEMPERATURE) {
            String tempError = validateTemperatureLimit(inputValue, from);
            if (tempError != null) {
                return ConversionResult.error(tempError, formula);
            }

            double result = convertTemperature(inputValue, from.getId(), to.getId());
            return ConversionResult.success(result, formatResult(result), formula);
        }

        // Linear conversions (Length & Weight)
        if (from.equals(to)) {
            return ConversionResult.success(inputValue, formatResult(inputValue), formula);
        }

        // Convert input -> base unit -> target unit
        double baseValue = inputValue * from.getFactorToBase();
        double targetValue = baseValue / to.getFactorToBase();

        return ConversionResult.success(targetValue, formatResult(targetValue), formula);
    }

    private static String validateTemperatureLimit(double value, ConversionUnit unit) {
        if ("c".equals(unit.getId()) && value < ABSOLUTE_ZERO_CELSIUS) {
            return String.format(Locale.US, "Value is below absolute zero (-273.15 °C)");
        }
        if ("f".equals(unit.getId()) && value < ABSOLUTE_ZERO_FAHRENHEIT) {
            return String.format(Locale.US, "Value is below absolute zero (-459.67 °F)");
        }
        if ("k".equals(unit.getId()) && value < ABSOLUTE_ZERO_KELVIN) {
            return String.format(Locale.US, "Value is below absolute zero (0 K)");
        }
        return null;
    }

    private static double convertTemperature(double value, String fromId, String toId) {
        if (fromId.equals(toId)) {
            return value;
        }

        // Convert everything to Celsius first
        double celsius;
        switch (fromId) {
            case "f":
                celsius = (value - 32.0) * (5.0 / 9.0);
                break;
            case "k":
                celsius = value - 273.15;
                break;
            case "c":
            default:
                celsius = value;
                break;
        }

        // Convert Celsius to destination unit
        switch (toId) {
            case "f":
                return (celsius * 9.0 / 5.0) + 32.0;
            case "k":
                return celsius + 273.15;
            case "c":
            default:
                return celsius;
        }
    }

    public static String getFormulaExplanation(ConversionUnit from, ConversionUnit to) {
        if (from == null || to == null) return "";

        if (from.getCategory() == UnitCategory.TEMPERATURE) {
            String fromId = from.getId();
            String toId = to.getId();
            if (fromId.equals(toId)) {
                return "1 " + from.getSymbol() + " = 1 " + to.getSymbol();
            }
            if ("c".equals(fromId) && "f".equals(toId)) {
                return "°F = (°C × 9/5) + 32";
            }
            if ("f".equals(fromId) && "c".equals(toId)) {
                return "°C = (°F − 32) × 5/9";
            }
            if ("c".equals(fromId) && "k".equals(toId)) {
                return "K = °C + 273.15";
            }
            if ("k".equals(fromId) && "c".equals(toId)) {
                return "°C = K − 273.15";
            }
            if ("f".equals(fromId) && "k".equals(toId)) {
                return "K = (°F − 32) × 5/9 + 273.15";
            }
            if ("k".equals(fromId) && "f".equals(toId)) {
                return "°F = (K − 273.15) × 9/5 + 32";
            }
        }

        if (from.equals(to)) {
            return "1 " + from.getSymbol() + " = 1 " + to.getSymbol();
        }

        double directFactor = from.getFactorToBase() / to.getFactorToBase();
        String formattedFactor = formatFactor(directFactor);

        return "1 " + from.getSymbol() + " = " + formattedFactor + " " + to.getSymbol();
    }

    public static String formatFactor(double factor) {
        if (factor == Math.floor(factor) && !Double.isInfinite(factor)) {
            return String.format(Locale.US, "%,d", (long) factor);
        }
        // Round cleanly to 6 significant digits for factor display
        BigDecimal bd = new BigDecimal(Double.toString(factor));
        bd = bd.round(new java.math.MathContext(6, RoundingMode.HALF_UP));
        return bd.stripTrailingZeros().toPlainString();
    }

    public static String formatResult(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return "Invalid";
        }

        double abs = Math.abs(value);
        if (abs >= 1e12 || (abs > 0 && abs < 1e-6)) {
            DecimalFormat sciFormat = new DecimalFormat("0.######E0", new DecimalFormatSymbols(Locale.US));
            return sciFormat.format(value);
        }

        // Standard decimal formatting up to 8 decimal places
        DecimalFormat df = new DecimalFormat("#,##0.########", new DecimalFormatSymbols(Locale.US));
        df.setRoundingMode(java.math.RoundingMode.HALF_UP);
        return df.format(value);
    }
}
