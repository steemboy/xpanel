package arca.xpanel;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import androidx.annotation.Nullable;

import arca.xpanel.custome_interfaces.SeekBarPreference;
import arca.xpanel.services.service_accessibility;

public class preferenses extends PreferenceFragment implements Preference.OnPreferenceChangeListener, SeekBarPreference.SeekBarPreferenceListner {
    Context con;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        con = getActivity();
        ((SeekBarPreference)findPreference("width")).setListner(this);
        ((SeekBarPreference)findPreference("height")).setListner(this);
        ((SeekBarPreference)findPreference("margin")).setListner(this);
        ((SeekBarPreference)findPreference("vibrate_start_power")).setListner(this);
        ((SeekBarPreference)findPreference("vibrate_end_power")).setListner(this);
        ((SeekBarPreference)findPreference("shadow_power")).setListner(this);
        findPreference("color").setOnPreferenceChangeListener(this);
        findPreference("shadow").setOnPreferenceChangeListener(this);
        findPreference("vibrate").setOnPreferenceChangeListener(this);
        findPreference("gravity").setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        Intent i = new Intent(con, service_accessibility.class).setAction(service_accessibility.ACTION_UPDATE_SERVICE);
        i.putExtra(service_accessibility.EXTRA_NAME, key);
        switch (key) {
            case "color":
            case "gravity":
                i.putExtra(service_accessibility.EXTRA_PARAM, (String) newValue);
                break;
            case "shadow":
            case "always_on":
            case "vibrate":
                i.putExtra(service_accessibility.EXTRA_PARAM, (boolean)newValue);
                break;
        }
        con.startService(i);
        return true;
    }

    @Override
    public void OnChange(String key, int value) {
        Intent i = new Intent(con, service_accessibility.class).setAction(service_accessibility.ACTION_UPDATE_SERVICE);
        i.putExtra(service_accessibility.EXTRA_NAME, key);
        i.putExtra(service_accessibility.EXTRA_PARAM, value);
        con.startService(i);
    }
}