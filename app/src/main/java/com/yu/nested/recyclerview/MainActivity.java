package com.yu.nested.recyclerview;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import com.yu.lib.common.ui.HostActivityKt;
import com.yu.nested.recyclerview.demo.AdTabFragment;
import com.yu.nested.recyclerview.demo.NormalFragment;
import com.yu.nested.recyclerview.demo.OutTabFragment;
import com.yu.nested.recyclerview.demo.PullRefreshFragment;

//理论上也可以实现 RecyclerView嵌套RecyclerView，但是感觉没什么意义就不写demo了
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final CheckBox checkBox = findViewById(R.id.checkBox);

        findViewById(R.id.normal).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("cache", checkBox.isChecked());
                HostActivityKt.startFragment(v.getContext(), NormalFragment.class, bundle);
            }
        });
        findViewById(R.id.pull).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HostActivityKt.startFragment(v.getContext(), PullRefreshFragment.class);
            }
        });
        findViewById(R.id.ad).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HostActivityKt.startFragment(v.getContext(), AdTabFragment.class);
            }
        });
        findViewById(R.id.outTab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HostActivityKt.startFragment(v.getContext(), OutTabFragment.class);
            }
        });
    }
}
