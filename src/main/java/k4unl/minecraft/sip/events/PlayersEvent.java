package k4unl.minecraft.sip.events;

import k4unl.minecraft.k4lib.lib.Functions;
import k4unl.minecraft.sip.api.event.PlayerInfoEvent;
import k4unl.minecraft.sip.storage.Players;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

public class PlayersEvent {

    private static List<String> getPlayers() {

        List<String> players = new ArrayList<>();
        for (World world : Functions.getServer().getWorlds()) {
            for (Object player : world.getPlayers()) {
                players.add(((PlayerEntity) player).getGameProfile().getName());
            }
        }

        return players;
    }

    private static Map<String, String> getLatestDeaths(List<String> players) {

        Map<String, String> ret = new HashMap<>();
        for (String p : players) {
            ret.put(p, Players.getLatestDeath(p));
        }
        return ret;
    }

    @SubscribeEvent
    public static void playersEvent(PlayerInfoEvent event) {
        for (World world : Functions.getServer().getWorlds()) {
            for (PlayerEntity player : world.getPlayers()) {
                UUID id = player.getGameProfile().getId();
                String name = player.getGameProfile().getName();
                event.addPlayerInfo(id, "name", name);

                if (event.getArgument().equals("latestdeath")) {
                    event.addPlayerInfo(id, "latestDeath", Players.getLatestDeath(name));
                }
            }
        }
    }
}
