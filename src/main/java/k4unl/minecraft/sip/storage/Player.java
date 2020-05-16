package k4unl.minecraft.sip.storage;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Koen Beckers (K-4U)
 */
public class Player {
	private String name;
	private Map<String, Integer> deaths;
	private String latestDeath;
	private boolean online;
	private long lastOnline;

	public Player(String _name) {
		name = _name;
		online = false;
		deaths = new HashMap<String, Integer>();
		lastOnline = 0;
		latestDeath = "None";
	}


	public Player(CompoundNBT compoundnbt) {
		this.name = compoundnbt.getString("name");
		ListNBT deaths = compoundnbt.getList("deaths", 10);
		for (int i = 0; i < deaths.size(); ++i) {
			CompoundNBT death = deaths.getCompound(i);
			this.deaths.put(death.getString("cause"), death.getInt("times"));
		}
		latestDeath = compoundnbt.getString("latestDeath");
		online = false;
		lastOnline = compoundnbt.getLong("lastOnline");
	}

	public CompoundNBT save() {
		CompoundNBT nbt = new CompoundNBT();
		nbt.putString("name", this.getName());
		if (null != this.getDeathsWithCauses()) {
			ListNBT deaths = new ListNBT();
			this.getDeathsWithCauses().forEach((cause, times) -> {
				CompoundNBT death = new CompoundNBT();
				death.putString("cause", cause);
				death.putInt("times", times);
				deaths.add(death);
			});
		}
		if (null != latestDeath) {
			nbt.putString("latestDeath", this.getLatestDeath());
		}
		nbt.putLong("lastOnline", lastOnline);
		return nbt;
	}

	public String getName() {
		return this.name;
	}

	public void addDeath(String cause) {
		int count = 0;
		if (this.deaths.containsKey(cause)) {
			count = this.deaths.get(cause);
		}
		latestDeath = cause;
		this.deaths.put(cause, count + 1);
	}

	public int getDeaths(String cause) {
		if (cause != null) {
			return this.deaths.get(cause);
		}
		int c = 0;
		for (Map.Entry<String, Integer> death : this.deaths.entrySet()) {
			c += death.getValue();
		}
		return c;
	}

	public Map<String, Integer> getDeathsWithCauses() {

		return new HashMap<String, Integer>(this.deaths);
	}

	public String getLatestDeath() {
		return latestDeath;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline() {
		this.online = true;
		lastOnline = (new Date()).getTime();
	}

	public void setOffline() {
		this.online = false;
		lastOnline = (new Date()).getTime();
	}

	public long getLastOnline() {
		return lastOnline;
	}
}
