package k4unl.minecraft.sip.api;

import k4unl.minecraft.k4lib.lib.Location;
import net.minecraft.util.Direction;

/**
 * @author Koen Beckers (K-4U)
 */
public interface ISIPRequest {
    
    /**
     * To fetch the key that was requested
     *
     * @return a string containing the key that was requested
     */
    String getKey();
    
    
    /**
     * Use this to fetch the raw string argument. Note that this CAN be null!
     *
     * @return the argument as a string
     */
    String getArgument();
    
    /**
     * Use this to fetch the argument parsed as int. Note that this CAN be null!
     *
     * @return the argument as an int
     */
    int getIntArgument();
    
    
    /**
     * Use this to fetch the argument as a position. Note that this CAN be null!
     *
     * @return a {@link k4unl.minecraft.k4lib.lib.Location} object containing the position requested, including dimension
     */
    Location getPosArgument();
    
    /**
     * Use this to check if there is a valid position given as argument.
     *
     * @return
     */
    boolean isArgumentPos();

    /**
     * Use this to fetch the side-part of an argument. Note that this CAN be null!
     *
     * @return a {@link net.minecraft.util.Direction} object with the side.
     */
    Direction getSideArgument();
    
    /**
     * Use this to check if there is a valid side given with the argument
     *
     * @return
     */
    boolean hasArgumentSide();
    
}
