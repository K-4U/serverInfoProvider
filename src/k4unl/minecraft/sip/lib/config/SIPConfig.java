package k4unl.minecraft.sip.lib.config;

import k4unl.minecraft.k4lib.lib.config.Config;
import k4unl.minecraft.k4lib.lib.config.ConfigOption;

public class SIPConfig extends Config{

    public static final SIPConfig INSTANCE = new SIPConfig();

    public void init() {
        
        configOptions.add(new ConfigOption("port", 25566).setComment("The port for the TCP Socket to listen on."));
        super.init();
    }
}
