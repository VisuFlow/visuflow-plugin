package de.unipaderborn.visuflow.debug;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * This is a wrapper for IMarker, which does not provide any additional functionality.
 * It's only purpose is, to mark breakpoints set by Visuflow, so that we know, which
 * breakpoints have been set by us and which have been set by the user.
 *
 * @author henni@upb.de
 *
 */
public class VisuflowMarkerWrapper implements IMarker {
    private IMarker delegate;

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        return delegate.getAdapter(adapter);
    }

    @Override
    public void delete() throws CoreException {
        delegate.delete();
    }

    @Override
    public boolean equals(Object object) {
        return delegate.equals(object);
    }

    @Override
    public boolean exists() {
        return delegate.exists();
    }

    @Override
    public Object getAttribute(String attributeName) throws CoreException {
        return delegate.getAttribute(attributeName);
    }

    @Override
    public int getAttribute(String attributeName, int defaultValue) {
        return delegate.getAttribute(attributeName, defaultValue);
    }

    @Override
    public String getAttribute(String attributeName, String defaultValue) {
        return delegate.getAttribute(attributeName, defaultValue);
    }

    @Override
    public boolean getAttribute(String attributeName, boolean defaultValue) {
        return delegate.getAttribute(attributeName, defaultValue);
    }

    @Override
    public Map<String, Object> getAttributes() throws CoreException {
        return delegate.getAttributes();
    }

    @Override
    public Object[] getAttributes(String[] attributeNames) throws CoreException {
        return delegate.getAttributes(attributeNames);
    }

    @Override
    public long getCreationTime() throws CoreException {
        return delegate.getCreationTime();
    }

    @Override
    public long getId() {
        return delegate.getId();
    }

    @Override
    public IResource getResource() {
        return delegate.getResource();
    }

    @Override
    public String getType() throws CoreException {
        return delegate.getType();
    }

    @Override
    public boolean isSubtypeOf(String superType) throws CoreException {
        return delegate.isSubtypeOf(superType);
    }

    @Override
    public void setAttribute(String attributeName, int value) throws CoreException {
        delegate.setAttribute(attributeName, value);
    }

    @Override
    public void setAttribute(String attributeName, Object value) throws CoreException {
        delegate.setAttribute(attributeName, value);
    }

    @Override
    public void setAttribute(String attributeName, boolean value) throws CoreException {
        delegate.setAttribute(attributeName, value);
    }

    @Override
    public void setAttributes(String[] attributeNames, Object[] values) throws CoreException {
        delegate.setAttributes(attributeNames, values);
    }

    @Override
    public void setAttributes(Map<String, ? extends Object> attributes) throws CoreException {
        delegate.setAttributes(attributes);
    }

    public VisuflowMarkerWrapper(IMarker delegate) {
        this.delegate = delegate;
    }


}
