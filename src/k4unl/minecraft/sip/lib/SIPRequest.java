package k4unl.minecraft.sip.lib;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import k4unl.minecraft.k4lib.lib.Location;
import k4unl.minecraft.sip.api.ISIPRequest;
import net.minecraft.util.EnumFacing;

import java.util.Map;

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
    
    public Map<String, Object> getArrayArgument() {
        if(argument != null){
            Gson nGson = new Gson();
            try {
                return nGson.fromJson(getArgument(), Map.class);
            } catch (JsonSyntaxException e) {
                return null;
            }
        }
        return null;
    }
    
    public boolean hasArrayArgument() {
        return (getArrayArgument() != null);
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
    
    public EnumFacing getSideArgument() {
        
        if (argument != null) {
            Gson nGson = new Gson();
            try {
                Map<String, Object> arg = nGson.fromJson(getArgument(), Map.class);
                if (arg.containsKey("side")) {
                    return EnumFacing.byName((String) arg.get("side"));
                } else {
                    return null;
                }
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
    
    public boolean hasArgumentSide() {
        
        return getSideArgument() != null;
    }
    
}
