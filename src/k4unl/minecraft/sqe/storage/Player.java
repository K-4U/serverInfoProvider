package k4unl.minecraft.sqe.storage;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Koen Beckers (K-4U)
 */
public class Player {
    private String name;
    private Map<String, Integer> deaths;
    private String latestDeath;
    private boolean online;
    private long lastOnline;

    public Player(String _name){
        name = _name;
        online = false;
        deaths = new HashMap<String, Integer>();
        lastOnline = 0;
    }

    public void addDeath(String cause){
        int count = 0;
        if(deaths.containsKey(cause)){
            count = deaths.get(cause);
        }
        latestDeath = cause;
        deaths.put(cause, count + 1);
    }

    public int getDeaths(String cause){
        if(cause != null){
            return deaths.get(cause);
        }
        int c = 0;
        for(Map.Entry<String, Integer> death : deaths.entrySet()){
            c += death.getValue();
        }
        return c;
    }

    public Map<String, Integer> getDeathsWithCauses(){

        return new HashMap<String, Integer>(deaths);
    }

    public String getLatestDeath() {
        return latestDeath;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline() {
        this.online = true;
        lastOnline = (new Date()).getTime();
    }

    public void setOffline() {
        this.online = false;
        lastOnline = (new Date()).getTime();
    }

    public long getLastOnline() {
        return lastOnline;
    }
}
