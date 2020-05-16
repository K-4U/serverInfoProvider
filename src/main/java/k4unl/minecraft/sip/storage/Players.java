package k4unl.minecraft.sip.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Koen Beckers (K-4U)
 */
public class Players {
	private static Map<String, Player> playerList = new HashMap<String, Player>();

	private static Player getOrCreate(String playerName) {
		if (playerList.containsKey(playerName)) {
			return playerList.get(playerName);
		}
		Player newPlayer = new Player(playerName);
		playerList.put(playerName, newPlayer);
		return newPlayer;
	}

	public static void playerLoggedIn(String playerName) {
		getOrCreate(playerName).setOnline();
	}

	public static void playerLoggedOff(String playerName) {
		getOrCreate(playerName).setOffline();
	}

	public static void addDeath(String playerName, String damageType) {
		getOrCreate(playerName).addDeath(damageType);
	}

	public static Map<String, Integer> getDeaths(String playerName) {
		return getOrCreate(playerName).getDeathsWithCauses();
	}

	public static Map<String, Integer> getDeathLeaderboard() {
		Map<String, Integer> ret = new HashMap<String, Integer>();
		for (Map.Entry<String, Player> playerEntry : playerList.entrySet()) {
			int deaths = playerEntry.getValue().getDeaths(null);
			if (deaths != 0) {
				ret.put(playerEntry.getKey(), deaths);
			}
		}
		return ret;
	}

	public static String getLatestDeath(String playerName) {
		return getOrCreate(playerName).getLatestDeath();
	}

	public static void addPlayer(Player player) {
		playerList.put(player.getName(), player);
	}

	public static Collection<Player> getPlayers() {
		return playerList.values();
	}
}
