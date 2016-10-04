package k4unl.minecraft.sip.proxy;

import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import k4unl.minecraft.sip.lib.Log;
import k4unl.minecraft.sip.network.TCPServerThread;
import net.minecraft.server.dedicated.DedicatedServer;

/**
 * @author Koen Beckers (K-4U)
 */
public class CommonProxy {
    private static TCPServerThread tcpServerThread;
    private static Thread thread;
    
    @SideOnly(Side.SERVER)
    public void serverStarted(FMLServerStartingEvent event){
        Log.info("Server starting");
        if (event.getServer() instanceof DedicatedServer) {
            tcpServerThread = new TCPServerThread();
            thread = new Thread(tcpServerThread);
            thread.setName("ServerInfoListener");
            thread.start();
            
        }
    }
    
    @SideOnly(Side.SERVER)
    public void serverStopping(FMLServerStoppingEvent event){
        Log.info("Server stopping");
        if(thread != null){
            tcpServerThread.stop();
        }
    }
}
