package vibe.client.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/** Число с ползунком от min до max с шагом step. */
public final class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double step;

    public NumberSetting(String name, double value, double min, double max, double step) {
        super(name, value);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public double min() {
        return min;
    }

    public double max() {
        return max;
    }

    public float floatValue() {
        return value.floatValue();
    }

    public int intValue() {
        return (int) Math.round(value);
    }

    @Override
    public void set(Double value) {
        double snapped = Math.round((value - min) / step) * step + min;
        this.value = Math.max(min, Math.min(max, snapped));
    }

    /** Доля заполнения ползунка, от 0 до 1. */
    public double fraction() {
        return (value - min) / (max - min);
    }

    public void setFraction(double fraction) {
        set(min + (max - min) * Math.max(0, Math.min(1, fraction)));
    }

    @Override
    public String display() {
        return step >= 1 ? String.valueOf(intValue()) : String.format(java.util.Locale.ROOT, "%.2f", value);
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public void fromJson(JsonElement json) {
        set(json.getAsDouble());
    }
}
