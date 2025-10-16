package com.daqem.irobot.client.entity;

import com.daqem.irobot.entity.InteractableRobot;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

public class ClientSideInteractableRobot implements InteractableRobot {

    private final ContainerData containerData = new SimpleContainerData(3);
    private final Container inventory = new SimpleContainer(28);
    private Player interactingPlayer;

    public ClientSideInteractableRobot(Player interactingPlayer) {
        this.interactingPlayer = interactingPlayer;
    }

    @Override
    public ContainerData getContainerData() {
        return containerData;
    }

    @Override
    public Container getInventory() {
        return inventory;
    }

    @Override
    public Player getInteractingPlayer() {
        return interactingPlayer;
    }

    @Override
    public void setInteractingPlayer(Player player) {
        this.interactingPlayer = player;
    }

    @Override
    public boolean stillValid(Player player) {
        return player == this.interactingPlayer;
    }

    @Override
    public int getEntityId() {
        return this.containerData.get(0);
    }
}
