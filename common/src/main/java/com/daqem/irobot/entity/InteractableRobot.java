package com.daqem.irobot.entity;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import org.jetbrains.annotations.Nullable;

public interface InteractableRobot {
    ContainerData getContainerData();
    Container getInventory();
    @Nullable Player getInteractingPlayer();
    void setInteractingPlayer(@Nullable Player player);
    boolean stillValid(Player player);
    int getEntityId();
}
