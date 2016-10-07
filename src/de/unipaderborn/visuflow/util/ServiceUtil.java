package de.unipaderborn.visuflow.util;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceException;
import org.osgi.util.tracker.ServiceTracker;

public class ServiceUtil {

    public static <T> T getService(Class<T> serviceClass) {
        BundleContext ctx = FrameworkUtil.getBundle(ServiceUtil.class).getBundleContext();
        ServiceTracker<T, T> tracker = new ServiceTracker<>(ctx, serviceClass, null);
        tracker.open();
        T service = tracker.getService();
        tracker.close();
        if(service != null) {
            return service;
        } else {
            throw new ServiceException("Service "+serviceClass.getName()+" not available");
        }
    }
}
