package k4unl.minecraft.sqe.lib;

/**
 * @author Koen Beckers (K-4U)
 */
public enum EnumValues {
    INVALID, MISFORMED, TIME, PLAYERS, DAYNIGHT, DIMENSIONS, UPTIME;

    public static EnumValues fromString(String str) {
        for(EnumValues v : values()){
            if(v.toString().toLowerCase().equals(str.toLowerCase())){
                return v;
            }
        }
        return INVALID;
    }
}
