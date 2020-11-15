package org.getcarebase.carebase.utils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

/**
 * A MediatorLiveData decorator that manages single Resource events. Once a source updates with a
 * success or an error Resource the value is set and the source is removed.
 */
public class SingleEventMediatorLiveData<T> {
    MediatorLiveData<Resource<T>> mediatorLiveData = new MediatorLiveData<>();

    /**
     * Adds a new source that will replace current value of mediatorLiveData with result of
     * new source. The source is removed as a source once it is updated its is a single event.
     * @param source LiveData object to replace current value
     */
    public void addSource(final LiveData<Resource<T>> source) {
       mediatorLiveData.addSource(source, new Observer<Resource<T>>() {
           @Override
           public void onChanged(Resource<T> tResource) {
               if (tResource.getRequest().getStatus() == Request.Status.SUCCESS || tResource.getRequest().getStatus() == Request.Status.ERROR) {
                   mediatorLiveData.setValue(tResource);
                   mediatorLiveData.removeSource(source);
               }
           }
       });
    }

    public LiveData<Resource<T>> getLiveData() {
        return mediatorLiveData;
    }
}
