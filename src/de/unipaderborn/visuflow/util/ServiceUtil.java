package de.unipaderborn.visuflow.util;

import java.util.Dictionary;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceException;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Helper class to retrieve and register OSGi services.
 *
 * @author henni@upb.de
 *
 */
public class ServiceUtil {

    /**
     * @see {@link ServiceUtil#getService(Class, long)}
     */
    public static <T> T getService(Class<T> serviceClass) {
        return getService(serviceClass, TimeUnit.SECONDS.toMillis(60));
    }

    /**
     * Looks up an OSGi service. If the service is not yet available, this method will wait for 60 seconds for the service to become available. If the service
     * does not appear in this period, a ServiceException is thrown.
     *
     * @param serviceClass
     *            The service interface of the service to look up
     * @param timeoutInMillis
     *            The amount of time in milliseconds to wait for the service to become available
     * @return an implementation of the given service interface
     * @throws a
     *             ServiceException, if the service couldn't be found in the OSGi service registry
     */
    public static <T> T getService(Class<T> serviceClass, long timeoutInMillis) {
        BundleContext ctx = FrameworkUtil.getBundle(ServiceUtil.class).getBundleContext();
        ServiceTracker<T, T> tracker = new ServiceTracker<>(ctx, serviceClass, null);
        tracker.open();
        T service = null;
        try {
            service = tracker.waitForService(timeoutInMillis);
        } catch (InterruptedException e) {
            throw new ServiceException("Interrupted while waiting for the service " + serviceClass.getName(), e);
        }

        tracker.close();
        if (service != null) {
            return service;
        } else {
            throw new ServiceException("Service " + serviceClass.getName() + " not available");
        }
    }

    /**
     * Registers a service implementation at the OSGi service registry.
     *
     * @param serviceClass
     *            The interface class of the service
     * @param service
     *            The service implementation
     * @param properties
     *            Additional properties assigned to this implementation
     * @see BundleContext#registerService(Class, Object, Dictionary)
     */
    public static <T> void registerService(Class<T> serviceClass, T service, Dictionary<String, ?> properties) {
        BundleContext ctx = FrameworkUtil.getBundle(ServiceUtil.class).getBundleContext();
        ctx.registerService(serviceClass, service, properties);
    }
}
