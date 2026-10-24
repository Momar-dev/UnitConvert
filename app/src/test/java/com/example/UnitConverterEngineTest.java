package com.example;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.List;

public class UnitConverterEngineTest {

    private ConversionUnit findUnit(UnitCategory category, String id) {
        List<ConversionUnit> units = UnitConverterEngine.getUnitsForCategory(category);
        for (ConversionUnit u : units) {
            if (u.getId().equalsIgnoreCase(id)) return u;
        }
        throw new IllegalArgumentException("Unit not found: " + id);
    }

    @Test
    public void testLengthConversions() {
        ConversionUnit km = findUnit(UnitCategory.LENGTH, "km");
        ConversionUnit m = findUnit(UnitCategory.LENGTH, "m");
        ConversionUnit cm = findUnit(UnitCategory.LENGTH, "cm");
        ConversionUnit mm = findUnit(UnitCategory.LENGTH, "mm");
        ConversionUnit mi = findUnit(UnitCategory.LENGTH, "mi");
        ConversionUnit ft = findUnit(UnitCategory.LENGTH, "ft");
        ConversionUnit in = findUnit(UnitCategory.LENGTH, "in");

        // 1 km = 1,000 m
        ConversionResult resKmToM = UnitConverterEngine.convert(1.0, km, m);
        assertTrue(resKmToM.isSuccess());
        assertEquals(1000.0, resKmToM.getNumericValue(), 1e-6);
        assertEquals("1,000", resKmToM.getFormattedResult());

        // 100 cm = 1 m
        ConversionResult resCmToM = UnitConverterEngine.convert(100.0, cm, m);
        assertTrue(resCmToM.isSuccess());
        assertEquals(1.0, resCmToM.getNumericValue(), 1e-6);
        assertEquals("1", resCmToM.getFormattedResult());

        // 1 mi to ft: 1 mile = 5280 ft
        ConversionResult resMiToFt = UnitConverterEngine.convert(1.0, mi, ft);
        assertTrue(resMiToFt.isSuccess());
        assertEquals(5280.0, resMiToFt.getNumericValue(), 1e-3);

        // 1 ft to in: 12 inches
        ConversionResult resFtToIn = UnitConverterEngine.convert(1.0, ft, in);
        assertTrue(resFtToIn.isSuccess());
        assertEquals(12.0, resFtToIn.getNumericValue(), 1e-4);

        // Negative length rejected
        ConversionResult resNegative = UnitConverterEngine.convert(-5.0, m, km);
        assertFalse(resNegative.isSuccess());
        assertNotNull(resNegative.getErrorMessage());
    }

    @Test
    public void testWeightConversions() {
        ConversionUnit kg = findUnit(UnitCategory.WEIGHT, "kg");
        ConversionUnit g = findUnit(UnitCategory.WEIGHT, "g");
        ConversionUnit mg = findUnit(UnitCategory.WEIGHT, "mg");
        ConversionUnit lb = findUnit(UnitCategory.WEIGHT, "lb");
        ConversionUnit oz = findUnit(UnitCategory.WEIGHT, "oz");

        // 1 kg = 1,000 g
        ConversionResult resKgToG = UnitConverterEngine.convert(1.0, kg, g);
        assertTrue(resKgToG.isSuccess());
        assertEquals(1000.0, resKgToG.getNumericValue(), 1e-6);
        assertEquals("1,000", resKgToG.getFormattedResult());

        // 1 lb ≈ 0.45359237 kg
        ConversionResult resLbToKg = UnitConverterEngine.convert(1.0, lb, kg);
        assertTrue(resLbToKg.isSuccess());
        assertEquals(0.45359237, resLbToKg.getNumericValue(), 1e-6);

        // 1 lb = 16 oz
        ConversionResult resLbToOz = UnitConverterEngine.convert(1.0, lb, oz);
        assertTrue(resLbToOz.isSuccess());
        assertEquals(16.0, resLbToOz.getNumericValue(), 1e-4);

        // Negative weight rejected
        ConversionResult resNegWeight = UnitConverterEngine.convert(-1.0, kg, lb);
        assertFalse(resNegWeight.isSuccess());
        assertNotNull(resNegWeight.getErrorMessage());
    }

    @Test
    public void testTemperatureConversions() {
        ConversionUnit c = findUnit(UnitCategory.TEMPERATURE, "c");
        ConversionUnit f = findUnit(UnitCategory.TEMPERATURE, "f");
        ConversionUnit k = findUnit(UnitCategory.TEMPERATURE, "k");

        // 0 °C = 32 °F
        ConversionResult res0CtoF = UnitConverterEngine.convert(0.0, c, f);
        assertTrue(res0CtoF.isSuccess());
        assertEquals(32.0, res0CtoF.getNumericValue(), 1e-6);

        // 100 °C = 212 °F
        ConversionResult res100CtoF = UnitConverterEngine.convert(100.0, c, f);
        assertTrue(res100CtoF.isSuccess());
        assertEquals(212.0, res100CtoF.getNumericValue(), 1e-6);

        // 0 °C = 273.15 K
        ConversionResult res0CtoK = UnitConverterEngine.convert(0.0, c, k);
        assertTrue(res0CtoK.isSuccess());
        assertEquals(273.15, res0CtoK.getNumericValue(), 1e-6);

        // 32 °F = 0 °C
        ConversionResult res32FtoC = UnitConverterEngine.convert(32.0, f, c);
        assertTrue(res32FtoC.isSuccess());
        assertEquals(0.0, res32FtoC.getNumericValue(), 1e-6);

        // Negative temperature: -40 °C = -40 °F
        ConversionResult resNeg40 = UnitConverterEngine.convert(-40.0, c, f);
        assertTrue(resNeg40.isSuccess());
        assertEquals(-40.0, resNeg40.getNumericValue(), 1e-6);

        // Absolute zero rejection
        ConversionResult resBelowAbsC = UnitConverterEngine.convert(-274.0, c, f);
        assertFalse(resBelowAbsC.isSuccess());
        assertTrue(resBelowAbsC.getErrorMessage().contains("absolute zero"));

        ConversionResult resBelowAbsF = UnitConverterEngine.convert(-460.0, f, c);
        assertFalse(resBelowAbsF.isSuccess());

        ConversionResult resBelowAbsK = UnitConverterEngine.convert(-1.0, k, c);
        assertFalse(resBelowAbsK.isSuccess());
    }

    @Test
    public void testAllCategoryUnitsPairs() {
        for (UnitCategory cat : UnitCategory.values()) {
            List<ConversionUnit> units = UnitConverterEngine.getUnitsForCategory(cat);
            for (ConversionUnit u1 : units) {
                for (ConversionUnit u2 : units) {
                    double testVal = (cat == UnitCategory.TEMPERATURE) ? 50.0 : 10.0;
                    ConversionResult res = UnitConverterEngine.convert(testVal, u1, u2);
                    assertTrue("Conversion failed for " + u1.getId() + " -> " + u2.getId(), res.isSuccess());
                    assertFalse(res.getFormattedResult().isEmpty());
                    assertNotNull(res.getFormulaExplanation());
                }
            }
        }
    }

    @Test
    public void testFormulaGeneration() {
        ConversionUnit c = findUnit(UnitCategory.TEMPERATURE, "c");
        ConversionUnit f = findUnit(UnitCategory.TEMPERATURE, "f");
        String formulaCF = UnitConverterEngine.getFormulaExplanation(c, f);
        assertTrue(formulaCF.contains("°F = (°C × 9/5) + 32"));

        ConversionUnit km = findUnit(UnitCategory.LENGTH, "km");
        ConversionUnit m = findUnit(UnitCategory.LENGTH, "m");
        String formulaKmM = UnitConverterEngine.getFormulaExplanation(km, m);
        assertEquals("1 km = 1,000 m", formulaKmM);
    }
}
