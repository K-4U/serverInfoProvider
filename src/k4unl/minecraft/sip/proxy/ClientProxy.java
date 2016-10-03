package k4unl.minecraft.sip.proxy;

import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import k4unl.minecraft.sip.lib.Log;

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
