package k4unl.minecraft.sip;

import k4unl.minecraft.k4lib.lib.config.Config;
import k4unl.minecraft.sip.events.EnergyEvent;
import k4unl.minecraft.sip.events.EventHelper;
import k4unl.minecraft.sip.events.PlayersEvent;
import k4unl.minecraft.sip.lib.config.ModInfo;
import k4unl.minecraft.sip.lib.config.SIPConfig;
import k4unl.minecraft.sip.proxy.ClientProxy;
import k4unl.minecraft.sip.proxy.CommonProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ModInfo.ID)
public class ServerInfoProvider {

    public static ServerInfoProvider instance;

    public static CommonProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> CommonProxy::new);
    private boolean canWork = true;

    public ServerInfoProvider() {
        Config config = new SIPConfig();
        config.load(ModInfo.ID);
        instance = this;
        FMLJavaModLoadingContext.get().getModEventBus().register(this);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStart);
        MinecraftForge.EVENT_BUS.addListener(EnergyEvent::energyEvent);
        MinecraftForge.EVENT_BUS.addListener(PlayersEvent::playersEvent);
        EventHelper.init();
    }

    @SubscribeEvent
    public void onServerStop(FMLServerStoppingEvent event) {
//        Players.savePlayers();
        proxy.serverStopping(event);
    }

    @SubscribeEvent
    public void setup(final FMLCommonSetupEvent event) {
//		DeferredWorkQueue.runLater(NetworkHandler::init);
    }

    @SubscribeEvent
    public void onServerStart(FMLServerStartingEvent event) {
//        boolean b = event.getServer() instanceof DedicatedServer;
//        CommandsRegistry commandsRegistry = new CommandsRegistry(b, event.getServer().getCommandManager().getDispatcher());

//        Players.loadPlayers();
        proxy.serverStarted(event);
    }
}

