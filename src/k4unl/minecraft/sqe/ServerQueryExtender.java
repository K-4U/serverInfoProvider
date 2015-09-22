package k4unl.minecraft.sqe;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import k4unl.minecraft.k4lib.lib.config.ConfigHandler;
import k4unl.minecraft.sqe.events.EventHelper;
import k4unl.minecraft.sqe.lib.Log;
import k4unl.minecraft.sqe.lib.config.ModInfo;
import k4unl.minecraft.sqe.lib.config.SQEConfig;
import k4unl.minecraft.sqe.proxy.CommonProxy;
import k4unl.minecraft.sqe.storage.Players;

@Mod(
	modid = ModInfo.ID,
	name = ModInfo.NAME,
	version = ModInfo.VERSION,
	acceptableRemoteVersions="*"
)

public class ServerQueryExtender {

    @SidedProxy(
            clientSide = "k4unl.minecraft.sqe.proxy.ClientProxy",
            serverSide = "k4unl.minecraft.sqe.proxy.CommonProxy"
    )
    public static CommonProxy proxy;

    private boolean canWork = true;
    @Instance(value = ModInfo.ID)
    public static ServerQueryExtender instance;

    private ConfigHandler SQEConfigHandler = new ConfigHandler();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        Log.init();

        if(event.getSide().equals(cpw.mods.fml.relauncher.Side.CLIENT)){
            canWork = false;
            Log.error("SQE IS A SERVER ONLY MOD! IT WILL NOT WORK ON CLIENTS!");
        }else {
            SQEConfig.INSTANCE.init();
            SQEConfigHandler.init(SQEConfig.INSTANCE, event.getSuggestedConfigurationFile());
        }
    }

    @EventHandler
    public void load(FMLInitializationEvent event) {
        EventHelper.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {

    }

    @EventHandler
    public void onServerStart(FMLServerStartingEvent event) {
        Players.loadPlayers();
        proxy.serverStarted(event);
    }
}
