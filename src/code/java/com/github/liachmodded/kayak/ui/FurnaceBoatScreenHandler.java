/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.github.liachmodded.kayak.ui;

import com.github.liachmodded.kayak.Kayak;
import com.github.liachmodded.kayak.entity.FurnaceBoatEntity;
import com.github.liachmodded.kayak.ui.property.KayakPropertyFactory;
import com.google.common.base.Preconditions;
import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.checkerframework.checker.nullness.qual.Nullable;

public class FurnaceBoatScreenHandler extends InventoryScreenHandler {

  private final FurnaceBoatEntity boat;

  protected FurnaceBoatScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, FurnaceBoatEntity boat) {
    super(syncId, playerInventory, inventory);
    this.boat = Preconditions.checkNotNull(boat);

    addSlots(inventory, playerInventory);
  }

  public static void open(PlayerEntity player, FurnaceBoatEntity boat) {
    ContainerProviderRegistry.INSTANCE.openContainer(KayakScreenHandlerProviders.FURNACE_BOAT, player, buf -> {
      buf.writeVarInt(boat.getEntityId());
    });
  }

  public static @Nullable FurnaceBoatScreenHandler fromPacket(int syncId, Identifier guiId, PlayerEntity player, PacketByteBuf buf) {
    int entityId = buf.readVarInt();
    Entity entity = player.world.getEntityById(entityId);
    if (!(entity instanceof FurnaceBoatEntity)) {
      Kayak.LOGGER.warn("Invalid entity network id {} received!", entityId);
      return null;
    }
    FurnaceBoatEntity boat = (FurnaceBoatEntity) entity;
    // Cannot use default inventory because otherwise it is just discarded by fabric container api
    return new FurnaceBoatScreenHandler(syncId, player.inventory, boat.getBackingInventory(), boat);
  }

  public FurnaceBoatEntity getBoat() {
    return boat;
  }

  public int getFuelProgress() {
    return MathHelper.clamp(boat.getFuel() * 13 / FurnaceBoatEntity.FUEL_CONSUMPTION_THRESHOLD, 0, 13);
  }

  @Override
  protected int getSize() {
    return 1;
  }

  private void addSlots(Inventory inventory_1, PlayerInventory playerInventory_1) {
    this.addSlot(new Slot(inventory_1, 0, 56, 53) {
      @Override
      public boolean canInsert(ItemStack itemStack_1) {
        return AbstractFurnaceBlockEntity.canUseAsFuel(itemStack_1);
      }
    });

    int int_4;
    for (int_4 = 0; int_4 < 3; ++int_4) {
      for (int int_3 = 0; int_3 < 9; ++int_3) {
        this.addSlot(new Slot(playerInventory_1, int_3 + int_4 * 9 + 9, 8 + int_3 * 18, 84 + int_4 * 18));
      }
    }

    for (int_4 = 0; int_4 < 9; ++int_4) {
      this.addSlot(new Slot(playerInventory_1, int_4, 8 + int_4 * 18, 142));
    }

    addProperty(KayakPropertyFactory.of(boat::getFuel));
  }
}
