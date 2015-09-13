package k4unl.minecraft.sqe.lib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import k4unl.minecraft.k4lib.lib.Functions;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.rcon.RConOutputStream;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Koen Beckers (K-4U)
 */
public class Values {

    public static class ValuePair {
        private EnumValues value;
        private int argument;

        public ValuePair(EnumValues value_, int argument_){
            value = value_;
            argument = argument_;
        }

        public EnumValues getValue() {
            return value;
        }

        public int getArgument() {
            return argument;
        }
    }

    public static void writeToOutputStream(RConOutputStream outputStream, List<ValuePair> valueList){
        Map<String, Object> endMap = new HashMap<String, Object>();

        for(ValuePair value : valueList) {
            Object ret = null;
            switch (value.getValue()) {
                case TIME:
                    ret = getWorldTime(value.getArgument());
                    break;
                case PLAYERS:
                    ret = getPlayers();
                    break;
                case DAYNIGHT:
                    ret = MinecraftServer.getServer().getEntityWorld().isDaytime();
                    break;
                case DIMENSIONS:
                    ret = getDimensions();
                    break;
                case INVALID:
                    ret = null;
                    break;
            }
            endMap.put(value.getValue().toString(), ret);
        }

        GsonBuilder builder = new GsonBuilder();
        builder = builder.setPrettyPrinting();
        Gson gson = builder.create();
        String endString = gson.toJson(endMap);

        Log.debug(endString);

        try {
            outputStream.writeString(endString);
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    private static List<EntityPlayer> getPlayers(){
        List<EntityPlayer> players = new ArrayList<EntityPlayer>();
        for(World world : MinecraftServer.getServer().worldServers){
            players = Functions.mergeList(world.playerEntities, players);
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

    private static <A, B> Map<A, B> getMap(A key, B value){
        Map<A, B> ret = new HashMap<A, B>();
        ret.put(key, value);
        return ret;
    }
}
