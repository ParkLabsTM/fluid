package net.parklabs.fluid.api;

import net.parklabs.fluid.wrapper.FluidEntityWrapper;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface FluidEntity {
  void equip(@NotNull EquipmentSlot slot, ItemStack stack, boolean silent);

  void moveTo(@NotNull Location location);

  void headPose(@NotNull EulerAngle angle);

  void customName(String name, boolean visible);

  void tickFluid();

  void tickMetaData();

  List<Player> viewersAsPlayers();

  boolean canSeeBy(@NotNull FluidEntityWrapper<Entity> entityWrapper);

  void destroyFor(@NotNull FluidEntityWrapper<Entity> entityWrapper);

  void spawnFor(@NotNull FluidEntityWrapper<Entity> entityWrapper);

  void gravity(boolean gravity);
}
