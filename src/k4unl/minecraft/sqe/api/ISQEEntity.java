package k4unl.minecraft.sqe.api;

import java.util.Map;

/**
 * @author Koen Beckers (K-4U)
 */
public interface ISQEEntity {
    
    /**
     * Triggered whenever info is requested from the Server Query.
     * This can be a webserver or another MC client.
     * @return a map of key-value to be sent to the requester
     */
    Map<String, Object> getSQEInfo();
    
}
