package k4unl.minecraft.sip.proxy;

import k4unl.minecraft.sip.lib.Log;
import k4unl.minecraft.sip.network.TCPServerThread;
import k4unl.minecraft.sip.network.rcon.RConThreadQuery;
import net.minecraft.network.rcon.RConThreadBase;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;

/**
 * @author Koen Beckers (K-4U)
 */
public class CommonProxy {
    
    private static TCPServerThread tcpServerThread;
    private static Thread          thread;
    
    @SideOnly(Side.SERVER)
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
                Field thread = ReflectionHelper.findField(theServer.getClass(), "rconQueryThread", "field_71342_m");
                if (theServer.getBooleanProperty("enable-query", false)) {
                    Field isRunning = ReflectionHelper.findField(RConThreadBase.class, "running", "field_72619_a");
                    Log.info("Disabling vanilla query listener");
                    isRunning.setBoolean(thread.get(theServer), false);
                }
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
    }
    
    @SideOnly(Side.SERVER)
    public void serverStopping(FMLServerStoppingEvent event) {
        
        Log.info("Server stopping");
        if (thread != null) {
            tcpServerThread.stop();
        }
    }
}
