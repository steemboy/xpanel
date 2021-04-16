package arca.xpanel;

import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import arca.xpanel.services.service_accessibility;

import static arca.xpanel.singleton.app;

public class xPanel extends AppCompatActivity implements View.OnClickListener {
    LinearLayout accessibility;
    LinearLayout alert;
    LinearLayout admin;
    LinearLayout access_usage;
    String clicked_viwe = "";
    Resources res;
    CharSequence[] actions;
    CharSequence[] lables;
    CharSequence[] packages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xpanel);
        res = getResources();
        getFragmentManager().beginTransaction().replace(R.id.farme, new preferenses()).commit();
        (accessibility = findViewById(R.id.accessibility)).setOnClickListener(this);
        (alert = findViewById(R.id.alert)).setOnClickListener(this);
        (admin = findViewById(R.id.admin)).setOnClickListener(this);
        (access_usage = findViewById(R.id.access_usage)).setOnClickListener(this);

        actions = res.getTextArray(R.array.actions);
        loadApplication();
        setting_init(R.id.left);
        setting_init(R.id.upleft);
        setting_init(R.id.up);
        setting_init(R.id.upright);
        setting_init(R.id.right);
        setting_init(R.id.tap);
        setting_init(R.id.dtap);
        setting_init(R.id.ltap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((Switch)accessibility.getChildAt(0)).setChecked(service_accessibility.isEnabled);

        boolean check = app.check_usageStats();
        ((Switch)access_usage.getChildAt(0)).setChecked(check);
        if(check)
            access_usage.setVisibility(View.GONE);
        else
            access_usage.setVisibility(View.VISIBLE);

        check = app.check_alert();
        ((Switch)alert.getChildAt(0)).setChecked(check);
        if(check)
            alert.setVisibility(View.GONE);
        else
            alert.setVisibility(View.VISIBLE);

        check = app.chec_devadmin();
        ((Switch)admin.getChildAt(0)).setChecked(check);
        if(check)
            admin.setVisibility(View.GONE);
        else
            admin.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.accessibility:
                show_dialog(3);
                break;
            case R.id.alert:
                show_dialog(2);
                return;
            case R.id.admin:
                show_dialog(1);
                return;
            case R.id.access_usage:
                show_dialog(0);
                return;
            default:
                AlertDialog.Builder alert = new AlertDialog.Builder(this).setTitle("Перенаправлеие в системные настройки");
                clicked_viwe = v.getTag().toString();
                alert.setTitle("Выберите действие")
                        .setSingleChoiceItems(actions, app.set.getInt(clicked_viwe, 0), (dialog, which) -> {
                            dialog.dismiss();
                            if(get_holder_visible((LinearLayout) v))
                                holder_init((LinearLayout) v, false);
                            switch (which) {
                                case 11:
                                    holder_init((LinearLayout) v, true);
                                    break;
                                case 5:
                                    if(!app.chec_devadmin())
                                        return;
                                    break;
                            }
                            app.set.edit().putInt(clicked_viwe, which).apply();
                            set_description((LinearLayout) v, actions[which]);
                        })
                        .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
                alert.create().show();
                break;
        }
    }

    private void setting_init(int id) {
        LinearLayout ll = findViewById(id);
        ll.setOnClickListener(this);
        int index = app.set.getInt(ll.getTag().toString(), 0);
        set_description(ll, actions[index]);
        if (index == 11)
            holder_init(ll, true);
    }

    private boolean get_holder_visible(LinearLayout ll) {
        if(ll.getChildAt(1) instanceof LinearLayout)
            return ((LinearLayout)ll.getChildAt(1)).getChildAt(2).getVisibility() == View.VISIBLE;
        else
            return ll.getChildAt(2).getVisibility() == View.VISIBLE;
    }

    private void holder_init(LinearLayout ll, boolean need) {
        LinearLayout holder;
        if(ll.getChildAt(1) instanceof LinearLayout)
            holder = (LinearLayout)((LinearLayout)ll.getChildAt(1)).getChildAt(2);
        else
            holder = (LinearLayout) ll.getChildAt(2);

        holder.setVisibility(View.GONE);
        ((TextView)holder.getChildAt(1)).setText("Приложение не выбрано");
        holder.getChildAt(0).setOnClickListener(null);

        SharedPreferences.Editor edit = app.set.edit();
        edit.putString(clicked_viwe + "_label", "");
        edit.putString(clicked_viwe + "_packge", "");
        edit.apply();

        if(need) {
            holder.setVisibility(View.VISIBLE);
            ((TextView)holder.getChildAt(1)).setText(app.set.getString(ll.getTag() + "_label", "Приложение не выбрано"));
            holder.getChildAt(0).setOnClickListener(v -> {
                int ind = -1;
                String s = app.set.getString(v.getTag() + "_label", "");
                if (!s.isEmpty())
                    for (int z = 0; z < lables.length; z++)
                        if (lables[z].equals(s)) {
                            ind = z;
                            break;
                        }
                new AlertDialog.Builder(xPanel.this)
                        .setTitle("Выберите приложение")
                        .setSingleChoiceItems(lables, ind, (dialog, which) -> {
                            SharedPreferences.Editor editor = app.set.edit();
                            String label = lables[which].toString();
                            ((TextView)((LinearLayout)v.getParent()).getChildAt(1)).setText(label);
                            editor.putString(v.getTag() + "_label", label);
                            editor.putString(v.getTag() + "_packge", packages[which].toString());
                            editor.apply();
                            dialog.dismiss();
                        })
                        .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss())
                        .create()
                        .show();
            });
        }
    }

    private void set_description(LinearLayout ll, CharSequence description) {
        View v = ll.getChildAt(1);
        if(v instanceof LinearLayout)
            ((TextView)((LinearLayout)v).getChildAt(1)).setText(description);
        else
            ((TextView)ll.getChildAt(1)).setText(description);
    }

    private void loadApplication() {
        Intent i = new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> list = app.pm.queryIntentActivities(i, 0);
        lables = new String[list.size()];
        packages = new String[list.size()];
        int index = 0;
        for (ResolveInfo info : list) {
            lables[index] = info.loadLabel(app.pm);
            packages[index] = info.activityInfo.packageName;
            index++;
        }
    }

    private void show_dialog(int type) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Перенаправлеие в системные настройки")
                .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());
        switch (type) {
            case 0:
                alert.setMessage("");
                alert.setPositiveButton("Хорошо", (dialog, which) -> startActivity(new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, app.adminComponent)));
                break;
            case 1:
                alert.setMessage("");
                alert.setPositiveButton("Хорошо", (dialog, which) -> startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));
                break;
            case 2:
                alert.setMessage("");
                alert.setPositiveButton("Хорошо", (dialog, which) -> startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + app.name))));
                break;
            case 3:
                alert.setMessage("");
                alert.setPositiveButton("Хорошо", (dialog, which) -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
                break;
        }
        alert.create().show();
    }
}