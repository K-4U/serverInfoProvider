package k4unl.minecraft.sip;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import k4unl.minecraft.k4lib.lib.config.ConfigHandler;
import k4unl.minecraft.sip.events.EventHelper;
import k4unl.minecraft.sip.lib.Log;
import k4unl.minecraft.sip.lib.config.ModInfo;
import k4unl.minecraft.sip.lib.config.SIPConfig;
import k4unl.minecraft.sip.proxy.CommonProxy;
import k4unl.minecraft.sip.storage.Players;

@Mod(
	modid = ModInfo.ID,
	name = ModInfo.NAME,
	version = ModInfo.VERSION,
	acceptableRemoteVersions="*"
)

public class ServerInfoProvider {

    @SidedProxy(
            clientSide = "k4unl.minecraft.sip.proxy.ClientProxy",
            serverSide = "k4unl.minecraft.sip.proxy.CommonProxy"
    )
    public static CommonProxy proxy;

    private boolean canWork = true;
    @Mod.Instance(value = ModInfo.ID)
    public static ServerInfoProvider instance;

    private ConfigHandler SQEConfigHandler = new ConfigHandler();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        Log.init();

        if(event.getSide().equals(cpw.mods.fml.relauncher.Side.CLIENT)){
            canWork = false;
            Log.error("SIP IS A SERVER ONLY MOD! IT WILL NOT WORK ON CLIENTS!");
        }else {
            SIPConfig.INSTANCE.init();
            SQEConfigHandler.init(SIPConfig.INSTANCE, event.getSuggestedConfigurationFile());
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
    @SideOnly(Side.SERVER)
    public void onServerStart(FMLServerStartingEvent event) {
        Players.loadPlayers();
        proxy.serverStarted(event);
    }
    
    @Mod.EventHandler
    @SideOnly(Side.SERVER)
    public void onServerStop(FMLServerStoppingEvent event) {
        Players.savePlayers();
        proxy.serverStopping(event);
    }
}

