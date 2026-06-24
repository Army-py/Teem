package fr.army.stelyteam.utils.manager.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import fr.army.stelyteam.StelyTeamPlugin;


public class ItemStackSerializer {
    private static final byte[] STORAGE_FORMAT_HEADER = new byte[] {'S', 'T', 'N', 'B', 'T', '1', 0};
    private static final String STORAGE_ITEMS_KEY = "items";

    public String serializeToBase64(ItemStack[] itemStack) {
        return Base64.getMimeEncoder().encodeToString(serializeToByte(itemStack));
    }


    public ItemStack[] deserializeFromBase64(String base64) {
        return deserializeFromByte(Base64.getMimeDecoder().decode(base64));
    }


    public byte[] serializeToByte(ItemStack[] itemStack) {
        try {
            final ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            arrayOutputStream.write(STORAGE_FORMAT_HEADER);
            final ReadWriteNBT storageNbt = NBT.createNBTObject();
            storageNbt.setItemStackArray(STORAGE_ITEMS_KEY, itemStack);
            storageNbt.writeCompound(arrayOutputStream);

            return arrayOutputStream.toByteArray();
        } catch (final Exception exception) {
            throw new RuntimeException("Error turning ItemStack into NBT storage bytes", exception);
        }
    }


    public ItemStack[] deserializeFromByte(byte[] bytes) {
        if (bytes.length == 0) return new ItemStack[0];
        if (isNbtStorageFormat(bytes)) return deserializeFromNbtStorage(bytes);
        return deserializeLegacyBukkitStorage(bytes);
    }


    private ItemStack[] deserializeFromNbtStorage(byte[] bytes) {
        try {
            final ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(
                    bytes,
                    STORAGE_FORMAT_HEADER.length,
                    bytes.length - STORAGE_FORMAT_HEADER.length
            );
            return NBT.readNBT(arrayInputStream).getItemStackArray(STORAGE_ITEMS_KEY);
        } catch (final Exception exception) {
            throw new RuntimeException("Error turning NBT storage bytes into ItemStack", exception);
        }
    }


    private ItemStack[] deserializeLegacyBukkitStorage(byte[] bytes) {
        try {
            final ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(bytes);
            final BukkitObjectInputStream objectInputStream = new BukkitObjectInputStream(arrayInputStream);
            return (ItemStack[]) objectInputStream.readObject();
        } catch (final Exception exception) {
            throw new RuntimeException("Error turning legacy Bukkit storage bytes into ItemStack", exception);
        }
    }


    private boolean isNbtStorageFormat(byte[] bytes) {
        if (bytes.length < STORAGE_FORMAT_HEADER.length) return false;
        for (int i = 0; i < STORAGE_FORMAT_HEADER.length; i++) {
            if (bytes[i] != STORAGE_FORMAT_HEADER[i]) return false;
        }
        return true;
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
