package pl.shooter.engine.ecs.components;

import pl.shooter.engine.ecs.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores all weapons available to an entity and manages the active selection.
 */
public class InventoryComponent implements Component {
    public List<WeaponComponent> weapons = new ArrayList<>();
    public int currentWeaponIndex = 0;

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
}
