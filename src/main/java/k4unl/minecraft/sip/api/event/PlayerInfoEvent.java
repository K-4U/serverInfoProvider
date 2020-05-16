package k4unl.minecraft.sip.api.event;

import net.minecraftforge.eventbus.api.Event;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerInfoEvent extends Event {

    private final String argument;
    private final Map<UUID, Map<String, Object>> playerInfo;

    public PlayerInfoEvent(String argument) {
        this.argument = argument;
        this.playerInfo = new HashMap<>();
    }


    public String getArgument() {
        return argument;
    }

    public Map<UUID, Map<String, Object>> getAllPlayerInfo() {
        return playerInfo;
    }

    public Map<String, Object> getPlayerInfo(UUID playerUUID) {
        return playerInfo.get(playerUUID);
    }

    public void addPlayerInfo(UUID playerUUID, String key, Object value) {
        if (!playerInfo.containsKey(playerUUID)) {
            playerInfo.put(playerUUID, new HashMap<>());
        }
        getPlayerInfo(playerUUID).put(key, value);
    }
}
