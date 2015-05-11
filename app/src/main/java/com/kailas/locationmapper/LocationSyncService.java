package com.kailas.locationmapper;

/**
 * Created by kailasl on 5/10/2015.
 */

/**
 * You now have your data transfer code encapsulated in a sync adapter component, but you have to provide the framework with access to your code. To do this, you need to create a bound Service that passes a special Android binder object from the sync adapter component to the framework. With this binder object, the framework can invoke the onPerformSync() method and pass data to it.

 Instantiate your sync adapter component as a singleton in the onCreate() method of the service. By instantiating the component in onCreate(), you defer creating it until the service starts, which happens when the framework first tries to run your data transfer. You need to instantiate the component in a thread-safe manner, in case the sync adapter framework queues up multiple executions of your sync adapter in response to triggers or scheduling.
 */

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Define a Service that returns an IBinder for the
 * sync adapter class, allowing the sync adapter framework to call
 * onPerformSync().
 */
public class LocationSyncService extends Service {

    // Storage for an instance of the sync adapter
    private static LocationSyncAdapter sSyncAdapter = null;
    // Object to use as a thread-safe lock
    private static final Object sSyncAdapterLock = new Object();
    /*
     * Instantiate the sync adapter object.
     */
    @Override
    public void onCreate() {
        /*
         * Create the sync adapter as a singleton.
         * Set the sync adapter as syncable
         * Disallow parallel syncs
         */
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new LocationSyncAdapter(getApplicationContext(), true);
            }
        }
    }
    /**
     * Return an object that allows the system to invoke
     * the sync adapter.
     *
     */
    @Override
    public IBinder onBind(Intent intent) {
        /*
         * Get the object that allows external processes
         * to call onPerformSync(). The object is created
         * in the base class code when the SyncAdapter
         * constructors call super()
         */
        return sSyncAdapter.getSyncAdapterBinder();
    }
}
