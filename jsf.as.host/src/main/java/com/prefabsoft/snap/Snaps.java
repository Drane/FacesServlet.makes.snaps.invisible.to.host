package com.prefabsoft.snap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.eclipse.gemini.web.core.WebContainer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ManagedBean
@SessionScoped
public class Snaps {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static final String SNAP_SERVICE_CLASS = "org.eclipse.virgo.snaps.core.internal.Snap";
	public static final String SNAPS_ATTRIBUTE_NAME = "snaps";
	private volatile String attributeName = SNAPS_ATTRIBUTE_NAME;

	private List<Snap> snapList;
	

	public List<Snap> getSnapList() {
		return snapList;
	}

	public void setSnapList(List<Snap> snapList) {
		this.snapList = snapList;
	}

	public Snaps() {

		snapList = new ArrayList<Snap>();

		ServletContext servletContext = (ServletContext) FacesContext
				.getCurrentInstance().getExternalContext().getContext();
		
		BundleContext bundleContext = (BundleContext) servletContext.getAttribute(WebContainer.ATTRIBUTE_BUNDLE_CONTEXT);
		

		long hostId = bundleContext.getBundle().getBundleId();

		try {
			ServiceReference<?>[] serviceReferences = bundleContext
					.getServiceReferences(SNAP_SERVICE_CLASS, "(snap.host.id="
							+ hostId + ")");

			if (serviceReferences != null) {
				Arrays.sort(serviceReferences);
				for (ServiceReference<?> serviceReference : serviceReferences) {
					logger.info(
							"serviceReference '{}' found creating snap for it...",
							serviceReference);
					snapList.add(createSnap(serviceReference));
				}
			}
		} catch (InvalidSyntaxException ise) {
			// throw new
			// Exception("Unexpected InvalidSyntaxException when querying service registry for Snaps",
			// ise);\
			logger.error("InvalidSyntaxException: '{}'", ise);
			ise.printStackTrace();
		}
	}

	private static Snap createSnap(ServiceReference<?> serviceReference) {
		String[] propertyKeys = serviceReference.getPropertyKeys();
		Map<String, Object> attributes = new HashMap<String, Object>();
		for (String key : propertyKeys) {
			attributes.put(key, serviceReference.getProperty(key));
		}
		return new Snap(attributes);
	}

}
