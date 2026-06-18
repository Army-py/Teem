package fr.army.stelyteam.utils.builder;

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
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ItemBuilder {
	private static final UUID GENERIC_UUID = new UUID(0L, 0L);
	private static final Pattern TEXTURE_URL_PATTERN = Pattern.compile("\"url\"\\s*:\\s*\"([^\"]+)\"");

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


	private static ItemStack getCustomHead(String texture, String buttonName, String name, List<String> lore, UUID uuid) {
		ItemStack item = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta skullMeta = (SkullMeta) item.getItemMeta();

		if (texture != null) {
			applyTexture(skullMeta, uuid == null ? GENERIC_UUID : uuid, texture);
		}

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

	private static void applyTexture(SkullMeta skullMeta, UUID uuid, String texture) {
		URL skinUrl = getSkinUrl(texture);
		if (skinUrl == null) {
			return;
		}

		PlayerProfile profile = Bukkit.createPlayerProfile(uuid);
		PlayerTextures textures = profile.getTextures();
		textures.setSkin(skinUrl);
		profile.setTextures(textures);
		skullMeta.setOwnerProfile(profile);
	}

	private static URL getSkinUrl(String texture) {
		String cleanTexture = texture.trim();
		if (cleanTexture.isEmpty()) {
			return null;
		}

		if (cleanTexture.startsWith("http://") || cleanTexture.startsWith("https://")) {
			return toUrl(cleanTexture);
		}

		if (cleanTexture.matches("[a-fA-F0-9]{32,}")) {
			return toUrl("http://textures.minecraft.net/texture/" + cleanTexture);
		}

		String paddedTexture = cleanTexture + "=".repeat((4 - cleanTexture.length() % 4) % 4);
		try {
			String decodedTexture = new String(Base64.getDecoder().decode(paddedTexture), StandardCharsets.UTF_8);
			Matcher matcher = TEXTURE_URL_PATTERN.matcher(decodedTexture);
			if (matcher.find()) {
				return toUrl(matcher.group(1).replace("\\/", "/"));
			}
		} catch (IllegalArgumentException ignored) {
			return null;
		}
		return null;
	}

	private static URL toUrl(String value) {
		try {
			return new URL(value);
		} catch (MalformedURLException exception) {
			return null;
		}
	}
}
