package fr.army.stelyteam.utils;

import org.bukkit.inventory.Inventory;

import fr.army.stelyteam.StelyTeamPlugin;

public class Storage {
    private Team team;
    private Integer storageId;
    private Inventory storageInstance;
    private byte[] storageContent;

    public Storage(Team team, Integer storageId, Inventory storageInstance, byte[] storageContent){
        this.team = team;
        this.storageId = storageId;
        this.storageInstance = storageInstance;
        this.storageContent = storageContent;
    }


    public void saveStorage(){
        StelyTeamPlugin.getPlugin().getSQLManager().saveStorage(this);
    }


    public Team getTeam() {
        return team;
    }

    public Integer getStorageId() {
        return storageId;
    }

    public Inventory getStorageInstance() {
        return storageInstance;
    }

    public byte[] getStorageContent() {
        return storageContent;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public void setStorageId(Integer storageId) {
        this.storageId = storageId;
    }

    public void setStorageInstance(Inventory storageInstance) {
        this.storageInstance = storageInstance;
    }

    public void setStorageContent(byte[] storageContent) {
        this.storageContent = storageContent;
    }
}
