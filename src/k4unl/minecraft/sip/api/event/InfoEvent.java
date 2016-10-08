package k4unl.minecraft.sip.api.event;

import k4unl.minecraft.sip.api.ISIPRequest;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Koen Beckers (K-4U)
 */
public class InfoEvent extends Event {
    
    private final ISIPRequest         request;
    private       Map<String, Object> toSend;
    
    public InfoEvent(ISIPRequest _request) {
        
        this.request = _request;
        toSend = new HashMap<>();
    }
    
    /**
     * Adds info to return to the sender
     * Will translate into json.
     *
     * @param key
     * @param object
     */
    public void addInfo(String key, Object object) {
        
        toSend.put(key, object);
    }
    
    public String getKey() {
        
        return request.getKey();
    }
    
    public ISIPRequest getRequest(){
        return request;
    }
    
    /**
     * Only used internally. do NOT call yourself, unless you want to know what is returned
     * @return
     */
    public Map<String, Object> getReturn() {
        
        return toSend;
    }
}
