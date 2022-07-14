package net.parklabs.packets;

import net.minecraft.network.protocol.Packet;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public final class Packets {
  public static void send(Player player, Packet<?> packet) {
    ((CraftPlayer)player).getHandle().connection.send(packet);
  }
}
