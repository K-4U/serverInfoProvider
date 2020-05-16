package k4unl.minecraft.sip.proxy;

import k4unl.minecraft.sip.lib.Log;
import k4unl.minecraft.sip.network.TCPServerThread;
import k4unl.minecraft.sip.network.rcon.QueryThread;
import net.minecraft.network.rcon.RConThread;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

import java.lang.reflect.Field;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Koen Beckers (K-4U)
 */
public class CommonProxy {

    private static TCPServerThread tcpServerThread;
    private static Thread thread;

    final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();


    @OnlyIn(Dist.DEDICATED_SERVER)
    public void serverStarted(FMLServerStartingEvent event) {

        Log.info("Server starting");
        if (event.getServer() instanceof DedicatedServer) {
            tcpServerThread = new TCPServerThread();
            thread = new Thread(tcpServerThread);
            thread.setName("ServerInfoListener");
            thread.start();

            //Also: replace the query class to serve our own.
            DedicatedServer theServer = (DedicatedServer) event.getServer();

            try {
                Field thread = ObfuscationReflectionHelper.findField(DedicatedServer.class, "field_71342_m");//"rconQueryThread",
                if (theServer.getServerProperties().enableQuery) {
                    Field isRunning = ObfuscationReflectionHelper.findField(RConThread.class, "field_72619_a");//"running",
                    Log.info("Disabling vanilla query listener");
                    isRunning.setBoolean(thread.get(theServer), false);
                }
                executorService.schedule(() -> {
                    try {
                        replaceThread(theServer, thread);
                    } catch (IllegalAccessException e) {
                        Log.error("Error during reflection of theRConThreadQuery: ");
                        e.printStackTrace();
                    }
                }, 1, TimeUnit.SECONDS);

            } catch (IllegalAccessException e) {
                Log.error("Error during reflection of theRConThreadQuery: ");
                e.printStackTrace();
            }

        }
    }

    private void replaceThread(DedicatedServer theServer, Field thread) throws IllegalAccessException {
        //Possibly that we need to wait a while before starting the new thread here..
        QueryThread theNewThread = new QueryThread(theServer);
        Log.info("Starting Extended Query Listener");
        thread.set(theServer, theNewThread);
        theNewThread.startThread();
    }

    @OnlyIn(Dist.DEDICATED_SERVER)
    public void serverStopping(FMLServerStoppingEvent event) {

        Log.info("Server stopping");
        if (thread != null) {
            tcpServerThread.stop();
        }
    }
}
