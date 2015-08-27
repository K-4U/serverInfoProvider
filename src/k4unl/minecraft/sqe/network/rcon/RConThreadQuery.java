package k4unl.minecraft.sqe.network.rcon;

import k4unl.minecraft.sqe.lib.Log;
import net.minecraft.network.rcon.IServer;
/**
 * @author Koen Beckers (K-4U)
 */
public class RConThreadQuery extends net.minecraft.network.rcon.RConThreadQuery {

    public RConThreadQuery(IServer p_i1536_1_) {
        super(p_i1536_1_);
    }

    @Override
    protected void logInfo(String p_72609_1_) {
        Log.info(p_72609_1_);
    }

    @Override
    public void startThread() {
        //Just wait a second to close the sockets
        try {
            Thread.sleep(1000);
            super.startThread();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
