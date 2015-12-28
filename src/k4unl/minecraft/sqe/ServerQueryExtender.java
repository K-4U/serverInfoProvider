package k4unl.minecraft.sqe;

import k4unl.minecraft.k4lib.lib.config.ConfigHandler;
import k4unl.minecraft.sqe.events.EventHelper;
import k4unl.minecraft.sqe.lib.Log;
import k4unl.minecraft.sqe.lib.config.ModInfo;
import k4unl.minecraft.sqe.lib.config.SQEConfig;
import k4unl.minecraft.sqe.proxy.CommonProxy;
import k4unl.minecraft.sqe.storage.Players;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
    @Mod.Instance(value = ModInfo.ID)
    public static ServerQueryExtender instance;

    private ConfigHandler SQEConfigHandler = new ConfigHandler();

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        Log.init();

        if(event.getSide().equals(Side.CLIENT)){
            canWork = false;
            Log.error("SQE IS A SERVER ONLY MOD! IT WILL NOT WORK ON CLIENTS!");
        }else {
            SQEConfig.INSTANCE.init();
            SQEConfigHandler.init(SQEConfig.INSTANCE, event.getSuggestedConfigurationFile());
        }
    }

    @Mod.EventHandler
    public void load(FMLInitializationEvent event) {
        EventHelper.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {

    }

    @Mod.EventHandler
    @SideOnly(Side.SERVER)
    public void onServerStart(FMLServerStartingEvent event) {
        Players.loadPlayers();
        proxy.serverStarted(event);
    }
}
