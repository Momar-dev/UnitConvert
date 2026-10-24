# Unit Converter

A premium, fully responsive native Android application engineered in **Java** and **XML layouts**. Designed with a sophisticated dark navy, indigo, and cyan aesthetic inspired by professional financial and utility applications.

---

## 📱 Features

- **Three Core Conversion Categories**:
  - **Length**: Meter (`m`), Kilometer (`km`), Centimeter (`cm`), Millimeter (`mm`), Mile (`mi`), Foot (`ft`), Inch (`in`).
  - **Weight**: Kilogram (`kg`), Gram (`g`), Milligram (`mg`), Pound (`lb`), Ounce (`oz`).
  - **Temperature**: Celsius (`°C`), Fahrenheit (`°F`), Kelvin (`K`).
- **Real-Time Live Conversion**: Instant calculation as you type with smooth decimal and comma parsing.
- **Bi-Directional Unit Swapping**: Swap source and destination units with a single tap featuring a 180° rotation animation and instant recalculation.
- **Live Formula & Conversion Factor Breakdown**: Supporting card displays dynamic mathematical formulas (e.g., `°F = (°C × 9/5) + 32`, `1 km = 1,000 m`).
- **Robust Domain Validation**:
  - Absolute zero boundary enforcement (Celsius < -273.15 °C, Fahrenheit < -459.67 °F, Kelvin < 0 K).
  - Negative values rejected for Length and Weight.
  - Safe parsing against non-finite or empty inputs.
- **Productivity & Clipboard Support**: One-tap copy button to copy converted results to the Android system clipboard with toast confirmation.
- **Responsive Layout & State Preservation**:
  - Built with `ScrollView` and flexible layout constraints to support compact phones, standard devices, tablets, and landscape mode.
  - Handles keyboard presentation without obstructing interactive fields.
  - Preserves category, units, user input, results, and errors across device rotations (`onSaveInstanceState`).

---

## 🎨 Visual Identity

- **Theme Palette**:
  - Primary Background: `#0B1020`
  - Elevated Surface: `#151D31`
  - Secondary Surface: `#1C2740`
  - Primary Accent: `#7567F8` (Indigo)
  - Secondary Accent: `#56D6E8` (Cyan)
  - Main Text: `#F4F6FC`
  - Secondary Text: `#9AA7BD`
  - Error: `#EF4444` / Success: `#22C55E`
- **Component Design**: 14–22dp corner radii, subtle borders, minimum 48dp touch targets, and custom adaptive icon with raster fallbacks across all screen densities.

---

## 🛠️ Architecture & Tech Stack

- **Application Logic**: Pure Java (Android Studio compatible).
- **Interface Layouts**: Native Android XML layouts using ConstraintLayout and LinearLayout with Material 3 theming.
- **Conversion Engine**: `UnitConverterEngine.java` — modular, separate from UI, with high-precision factor conversion and unit-pair formula generation.
- **Android SDK**: `minSdk 24`, `compileSdk 36`, `targetSdk 36`.
- **Key Libraries**:
  - `androidx.appcompat:appcompat`
  - `com.google.android.material:material`
  - `androidx.constraintlayout:constraintlayout`

---

## 📂 Project Structure

```text
app/src/main/
├── AndroidManifest.xml
├── java/com/example/
│   ├── MainActivity.java           # Activity handling UI events, insets, dialogs & state
│   ├── UnitCategory.java           # Enum for LENGTH, WEIGHT, TEMPERATURE
│   ├── ConversionUnit.java         # Model for unit metadata and base conversion factors
│   ├── ConversionResult.java       # Result holder (value, formatted string, error, formula)
│   └── UnitConverterEngine.java    # Reusable conversion engine and formula generator
└── res/
    ├── drawable/                   # Custom vector icons & shape gradient drawables
    ├── layout/
    │   ├── activity_main.xml       # Responsive main screen XML layout
    │   └── item_unit_choice.xml    # Unit selector dialog option item
    ├── mipmap-*/                   # Adaptive launcher icons and raster PNGs
    └── values/
        ├── colors.xml              # Navy, indigo, and cyan color palette
        ├── strings.xml             # Localized user-facing strings & accessibility labels
        └── themes.xml              # Material 3 dark application theme
```

---

## 🧪 Testing

The project includes JVM unit and Robolectric tests validating all mathematical conversions, edge cases, formulas, and string resources.

To run tests:
```bash
gradle :app:testDebugUnitTest
```

Key test cases verified:
- `1 km = 1,000 m`
- `100 cm = 1 m`
- `1 kg = 1,000 g`
- `1 lb ≈ 0.45359237 kg`
- `0 °C = 32 °F`, `100 °C = 212 °F`, `0 °C = 273.15 K`, `32 °F = 0 °C`
- Absolute zero rejection & negative length/weight handling
