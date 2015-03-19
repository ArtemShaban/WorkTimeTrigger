package com.shaban.worktimetrigger;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.shaban.worktimetrigger.service.TriggerLocationService;
import com.shaban.worktimetrigger.util.DateUtils;

public class StartActivity extends Activity
{
    private String TAG = "@@@";
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button startTrigger = (Button) findViewById(R.id.start_trigger);
        startTrigger.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent service = new Intent(StartActivity.this, TriggerLocationService.class);
                startService(service);
            }
        });

        TextView info = (TextView) findViewById(R.id.info);
        SharedPreferences sharedPreferences = getSharedPreferences(TriggerLocationService.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        long duration = sharedPreferences.getLong(DateUtils.getTodayTimestamp() + TriggerLocationService.DAY_DURATION_SUFFIX, -1);
        info.setText("Today duration = " + duration / 60 / 1000 + " min");
    }
}
