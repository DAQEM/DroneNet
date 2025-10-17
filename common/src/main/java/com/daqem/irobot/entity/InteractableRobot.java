package com.daqem.irobot.entity;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import org.jetbrains.annotations.Nullable;

/**
 * Defines the contract for a robot entity that can be interacted with by a player,
 * typically involving a GUI screen.
 */
public interface InteractableRobot {

    /**
     * Gets the container data for syncing menu information between the server and client.
     *
     * @return The container data.
     */
    ContainerData getContainerData();

    /**
     * Gets the robot's inventory.
     *
     * @return The robot's container instance.
     */
    Container getInventory();

    /**
     * Gets the player currently interacting with this robot.
     *
     * @return The interacting player, or null if none.
     */
    @Nullable Player getInteractingPlayer();

    /**
     * Sets the player currently interacting with this robot.
     *
     * @param player The player to set as the interactor.
     */
    void setInteractingPlayer(@Nullable Player player);

    /**
     * Checks if the specified player can still interact with this robot.
     * This is typically used by the menu to determine if it should remain open.
     *
     * @param player The player to check.
     * @return True if the player can still interact, false otherwise.
     */
    boolean stillValid(Player player);

    /**
     * Gets the unique entity ID of the robot.
     *
     * @return The entity ID.
     */
    int getEntityId();
}