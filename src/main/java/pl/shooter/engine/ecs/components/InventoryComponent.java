package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;
import pl.shooter.engine.config.models.AmmoPrefab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores all weapons and ammunition types available to an entity.
 */
public class InventoryComponent implements Component {
    // Weapons
    public List<WeaponComponent> weapons = new ArrayList<>();
    public int currentWeaponIndex = 0;

    // Ammunition (AmmoPrefab.id -> quantity)
    public Map<String, Integer> ammoCounts = new HashMap<>();
    
    // Tracks which AmmoPrefab is currently selected for which category
    // Category -> AmmoPrefab.id
    public Map<String, String> activeAmmoPerCategory = new HashMap<>();

    public InventoryComponent() {}

    public void addWeapon(WeaponComponent weapon) {
        weapons.add(weapon);
    }

    public WeaponComponent getActiveWeapon() {
        if (weapons.isEmpty()) return null;
        return weapons.get(currentWeaponIndex);
    }

    public void nextWeapon() {
        if (weapons.isEmpty()) return;
        currentWeaponIndex = (currentWeaponIndex + 1) % weapons.size();
    }

    public void previousWeapon() {
        if (weapons.isEmpty()) return;
        currentWeaponIndex = (currentWeaponIndex - 1 + weapons.size()) % weapons.size();
    }

    public void addAmmo(String ammoId, int amount) {
        ammoCounts.put(ammoId, ammoCounts.getOrDefault(ammoId, 0) + amount);
    }

    public int getAmmoCount(String ammoId) {
        return ammoCounts.getOrDefault(ammoId, 0);
    }
}
