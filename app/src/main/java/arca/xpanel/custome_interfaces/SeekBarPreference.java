package arca.xpanel.custome_interfaces;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import arca.xpanel.R;

public final class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener {
    public interface SeekBarPreferenceListner {
        void OnChange(String key, int value);
    }
    // Namespaces to read attributes
    private static final String PREFERENCE_NS = "http://schemas.android.com/apk/res/arca.xpanel";
    private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";

    // Attribute names
    private static final String ATTR_DEFAULT_VALUE = "defaultValue";
    private static final String ATTR_MIN_VALUE = "minValue";
    private static final String ATTR_MAX_VALUE = "maxValue";

    // Default values for defaults
    private static final int DEFAULT_MIN_VALUE = 0;
    private static final int DEFAULT_MAX_VALUE = 100;

    // Real defaults
    private final int mMaxValue;
    private final int mMinValue;
    private final int mDefValue;

    // Current value
    private int mCurrentValue;

    private TextView mValueText;
    private SeekBarPreferenceListner listner = null;

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Read parameters from attributes
        mMinValue = attrs.getAttributeIntValue(PREFERENCE_NS, ATTR_MIN_VALUE, DEFAULT_MIN_VALUE);
        mMaxValue = attrs.getAttributeIntValue(PREFERENCE_NS, ATTR_MAX_VALUE, DEFAULT_MAX_VALUE);

        // Get current value from preferences
        mDefValue = attrs.getAttributeIntValue(ANDROID_NS, ATTR_DEFAULT_VALUE, mMaxValue - mMinValue);
    }


    public void setListner(SeekBarPreferenceListner lis) {
        listner = lis;
    }
    @Override
    protected View onCreateDialogView() {
        mCurrentValue = getPersistedInt(mDefValue);
        // Inflate layout
        View view = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.dialog_slider, null);

        // Setup minimum and maximum text labels
        ((TextView) view.findViewById(R.id.min_value)).setText(Integer.toString(mMinValue));
        ((TextView) view.findViewById(R.id.max_value)).setText(Integer.toString(mMaxValue));

        // Setup SeekBar
        SeekBar mSeekBar = view.findViewById(R.id.seek_bar);
        mSeekBar.setMax(mMaxValue - mMinValue);
        mSeekBar.setProgress(mCurrentValue - mMinValue);
        mSeekBar.setOnSeekBarChangeListener(this);

        // Setup text label for current value
        mValueText = view.findViewById(R.id.current_value);
        mValueText.setText(Integer.toString(mCurrentValue));

        return view;
    }



    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (!positiveResult)
            return;

        if (shouldPersist())
            persistInt(mCurrentValue);

        notifyChanged();
        if(listner != null)
            listner.OnChange(getKey(), mCurrentValue);
    }

    @Override
    public CharSequence getSummary() {
        return Integer.toString(getPersistedInt(mDefValue));
    }

    public int get_value() {
        return mCurrentValue;
    }

    public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
        mValueText.setText(Integer.toString(mCurrentValue = value + mMinValue));
    }

    public void onStartTrackingTouch(SeekBar seek) {
        // Not used
    }

    public void onStopTrackingTouch(SeekBar seek) {
        // Not used
    }
}