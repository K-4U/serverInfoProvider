package k4unl.minecraft.sip.api;

import java.util.Map;

/**
 * @author Koen Beckers (K-4U)
 */
public interface ISIPEntity {
    
    /**
     * Triggered whenever info is requested from the endpoint.
     * This can be a webserver or another MC client.
     * @return a map of key-value to be sent to the requester
     */
    Map<String, Object> getSIPInfo();
    
}
