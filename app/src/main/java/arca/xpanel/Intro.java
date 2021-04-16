package arca.xpanel;

import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

import static arca.xpanel.singleton.app;


public class Intro extends AppCompatActivity implements pages.callback {
    private Resources res;
    private ArrayList<ImageView> dots = new ArrayList<>();
    private int NUM_PAGES = 3;
    private ViewPager view_pager;
    private Button b;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);

        view_pager = findViewById(R.id.pager);
        findViewById(R.id.skip).setOnClickListener(v -> fin());
        (b = findViewById(R.id.next)).setOnClickListener(v -> {
            int p = view_pager.getCurrentItem();
            if(p == NUM_PAGES - 1)
                fin();
            else
                view_pager.setCurrentItem(p + 1);
        });

        res = getResources();
        addDots();

        pager pg = new pager(getSupportFragmentManager());
        pg.add(new pages().init(this, 0, res.getString(R.string.str_admin)));
        pg.add(new pages().init(this, 1, res.getString(R.string.str_statusage)));
        pg.add(new pages().init(this, 2, res.getString(R.string.str_alert)));
        view_pager.setAdapter(pg);
        selectDot(0);
    }

    private void fin() {
        app.set.edit().putBoolean("first", false).apply();
        startActivity(new Intent(Intro.this, arca.xpanel.xPanel.class));
        finish();
    }

    public void addDots() {
        LinearLayout dotsLayout = findViewById(R.id.dots);
        LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(75, 75);
        for(int i = 0; i < NUM_PAGES; i++) {
            ImageView dot = new ImageView(this);
            dot.setImageDrawable(res.getDrawable(R.drawable.dot_not_selected));
            dotsLayout.addView(dot, lllp);
            dots.add(dot);
        }

        view_pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }
            @Override public void onPageScrollStateChanged(int state) { }
            @Override public void onPageSelected(int position) {
                selectDot(position);
            }
        });
    }

    public void selectDot(int idx) {
        for(int i = 0; i < NUM_PAGES; i++)
            dots.get(i).setImageDrawable(res.getDrawable(R.drawable.dot_not_selected));
        dots.get(idx).setImageDrawable(res.getDrawable(R.drawable.dot_selected));
        if(idx == NUM_PAGES - 1)
            b.setText("finish");
        else
            b.setText("next");
    }

    @Override
    public void button_click(int type) {
        switch (type) {
            case 0:
                startActivity(new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, app.adminComponent));
                break;
            case 1:
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                break;
            case 2:
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + app.name)));
                break;
            default:
                break;
        }
    }

    class pager extends FragmentPagerAdapter {
        ArrayList<pages> o = new ArrayList<>();

        pager(FragmentManager fm) {
            super(fm);
        }

        public void add(pages p) {
            o.add(p);
        }

        @Override
        public int getCount() {
            return o.size();
        }

        @Override
        public Fragment getItem(int position) {
            return o.get(position);
        }
    }
}
