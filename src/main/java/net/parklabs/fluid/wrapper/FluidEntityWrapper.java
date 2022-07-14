package net.parklabs.fluid.wrapper;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public final class FluidEntityWrapper<E extends Entity> {

  private static final Map<UUID, FluidEntityWrapper<? extends Entity>> WRAPPER_CACHE = new LinkedHashMap<>();

  private final E entity;
  private double entityDistance;

  public FluidEntityWrapper(E player, double entityDistance) {
    this.entity = player;
    this.entityDistance = entityDistance;
  }

  @Contract(value = "_ -> new", pure = true)
  public static <T extends Entity> @NotNull FluidEntityWrapper<T> of(@NotNull T entity) {
    if(!WRAPPER_CACHE.containsKey(entity.getUniqueId())){
      WRAPPER_CACHE.put(entity.getUniqueId(), new FluidEntityWrapper<>(entity, 4096));
    }
    return (FluidEntityWrapper<T>) WRAPPER_CACHE.get(entity.getUniqueId());
  }

  public double entityDistance() {
    return this.entityDistance;
  }

  public void entityDistance(double entityDistance) {
    this.entityDistance = entityDistance;
  }

  public E entity() {
    return this.entity;
  }
}
