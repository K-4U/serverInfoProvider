package k4unl.minecraft.sqe;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import k4unl.minecraft.k4lib.lib.config.ConfigHandler;
import k4unl.minecraft.sqe.lib.Log;
import k4unl.minecraft.sqe.lib.config.ModInfo;
import k4unl.minecraft.sqe.lib.config.SQEConfig;
import k4unl.minecraft.sqe.network.rcon.RConThreadQuery;
import net.minecraft.network.rcon.RConThreadBase;
import net.minecraft.server.dedicated.DedicatedServer;

import java.lang.reflect.Field;

@Mod(
	modid = ModInfo.ID,
	name = ModInfo.NAME,
	version = ModInfo.VERSION,
	acceptableRemoteVersions="*"
)

public class ServerQueryExtender {

    @Instance(value = ModInfo.ID)
    public static ServerQueryExtender instance;

    private ConfigHandler SQEConfigHandler = new ConfigHandler();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        Log.init();
        SQEConfig.INSTANCE.init();
        SQEConfigHandler.init(SQEConfig.INSTANCE, event.getSuggestedConfigurationFile());
    }

    @EventHandler
    public void load(FMLInitializationEvent event) {

    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {

    }

    @EventHandler
    public void onServerStart(FMLServerStartingEvent event) {
        Log.info("Server starting");
        if(event.getServer() instanceof DedicatedServer) {
            DedicatedServer theServer = (DedicatedServer) event.getServer();

            if (theServer.getBooleanProperty("enable-query", false)) {

                try {
                    Field thread = ReflectionHelper.findField(theServer.getClass(), "theRConThreadQuery", "field_71342_m");
                    Field isRunning = ReflectionHelper.findField(RConThreadBase.class, "running", "field_72619_a");
                    Log.info("Disabling vanilla query listener");
                    isRunning.setBoolean(thread.get(theServer), false);
                    //Possibly that we need to wait a while before starting the new thread here..
                    RConThreadQuery theNewThread = new RConThreadQuery(theServer);
                    Log.info("Starting Extended Query Listener");
                    thread.set(theServer, theNewThread);
                    theNewThread.startThread();
                } catch (IllegalAccessException e) {
                    Log.error("Error during reflection of theRConThreadQuery: ");
                    e.printStackTrace();
                }
            }

            if (theServer.getBooleanProperty("enable-rcon", false)) {
                /*field_155771_h.info("Starting remote control listener");
                this.theRConThreadMain = new RConThreadMain(theServer);
                this.theRConThreadMain.startThread();*/
            }
        }
    }
}
