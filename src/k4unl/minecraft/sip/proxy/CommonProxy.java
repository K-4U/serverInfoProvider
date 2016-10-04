package k4unl.minecraft.sip.proxy;

import k4unl.minecraft.sip.lib.Log;
import k4unl.minecraft.sip.network.TCPServerThread;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
