package net.parklabs.fluid.entity;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Rotations;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import net.parklabs.fluid.api.FluidEntity;
import net.parklabs.fluid.wrapper.FluidEntityWrapper;
import net.parklabs.packets.Packets;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Consumer;
import org.bukkit.util.EulerAngle;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class FluidArmorStand extends ArmorStand implements FluidEntity {

  private static final Map<UUID,FluidArmorStand> ENTITY_INSTANCES = new LinkedHashMap<>();

  public static @NotNull FluidArmorStand create(Location location, @NotNull Consumer<FluidArmorStand> consumer){
    FluidArmorStand armorStand = new FluidArmorStand(location.getWorld(), location);
    ENTITY_INSTANCES.put(armorStand.getUUID(), armorStand);
    consumer.accept(armorStand);
    return armorStand;
  }

  private final List<UUID> viewers = new LinkedList<>();

  private List<Pair<net.minecraft.world.entity.EquipmentSlot, net.minecraft.world.item.ItemStack>> equipment = new LinkedList<>();

  protected FluidArmorStand(World world, Location location) {
    super(EntityType.ARMOR_STAND, ((CraftWorld)world).getHandle());
    this.moveTo(location);
  }

  public static void tickAll() {
    new ArrayList<>(ENTITY_INSTANCES.values()).forEach(FluidArmorStand::tickFluid);
  }

  private double distanceToEntity(@NotNull Entity entity){
    Vec3 pos = position();
    Vec3 to = new Vec3(entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ());
    return pos.distanceToSqr(to);
  }

  @Override
  public boolean canSeeBy(@NotNull FluidEntityWrapper<Entity> entityWrapper){
    return distanceToEntity(entityWrapper.entity()) <= entityWrapper.entityDistance();
  }

  @Override
  public void equip(@NotNull EquipmentSlot slot, ItemStack stack, boolean silent){
    this.onEquipItem(
      net.minecraft.world.entity.EquipmentSlot.valueOf(slot.name()),
      CraftItemStack.asNMSCopy(stack), CraftItemStack.asNMSCopy(stack),
      silent
    );

    this.equipment = new LinkedList<>();
    this.equipment.add(Pair.of(net.minecraft.world.entity.EquipmentSlot.valueOf(slot.name()), CraftItemStack.asNMSCopy(stack)));
    ClientboundSetEquipmentPacket setEquipmentPacket = new ClientboundSetEquipmentPacket(this.getId(), equipment);

    for (Player player : viewersAsPlayers()) {
      Packets.send(player, setEquipmentPacket);
    }

    this.detectEquipmentUpdates();
  }

  @Override
  public void moveTo(@NotNull Location location){
    this.moveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
  }

  @Override
  public void headPose(@NotNull EulerAngle angle){
    this.setHeadPose(new Rotations(
      (float) Math.toDegrees(angle.getX()),
      (float) Math.toDegrees(angle.getY()),
      (float) Math.toDegrees(angle.getZ())
    ));
  }

  @Override
  public void tickFluid(){
    for (Player player : Bukkit.getServer().getOnlinePlayers()) {
      FluidEntityWrapper<Entity> entityWrapper = FluidEntityWrapper.of(player);

      if(canSeeBy(entityWrapper)){
        spawnFor(entityWrapper);
      } else {
        destroyFor(entityWrapper);
      }
    }

    tickPositions();
    tickMetaData();
  }


  private void tickPositions() {
    ClientboundTeleportEntityPacket teleportEntityPacket = new ClientboundTeleportEntityPacket(this);
    for (Player player : viewersAsPlayers()) {
      Packets.send(player, teleportEntityPacket);
    }
  }

  @Override
  public void tickMetaData() {
    ClientboundSetEntityDataPacket setEntityDataPacket = new ClientboundSetEntityDataPacket(this.getId(), entityData, true);
    for (Player player : viewersAsPlayers()) {
      Packets.send(player, setEntityDataPacket);
    }
  }

  @Override
  public List<Player> viewersAsPlayers() {
    List<UUID> uuids = new ArrayList<>(viewers);
    return uuids.stream().filter(uuid1 -> Bukkit.getPlayer(uuid1) != null).map(Bukkit::getPlayer).collect(Collectors.toList());
  }

  @Override
  public void destroyFor(@NotNull FluidEntityWrapper<Entity> entityWrapper) {
    if(entityWrapper.entity() instanceof Player player) {
      if (viewers.contains(player.getUniqueId())) {
        ClientboundRemoveEntitiesPacket removeEntitiesPacket = new ClientboundRemoveEntitiesPacket(this.getId());
        Packets.send(player, removeEntitiesPacket);

        viewers.remove(player.getUniqueId());
      }
    }
  }

  @Override
  public void spawnFor(@NotNull FluidEntityWrapper<Entity> entityWrapper) {
    if(entityWrapper.entity() instanceof Player player){
      if(!viewers.contains(player.getUniqueId())){

        ClientboundAddEntityPacket entityAddPacket = new ClientboundAddEntityPacket(this.getId(), this.uuid, position().x, position().y,position().z, this.xRotO, this.yRot, EntityType.ARMOR_STAND, 0, Vec3.ZERO, 0.0);
        ClientboundSetEntityDataPacket setEntityDataPacket = new ClientboundSetEntityDataPacket(this.getId(), entityData, false);
        ClientboundSetEquipmentPacket equipmentPacket = new ClientboundSetEquipmentPacket(this.getId(), this.equipment);
        ClientboundTeleportEntityPacket teleportEntityPacket = new ClientboundTeleportEntityPacket(this);

        Packets.send(player, entityAddPacket);
        Packets.send(player, setEntityDataPacket);
        Packets.send(player, equipmentPacket);
        Packets.send(player, teleportEntityPacket);

        viewers.add(player.getUniqueId());
      }
    }
  }

  @Override
  public void customName(String name, boolean visible){
    this.setCustomName(Component.literal(name));
    this.setCustomNameVisible(visible);
  }

  @Override
  public void gravity(boolean gravity) {
    this.setNoGravity(!gravity);
  }
}
