package com.github.maxopoly.finale;

import java.util.Collection;

import org.bukkit.enchantments.Enchantment;

import com.github.maxopoly.finale.misc.SaturationHealthRegenHandler;
import com.github.maxopoly.finale.misc.WeaponModifier;

public class FinaleManager {
	
	private boolean debug;
	private boolean attackSpeedEnabled;
	private double attackSpeed;
	private boolean regenHandlerEnabled;
	private SaturationHealthRegenHandler regenHandler;
	private WeaponModifier weaponModifier;
	private boolean protocolLibEnabled;
	private Collection <Enchantment> disabledEnchantments;
	
	private Double splashHealth;
	
	private Integer strengthMultiplier;
	
	public FinaleManager(boolean debug, boolean attackSpeedEnabled, double attackSpeed, boolean regenHandlerEnabled,
			SaturationHealthRegenHandler regenHandler,WeaponModifier weaponModifier, Collection <Enchantment> disabledEnchants,
			Double splashHealth, Integer strengthMultiplier) {
		this.attackSpeedEnabled = attackSpeedEnabled;
		this.attackSpeed = attackSpeed;
		this.regenHandlerEnabled = regenHandlerEnabled;
		this.regenHandler = regenHandler;
		this.weaponModifier = weaponModifier;
		this.disabledEnchantments = disabledEnchants;
		this.splashHealth = splashHealth;
		this.strengthMultiplier = strengthMultiplier;
	}
	
	public boolean isDebug() {
		return debug;
	}
	
	public boolean isAttackSpeedEnabled() {
		return attackSpeedEnabled;
	}

	public double getAttackSpeed() {
		return attackSpeed;
	}
	
	public boolean isRegenHandlerEnabled() {
		return regenHandlerEnabled;
	}

	public SaturationHealthRegenHandler getPassiveRegenHandler() {
		return regenHandler;
	}
	
	public WeaponModifier getWeaponModifer() {
	    return weaponModifier;
	}
	
	public boolean isDisabledEnchantment(Enchantment enchant) {
		return disabledEnchantments.contains(enchant);
	}
	
	public Double getSplashHealth() {
		return splashHealth;
	}
	
	public Integer getStrengthMultiplier() {
		return strengthMultiplier;
	}
}
