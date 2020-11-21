package com.example.sensair;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class BluetoothRestarter
{

    private BroadcastReceiver mReceiver;

    public BluetoothRestarter()
    {
    }

    public void restart()
    {
        if (mReceiver == null)
        {
            mReceiver = new BroadcastReceiver()
            {
                @Override
                public void onReceive(Context context, Intent intent)
                {
                    String action = intent.getAction();
                    if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED))
                    {
                        final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                                BluetoothAdapter.ERROR);
                        switch (state)
                        {
                            case BluetoothAdapter.STATE_OFF:
                                BluetoothAdapter.getDefaultAdapter().enable();
                                break;
                            case BluetoothAdapter.STATE_ON:
                                break;
                        }
                    }
                    context.unregisterReceiver(this);
                }
            };
        }
    }

}
