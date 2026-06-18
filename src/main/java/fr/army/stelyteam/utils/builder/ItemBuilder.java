package fr.army.stelyteam.utils.builder;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import fr.army.stelyteam.StelyTeamPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


public class ItemBuilder {
	private static final Field profileField;
	private static final Constructor<?> profileConstructor;
	private static final UUID genericUUID;

	static {
		genericUUID = new UUID(0L, 0L);
		try {
			final Class<?> cSkullMetaClass = Class
					.forName(Bukkit.getServer().getClass().getPackage().getName() + ".inventory.CraftMetaSkull");
			profileField = cSkullMetaClass.getDeclaredField("profile");
			profileField.setAccessible(true);
			final Class<?> nmsResolvableProfileClass = Class
					.forName("net.minecraft.world.item.component.ResolvableProfile");
			profileConstructor = nmsResolvableProfileClass.getDeclaredConstructor(GameProfile.class);
			profileConstructor.setAccessible(true);
		} catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	public static ItemStack getItem(Material material, String buttonName, String displayName, List<String> lore, String headTexture, boolean isEnchanted) {
		if (material.equals(Material.PLAYER_HEAD) && !headTexture.isBlank()) return getCustomHead(headTexture, buttonName, displayName, lore, null);

		ItemStack item = new ItemStack(material, 1);
		ItemMeta meta = item.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
		meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		if(!lore.isEmpty()) {
			List<String> loreList = lore;
			meta.setLore(loreList);
		}

		if (isEnchanted) {
			meta.addEnchant(Enchantment.INFINITY, 1, true);
		}

		NamespacedKey key = new NamespacedKey(StelyTeamPlugin.getPlugin(), "buttonName");
		meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, buttonName);

		meta.setDisplayName(displayName);
		item.setItemMeta(meta);
		return item;
	}


	public static ItemStack getPlayerHead(UUID uuid, String name, List<String> lore) {
		return getCustomHead(null, null, name, lore, uuid);
	}


	private static void applyProfile(SkullMeta meta, GameProfile profile) {
		Objects.requireNonNull(meta);
		try {
			profileField.set(meta, profileConstructor.newInstance(profile));
		} catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
			throw new RuntimeException(e);
		}
	}

	private static GameProfile getProfile(String textures, String signature) {
		return getProfile(genericUUID, textures, signature);
	}

	private static GameProfile getProfile(UUID uuid, String textures, String signature) {
		final GameProfile profile = new GameProfile(uuid, "");
		profile.getProperties().put("textures", new Property("textures", textures, signature));
		return profile;
	}

	private static ItemStack getCustomHead(String texture, String buttonName, String name, List<String> lore, UUID uuid) {
		ItemStack item = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta skullMeta = (SkullMeta) item.getItemMeta();

		UUID headUuid = uuid == null ? genericUUID : uuid;
		// String playerName = uuid == null ? "" : Bukkit.getOfflinePlayer(uuid).getName();
		//
		// if (playerName == null) {
		// 	playerName = "";
		// }

		GameProfile profile;
		if (texture != null) {
			profile = getProfile(headUuid, texture, "");
			applyProfile(skullMeta, profile);
		}
		// else {
		// 	profile = new GameProfile(headUuid, "Steve");
		// }

		// applyProfile(skullMeta, profile);

		skullMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		skullMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
		skullMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		skullMeta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
		skullMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		if(!lore.isEmpty()) {
			List<String> loreList = lore;
			skullMeta.setLore(loreList);
		}

		if (uuid != null){
			NamespacedKey key = new NamespacedKey(StelyTeamPlugin.getPlugin(), "uuid");
			skullMeta.getPersistentDataContainer().set(key, PersistentDataType.LONG_ARRAY, new long[]{uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()});
		}

		if (buttonName != null){
			NamespacedKey key = new NamespacedKey(StelyTeamPlugin.getPlugin(), "buttonName");
			skullMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, buttonName);
		}

		skullMeta.setDisplayName(name);
		item.setItemMeta(skullMeta);
		return item;
	}
}
