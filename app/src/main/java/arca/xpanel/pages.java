package arca.xpanel;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import static arca.xpanel.singleton.app;

public class pages extends Fragment {
    private int type = -1;
    private Button b;
    private callback call;
    private String text;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.temp_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        (b = view.findViewById(R.id.check)).setOnClickListener(v -> {
            if(call != null)
                call.button_click(type);
        });
        ((TextView)view.findViewById(R.id.text)).setText(text);
    }

    public pages init(callback c, int ty, String te) {
        type = ty;
        text = te;
        call = c;
        return this;
    }

    @Override
    public void onResume() {
        super.onResume();
        switch (type) {
            case 0:
                b.setText(app.chec_devadmin() ? "Получено" : "Не получено");
                break;
            case 1:
                b.setText(app.check_usageStats() ? "Получено" : "Не получено");
                break;
            case 2:
                b.setText(app.check_alert() ? "Получено" : "Не получено");
                break;
            case 3:
                b.setText(app.check_writeSet() ? "Получено" : "Не получено");
                break;
            default:
                b.setText("Потрачено...");
                break;
        }
    }

    interface callback {
        void button_click(int type);
    }
}
