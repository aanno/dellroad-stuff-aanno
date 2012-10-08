package org.dellroad.stuff.vaadin7;

import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.vaadin.server.WrappedSession;

public class SessionIdUtils {
	
    private static final Logger LOG = Logger.getLogger(SessionIdUtils.class);
	
    private static final String SESSION_ATTRIBUTE = "BaseContextApplication.SESSION_ATTRIBUTE";
    
    private static final AtomicInteger SESSION_NUMBER = new AtomicInteger();

	private SessionIdUtils() {
	}
	
	public static String createOrRetrieveSessionId(HttpSession session) {
        Object so = session.getAttribute(SESSION_ATTRIBUTE);
        if (so == null) {
        	so = Integer.toString(SESSION_NUMBER.incrementAndGet());
        	session.setAttribute(SESSION_ATTRIBUTE, so);
        }
		return (String) so;
	}

	public static String retrieveSessionId(HttpSession session) {
        return (String) session.getAttribute(SESSION_ATTRIBUTE);
	}

	public static String createOrRetrieveSessionId(WrappedSession session) {
        Object so = session.getAttribute(SESSION_ATTRIBUTE);
        if (so == null) {
        	so = Integer.toString(SESSION_NUMBER.incrementAndGet());
        	session.setAttribute(SESSION_ATTRIBUTE, so);
        }
		return (String) so;
	}

	public static String retrieveSessionId(WrappedSession session) {
		try {
			return (String) session.getAttribute(SESSION_ATTRIBUTE);
		}
		catch (IllegalStateException e) {
			LOG.warn("No sessionId found: " + e);
			return null;
		}
	}

}
