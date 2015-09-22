package k4unl.minecraft.sqe.events;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import k4unl.minecraft.sqe.storage.Players;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.WorldEvent;

public class EventHelper {

	public static void init(){
		MinecraftForge.EVENT_BUS.register(new EventHelper());
		FMLCommonHandler.instance().bus().register(new EventHelper());
	}


	@SubscribeEvent
	public void WorldEventSave(WorldEvent.Save event){
		Players.savePlayers();
	}

	@SubscribeEvent
	public void loggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
		Players.playerLoggedIn(event.player.getGameProfile().getName());
	}

    @SubscribeEvent
    public void loggedOutEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        Players.playerLoggedOff(event.player.getGameProfile().getName());
    }

    @SubscribeEvent
    public void playerDeathEvent(LivingDeathEvent event){
        if(event.entityLiving instanceof EntityPlayer){
            Players.addDeath(((EntityPlayer)event.entityLiving).getGameProfile().getName(), event.source.getDamageType());
        }
    }
}

