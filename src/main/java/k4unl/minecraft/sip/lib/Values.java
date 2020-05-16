package k4unl.minecraft.sip.lib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import k4unl.minecraft.k4lib.lib.Functions;
import k4unl.minecraft.k4lib.lib.Location;
import k4unl.minecraft.k4lib.network.EnumSIPValues;
import k4unl.minecraft.sip.api.ISIPEntity;
import k4unl.minecraft.sip.api.event.InfoEvent;
import k4unl.minecraft.sip.api.event.PlayerInfoEvent;
import k4unl.minecraft.sip.storage.Players;
import k4unl.minecraft.sip.storage.TileEntityInfo;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.state.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.versions.forge.ForgeVersion;
import net.minecraftforge.versions.mcp.MCPVersion;

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

            boolean isSingleton = false;
            EnumSIPValues v = EnumSIPValues.fromString(value.getKey());
            try {
                switch (v) {
                    case TIME:
                        ret = getWorldTime(value.getIntArgument());
                        isSingleton = true;
                        break;
                    case PLAYERS:
                        PlayerInfoEvent pie = new PlayerInfoEvent(value.getArgument());
                        MinecraftForge.EVENT_BUS.post(pie);

                        ret = pie.getAllPlayerInfo();
                        isSingleton = true;
                        break;
                    case DAYNIGHT:
                        ret = getWorldDayNight(value.getIntArgument());
                        isSingleton = true;
                        break;
                    case DIMENSIONS:
                        ret = getDimensions();
                        isSingleton = true;
                        break;
                    case UPTIME:
                        ret = getUptime();
                        isSingleton = true;
                        break;
                    case DEATHS:
                        //Get a leaderboard of deaths, or the deaths of a player
                        if (!value.getArgument().equals("")) {
                            ret = getDeathsByPlayer(value.getArgument());
                        } else {
                            ret = getDeathLeaderboard();
                        }
                        isSingleton = true;

                        break;
                    case WEATHER:
                        ret = getWorldWeather(value.getIntArgument());
                        isSingleton = true;
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
                    case TPS:
                        if (value.getArgument().equals("")) {
                            //No dimension given
                            ret = getTPS();
                        } else {
                            //Dimension given
                            ret = getTPS(DimensionType.getById(value.getIntArgument()));
                        }
                        isSingleton = true;
                        break;
                    case ENTITIES:
                        if (value.getArgument().equals("")) {
                            //Do all dimensions.
                            ret = getEntities();
                        } else {
                            ret = getEntities(DimensionType.getById(value.getIntArgument()));
                        }
                        isSingleton = true;
                        break;
                    case VERSIONS:
                        ret = getVersions();
                        isSingleton = true;
                        break;
                    case TILES:
                        if (value.getArgument().equals("")) {
                            ret = "Please supply a dimension id";
                        } else {
                            ret = getTileEntities(value.getIntArgument());
                        }
                        isSingleton = true;
                        break;
                    case TILELIST:
                        if (!value.hasArrayArgument()) {
                            ret = "Please supply a dimension id and an unlocalized name";
                            break;
                        }
                        Map<String, Object> args = value.getArrayArgument();
                        if (!args.containsKey("dimensionid")) {
                            ret = "Please supply a dimension id";
                            break;
                        }
                        if (!args.containsKey("name")) {
                            ret = "Please supply an unlocalized name";
                            break;
                        }
                        ret = getTileEntitiesInfo((int) (Math.floor((Double) args.get("dimensionid"))), args.get("name").toString().toLowerCase());
                        isSingleton = true;
                        break;
                    case INVALID:
                        break;
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }

            if (ret == null) {
                //If nothing has been returned on our side, that means we don't know it.
                //Thus, ask the rest of the mods:
                InfoEvent evt = new InfoEvent(value);
                if (value.isArgumentPos()) {
                    BlockState state = value.getPosArgument().getBlockState(Functions.getWorldServerForDimensionId(value.getPosArgument().getDimension()));
                    evt.addInfo("unlocalized-name", state.getBlock().getTranslationKey());
                    evt.addInfo("localized-name", I18n.format(state.getBlock().getTranslationKey()));
                    evt.addInfo("coords", value.getPosArgument());
                }
                MinecraftForge.EVENT_BUS.post(evt);

                ret = evt.getReturn();
//                isSingleton = evt.isSingleton();
            }

            if (isSingleton) {
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

    private static Map<String, Object> getInventoryInfo(Location loc, Direction side) {

        //Return a single Key-Value pair of strings.
        Map<String, Object> ret = new HashMap<>();
        BlockState state = loc.getBlockState(getWorldServerForDimensionId(loc.getDimension()));
        ret.put("unlocalized-name", state.getBlock().getTranslationKey());
        ret.put("localized-name", I18n.format(state.getBlock().getTranslationKey()));
        ret.put("coords", loc);


        TileEntity tileEntity = loc.getTE(getWorldServerForDimensionId(loc.getDimension()));
        if (tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side).isPresent()) {
            IItemHandler cap = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side).orElse(null);
            int maxSlots = cap.getSlots();

            List<Map<String, Object>> items = new ArrayList<>();
            for (int i = 0; i < maxSlots; i++) {
                Map<String, Object> itemMap = new HashMap<>();
                ItemStack itemStack = cap.getStackInSlot(i);
                if (itemStack != null) {
                    itemMap.put("unlocalized-name", itemStack.getTranslationKey());
                    itemMap.put("localized-name", itemStack.getDisplayName());
                    itemMap.put("stacksize", itemStack.getCount());
                    itemMap.put("damage", itemStack.getDamage());
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

        BlockState state = loc.getBlockState(getWorldServerForDimensionId(loc.getDimension()));
        ret.put("unlocalized-name", state.getBlock().getTranslationKey());
        ret.put("localized-name", I18n.format(state.getBlock().getTranslationKey()));
        ret.put("coords", loc);
        Map<String, Map<String, Object>> properties = new HashMap<>();
        for (IProperty<?> entry : state.getProperties()) {
            Map<String, Object> propertyData = new HashMap<>();

            String s = entry.getName();

            String type = "";
            List<String> possibleValues = new ArrayList<>();
            // My IDE says this is wrong, but it compiles anyway. *shrugs*
            if (entry instanceof DirectionProperty) {
                type = "direction";
            } else if (entry instanceof EnumProperty) {
                type = "enum";
            } else if (entry instanceof BooleanProperty) {
                type = "bool";
            } else if (entry instanceof IntegerProperty) {
                type = "int";
            }

            propertyData.put("type", type);
            propertyData.put("allowedValues", entry.getAllowedValues());
            propertyData.put("value", state.get(entry));

            properties.put(entry.getName(), propertyData);
        }


        ret.put("state", properties);

        return ret;
    }


    private static Map<String, Object> getFluidInfo(Location loc, Direction side) {
        //Return a single Key-Value pair of strings.
        Map<String, Object> ret = new HashMap<>();
        BlockState state = loc.getBlockState(getWorldServerForDimensionId(loc.getDimension()));
        ret.put("unlocalized-name", state.getBlock().getTranslationKey());
        ret.put("localized-name", I18n.format(state.getBlock().getTranslationKey()));
        ret.put("coords", loc);

        TileEntity tileEntity = loc.getTE(getWorldServerForDimensionId(loc.getDimension()));
        IFluidHandler fluidHandler = FluidUtil.getFluidHandler(getWorldServerForDimensionId(loc.getDimension()), loc.toBlockPos(), side).orElse(null);
        if (null != fluidHandler) {
            int maxTanks = fluidHandler.getTanks();

            for (int tank = 0; tank < maxTanks; tank++) {
                TankInfo tI = new TankInfo();
                FluidStack fluid = fluidHandler.getFluidInTank(tank);
                if (!fluid.isFluidEqual(FluidStack.EMPTY)) {
                    tI.stored = fluid.getAmount();
                    tI.fluid = fluid.getTranslationKey();
                }
                tI.capacity = fluidHandler.getTankCapacity(tank);
            }
        } else {
            ret.put("error", "No fluid handler at these coordinates");
        }

        return ret;
    }

    private static Map<String, Integer> getDimensions() {

        Map<String, Integer> map = new HashMap<>();

        for (DimensionType dimensionType : Registry.DIMENSION_TYPE) {
            map.put(DimensionType.getKey(dimensionType).getPath(), dimensionType.getId());
        }
        return map;
    }


    private static Map<Integer, String> getWorldTime(int dimensionId) {

        ServerWorld w = getWorldServerForDimensionId(dimensionId);
        if (w != null) {
            int time = (int) (w.getDayTime() % 24000);
            int hour = ((int) (time / 1000) + 6) % 24;
            int minute = (int) ((time % 1000 * 60 / 1000));

            return getMap(dimensionId, String.format("%02d", hour) + ":" + String.format("%02d", minute));
        } else {
            return getMap(dimensionId, "NaW");
        }
    }

    private static Map<Integer, Map<String, Double>> getTPS() {

        Map<Integer, Map<String, Double>> ret = new HashMap<>();


        for (DimensionType dimId : Functions.getDimensions()) {
            ret.put(dimId.getId(), getTPS(dimId));
        }

        return ret;
    }

    private static Map<String, Double> getTPS(DimensionType dimensionId) {

        Map<String, Double> ret = new HashMap<>();

        double worldTickTime = mean(Functions.getServer().getTickTime(dimensionId)) * 1.0E-6D;
        double worldTPS = Math.min(1000.0 / worldTickTime, 20);
        ret.put("ticktime", worldTickTime);
        ret.put("tps", worldTPS);

        return ret;
    }

    private static long mean(long[] values) {

        long sum = 0l;
        for (long v : values) {
            sum += v;
        }

        return sum / values.length;
    }

    private static Long getUptime() {

        Date now = new Date();
        return now.getTime() - startDate.getTime();
    }

    private static boolean getWorldDayNight(int dimensionId) {

        ServerWorld w = getWorldServerForDimensionId(dimensionId);
        if (w != null) {
            return w.isDaytime();
        } else {
            return false;
        }
    }

    private static Map<Integer, String> getWorldWeather(int dimensionId) {

        ServerWorld w = getWorldServerForDimensionId(dimensionId);
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

    private static Map<Integer, Map<String, Integer>> getEntities() {
        Map<Integer, Map<String, Integer>> ret = new HashMap<>();

        for (DimensionType dimId : Functions.getDimensions()) {
            ret.put(dimId.getId(), getEntities(dimId));
        }

        return ret;
    }

    private static Map<String, Integer> getEntities(DimensionType dimensionType) {

        ServerWorld world = Functions.getWorldServerForDimensionType(dimensionType);
        Map<String, Integer> ret = new HashMap<>();

        world.getEntities().map(entity -> entity.getName().getUnformattedComponentText()).forEach(n -> {
            ret.computeIfAbsent(n, s -> 0);
            ret.computeIfPresent(n, (s, value) -> value++);
        });

        return ret;
    }

    private static Map<String, Map<String, Integer>> getDeathLeaderboard() {

        return getMap("LEADERBOARD", Players.getDeathLeaderboard());
    }

    private static Map<String, Map<String, Integer>> getDeathsByPlayer(String playerName) {

        return getMap(playerName, Players.getDeaths(playerName));
    }

    private static Map<String, Object> getVersions() {
        Map<String, Object> ret = new HashMap<>();
        ret.put("minecraft", MCPVersion.getMCVersion());
        ret.put("mcp", MCPVersion.getMCPVersion());
        ret.put("forge", ForgeVersion.getVersion());
        Map<String, String> mods = new HashMap<>();


        List<ModInfo> activeModList = ModList.get().getMods();
        for (ModInfo mod : activeModList) {
            mods.put(mod.getDisplayName(), mod.getVersion().toString());
        }
        ret.put("mods", mods);

        return ret;

    }

    private static Map<String, Integer> getTileEntities(int dimensionId) {
        World world = Functions.getWorldServerForDimensionId(dimensionId);
        Map<String, Integer> ret = new HashMap<>();

        List<TileEntity> loadedEntities = world.tickableTileEntities;

        for (TileEntity entity : loadedEntities) {
            String n = entity.getClass().getCanonicalName();
            if (!ret.containsKey(n)) {
                ret.put(n, 0);
            }
            ret.put(n, ret.get(n) + 1);
        }

        return ret;
    }

    private static List<TileEntityInfo> getTileEntitiesInfo(int dimensionId, String tileName) {
        List<TileEntityInfo> ret = new ArrayList<>();

        World world = Functions.getWorldServerForDimensionId(dimensionId);
        List<TileEntity> loadedEntities = world.tickableTileEntities;


        int i = 0;
        for (TileEntity entity : loadedEntities) {
            try {
                if (entity.getClass().getCanonicalName().equalsIgnoreCase(tileName)) {
                    ret.add(new TileEntityInfo(entity));
                }
            } catch (Exception exception) {
                Log.error(exception.getLocalizedMessage());
            }
            i++;
        }

        return ret;
    }

    private static <A, B> Map<A, B> getMap(A key, B value) {

        Map<A, B> ret = new HashMap<A, B>();
        ret.put(key, value);
        return ret;
    }

    static class TankInfo {
        public int stored;
        public int capacity;
        public String fluid = "none";
    }
}
