package k4unl.minecraft.sip.lib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import k4unl.minecraft.k4lib.lib.Functions;
import k4unl.minecraft.k4lib.lib.Location;
import k4unl.minecraft.k4lib.network.EnumSIPValues;
import k4unl.minecraft.sip.api.ISIPEntity;
import k4unl.minecraft.sip.storage.Players;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.rcon.RConOutputStream;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.*;

/**
 * @author Koen Beckers (K-4U)
 */
public class Values {
    
    private static Date startDate = (new Date());
    
    public static class ValuePair {
        
        private EnumSIPValues value;
        private Object        argument;
        private String        invalid;
        
        public ValuePair(EnumSIPValues value_, Object argument_) {
            
            value = value_;
            argument = argument_;
        }
        
        public ValuePair(EnumSIPValues value_, String invalid_) {
            
            value = value_;
            invalid = invalid_;
        }
        
        public EnumSIPValues getValue() {
            
            return value;
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
        
        
        public String getInvalid() {
            
            return invalid;
        }
    }
    
    private static void putInMap(Map theMap, Object key, Object value) {
        
        if (theMap.containsKey(key)) {
            //merge maps
            if (theMap.get(key) instanceof Map && value instanceof Map) {
                ((Map) theMap.get(key)).putAll((Map) value);
            }
            //Otherwise, keep old info
        } else {
            theMap.put(key, value);
        }
    }
    
    public static void writeToOutputStream(RConOutputStream outputStream, List<ValuePair> valueList) {
        
        Map<String, Object> endMap = new HashMap<String, Object>();
        List<Object> blockInfoMap = new ArrayList<>();
        List<Object> RFInfoMap = new ArrayList<>();
        List<Object> fluidInfoMap = new ArrayList<>();
        
        for (ValuePair value : valueList) {
            Object ret = null;
            switch (value.getValue()) {
                case TIME:
                    ret = getWorldTime(value.getIntArgument());
                    break;
                case PLAYERS:
                    ret = getPlayers();
                    if (value.getArgument().equals("latestdeath")) {
                        ret = getLatestDeaths((List<String>) ret);
                    }
                    break;
                case DAYNIGHT:
                    ret = getWorldDayNight(value.getIntArgument());
                    break;
                case DIMENSIONS:
                    ret = getDimensions();
                    break;
                case UPTIME:
                    ret = getUptime();
                    break;
                case INVALID:
                    ret = null;
                    break;
                case MISFORMED:
                    ret = "MISFORMED JSON";
                    break;
                case DEATHS:
                    //Get a leaderboard of deaths, or the deaths of a player
                    if (!value.getArgument().equals("")) {
                        ret = getDeathsByPlayer(value.getArgument());
                    } else {
                        ret = getDeathLeaderboard();
                    }
                    
                    break;
                case WEATHER:
                    ret = getWorldWeather(value.getIntArgument());
                    break;
                
                case BLOCKINFO:
                    if (value.isArgumentPos()) {
                        ret = getBlockInfo(value.getPosArgument());
                    } else {
                        ret = "No position argument";
                    }
                    break;
                
                case RF:
                    if (value.isArgumentPos()) {
                        ret = getRFInfo(value.getPosArgument());
                    } else {
                        ret = "No position argument";
                    }
                    break;
                
                case FLUID:
                    if (value.isArgumentPos() && value.hasArgumentSide()) {
                        ret = getFluidInfo(value.getPosArgument(), value.getSideArgument());
                    } else if (!value.isArgumentPos()) {
                        ret = "No position argument";
                    } else {
                        ret = "No side argument";
                    }
                    break;
                
                case INVENTORY:
                    if (value.isArgumentPos() && value.hasArgumentSide()){
                        ret = getInventoryInfo(value.getPosArgument(), value.getSideArgument());
                    } else if (!value.isArgumentPos()) {
                        ret = "No position argument";
                    } else {
                        ret = "No side argument";
                    }
            }
            if (value.getValue().equals(EnumSIPValues.BLOCKINFO)) {
                blockInfoMap.add(ret);
            } else if (value.getValue().equals(EnumSIPValues.FLUID)) {
                fluidInfoMap.add(ret);
            } else if (value.getValue().equals(EnumSIPValues.RF)) {
                RFInfoMap.add(ret);
            } else {
                putInMap(endMap, value.getValue().toString(), ret);
            }
        }
        
        
        if (blockInfoMap.size() > 0) putInMap(endMap, EnumSIPValues.BLOCKINFO.toString(), blockInfoMap);
        if (fluidInfoMap.size() > 0) putInMap(endMap, EnumSIPValues.FLUID.toString(), fluidInfoMap);
        if (RFInfoMap.size() > 0) putInMap(endMap, EnumSIPValues.RF.toString(), RFInfoMap);
        
        GsonBuilder builder = new GsonBuilder();
        builder = builder.setPrettyPrinting();
        Gson gson = builder.create();
        try {
            String endString = gson.toJson(endMap);
            
            outputStream.writeString(endString);
        } catch (Exception e){
            e.printStackTrace();
        }
        
    }
    
    private static Map<String, Object> getInventoryInfo(Location loc, EnumFacing side) {
    
        //Return a single Key-Value pair of strings.
        Map<String, Object> ret = new HashMap<>();
        IBlockState state = loc.getBlockState(getWorldServerForDimensionId(loc.getDimension()));
        ret.put("unlocalized-name", state.getBlock().getUnlocalizedName());
        ret.put("coords", loc);
    
        
        TileEntity tileEntity = loc.getTE(getWorldServerForDimensionId(loc.getDimension()));
        if (tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)) {
            IItemHandler cap = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
            int maxSlots = cap.getSlots();
            
            List<Map<String, Object>> items = new ArrayList<>();
            for(int i = 0; i < maxSlots; i++){
                Map<String, Object> itemMap = new HashMap<>();
                ItemStack itemStack = cap.getStackInSlot(i);
                if(itemStack != null) {
                    itemMap.put("unlocalized-name", itemStack.getUnlocalizedName());
                    itemMap.put("stacksize", itemStack.stackSize);
                    itemMap.put("metadata", itemStack.getMetadata());
                    itemMap.put("damage", itemStack.getItemDamage());
                    itemMap.put("maxdamage", itemStack.getMaxDamage());
                    itemMap.put("enchantments", itemStack.getEnchantmentTagList());
                }else{
                    itemMap.put("unlocalized-name", "empty");
                }
                items.add(i, itemMap);
            }
            ret.put("items", items);
        } else {
            ret.put("error", "No inventory at these coordinates");
        }
    
        return ret;
    }
    
    private static <T extends Comparable<T>> Map<String, Object> getBlockInfo(Location loc) {
        //Return a single Key-Value pair of strings.
        Map<String, Object> ret = new HashMap<>();
        TileEntity tileEntity = loc.getTE(getWorldServerForDimensionId(loc.getDimension()));
        if (tileEntity instanceof ISIPEntity) {
            Map<String, Object> functionRet = ((ISIPEntity) tileEntity).getSIPInfo();
            if (functionRet != null) {
                //Parse this to json, just to make sure it's possible.
                GsonBuilder builder = new GsonBuilder();
                builder = builder.setPrettyPrinting();
                Gson gson = builder.create();
                try {
                    String testString = gson.toJson(functionRet);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    ret.putAll(functionRet);
                }
            }
        }
        
        IBlockState state = loc.getBlockState(getWorldServerForDimensionId(loc.getDimension()));
        ret.put("unlocalized-name", state.getBlock().getUnlocalizedName());
        ret.put("coords", loc);
        Map<String, Map<String, Object>> properties = new HashMap<>();
        for (Map.Entry<IProperty<?>, Comparable<?>> entry : state.getProperties().entrySet()) {
            Map<String, Object> propertyData = new HashMap<>();
            
            IProperty<T> iproperty = (IProperty) entry.getKey();
            T t = (T) entry.getValue();
            String s = iproperty.getName(t);
            
            String type = "";
            List<String> possibleValues = new ArrayList<>();
            if (iproperty instanceof PropertyDirection) {
                type = "direction";
            } else if (iproperty instanceof PropertyEnum) {
                type = "enum";
            } else if (iproperty instanceof PropertyBool) {
                type = "bool";
            } else if (iproperty instanceof PropertyInteger) {
                type = "int";
                
            }
            
            propertyData.put("type", type);
            propertyData.put("allowedValues", iproperty.getAllowedValues());
            propertyData.put("value", t);
            
            properties.put(iproperty.getName(), propertyData);
        }
        
        ret.put("state", properties);
        
        return ret;
    }
    
    private static Map<String, Object> getRFInfo(Location loc) {
        
        return null;
    }
    
    private static Map<String, Object> getFluidInfo(Location loc, EnumFacing side) {
        //Return a single Key-Value pair of strings.
        Map<String, Object> ret = new HashMap<>();
        IBlockState state = loc.getBlockState(getWorldServerForDimensionId(loc.getDimension()));
        ret.put("unlocalized-name", state.getBlock().getUnlocalizedName());
        ret.put("coords", loc);
        
        //TODO: Figure out why this gives an NPE
        TileEntity tileEntity = loc.getTE(getWorldServerForDimensionId(loc.getDimension()));
        if (tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side)) {
            IFluidHandler cap = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
            
            IFluidTankProperties tankProperties = cap.getTankProperties()[0];
            if (tankProperties != null) {
                FluidStack fluid = tankProperties.getContents();
                if (fluid != null) {
                    ret.put("level", fluid.amount);
                    ret.put("fluid", fluid.getUnlocalizedName());
                } else {
                    ret.put("level", 0);
                    ret.put("fluid", "none");
                }
                ret.put("capacity", tankProperties.getCapacity());
            } else {
                ret.put("error", "No tank properties found");
            }
        } else {
            ret.put("error", "No fluid handler at these coordinates");
        }
        
        return ret;
    }
    
    
    private static Map<String, String> getLatestDeaths(List<String> players) {
        
        Map<String, String> ret = new HashMap<String, String>();
        for (String p : players) {
            ret.put(p, Players.getLatestDeath(p));
        }
        return ret;
    }
    
    private static Map<String, Integer> getDimensions() {
        
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (WorldServer server : Functions.getServer().worldServers) {
            //First argument is dimension name.
            map.put(server.provider.getDimensionType().getName(), server.provider.getDimension());
        }
        return map;
    }
    
    private static WorldServer getWorldServerForDimensionId(int dimensionId) {
        
        for (WorldServer server : Functions.getServer().worldServers) {
            if (server.provider.getDimension() == dimensionId) {
                return server;
            }
        }
        return null;
    }
    
    private static List<String> getPlayers() {
        
        List<String> players = new ArrayList<String>();
        for (World world : Functions.getServer().worldServers) {
            for (Object player : world.playerEntities) {
                players.add(((EntityPlayer) player).getGameProfile().getName());
            }
        }
        
        return players;
    }
    
    private static Map<Integer, String> getWorldTime(int dimensionId) {
        
        WorldServer w = getWorldServerForDimensionId(dimensionId);
        if (w != null) {
            int time = (int) (w.getWorldTime() % 24000);
            int hour = ((int) (time / 1000) + 6) % 24;
            int minute = (int) ((time % 1000 * 60 / 1000));
            
            return getMap(dimensionId, String.format("%02d", hour) + ":" + String.format("%02d", minute));
        } else {
            return getMap(dimensionId, "NaW");
        }
    }
    
    private static Long getUptime() {
        
        Date now = new Date();
        return now.getTime() - startDate.getTime();
    }
    
    private static boolean getWorldDayNight(int dimensionId) {
        
        WorldServer w = getWorldServerForDimensionId(dimensionId);
        if (w != null) {
            return w.isDaytime();
        } else {
            return false;
        }
    }
    
    private static Map<Integer, String> getWorldWeather(int dimensionId) {
        
        WorldServer w = getWorldServerForDimensionId(dimensionId);
        if (w != null) {
            if (w.getWorldInfo().isRaining() && w.getWorldInfo().isThundering()) {
                return getMap(dimensionId, "thunder");
            } else if (w.getWorldInfo().isRaining()) {
                return getMap(dimensionId, "rain");
            } else {
                return getMap(dimensionId, "clear");
            }
        } else {
            return getMap(dimensionId, "");
        }
        
    }
    
    private static Map<String, Map<String, Integer>> getDeathLeaderboard() {
        
        return getMap("LEADERBOARD", Players.getDeathLeaderboard());
    }
    
    private static Map<String, Map<String, Integer>> getDeathsByPlayer(String playerName) {
        
        return getMap(playerName, Players.getDeaths(playerName));
    }
    
    private static <A, B> Map<A, B> getMap(A key, B value) {
        
        Map<A, B> ret = new HashMap<A, B>();
        ret.put(key, value);
        return ret;
    }
}
