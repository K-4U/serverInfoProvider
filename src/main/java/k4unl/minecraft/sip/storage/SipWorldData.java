package k4unl.minecraft.sip.storage;

import k4unl.minecraft.sip.lib.config.ModInfo;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;

/**
 * @author Koen Beckers (K-4U)
 */
public class SipWorldData extends WorldSavedData {

    public SipWorldData() {
        super(ModInfo.ID);
    }

    public static SipWorldData get(ServerWorld world) {
        DimensionSavedDataManager storage = world.getSavedData();
        return storage.getOrCreate(SipWorldData::new, ModInfo.ID);
    }

    @Override
    public void read(CompoundNBT nbt) {
        ListNBT users = nbt.getList("players", 10);
        for (int i = 0; i < users.size(); ++i) {
            CompoundNBT compoundnbt = users.getCompound(i);
            Player player = new Player(compoundnbt);
            Players.addPlayer(player);
        }
    }

    @Override
    public boolean isDirty() {
        return true;
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT players = new ListNBT();
        for (Player player : Players.getPlayers()) {
            CompoundNBT nbt = player.save();
            players.add(nbt);
        }
        compound.put("players", players);

        return compound;
    }
}
