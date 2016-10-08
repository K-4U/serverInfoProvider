package k4unl.minecraft.sip.lib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import k4unl.minecraft.k4lib.lib.Functions;
import k4unl.minecraft.k4lib.lib.Location;
import k4unl.minecraft.k4lib.network.EnumSIPValues;
import k4unl.minecraft.sip.api.ISIPEntity;
import k4unl.minecraft.sip.api.event.InfoEvent;
import k4unl.minecraft.sip.storage.Players;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.*;

import static k4unl.minecraft.k4lib.lib.Functions.getWorldServerForDimensionId;

/**
 * @author Koen Beckers (K-4U)
 */
public class Values {
    
    private static Date startDate = (new Date());
    
    
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
                    if (value.getArgument().equals("latestdeath")) {
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
                    if (!value.getArgument().equals("")) {
                        ret = getDeathsByPlayer(value.getArgument());
                    } else {
                        ret = getDeathLeaderboard();
                    }
                    doNotAddToMap = true;
                    
                    break;
                case WEATHER:
                    ret = getWorldWeather(value.getIntArgument());
                    doNotAddToMap = true;
                    break;
                
                case BLOCKINFO:
                    if (value.isArgumentPos()) {
                        ret = getBlockInfo(value.getPosArgument());
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
                    if (value.isArgumentPos() && value.hasArgumentSide()) {
                        ret = getInventoryInfo(value.getPosArgument(), value.getSideArgument());
                    } else if (!value.isArgumentPos()) {
                        ret = "No position argument";
                    } else {
                        ret = "No side argument";
                    }
                    break;
                case INVALID:
                    break;
            }
            
            if (ret == null) {
                //If nothing has been returned on our side, that means we don't know it.
                //Thus, ask the rest of the mods:
                InfoEvent evt = new InfoEvent(value);
                if (value.isArgumentPos()) {
                    IBlockState state = value.getPosArgument().getBlockState(Functions.getWorldServerForDimensionId(value.getPosArgument().getDimension()));
                    evt.addInfo("unlocalized-name", state.getBlock().getUnlocalizedName());
                    evt.addInfo("localized-name", I18n.translateToLocal(state.getBlock().getLocalizedName()));
                    evt.addInfo("coords", value.getPosArgument());
                }
                MinecraftForge.EVENT_BUS.post(evt);
                
                ret = evt.getReturn();
            }
            
            
            if (doNotAddToMap) {
                putInMap(endMap, value.getKey(), ret);
            } else {
                if (!infoMap.containsKey(value.getKey())) {
                    infoMap.put(value.getKey(), new ArrayList<>());
                }
                infoMap.get(value.getKey()).add(ret);
            }
        }
        
        for (Map.Entry<String, List<Object>> obj : infoMap.entrySet()) {
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
    
    private static Map<String, Object> getInventoryInfo(Location loc, EnumFacing side) {
        
        //Return a single Key-Value pair of strings.
        Map<String, Object> ret = new HashMap<>();
        IBlockState state = loc.getBlockState(getWorldServerForDimensionId(loc.getDimension()));
        ret.put("unlocalized-name", state.getBlock().getUnlocalizedName());
        ret.put("localized-name", I18n.translateToLocal(state.getBlock().getLocalizedName()));
        ret.put("coords", loc);
        
        
        TileEntity tileEntity = loc.getTE(getWorldServerForDimensionId(loc.getDimension()));
        if (tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side)) {
            IItemHandler cap = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side);
            int maxSlots = cap.getSlots();
            
            List<Map<String, Object>> items = new ArrayList<>();
            for (int i = 0; i < maxSlots; i++) {
                Map<String, Object> itemMap = new HashMap<>();
                ItemStack itemStack = cap.getStackInSlot(i);
                if (itemStack != null) {
                    itemMap.put("unlocalized-name", itemStack.getUnlocalizedName());
                    itemMap.put("localized-name", itemStack.getDisplayName());
                    itemMap.put("stacksize", itemStack.stackSize);
                    itemMap.put("metadata", itemStack.getMetadata());
                    itemMap.put("damage", itemStack.getItemDamage());
                    itemMap.put("maxdamage", itemStack.getMaxDamage());
                    itemMap.put("enchantments", itemStack.getEnchantmentTagList());
                    
                    //TODO: Add capabilities?
                    
                } else {
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
        ret.put("localized-name", I18n.translateToLocal(state.getBlock().getLocalizedName()));
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
    
    
    private static Map<String, Object> getFluidInfo(Location loc, EnumFacing side) {
        //Return a single Key-Value pair of strings.
        Map<String, Object> ret = new HashMap<>();
        IBlockState state = loc.getBlockState(getWorldServerForDimensionId(loc.getDimension()));
        ret.put("unlocalized-name", state.getBlock().getUnlocalizedName());
        ret.put("localized-name", I18n.translateToLocal(state.getBlock().getLocalizedName()));
        ret.put("coords", loc);
        
        TileEntity tileEntity = loc.getTE(getWorldServerForDimensionId(loc.getDimension()));
        if (tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side)) {
            IFluidHandler cap = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
            
            IFluidTankProperties tankProperties = cap.getTankProperties()[0];
            if (tankProperties != null) {
                FluidStack fluid = tankProperties.getContents();
                if (fluid != null) {
                    ret.put("stored", fluid.amount);
                    ret.put("fluid", fluid.getUnlocalizedName());
                } else {
                    ret.put("stored", 0);
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
