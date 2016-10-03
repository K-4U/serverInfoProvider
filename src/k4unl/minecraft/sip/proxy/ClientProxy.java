package k4unl.minecraft.sip.proxy;

import k4unl.minecraft.sip.lib.Log;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author Koen Beckers (K-4U)
 */
public class ClientProxy extends CommonProxy {
    @Override
    @SideOnly(Side.CLIENT)
    public void serverStarted(FMLServerStartingEvent event){
        Log.debug("Derp");
    }
}
