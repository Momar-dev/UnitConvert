package com.example;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_CATEGORY = "key_category";
    private static final String KEY_FROM_UNIT_ID = "key_from_unit_id";
    private static final String KEY_TO_UNIT_ID = "key_to_unit_id";
    private static final String KEY_INPUT_VALUE = "key_input_value";
    private static final String KEY_RESULT_VALUE = "key_result_value";
    private static final String KEY_ERROR_MESSAGE = "key_error_message";

    // Category UI
    private LinearLayout btnCategoryLength;
    private LinearLayout btnCategoryWeight;
    private LinearLayout btnCategoryTemperature;
    private ImageView ivCatLengthIcon;
    private ImageView ivCatWeightIcon;
    private ImageView ivCatTempIcon;
    private TextView tvCatLengthLabel;
    private TextView tvCatWeightLabel;
    private TextView tvCatTempLabel;

    // Workspace UI
    private LinearLayout btnSelectFromUnit;
    private TextView tvSelectedFromUnit;
    private TextView tvInputUnitSymbol;
    private EditText etInputValue;

    private ImageButton btnSwapUnits;

    private LinearLayout btnSelectToUnit;
    private TextView tvSelectedToUnit;
    private TextView tvResultValue;
    private TextView tvResultUnitName;
    private ImageButton btnCopyResult;

    // Action buttons
    private Button btnReset;
    private ImageButton btnHeaderReset;

    // Info & Error
    private TextView tvFormulaText;
    private LinearLayout layoutErrorBanner;
    private TextView tvErrorMessage;

    // State
    private UnitCategory currentCategory = UnitCategory.LENGTH;
    private ConversionUnit currentFromUnit;
    private ConversionUnit currentToUnit;
    private String lastResultText = "";
    private float swapRotation = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupWindowInsets();
        setupListeners();

        if (savedInstanceState != null) {
            restoreState(savedInstanceState);
        } else {
            setCategory(UnitCategory.LENGTH);
        }
    }

    private void initViews() {
        btnCategoryLength = findViewById(R.id.btn_category_length);
        btnCategoryWeight = findViewById(R.id.btn_category_weight);
        btnCategoryTemperature = findViewById(R.id.btn_category_temperature);

        ivCatLengthIcon = findViewById(R.id.iv_cat_length_icon);
        ivCatWeightIcon = findViewById(R.id.iv_cat_weight_icon);
        ivCatTempIcon = findViewById(R.id.iv_cat_temp_icon);

        tvCatLengthLabel = findViewById(R.id.tv_cat_length_label);
        tvCatWeightLabel = findViewById(R.id.tv_cat_weight_label);
        tvCatTempLabel = findViewById(R.id.tv_cat_temp_label);

        btnSelectFromUnit = findViewById(R.id.btn_select_from_unit);
        tvSelectedFromUnit = findViewById(R.id.tv_selected_from_unit);
        tvInputUnitSymbol = findViewById(R.id.tv_input_unit_symbol);
        etInputValue = findViewById(R.id.et_input_value);

        btnSwapUnits = findViewById(R.id.btn_swap_units);

        btnSelectToUnit = findViewById(R.id.btn_select_to_unit);
        tvSelectedToUnit = findViewById(R.id.tv_selected_to_unit);
        tvResultValue = findViewById(R.id.tv_result_value);
        tvResultUnitName = findViewById(R.id.tv_result_unit_name);
        btnCopyResult = findViewById(R.id.btn_copy_result);

        btnReset = findViewById(R.id.btn_reset);
        btnHeaderReset = findViewById(R.id.btn_header_reset);

        tvFormulaText = findViewById(R.id.tv_formula_text);
        layoutErrorBanner = findViewById(R.id.layout_error_banner);
        tvErrorMessage = findViewById(R.id.tv_error_message);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_scroll_view), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), insets.top, v.getPaddingRight(), insets.bottom);
            return windowInsets;
        });
    }

    private void setupListeners() {
        findViewById(R.id.main_scroll_view).setOnClickListener(v -> hideKeyboard());

        btnCategoryLength.setOnClickListener(v -> setCategory(UnitCategory.LENGTH));
        btnCategoryWeight.setOnClickListener(v -> setCategory(UnitCategory.WEIGHT));
        btnCategoryTemperature.setOnClickListener(v -> setCategory(UnitCategory.TEMPERATURE));

        btnSelectFromUnit.setOnClickListener(v -> showUnitPickerDialog(true));
        btnSelectToUnit.setOnClickListener(v -> showUnitPickerDialog(false));

        btnSwapUnits.setOnClickListener(v -> swapUnits());

        btnReset.setOnClickListener(v -> resetConverter());
        btnHeaderReset.setOnClickListener(v -> resetConverter());

        btnCopyResult.setOnClickListener(v -> copyResultToClipboard());

        etInputValue.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performConversion(true);
                return true;
            }
            return false;
        });

        etInputValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearError();
                String text = s != null ? s.toString().trim() : "";
                if (TextUtils.isEmpty(text)) {
                    clearResult();
                } else if (!"-".equals(text) && !".".equals(text) && !"-.".equals(text)) {
                    // Real-time live conversion preview as user types
                    try {
                        String parsedStr = text.replace(',', '.');
                        double val = Double.parseDouble(parsedStr);
                        ConversionResult result = UnitConverterEngine.convert(val, currentFromUnit, currentToUnit);
                        if (result.isSuccess()) {
                            lastResultText = result.getFormattedResult();
                            tvResultValue.setText(lastResultText);
                            if (currentToUnit != null) {
                                tvResultUnitName.setText(currentToUnit.getName() + " (" + currentToUnit.getSymbol() + ")");
                            }
                            tvFormulaText.setText(result.getFormulaExplanation());
                        }
                    } catch (NumberFormatException ignored) {
                        // Intermediate character entry
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    public void setCategory(UnitCategory category) {
        this.currentCategory = category;
        this.currentFromUnit = UnitConverterEngine.getDefaultFromUnit(category);
        this.currentToUnit = UnitConverterEngine.getDefaultToUnit(category);

        updateCategorySelectorVisuals();
        updateUnitDisplays();
        clearError();
        clearResult();
        updateFormulaDisplay();
    }

    private void updateCategorySelectorVisuals() {
        int colorActive = ContextCompat.getColor(this, R.color.primary_accent);
        int colorSecondaryAccent = ContextCompat.getColor(this, R.color.secondary_accent);
        int colorInactive = ContextCompat.getColor(this, R.color.text_secondary);
        int colorTextPrimary = ContextCompat.getColor(this, R.color.text_primary);

        // Reset all to inactive
        btnCategoryLength.setBackgroundResource(R.drawable.bg_category_inactive);
        btnCategoryWeight.setBackgroundResource(R.drawable.bg_category_inactive);
        btnCategoryTemperature.setBackgroundResource(R.drawable.bg_category_inactive);

        ivCatLengthIcon.setImageTintList(ColorStateList.valueOf(colorInactive));
        ivCatWeightIcon.setImageTintList(ColorStateList.valueOf(colorInactive));
        ivCatTempIcon.setImageTintList(ColorStateList.valueOf(colorInactive));

        tvCatLengthLabel.setTextColor(colorInactive);
        tvCatWeightLabel.setTextColor(colorInactive);
        tvCatTempLabel.setTextColor(colorInactive);

        tvCatLengthLabel.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvCatWeightLabel.setTypeface(null, android.graphics.Typeface.NORMAL);
        tvCatTempLabel.setTypeface(null, android.graphics.Typeface.NORMAL);

        // Set active category visual
        switch (currentCategory) {
            case LENGTH:
                btnCategoryLength.setBackgroundResource(R.drawable.bg_category_active);
                ivCatLengthIcon.setImageTintList(ColorStateList.valueOf(colorSecondaryAccent));
                tvCatLengthLabel.setTextColor(colorTextPrimary);
                tvCatLengthLabel.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
            case WEIGHT:
                btnCategoryWeight.setBackgroundResource(R.drawable.bg_category_active);
                ivCatWeightIcon.setImageTintList(ColorStateList.valueOf(colorSecondaryAccent));
                tvCatWeightLabel.setTextColor(colorTextPrimary);
                tvCatWeightLabel.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
            case TEMPERATURE:
                btnCategoryTemperature.setBackgroundResource(R.drawable.bg_category_active);
                ivCatTempIcon.setImageTintList(ColorStateList.valueOf(colorSecondaryAccent));
                tvCatTempLabel.setTextColor(colorTextPrimary);
                tvCatTempLabel.setTypeface(null, android.graphics.Typeface.BOLD);
                break;
        }
    }

    private void updateUnitDisplays() {
        if (currentFromUnit != null) {
            tvSelectedFromUnit.setText(currentFromUnit.getDisplayName());
            tvInputUnitSymbol.setText(currentFromUnit.getSymbol());
        }
        if (currentToUnit != null) {
            tvSelectedToUnit.setText(currentToUnit.getDisplayName());
            tvResultUnitName.setText(currentToUnit.getName() + " (" + currentToUnit.getSymbol() + ")");
        }
    }

    private void updateFormulaDisplay() {
        if (currentFromUnit != null && currentToUnit != null) {
            String formula = UnitConverterEngine.getFormulaExplanation(currentFromUnit, currentToUnit);
            tvFormulaText.setText(formula);
        }
    }

    private void swapUnits() {
        ConversionUnit temp = currentFromUnit;
        currentFromUnit = currentToUnit;
        currentToUnit = temp;

        // Smooth rotation animation
        swapRotation += 180f;
        btnSwapUnits.animate()
                .rotation(swapRotation)
                .setDuration(260)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        updateUnitDisplays();
        updateFormulaDisplay();

        // If input exists, recalculate immediately
        String inputStr = etInputValue.getText().toString().trim();
        if (!TextUtils.isEmpty(inputStr)) {
            performConversion(false);
        } else {
            clearResult();
        }
    }

    private void performConversion(boolean dismissKeyboard) {
        if (dismissKeyboard) {
            hideKeyboard();
        }

        String inputStr = etInputValue.getText().toString().trim();
        if (TextUtils.isEmpty(inputStr)) {
            showError(getString(R.string.error_empty_input));
            clearResult();
            return;
        }

        // Support both comma and dot as decimal separator
        inputStr = inputStr.replace(',', '.');

        double inputVal;
        try {
            inputVal = Double.parseDouble(inputStr);
        } catch (NumberFormatException e) {
            showError(getString(R.string.error_invalid_number));
            clearResult();
            return;
        }

        ConversionResult result = UnitConverterEngine.convert(inputVal, currentFromUnit, currentToUnit);
        if (result.isSuccess()) {
            clearError();
            lastResultText = result.getFormattedResult();
            tvResultValue.setText(lastResultText);
            tvResultUnitName.setText(currentToUnit.getName() + " (" + currentToUnit.getSymbol() + ")");
            tvFormulaText.setText(result.getFormulaExplanation());
        } else {
            showError(result.getErrorMessage());
            clearResult();
            tvFormulaText.setText(result.getFormulaExplanation());
        }
    }

    private void resetConverter() {
        hideKeyboard();
        setCategory(UnitCategory.LENGTH);
        etInputValue.setText("");
        etInputValue.clearFocus();
        clearError();
        clearResult();
        Toast.makeText(this, "Converter reset to defaults", Toast.LENGTH_SHORT).show();
    }

    private void clearResult() {
        lastResultText = "";
        tvResultValue.setText(R.string.label_result_placeholder);
        if (currentToUnit != null) {
            tvResultUnitName.setText(currentToUnit.getName() + " (" + currentToUnit.getSymbol() + ")");
        }
    }

    private void showError(String message) {
        tvErrorMessage.setText(message);
        layoutErrorBanner.setVisibility(View.VISIBLE);
    }

    private void clearError() {
        layoutErrorBanner.setVisibility(View.GONE);
        tvErrorMessage.setText("");
    }

    private void copyResultToClipboard() {
        String resultStr = tvResultValue.getText().toString();
        if (TextUtils.isEmpty(resultStr) || resultStr.equals(getString(R.string.label_result_placeholder))) {
            Toast.makeText(this, "No result to copy yet", Toast.LENGTH_SHORT).show();
            return;
        }

        String copyText = resultStr + " " + (currentToUnit != null ? currentToUnit.getSymbol() : "");
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Converted Result", copyText);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, getString(R.string.copied_to_clipboard) + ": " + copyText, Toast.LENGTH_SHORT).show();
        }
    }

    private void showUnitPickerDialog(boolean isFromUnit) {
        List<ConversionUnit> units = UnitConverterEngine.getUnitsForCategory(currentCategory);
        ConversionUnit activeUnit = isFromUnit ? currentFromUnit : currentToUnit;

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle(isFromUnit ? R.string.dialog_select_source_unit : R.string.dialog_select_target_unit);

        ArrayAdapter<ConversionUnit> adapter = new ArrayAdapter<ConversionUnit>(this, R.layout.item_unit_choice, units) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_unit_choice, parent, false);
                }

                ConversionUnit unit = getItem(position);
                TextView tvName = convertView.findViewById(R.id.tv_unit_item_name);
                TextView tvSymbol = convertView.findViewById(R.id.tv_unit_item_symbol);
                TextView tvSymbolBadge = convertView.findViewById(R.id.tv_unit_item_symbol_badge);
                ImageView ivCheck = convertView.findViewById(R.id.iv_unit_item_check);

                if (unit != null) {
                    tvName.setText(unit.getName());
                    tvSymbol.setText(unit.getSymbol());
                    if (tvSymbolBadge != null) {
                        tvSymbolBadge.setText(unit.getSymbol());
                    }

                    boolean isSelected = activeUnit != null && unit.getId().equals(activeUnit.getId());
                    if (isSelected) {
                        ivCheck.setVisibility(View.VISIBLE);
                        tvName.setTextColor(ContextCompat.getColor(getContext(), R.color.secondary_accent));
                    } else {
                        ivCheck.setVisibility(View.GONE);
                        tvName.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary));
                    }
                }

                return convertView;
            }
        };

        AlertDialog dialog = builder.setAdapter(adapter, (d, which) -> {
            ConversionUnit selected = units.get(which);
            if (isFromUnit) {
                currentFromUnit = selected;
            } else {
                currentToUnit = selected;
            }
            updateUnitDisplays();
            updateFormulaDisplay();

            String input = etInputValue.getText().toString().trim();
            if (!TextUtils.isEmpty(input)) {
                performConversion(false);
            }
        }).create();

        dialog.show();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CATEGORY, currentCategory.name());
        if (currentFromUnit != null) {
            outState.putString(KEY_FROM_UNIT_ID, currentFromUnit.getId());
        }
        if (currentToUnit != null) {
            outState.putString(KEY_TO_UNIT_ID, currentToUnit.getId());
        }
        outState.putString(KEY_INPUT_VALUE, etInputValue.getText().toString());
        outState.putString(KEY_RESULT_VALUE, lastResultText);
        if (layoutErrorBanner.getVisibility() == View.VISIBLE) {
            outState.putString(KEY_ERROR_MESSAGE, tvErrorMessage.getText().toString());
        }
    }

    private void restoreState(@NonNull Bundle savedInstanceState) {
        String catName = savedInstanceState.getString(KEY_CATEGORY);
        if (catName != null) {
            try {
                currentCategory = UnitCategory.valueOf(catName);
            } catch (IllegalArgumentException e) {
                currentCategory = UnitCategory.LENGTH;
            }
        }

        List<ConversionUnit> units = UnitConverterEngine.getUnitsForCategory(currentCategory);
        String fromId = savedInstanceState.getString(KEY_FROM_UNIT_ID);
        String toId = savedInstanceState.getString(KEY_TO_UNIT_ID);

        currentFromUnit = UnitConverterEngine.getDefaultFromUnit(currentCategory);
        currentToUnit = UnitConverterEngine.getDefaultToUnit(currentCategory);

        for (ConversionUnit u : units) {
            if (u.getId().equals(fromId)) currentFromUnit = u;
            if (u.getId().equals(toId)) currentToUnit = u;
        }

        updateCategorySelectorVisuals();
        updateUnitDisplays();
        updateFormulaDisplay();

        String inputVal = savedInstanceState.getString(KEY_INPUT_VALUE);
        if (inputVal != null) {
            etInputValue.setText(inputVal);
        }

        String resultVal = savedInstanceState.getString(KEY_RESULT_VALUE);
        if (!TextUtils.isEmpty(resultVal)) {
            lastResultText = resultVal;
            tvResultValue.setText(resultVal);
            if (currentToUnit != null) {
                tvResultUnitName.setText(currentToUnit.getName() + " (" + currentToUnit.getSymbol() + ")");
            }
        } else {
            clearResult();
        }

        String errorMsg = savedInstanceState.getString(KEY_ERROR_MESSAGE);
        if (!TextUtils.isEmpty(errorMsg)) {
            showError(errorMsg);
        } else {
            clearError();
        }
    }
}
