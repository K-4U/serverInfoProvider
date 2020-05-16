package k4unl.minecraft.sip.events;

import k4unl.minecraft.sip.storage.Players;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EventHelper {

    public static void init() {

        MinecraftForge.EVENT_BUS.register(new EventHelper());
    }

    @SubscribeEvent
    public void loggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {

        Players.playerLoggedIn(event.getPlayer().getGameProfile().getName());
    }

    @SubscribeEvent
    public void loggedOutEvent(PlayerEvent.PlayerLoggedOutEvent event) {

        Players.playerLoggedOff(event.getPlayer().getGameProfile().getName());
    }

    @SubscribeEvent
    public void playerDeathEvent(LivingDeathEvent event) {

        if (event.getEntityLiving() instanceof PlayerEntity) {
            Players.addDeath(((PlayerEntity) event.getEntityLiving()).getGameProfile().getName(), event.getSource().getDamageType());
        }
    }
}

