package com.github.maxopoly.finale.listeners;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.FinaleManager;

public class PlayerListener implements Listener {

	private FinaleManager manager;

	public PlayerListener(FinaleManager manager) {
		this.manager = manager;
	}

	@EventHandler
	public void playerLogin(PlayerJoinEvent e) {
		if (manager.isAttackSpeedEnabled()) {;
			// Set attack speed
			AttributeInstance attr = e.getPlayer().getAttribute(
				Attribute.GENERIC_ATTACK_SPEED);
			if (attr != null) {
				attr.setBaseValue(manager.getAttackSpeed());
			}
		}
		if (manager.isRegenHandlerEnabled()) {
			// Register login for custom health regen
			manager.getPassiveRegenHandler().registerPlayer(
					e.getPlayer().getUniqueId());
		}
	}

	@EventHandler
	public void healthRegen(EntityRegainHealthEvent e) {
		if (!manager.isRegenHandlerEnabled()) return;
		if (e.getEntityType() != EntityType.PLAYER) {
			return;
		}
		if (e.getRegainReason() == RegainReason.SATIATED
				&& manager.getPassiveRegenHandler().blockPassiveHealthRegen()) {
			// apparently setting to cancelled doesn't prevent the "consumption" of satiation.
			Player p = (Player) e.getEntity();

			double maxHealth = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
			double spigotRegenExhaustion = ((net.minecraft.server.v1_12_R1.World) ( (org.bukkit.craftbukkit.v1_12_R1.CraftWorld) p.getWorld()).getHandle()).spigotConfig.regenExhaustion;
			float newExhaustion = (float) (p.getExhaustion() - e.getAmount() * spigotRegenExhaustion);

			StringBuffer alterHealth = null;
			if (manager.isDebug()) {
				alterHealth = new StringBuffer("SATIATED: " + p.getName());
				alterHealth.append(":").append(p.getHealth()).append("<").append(maxHealth);
				alterHealth.append(":").append(p.getSaturation()).append(":").append(p.getExhaustion());
				alterHealth.append(":").append(p.getFoodLevel());
			}
			if(newExhaustion < 0) // not 100% sure this is correct route; intention was restoring what spigot takes, but we'll roll with it
				newExhaustion = 0;

			p.setExhaustion(newExhaustion);
			
			if (manager.isDebug()) {
				alterHealth.append(" TO ").append(p.getHealth()).append("<").append(p.getMaxHealth());
				alterHealth.append(":").append(p.getSaturation()).append(":").append(p.getExhaustion());
				alterHealth.append(":").append(p.getFoodLevel());
				Finale.getPlugin().getLogger().info(alterHealth.toString());
			}
			e.setCancelled(true);
			return;
		}
		if (e.getRegainReason() == RegainReason.EATING && manager.getPassiveRegenHandler().blockFoodHealthRegen()) {
			Player p = (Player) e.getEntity();
			double maxHealth = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
			if (manager.isDebug()) {
				StringBuffer alterHealth = new StringBuffer("EATING:" + p.getName());
				alterHealth.append(":").append(p.getHealth()).append("<").append(maxHealth);
				alterHealth.append(":").append(p.getSaturation()).append(":").append(p.getExhaustion());
				alterHealth.append(":").append(p.getFoodLevel());
				Finale.getPlugin().getLogger().info(alterHealth.toString());
			}
			e.setCancelled(true);
		}
	}

	// =================================================
	// Can be used to buff health splash to pre-1.6 levels

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPotionSplash(PotionSplashEvent event) {
		if (manager.getSplashHealth() == null) {
			return;
		}
		for (PotionEffect effect : event.getEntity().getEffects()) {
			if (!(effect.getType().equals(PotionEffectType.HEAL))) {
				return;
			}
		}
		for (LivingEntity entity : event.getAffectedEntities()) {
			if (entity instanceof Player) {
				if (((Damageable) entity).getHealth() > 0d) {
					final double newHealth = Math.min(((Damageable) entity).getHealth() + manager.getSplashHealth(),
							((Damageable) entity).getMaxHealth());
					entity.setHealth(newHealth);
				}
			}
		}
	}
	
	// ================================================
	// Changes Strength Potions, strength_multiplier 3 is roughly Pre-1.6 Level

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerDamage(EntityDamageByEntityEvent event) {
		Integer strengthMultiplier = manager.getStrengthMultiplier();
		if (strengthMultiplier == null) {
			return;
		}
		if (!(event.getDamager() instanceof Player)) {
			return;
		}
		Player player = (Player) event.getDamager();
		
		if (player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
			for (PotionEffect effect : player.getActivePotionEffects()) {
				if (effect.getType().equals(PotionEffectType.INCREASE_DAMAGE)) {
					final int potionLevel = effect.getAmplifier() + 1;
					final double unbuffedDamage = event.getDamage() / (1.3 * potionLevel + 1);
					final double newDamage = unbuffedDamage + (potionLevel * strengthMultiplier);
					event.setDamage(newDamage);
					break;
				}
			}
		}
	}
	
	//@EventHandler
	public void arrowHit(EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof LivingEntity)) {
			return;
		}
		if (e.getDamager().getType() == EntityType.TIPPED_ARROW) {
			return;
		}
	}

}
