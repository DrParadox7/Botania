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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.AxisAlignedBB;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.subtile.RadiusDescriptor;
import vazkii.botania.api.subtile.SubTileGenerating;
import vazkii.botania.client.core.handler.HUDHandler;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.lexicon.LexiconData;

public class SubTileEndoflame extends SubTileGenerating {

	private static final String TAG_BURNED_OUT = "burnOut";
	private static final int RANGE = 1;
	int burnOut = 0;

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (linkedCollector != null) {
			boolean didSomething = false;
			if (ticksExisted % 20 == 0) {
				List<EntityItem> items = supertile.getWorldObj().getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.getBoundingBox(supertile.xCoord - RANGE, supertile.yCoord - RANGE, supertile.zCoord - RANGE, supertile.xCoord + RANGE + 1, supertile.yCoord + RANGE + 1, supertile.zCoord + RANGE + 1));
				for (EntityItem item : items) {
					if (item.age >= (59 + getSlowdownFactor()) && !item.isDead) {
						ItemStack stack = item.getEntityItem();
						if (stack.getItem().hasContainerItem(stack) || stack.getItem() == Item.getItemFromBlock(ModBlocks.spreader))
							continue;

						int itemFuel = TileEntityFurnace.getItemBurnTime(stack) * stack.stackSize;

						if (itemFuel > 0 && stack.stackSize > 0) {
							if (burnOut > 0)
								burnOut = Math.min(burnOut + (itemFuel/100), 64);
							else {
								mana += transformProgressive(itemFuel);
								if (mana > getMaxMana()) {
									burnOut = Math.min((burnOut + (mana - getMaxMana()) * 5/100), 64);
									mana = getMaxMana();
								}
							}

							if (!supertile.getWorldObj().isRemote) {
								supertile.getWorldObj().playSoundEffect(supertile.xCoord, supertile.yCoord, supertile.zCoord, burnOut > 0 ? "random.fizz" : "botania:endoflame", 0.2F, 1F);
								item.setDead();

								didSomething = true;

							} else {
								if (burnOut > 0) {
									for (int i = 0; i < 5; i++) {
										double vx = supertile.getWorldObj().rand.nextGaussian() * 0.1;
										double vy = supertile.getWorldObj().rand.nextDouble() * 0.01;
										double vz = supertile.getWorldObj().rand.nextGaussian() * 0.1;

										item.worldObj.spawnParticle("largesmoke", supertile.xCoord + 0.5, supertile.yCoord + 0.1, supertile.zCoord + 0.5, vx, vy, vz);
									}
								} else
									item.worldObj.spawnParticle("flame", item.posX, item.posY, item.posZ, 0.0D, 0.0D, 0.0D);

								item.worldObj.spawnParticle("smoke", item.posX, item.posY + 0.1, item.posZ, 0.0D, 0.0D, 0.0D);
						}

							break;
						}
					}
				}

				if (didSomething)
					sync();

				if (burnOut > 0) {
					// Mana dumping from overheating at a rate of 128 mana/s
					// Operations resumes when empty
					burnOut--;

				} else if (mana > 0) {
					// Mana loss per second to Temperature Differential/Entropy
					// <25% = 2 | <50% = 4 | <75% = 8 | <100% = 16
					int coolingRate = (int) Math.pow(2, getCurrentCapacity());

					mana = Math.max(mana - coolingRate, 0);
				}
			}
		}

		// Display current state through particles
		if (burnOut > 0) {
			if (supertile.getWorldObj().rand.nextInt(5) == 0)
				supertile.getWorldObj().spawnParticle("largesmoke", supertile.xCoord + 0.4 + Math.random() * 0.2, supertile.yCoord + 0.65, supertile.zCoord + 0.4 + Math.random() * 0.2, 0.0D, 0.0D, 0.0D);

		} else if (mana > 0) {
			if (supertile.getWorldObj().rand.nextInt(10) == 0)
				supertile.getWorldObj().spawnParticle("flame", supertile.xCoord + 0.4 + Math.random() * 0.2, supertile.yCoord + 0.65, supertile.zCoord + 0.4 + Math.random() * 0.2, 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	public int getMaxMana() {
		// Increasing this buffs efficiency for high fuel value items
		return 6400;
	}

	@Override
	public int getMaxManaTransfer() {
		// Mana Output per second based on current mana capacity
		// <25% = 8 | <50% = 16 | <75% = 32 | <=100% = 64

		int transferRate = (int) Math.pow(2, getCurrentCapacity() + 1);

		return burnOut > 0 ? -1 : transferRate * 2;
	}

	@Override
	public int getManaTransferDelay() {
		return 10;
	}

	@Override
	public int getColor() {
		return 0x785000;
	}

	@Override
	public void renderHUD(Minecraft mc, ScaledResolution res) {
		super.renderHUD(mc, res);
		if (burnOut > 0) {
			int x = res.getScaledWidth() / 2 - 51;
			int y = res.getScaledHeight() / 2 + 20;

			HUDHandler.renderManaBar(x, y, 0xE81C00, 1F, burnOut, 64);
		}
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

		cmp.setInteger(TAG_BURNED_OUT, burnOut);

	}

	@Override
	public void readFromPacketNBT(NBTTagCompound cmp) {
		super.readFromPacketNBT(cmp);

		burnOut = cmp.getInteger(TAG_BURNED_OUT);
	}

	public int getCurrentCapacity()	{
		// Current capacity expressed between 1-5.
		return (int) (1 + Math.floor((4.0f * mana) / getMaxMana()));
	}

	public double getEfficiency(int quarter){
		// Turns fuelValue into mana at an efficiency rate based on the mana quarter it would fill into
		// <25% = +60% | <50% = -20% | <75% = -60% | <=100% = -80%
		return 1.6f / Math.pow(2, quarter - 1);
	}

	public int transformProgressive(int fuelValue) {
		int valueManaQuarter = getMaxMana() / 4;

		int manaValue = 0;

		// Derive the Gross value from existing (net) mana in buffer;
		int grossMana = estimateGrossFromNet(mana);
		int totalMana = grossMana + fuelValue;

		// Calculate and apply efficiency multipliers to mana progressively
		for (int i = 1; i < 5; i++) {
			int previousQuarter = valueManaQuarter * (i - 1);
			int currentQuarter = valueManaQuarter * i;

			if (totalMana <= previousQuarter || grossMana >= currentQuarter)
				continue;

			double quarterStart = Math.max(grossMana, previousQuarter);
			double quarterEnd = Math.min(totalMana, currentQuarter);

			manaValue += (quarterEnd - quarterStart) * getEfficiency(i);
		}

		// Anything after the 4rth brackets uses the last and worst efficiency rate
		double lastLimit = valueManaQuarter * 4;
		if (totalMana > lastLimit) {
			double overflow = Math.max(grossMana, lastLimit);

			manaValue += (totalMana - overflow) * getEfficiency(4);
		}

		return manaValue;
	}

	public int estimateGrossFromNet(double netMana) {
		int grossMana = 0;
		int valueManaQuarter = getMaxMana() / 4;

		for (int i = 1; i < 5; i++) {
			double bracketNet = valueManaQuarter * getEfficiency(i);

			if (netMana > bracketNet) {
				netMana -= bracketNet;
				grossMana += valueManaQuarter;
			} else {
				grossMana += netMana / getEfficiency(i);
				return grossMana;
			}
		}
		// Anything after the 4rth brackets uses the last and worst efficiency rate
		grossMana += netMana / getEfficiency(4);;
		return grossMana;
	}
}
