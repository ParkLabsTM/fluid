package net.example.placeholder;

import net.parklabs.fluid.entity.FluidArmorStand;
import org.bukkit.Bukkit;
import org.bukkit.Location;

//Example
public final class SpawnEntity {

  public void handle(){
    Location location = new Location(Bukkit.getWorld("world"), 0, 64, 0);
    FluidArmorStand.create(location, stand -> {
      stand.gravity(false);
      stand.customName("example-entity", true);
    });
  }
}
