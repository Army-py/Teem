package fr.army.stelyteam.utils.manager.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import fr.army.stelyteam.StelyTeamPlugin;


public class ItemStackSerializer {

    public String serializeToBase64(ItemStack[] itemStack) {
        try {
            final ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            final BukkitObjectOutputStream objectOutputStream = new BukkitObjectOutputStream(arrayOutputStream);
            objectOutputStream.writeObject(itemStack);
            return Base64.getMimeEncoder().encodeToString(arrayOutputStream.toByteArray());
        } catch (final Exception exception) {
            throw new RuntimeException("Error turning ItemStack into base64", exception);
        }
    }


    public ItemStack[] deserializeFromBase64(String base64) {
        try {
            final ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(Base64.getMimeDecoder().decode(base64));
            final BukkitObjectInputStream objectInputStream = new BukkitObjectInputStream(arrayInputStream);
            return (ItemStack[]) objectInputStream.readObject();
        } catch (final Exception exception) {
            throw new RuntimeException("Error turning base64 into ItemStack", exception);
        }
    }


    public byte[] serializeToByte(ItemStack[] itemStack) {
        try {
            final ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();

            final BukkitObjectOutputStream objectOutputStream = new BukkitObjectOutputStream(arrayOutputStream);
            // objectOutputStream.writeObject(removeUnsedSlots(itemStack));
            objectOutputStream.writeObject(itemStack);
            objectOutputStream.flush();


            return arrayOutputStream.toByteArray();
        } catch (final Exception exception) {
            throw new RuntimeException("Error turning ItemStack into byte", exception);
        }
    }


    public ItemStack[] deserializeFromByte(byte[] bytes) {
        if (bytes.length == 0) return new ItemStack[0];
        try {
            final ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(bytes);
            final BukkitObjectInputStream objectInputStream = new BukkitObjectInputStream(arrayInputStream);
            return (ItemStack[]) objectInputStream.readObject();
        } catch (final Exception exception) {
            throw new RuntimeException("Error turning byte into ItemStack", exception);
        }
    }


    private ArrayList<ItemStack> removeEmptySlots(ItemStack[] itemStacks){
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        for (ItemStack itemStack : itemStacks) {
            if(itemStack != null){
                items.add(itemStack);
            }
        }
        return items;
    }


    private ItemStack[] removeUnsedSlots(ItemStack[] itemStacks){
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        List<Integer> unsedSlots = StelyTeamPlugin.getPlugin().getConfig().getIntegerList("inventories.storage.emptyCase.slots");
        for (int i = 0; i < itemStacks.length; i++) {
            if (!unsedSlots.contains(i)){
                items.add(itemStacks[i]);
            }
        }
        return items.toArray(new ItemStack[0]);
    }
}
