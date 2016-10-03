package k4unl.minecraft.sip.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.common.DimensionManager;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Koen Beckers (K-4U)
 */
public class Players {
    private static Map<String, Player> playerList = new HashMap<String, Player>();

    private static Player getOrCreate(String playerName){
        if(playerList.containsKey(playerName)){
            return playerList.get(playerName);
        }
        Player newPlayer = new Player(playerName);
        playerList.put(playerName, newPlayer);
        return newPlayer;
    }

    public static void playerLoggedIn(String playerName){
        getOrCreate(playerName).setOnline();
    }

    public static void playerLoggedOff(String playerName){
        getOrCreate(playerName).setOffline();
    }

    public static void addDeath(String playerName, String damageType) {
        getOrCreate(playerName).addDeath(damageType);
    }

    public static Map<String, Integer> getDeaths(String playerName) {
        return getOrCreate(playerName).getDeathsWithCauses();
    }

    public static Map<String, Integer> getDeathLeaderboard(){
        Map<String, Integer> ret = new HashMap<String, Integer>();
        for(Map.Entry<String, Player> playerEntry : playerList.entrySet()){
            int deaths = playerEntry.getValue().getDeaths(null);
            if(deaths != 0){
                ret.put(playerEntry.getKey(), deaths);
            }
        }
        return ret;
    }

    public static String getLatestDeath(String playerName) {
        return getOrCreate(playerName).getLatestDeath();
    }


    public static void savePlayers(){
        GsonBuilder builder = new GsonBuilder();
        builder = builder.setPrettyPrinting();
        Gson gson = builder.create();
        String endString = gson.toJson(playerList);

        String p = DimensionManager.getCurrentSaveRootDirectory().getAbsolutePath();
        p += "/sip/players.json";
        File f = new File(p);
        if(!f.exists()){
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            PrintWriter opStream = new PrintWriter(f);
            opStream.write(endString);
            opStream.flush();
            opStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static void loadPlayers(){
        playerList.clear();


        Gson gson = new Gson();
        String p = DimensionManager.getCurrentSaveRootDirectory().getAbsolutePath();

        File d = new File(p + "/sip/");
        if(!d.exists()){
            d.mkdir();
        }
        p += "/sip/players.json";
        File f = new File(p);
        if(!f.exists()){
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            FileInputStream ipStream = new FileInputStream(f);
            InputStreamReader reader = new InputStreamReader(ipStream);
            BufferedReader bReader = new BufferedReader(reader);
            String json = "";
            String thisLine = "";
            while((thisLine = bReader.readLine()) != null) {
                json += thisLine;
            }
            reader.close();
            ipStream.close();
            bReader.close();

            Type myTypeMap = new TypeToken<Map<String, Player>>(){}.getType();
            playerList = gson.fromJson(json, myTypeMap);
            if(playerList== null){
                playerList = new HashMap<String, Player>();
            }

            //Log.info("Read from file: " + json);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
