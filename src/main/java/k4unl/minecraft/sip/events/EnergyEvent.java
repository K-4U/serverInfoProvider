package k4unl.minecraft.sip.events;

import k4unl.minecraft.k4lib.lib.Functions;
import k4unl.minecraft.sip.api.ISIPRequest;
import k4unl.minecraft.sip.api.event.InfoEvent;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * @author Koen Beckers (K-4U)
 */
public class EnergyEvent {


    @SubscribeEvent
    public static void energyEvent(InfoEvent event) {
        if (event.getKey().equalsIgnoreCase("energy")) {
            //Find RF
            ISIPRequest request = event.getRequest();
            if (request.isArgumentPos()) {

                //RF
                TileEntity tileEntity = request.getPosArgument().getTE(Functions.getWorldServerForDimensionId(request.getPosArgument().getDimension()));
                if (tileEntity != null) {
                    LazyOptional<IEnergyStorage> capability = tileEntity.getCapability(CapabilityEnergy.ENERGY, request.getSideArgument());
                    capability.ifPresent(iEnergyStorage -> {
                        event.addInfo("stored", iEnergyStorage.getEnergyStored());
                        event.addInfo("capacity", iEnergyStorage.getMaxEnergyStored());
                        event.addInfo("type", "rf");
                    });
                }

            } else if (!request.isArgumentPos()) {
                event.addInfo("error", "No position argument");
            }
        }
    }
}
