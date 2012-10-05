package org.dellroad.stuff.vaadin7;

import javax.servlet.http.HttpServletRequest;

import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServiceSession;
import com.vaadin.shared.ui.ui.UIConstants;
import com.vaadin.ui.UI;

public class VaadinUtils {
	
	private VaadinUtils() {
	}
	
    public static UI getUI(HttpServletRequest request) {
    	// Get the VaadinSession
    	final VaadinServiceSession session = (VaadinServiceSession) 
    			request.getAttribute(VaadinServiceSession.class.getName());
    	final Integer uiId = getUIId(request);
    	if (uiId == null) {
    		return null;
    	}
        return session.getUIById(uiId);
    }
    
    public static UI getUI(HttpServletRequest request, int uiId) {
    	// Get the VaadinSession
    	final VaadinServiceSession session = (VaadinServiceSession) 
    			request.getAttribute(VaadinServiceSession.class.getName());
        return session.getUIById(uiId);
    }
    
    public static Integer getUIId(HttpServletRequest request) {
    	if (request == null) {
    		return null;
    	}
    	// Get the VaadinSession
    	final VaadinServiceSession session = (VaadinServiceSession) 
    			request.getAttribute(VaadinServiceSession.class.getName());
    	if (session == null) {
    		return null;
    	}
        // Get UI id from the request
        final String uiIdString = request.getParameter(UIConstants.UI_ID_PARAMETER);
        if (uiIdString == null) {
        	return null;
        }
        final int uiId = Integer.parseInt(uiIdString);
        return uiId;
    }
    
    public static UI getUI(VaadinRequest request) {
    	// Get the VaadinSession
    	final VaadinServiceSession session = (VaadinServiceSession) 
    			request.getAttribute(VaadinServiceSession.class.getName());
    	final Integer uiId = getUIId(request);
    	if (uiId == null) {
    		return null;
    	}
        return session.getUIById(uiId);
    }
    
    public static Integer getUIId(VaadinRequest request) {
    	if (request == null) {
    		return null;
    	}
    	// Get the VaadinSession
    	final VaadinServiceSession session = (VaadinServiceSession) 
    			request.getAttribute(VaadinServiceSession.class.getName());
    	if (session == null) {
    		return null;
    	}
        // Get UI id from the request
        final String uiIdString = request.getParameter(UIConstants.UI_ID_PARAMETER);
        if (uiIdString == null) {
        	return null;
        }
        final int uiId = Integer.parseInt(uiIdString);
        return uiId;
    }	

}
