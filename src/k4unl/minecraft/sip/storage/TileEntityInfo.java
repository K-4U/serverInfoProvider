package k4unl.minecraft.sip.storage;

import k4unl.minecraft.k4lib.lib.Location;
import net.minecraft.tileentity.TileEntity;

/**
 * @author Koen Beckers (K-4U)
 */
public class TileEntityInfo {
    
    private Location location;
    private String name;
    
    
    public TileEntityInfo(TileEntity entity) {
        location = new Location(entity.xCoord, entity.yCoord, entity.zCoord);
        name = entity.getClass().getCanonicalName();
    }
}
