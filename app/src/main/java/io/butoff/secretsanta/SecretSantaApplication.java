package io.butoff.secretsanta;

import android.app.Application;

import ru.evotor.devices.commons.DeviceServiceConnector;

public final class SecretSantaApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DeviceServiceConnector.startInitConnections(getApplicationContext());
    }
}
