package k4unl.minecraft.sip.events;

import cofh.api.energy.IEnergyHandler;
import ic2.api.tile.IEnergyStorage;
import k4unl.minecraft.k4lib.lib.Functions;
import k4unl.minecraft.sip.api.ISIPRequest;
import k4unl.minecraft.sip.api.event.InfoEvent;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author Koen Beckers (K-4U)
 */
public class EnergyEvent {
    
    public static void init() {
        
        MinecraftForge.EVENT_BUS.register(new EnergyEvent());
    }
    
    @SubscribeEvent
    public void energyEvent(InfoEvent event) {
        //Find RF
        ISIPRequest request = event.getRequest();
        if (request.isArgumentPos()) {
            
            //RF
            TileEntity tileEntity = request.getPosArgument().getTE(Functions.getWorldServerForDimensionId(request.getPosArgument().getDimension()));
            if (tileEntity instanceof IEnergyHandler) {
                if (request.hasArgumentSide()) {
                    
                    event.addInfo("stored", ((IEnergyHandler) tileEntity).getEnergyStored(request.getSideArgument()));
                    event.addInfo("capacity", ((IEnergyHandler) tileEntity).getMaxEnergyStored(request.getSideArgument()));
                    event.addInfo("type", "rf");
                } else {
                    event.addInfo("warning", "There is an RF storage at these coordinates, but no side argument given!");
                }
                
            }
            
            // EU
            if(Loader.isModLoaded("IC2")){
                getEUInfo(event);
            }
            
            // Add more here
            // Will this give issues if more than one energy system is in place on the same block?
            // How often does this happen though..
        } else if (!request.isArgumentPos()) {
            event.addInfo("error", "No position argument");
        }
    }
    
    @Optional.Method(modid = "IC2")
    private static void getEUInfo(InfoEvent event) {
        ISIPRequest request = event.getRequest();
        TileEntity tileEntity = request.getPosArgument().getTE(Functions.getWorldServerForDimensionId(request.getPosArgument().getDimension()));
        
        if(tileEntity instanceof IEnergyStorage){
            IEnergyStorage storage = (IEnergyStorage) tileEntity;
            event.addInfo("stored", storage.getStored());
            event.addInfo("capacity", storage.getCapacity());
            event.addInfo("type", "eu");
        }
    }
}
