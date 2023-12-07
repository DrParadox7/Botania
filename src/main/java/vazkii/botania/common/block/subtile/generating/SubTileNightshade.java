/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Mar 16, 2014, 11:15:42 PM (GMT)]
 */
package vazkii.botania.common.block.subtile.generating;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.subtile.SubTileGenerating;
import vazkii.botania.api.subtile.signature.PassiveFlower;
import vazkii.botania.common.lexicon.LexiconData;

import java.util.ArrayList;

@PassiveFlower
public class SubTileNightshade extends SubTileGenerating {

	private static final String TAG_PRIME_POSITION_X = "primePositionX";
	private static final String TAG_PRIME_POSITION_Y = "primePositionY";
	private static final String TAG_PRIME_POSITION_Z = "primePositionZ";
	private static final String TAG_SAVED_POSITION = "savedPosition";
	private static final String TAG_SAVED_OUTPUT_TIMER = "outputTimer";



	int primePositionX, primePositionY, primePositionZ;
	boolean savedPosition;

	int outputTimer = 0;

	@Override
	public void onUpdate() {
		super.onUpdate();

		if (!supertile.getWorldObj().isRemote) {
			if(isPrime()) {
				if ((!savedPosition || primePositionX != supertile.xCoord || primePositionY != supertile.yCoord || primePositionZ != supertile.zCoord))
					supertile.getWorldObj().setBlockToAir(supertile.xCoord, supertile.yCoord, supertile.zCoord);
				return;
			} else if (supertile.getWorldObj().getWorldTime() % 10 == 0) {
				if (mana > 0) {
					boolean isBright = supertile.getWorldObj().getBlockLightValue(supertile.xCoord, supertile.yCoord, supertile.zCoord) > 7;
					boolean didSomething = false;

					if (isBright) {
						outputTimer = 600;
						didSomething = true;
					}

					//We use skylightSubtracted as that's when the sun is out of the horizon
					if (outputTimer > 0 && supertile.getWorldObj().skylightSubtracted > 7) {
						mana = Math.max(mana - 10, 0);
						didSomething = true;
					}

					if (didSomething)
						sync();
				}
			}
		} else if (supertile.getWorldObj().rand.nextInt(10) == 0 && outputTimer > 0) {
			supertile.getWorldObj().spawnParticle("mobSpell", supertile.xCoord + 0.4 + Math.random() * 0.2, supertile.yCoord + 0.65, supertile.zCoord + 0.4 + Math.random() * 0.2, 0.0D, 0.0D, 0.0D);
		}
		if (outputTimer > 0)
			outputTimer--;
	}

	public void setPrimusPosition() {
		primePositionX = supertile.xCoord;
		primePositionY = supertile.yCoord;
		primePositionZ = supertile.zCoord;

		savedPosition = true;
	}

	@Override
	public ArrayList<ItemStack> getDrops(ArrayList<ItemStack> list) {
		if(isPrime())
			list.clear();

		return super.getDrops(list);
	}

	@Override
	public boolean canGeneratePassively() {
		boolean canSeeSky = supertile.getWorldObj().canBlockSeeTheSky(supertile.xCoord, supertile.yCoord + 1, supertile.zCoord);
		World world = supertile.getWorldObj();

		return (canSeeSky && !world.isRaining() && supertile.getWorldObj().skylightSubtracted > 7 && outputTimer == 0);
	}

	@Override
	public int getColor() {
		return 0x3D2A90;
	}

	@Override
	public LexiconEntry getEntry() {
		return LexiconData.nightshade;
	}

	@Override
	public void writeToPacketNBT(NBTTagCompound cmp) {
		super.writeToPacketNBT(cmp);

		if(isPrime()) {
			cmp.setInteger(TAG_PRIME_POSITION_X, primePositionX);
			cmp.setInteger(TAG_PRIME_POSITION_Y, primePositionY);
			cmp.setInteger(TAG_PRIME_POSITION_Z, primePositionZ);
			cmp.setBoolean(TAG_SAVED_POSITION, savedPosition);
		} else {
			cmp.setInteger(TAG_SAVED_OUTPUT_TIMER, outputTimer);
		}
	}

	@Override
	public void readFromPacketNBT(NBTTagCompound cmp) {
		super.readFromPacketNBT(cmp);

		if(isPrime()) {
			primePositionX = cmp.getInteger(TAG_PRIME_POSITION_X);
			primePositionY = cmp.getInteger(TAG_PRIME_POSITION_Y);
			primePositionZ = cmp.getInteger(TAG_PRIME_POSITION_Z);
			savedPosition = cmp.getBoolean(TAG_SAVED_POSITION);
		} else {
			outputTimer = cmp.getInteger(TAG_SAVED_OUTPUT_TIMER);
		}
	}

	@Override
	public boolean shouldSyncPassiveGeneration() {
		return true;
	}

	public boolean isPrime() {
		return false;
	}
	@Override
	public int getMaxManaTransfer() {
		return 1;
	}
	@Override
	public boolean canTransferMana() { return isPrime() || (outputTimer > 0 && !supertile.getWorldObj().isDaytime()); }

	public int getValueForPassiveGeneration() {
		return 2;
	}

	@Override
	public int getDelayBetweenPassiveGeneration() {
		switch (supertile.getWorldObj().getMoonPhase()) {
			case 0:
				return 10; // Full Moon
			case 1:
			case 7:
				return 12; // Waning gibbous & Waxing gibbous
			case 2:
			case 6:
				return 13; // First quarter & Last quarter
			case 4:
				return 15; // New moon
			default:
				return 14; // Waxing crescent & Waning crescent
		}
	}

	@Override
	public int getMaxMana() {
		return 500;
	}

	public static class Prime extends SubTileNightshade {

		@Override
		public boolean isPrime() {
			return true;
		}

		@Override
		public LexiconEntry getEntry() {
			return LexiconData.primusLoci;
		}

	}

}
