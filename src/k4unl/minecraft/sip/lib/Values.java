package k4unl.minecraft.sip.lib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import k4unl.minecraft.k4lib.network.EnumSIPValues;
import k4unl.minecraft.sip.api.event.InfoEvent;
import k4unl.minecraft.sip.storage.Players;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;

import java.util.*;

/**
 * @author Koen Beckers (K-4U)
 */
public class Values {
    private static Date startDate = (new Date());

    
    
    private static void putInMap(Map theMap, Object key, Object value) {
        
        if (theMap.containsKey(key)) {
            //merge maps
            if(theMap.get(key) instanceof Map && value instanceof Map) {
                ((Map) theMap.get(key)).putAll((Map) value);
            }
            //Otherwise, keep old info
        }else {
            theMap.put(key, value);
        }
    }
    
    public static String writeToOutputStream(List<SIPRequest> valueList) {
        
        Map<String, Object> endMap = new HashMap<String, Object>();
        Map<String, List<Object>> infoMap = new HashMap<>();
        
        for (SIPRequest value : valueList) {
            Object ret = null;
            
            boolean doNotAddToMap = false;
            EnumSIPValues v = EnumSIPValues.fromString(value.getKey());
            
            switch (v) {
                case TIME:
                    ret = getWorldTime(value.getIntArgument());
                    doNotAddToMap = true;
                    break;
                case PLAYERS:
                    ret = getPlayers();
                    if(value.getArgument().equals("latestdeath")){
                        ret = getLatestDeaths((List<String>) ret);
                    }
                    doNotAddToMap = true;
                    break;
                case DAYNIGHT:
                    ret = getWorldDayNight(value.getIntArgument());
                    doNotAddToMap = true;
                    break;
                case DIMENSIONS:
                    ret = getDimensions();
                    doNotAddToMap = true;
                    break;
                case UPTIME:
                    ret = getUptime();
                    doNotAddToMap = true;
                    break;
                case DEATHS:
                    //Get a leaderboard of deaths, or the deaths of a player
                    if(!value.getArgument().equals("")){
                        ret = getDeathsByPlayer(value.getArgument());
                    }else{
                        ret = getDeathLeaderboard();
                    }
                    doNotAddToMap = true;
                    
                    break;
                case WEATHER:
                    ret = getWorldWeather(value.getIntArgument());
                    doNotAddToMap = true;
                    break;

            }
            
            if(ret == null){
                //If nothing has been returned on our side, that means we don't know it.
                //Thus, ask the rest of the mods:
                InfoEvent evt = new InfoEvent(value);
                MinecraftForge.EVENT_BUS.post(evt);
    
                ret = evt.getReturn();
            }
            
            
            if (doNotAddToMap) {
                putInMap(endMap, value.getKey(), ret);
            } else {
                if(!infoMap.containsKey(value.getKey())){
                    infoMap.put(value.getKey(), new ArrayList<>());
                }
                infoMap.get(value.getKey()).add(ret);
            }
        }
        
        for(Map.Entry<String, List<Object>> obj : infoMap.entrySet()){
            putInMap(endMap, obj.getKey(), obj.getValue());
        }

        GsonBuilder builder = new GsonBuilder();
        builder = builder.setPrettyPrinting();
        Gson gson = builder.create();
        String endString;
        try {
            endString = gson.toJson(endMap);
        } catch (Exception e) {
            e.printStackTrace();
            endString = "{'error': 'INVALID JSON, ERROR ON SERVER'}";
        }
        return endString;
    }
    
    private static Map<String, String> getLatestDeaths(List<String> players) {
        Map<String, String> ret = new HashMap<String, String>();
        for(String p : players){
            ret.put(p, Players.getLatestDeath(p));
        }
        return ret;
    }

    private static Map<String, Integer> getDimensions() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        for(WorldServer server : MinecraftServer.getServer().worldServers){
            map.put(server.provider.getDimensionName(), server.provider.dimensionId);
        }
        return map;
    }

    private static WorldServer getWorldServerForDimensionId(int dimensionId){
        for(WorldServer server : MinecraftServer.getServer().worldServers){
            if(server.provider.dimensionId == dimensionId){
                return server;
            }
        }
        return null;
    }

    private static List<String> getPlayers(){
        List<String> players = new ArrayList<String>();
        for(World world : MinecraftServer.getServer().worldServers){
            for(Object player : world.playerEntities){
                players.add(((EntityPlayer)player).getGameProfile().getName());
            }
        }

        return players;
    }

    private static Map<Integer, String> getWorldTime(int dimensionId){
        WorldServer w = getWorldServerForDimensionId(dimensionId);
        if(w != null) {
            int time = (int) (w.getWorldTime() % 24000);
            int hour = ((int) (time / 1000) + 6) % 24;
            int minute = (int) ((time % 1000 * 60 / 1000));

            return getMap(dimensionId, String.format("%02d", hour) + ":" + String.format("%02d", minute));
        }else{
            return getMap(dimensionId, "NaW");
        }
    }

    private static Long getUptime(){
        Date now = new Date();
        return now.getTime() - startDate.getTime();
    }

    private static boolean getWorldDayNight(int dimensionId){
        WorldServer w = getWorldServerForDimensionId(dimensionId);
        if(w != null) {
            return w.isDaytime();
        }else{
            return false;
        }
    }

    private static Map<Integer, String> getWorldWeather(int dimensionId){
        WorldServer w = getWorldServerForDimensionId(dimensionId);
        if(w != null) {
            if(w.getWorldInfo().isRaining() && w.getWorldInfo().isThundering()){
                return getMap(dimensionId, "thunder");
            }else if(w.getWorldInfo().isRaining()){
                return getMap(dimensionId, "rain");
            }else{
                return getMap(dimensionId, "clear");
            }
        }else{
            return getMap(dimensionId, "");
        }

    }

    private static Map<String, Map<String, Integer>> getDeathLeaderboard(){

        return getMap("LEADERBOARD", Players.getDeathLeaderboard());
    }

    private static Map<String, Map<String, Integer>> getDeathsByPlayer(String playerName){

        return getMap(playerName, Players.getDeaths(playerName));
    }

    private static <A, B> Map<A, B> getMap(A key, B value){
        Map<A, B> ret = new HashMap<A, B>();
        ret.put(key, value);
        return ret;
    }
}
