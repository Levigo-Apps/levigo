package com.levigo.levigoapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MyFirebaseMessagingService extends Service {
    public MyFirebaseMessagingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
