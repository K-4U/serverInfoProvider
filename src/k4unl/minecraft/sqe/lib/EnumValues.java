package k4unl.minecraft.sqe.lib;

/**
 * @author Koen Beckers (K-4U)
 */
public enum EnumValues {
    INVALID(0x00, false), TIME(0x01, true), PLAYERS(0x02, true), DAYNIGHT(0x03, true), DIMENSIONS(0x04, false);

    private char theNumber;
    private boolean hasArgument;

    EnumValues(int i, boolean arg) {
        theNumber = (char)i;
        hasArgument = arg;
    }

    public char getTheNumber() {
        return theNumber;
    }

    public boolean isHasArgument() {
        return hasArgument;
    }

    public static EnumValues getFromNumber(int i){
        for(EnumValues v : values()){
            if(v.getTheNumber() == i){
                return v;
            }
        }
        return INVALID;
    }
}
