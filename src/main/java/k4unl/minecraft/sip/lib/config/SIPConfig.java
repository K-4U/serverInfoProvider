package k4unl.minecraft.sip.lib.config;

import k4unl.minecraft.k4lib.lib.config.Config;
import k4unl.minecraft.sip.lib.Log;
import net.minecraftforge.common.ForgeConfigSpec;

public class SIPConfig extends Config {

    public static ForgeConfigSpec.ConfigValue<Integer> port;

    @Override
    protected void buildCommon(ForgeConfigSpec.Builder builder) {

    }

    @Override
    protected void buildServer(ForgeConfigSpec.Builder builder) {
        port = builder.comment("The port for the TCP socket to listen on.").define("port", 25566);
        Log.info(port.toString());
    }

    @Override
    protected void buildClient(ForgeConfigSpec.Builder builder) {

    }
}
