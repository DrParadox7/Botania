/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Feb 15, 2014, 9:47:56 PM (GMT)]
 */
package vazkii.botania.common.block.subtile.generating;

import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.AxisAlignedBB;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.SubTileGenerating;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.lexicon.LexiconData;

public class SubTileEndoflame extends SubTileGenerating {

	private static final String TAG_BURN_TIME = "burnTime";
	private static final String TAG_BURNED_OUT = "burnOut";
	private static final int FUEL_CAP = 16000;
	private static final int RANGE = 1;

	int burnTime = 0;
	boolean burnOut = false;

	@Override
	public void onUpdate() {
		super.onUpdate();

		if(linkedCollector != null) {
			boolean didSomething = false;
			if (ticksExisted % 20 == 0) {
				List<EntityItem> items = supertile.getWorldObj().getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.getBoundingBox(supertile.xCoord - RANGE, supertile.yCoord - RANGE, supertile.zCoord - RANGE, supertile.xCoord + RANGE + 1, supertile.yCoord + RANGE + 1, supertile.zCoord + RANGE + 1));
				for (EntityItem item : items) {
					if (item.age >= (59 + getSlowdownFactor()) && !item.isDead) {
						ItemStack stack = item.getEntityItem();
						if (stack.getItem().hasContainerItem(stack))
							continue;

						int itemFuel = stack == null || stack.getItem() == Item.getItemFromBlock(ModBlocks.spreader) ? 0 : TileEntityFurnace.getItemBurnTime(stack);
						if (itemFuel > 0 && stack.stackSize > 0) {
							this.burnTime = Math.min(FUEL_CAP, this.burnTime + itemFuel / 3);

							if (!supertile.getWorldObj().isRemote) {
								stack.stackSize--;
								supertile.getWorldObj().playSoundEffect(supertile.xCoord, supertile.yCoord, supertile.zCoord, "botania:endoflame", 0.2F, 1F);

								if (stack.stackSize == 0)
									item.setDead();

								didSomething = true;

							} else {
								item.worldObj.spawnParticle("largesmoke", item.posX, item.posY + 0.1, item.posZ, 0.0D, 0.0D, 0.0D);
								item.worldObj.spawnParticle("flame", item.posX, item.posY, item.posZ, 0.0D, 0.0D, 0.0D);
							}


							break;
						}
					}
				}
				if (didSomething)
					sync();
			}

			if (burnOut) {
				if(supertile.getWorldObj().rand.nextInt(3) == 0)
					supertile.getWorldObj().spawnParticle("largesmoke", supertile.xCoord + 0.4, supertile.yCoord + 0.65, supertile.zCoord + 0.4, 0.0D, 0.0D, 0.0D);

				if (burnTime == 0) {
					if (mana == 0) { burnOut = false; }
				} else
					burnTime = Math.min(burnTime -2, 0);
			}
			else if(burnTime > 0) {
				if (supertile.getWorldObj().rand.nextInt(10) == 0)
					supertile.getWorldObj().spawnParticle("flame", supertile.xCoord + 0.4 + Math.random() * 0.2, supertile.yCoord + 0.65, supertile.zCoord + 0.4 + Math.random() * 0.2, 0.0D, 0.0D, 0.0D);

				if (mana + getValueForPassiveGeneration() > getMaxMana())
					burnOut = true;

				burnTime--;
			}
		}
	}

	@Override
	public int getMaxMana() {
		return 900;
	}

	@Override
	public int getMaxManaTransfer() { return 2; }

	@Override
	public int getValueForPassiveGeneration() {
		return 11;
	}

	@Override
	public int getDelayBetweenPassiveGeneration() {
		return 5;
	}

	@Override
	public int getColor() {
		return 0x785000;
	}

	@Override
	public RadiusDescriptor getRadius() {
		return new RadiusDescriptor.Square(toChunkCoordinates(), RANGE);
	}

	@Override
	public LexiconEntry getEntry() {
		return LexiconData.endoflame;
	}

	@Override
	public void writeToPacketNBT(NBTTagCompound cmp) {
		super.writeToPacketNBT(cmp);

		cmp.setInteger(TAG_BURN_TIME, burnTime);
		cmp.setBoolean(TAG_BURNED_OUT, burnOut);
	}

	@Override
	public void readFromPacketNBT(NBTTagCompound cmp) {
		super.readFromPacketNBT(cmp);

		burnTime = cmp.getInteger(TAG_BURN_TIME);
		burnOut = cmp.getBoolean(TAG_BURNED_OUT);
	}

	@Override
	public boolean canGeneratePassively() {
		return burnTime > 0 && !burnOut;
	}
}
