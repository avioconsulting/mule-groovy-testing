package com.avioconsulting.mule.testing.mulereplacements;

import com.mulesoft.mule.runtime.module.batch.api.notification.BatchNotificationListener;
import org.mule.runtime.api.notification.CustomNotification;

import java.util.ArrayList;
import java.util.List;

public class OurBatchNotifyListener implements BatchNotificationListener {
    private final List<BatchNotificationListener> listeners;

    public OurBatchNotifyListener() {
        this.listeners = new ArrayList<>();
    }

    @Override
    public void onNotification(CustomNotification customNotification) {
        for (BatchNotificationListener listener : listeners) {
            listener.onNotification(customNotification);
        }
    }

    public synchronized void addListener(BatchNotificationListener listener) {
        this.listeners.add(listener);
    }

    public synchronized void removeListener(BatchNotificationListener listener) {
        this.listeners.remove(listener);
    }
}
