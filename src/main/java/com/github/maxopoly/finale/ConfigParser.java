package com.github.maxopoly.finale;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;

import static vg.civcraft.mc.civmodcore.util.ConfigParsing.parseTime;

import com.github.maxopoly.finale.misc.SaturationHealthRegenHandler;
import com.github.maxopoly.finale.misc.WeaponModifier;

public class ConfigParser {
	private Finale plugin;
	private FinaleManager manager;
	private boolean pearlEnabled;
	private long pearlCooldown;
	private boolean combatTagOnPearl;
	private boolean refundPearls;

	public ConfigParser(Finale plugin) {
		this.plugin = plugin;
	}

	public FinaleManager parse() {
		plugin.info("Parsing config");
		plugin.saveDefaultConfig();
		plugin.reloadConfig();
		FileConfiguration config = plugin.getConfig();
		// Attack Speed modification for all players
		boolean debug = config.getBoolean("debug", false);
		boolean attackEnabled = config.getBoolean("alterAttack.enabled", true);
		double attackSpeed = config.getDouble("alterAttack.speed", 9.4);
		// Food Health Regen modifications for all players
		boolean regenEnabled = config.getBoolean("foodHealthRegen.enabled", false);
		SaturationHealthRegenHandler regenhandler = regenEnabled ? parseHealthRegen(config
				.getConfigurationSection("foodHealthRegen")) : null;
		// Pearl cooldown changes
		this.pearlEnabled = parsePearls(config.getConfigurationSection("pearls"));
		WeaponModifier weapMod = parseWeaponModification(config.getConfigurationSection("weaponModification"));
		
		Collection <Enchantment> disabledEnchants = parseDisableEnchantments(config);
		
		Double splashHealth = config.contains("gameplay.splashHealthModifier") ? config.getDouble("gameplay.splashHealthModifier", 0.0d) : null;

		Integer strengthMultiplier = config.contains("gameplay.strengthMultiplier") ? config.getInt("gameplay.strengthMultiplier", 0) : null;
		Integer weaknessMultiplier = config.contains("gameplay.weaknessMultiplier") ? config.getInt("gameplay.weaknessMultiplier", 0) : null;
		// Initialize the manager
		manager = new FinaleManager(debug, attackEnabled, attackSpeed, regenEnabled, regenhandler, weapMod, disabledEnchants, splashHealth,
				strengthMultiplier, weaknessMultiplier);
		return manager;
	}

	private SaturationHealthRegenHandler parseHealthRegen(ConfigurationSection config) {
		// default values are vanilla 1.8 behavior
		int interval = (int) parseTime(config.getString("interval", "4s"));
		float exhaustionPerHeal = (float) config.getDouble("exhaustionPerHeal", 3.0);
		int minimumFood = config.getInt("minimumFood", 18);
		double healthPerCycle = config.getDouble("healthPerCycle", 1.0);
		boolean blockFoodRegen = config.getBoolean("blockFoodRegen", true);
		boolean blockSaturationRegen = config.getBoolean("blockSaturationRegen", true);
		return new SaturationHealthRegenHandler(interval, healthPerCycle, minimumFood, exhaustionPerHeal,
				blockSaturationRegen, blockFoodRegen);
	}

	private boolean parsePearls(ConfigurationSection config) {
		if (config == null || !config.getBoolean("enabled", false)) {
			return false;
		}
		pearlCooldown = parseTime(config.getString("cooldown", "10s"));
		combatTagOnPearl = config.getBoolean("combatTag", true)
				&& Bukkit.getPluginManager().isPluginEnabled("CombatTagPlus");
		refundPearls = config.getBoolean("refundBlockedPearls", false);
		return true;
	}

	private WeaponModifier parseWeaponModification(ConfigurationSection config) {
		WeaponModifier wm = new WeaponModifier();
		if (config == null) {
			return wm;
		}
		for(String key : config.getKeys(false)) {
			ConfigurationSection current = config.getConfigurationSection(key);
			if (current == null) {
				plugin.warning("Found invalid value " + key + " at " + config + " only mapping values allowed here");
				continue;
			}
			String matString = current.getString("material");
			if (matString == null) {
				plugin.warning("Found no material specified at " + current + ". Skipping attack damage adjustment");
				continue;
			}
			Material mat;
			try {

				mat = Material.valueOf(matString);
			} catch (IllegalArgumentException e) {
				plugin.warning("Found invalid material " + matString + " specified at " + current
						+ ". Skipping attack damage adjustment for it");
				continue;
			}
			int damage = current.getInt("damage", -1);
			double attackSpeed = current.getDouble("attackSpeed", -1.0);
			wm.addWeapon(mat, damage, attackSpeed);
		}
		return wm;
	}

	private Map<Material, Integer> parseAdjustedDamage(ConfigurationSection config) {
		Map<Material, Integer> damages = new HashMap<Material, Integer>();
		if (config == null) {
			return damages;
		}
		for (String key : config.getKeys(false)) {
			ConfigurationSection current = config.getConfigurationSection(key);
			if (current == null) {
				plugin.warning("Found invalid value " + key + " at " + config + " only mapping values allowed here");
				continue;
			}
			String matString = current.getString("material");
			if (matString == null) {
				plugin.warning("Found no material specified at " + current + ". Skipping attack damage adjustment");
				continue;
			}
			Material mat;
			try {

				mat = Material.valueOf(matString);
			} catch (IllegalArgumentException e) {
				plugin.warning("Found invalid material " + matString + " specified at " + current
						+ ". Skipping attack damage adjustment for it");
				continue;
			}
			int damage = current.getInt("damage", -1);
			if (damage == -1) {
				plugin.warning("Found no damage specified at " + current + ". Skipping attack damage adjustment");
				continue;
			}
			damages.put(mat, damage);
		}
		return damages;
	}

	public boolean isPearlEnabled() {
		return pearlEnabled;
	}

	public long getPearlCoolDown() {
		return pearlCooldown;
	}

	public boolean combatTagOnPearl() {
		return combatTagOnPearl;
	}
	
	public boolean refundBlockedPearls() {
		return refundPearls;
	}
	
	private Collection<Enchantment> parseDisableEnchantments(ConfigurationSection config) {
		List <Enchantment> enchants = new LinkedList<Enchantment>();
		if (!config.isList("disabledEnchantments")) {
			return enchants;
		}
		for(String ench : config.getStringList("disabledEnchantments")) {
			Enchantment en = Enchantment.getByName(ench);
			if (en == null) {
				plugin.warning("Could not parse disabled enchantment " + ench);
			}
			else {
				enchants.add(en);
			}
		}
		return enchants;
	}

}
