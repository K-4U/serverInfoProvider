package k4unl.minecraft.sqe.events;

import k4unl.minecraft.sqe.storage.Players;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class EventHelper {

	public static void init(){
		MinecraftForge.EVENT_BUS.register(new EventHelper());
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
        if(event.getEntityLiving() instanceof EntityPlayer){
            Players.addDeath(((EntityPlayer)event.getEntityLiving()).getGameProfile().getName(), event.getSource().getDamageType());
        }
    }
}

