/*
 * MIT License
 *
 * Copyright 2020 klikli-dev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.klikli_dev.occultism.common.container.storage;

import com.github.klikli_dev.occultism.api.common.data.GlobalBlockPos;
import com.github.klikli_dev.occultism.client.gui.storage.StorageControllerGuiBase;
import com.github.klikli_dev.occultism.common.misc.StorageControllerCraftingInventory;
import com.github.klikli_dev.occultism.common.tile.StableWormholeTileEntity;
import com.github.klikli_dev.occultism.common.tile.StorageControllerTileEntity;
import com.github.klikli_dev.occultism.network.MessageUpdateLinkedMachines;
import com.github.klikli_dev.occultism.network.OccultismPackets;
import com.github.klikli_dev.occultism.registry.OccultismContainers;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class StableWormholeContainer extends StorageControllerContainerBase {
    //region Fields
    protected StorageControllerTileEntity storageController;
    protected StableWormholeTileEntity stableWormhole;

    //endregion Fields
    //region Initialization
    public StableWormholeContainer(int id, PlayerInventory playerInventory,
                                   StableWormholeTileEntity stableWormhole) {
        super(OccultismContainers.STABLE_WORMHOLE.get(), id, playerInventory);
        this.stableWormhole = stableWormhole;
        this.storageController = (StorageControllerTileEntity) stableWormhole.getLinkedStorageController();
        this.matrix = new StorageControllerCraftingInventory(this, stableWormhole.getMatrix());
        this.orderInventory.setInventorySlotContents(0, this.stableWormhole.getOrderStack());

        this.setupCraftingOutput(); //output is slot 0

        this.setupCraftingGrid();
        this.setupOrderInventorySlot();
        this.setupPlayerInventorySlots();
        this.setupPlayerHotbar();

        this.onCraftMatrixChanged(this.matrix);
    }
    //endregion Initialization

    //region Getter / Setter
    public StableWormholeTileEntity getStableWormhole() {
        return this.stableWormhole;
    }
    //endregion Getter / Setter

    //region Overrides
    @Override
    protected void setupPlayerHotbar() {
        int hotbarTop = 232;
        int hotbarLeft = 8 + StorageControllerGuiBase.ORDER_AREA_OFFSET;
        for (int i = 0; i < 9; i++)
            this.addSlot(new Slot(this.playerInventory, i, hotbarLeft + i * 18, hotbarTop));
    }

    @Override
    public StorageControllerTileEntity getStorageController() {
        return this.storageController;
    }

    @Override
    public GlobalBlockPos getStorageControllerGlobalBlockPos() {
        return this.stableWormhole.getLinkedStorageControllerPosition();
    }

    @Override
    public boolean isContainerItem() {
        return false;
    }

    @Override
    public void updateCraftingSlots(boolean force) {
        for (int i = 0; i < 9; i++) {
            this.stableWormhole.getMatrix().put(i, this.matrix.getStackInSlot(i));
        }
        if (force)
            this.stableWormhole.markNetworkDirty();
    }

    @Override
    public void updateOrderSlot(boolean force) {
        this.stableWormhole.setOrderStack(this.orderInventory.getStackInSlot(0));
        if (force)
            this.stableWormhole.markNetworkDirty();
    }

    @Override
    public boolean canMergeSlot(ItemStack stack, Slot slot) {
        return slot.inventory != this.result && super.canMergeSlot(stack, slot);
    }


    @Override
    public boolean canInteractWith(PlayerEntity player) {
        if (this.storageController == null || this.stableWormhole == null)
            return false;
        World world = this.stableWormhole.getWorld();
        //send stack updates on a slow tick while interacting
        if (!world.isRemote && world.getGameTime() % 40 == 0) {
            OccultismPackets.sendTo((ServerPlayerEntity) player, this.storageController.getMessageUpdateStacks());
            OccultismPackets.sendTo((ServerPlayerEntity) player,
                    new MessageUpdateLinkedMachines(this.storageController.getLinkedMachines()));
        }
        BlockPos wormholePosition = this.stableWormhole.getPos();
        return player.getDistanceSq(wormholePosition.getX() + 0.5D, wormholePosition.getY() + 0.5D,
                wormholePosition.getZ() + 0.5D) <= 64.0D;
    }
    //endregion Overrides
}
