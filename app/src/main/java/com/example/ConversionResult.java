package com.example;

import java.io.Serializable;

public class ConversionResult implements Serializable {
    private final boolean success;
    private final double numericValue;
    private final String formattedResult;
    private final String errorMessage;
    private final String formulaExplanation;

    private ConversionResult(boolean success, double numericValue, String formattedResult, String errorMessage, String formulaExplanation) {
        this.success = success;
        this.numericValue = numericValue;
        this.formattedResult = formattedResult;
        this.errorMessage = errorMessage;
        this.formulaExplanation = formulaExplanation;
    }

    public static ConversionResult success(double numericValue, String formattedResult, String formulaExplanation) {
        return new ConversionResult(true, numericValue, formattedResult, null, formulaExplanation);
    }

    public static ConversionResult error(String errorMessage, String formulaExplanation) {
        return new ConversionResult(false, 0.0, "", errorMessage, formulaExplanation);
    }

    public boolean isSuccess() {
        return success;
    }

    public double getNumericValue() {
        return numericValue;
    }

    public String getFormattedResult() {
        return formattedResult;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getFormulaExplanation() {
        return formulaExplanation;
    }
}
