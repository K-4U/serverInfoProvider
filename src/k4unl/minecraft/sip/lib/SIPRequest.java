package k4unl.minecraft.sip.lib;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import k4unl.minecraft.k4lib.lib.Location;
import k4unl.minecraft.sip.api.ISIPRequest;

/**
 * @author Koen Beckers (K-4U)
 */
public class SIPRequest implements ISIPRequest {
    
    private String key;
    private Object argument;
    
    public SIPRequest(String _key, Object _argument) {
        
        key = _key;
        argument = _argument;
    }
    
    public String getKey() {
        
        return key;
    }
    
    public String getArgument() {
        
        if (argument != null) {
            return argument.toString().toLowerCase();
        } else {
            return "";
        }
    }
    
    public int getIntArgument() {
        
        if (argument != null) {
            return (int) (Math.floor((Double) argument));
        } else {
            return 0;
        }
    }
    
    public Location getPosArgument() {
        
        if (argument != null) {
            Gson nGson = new Gson();
            try {
                return nGson.fromJson(getArgument(), Location.class);
            } catch (JsonSyntaxException e) {
                return null;
            }
        } else {
            return null;
        }
    }
    
    public boolean isArgumentPos() {
        
        return getPosArgument() != null;
    }
    
}
