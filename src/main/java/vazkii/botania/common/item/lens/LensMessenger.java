/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Jan 24, 2015, 4:34:29 PM (GMT)]
 */
package vazkii.botania.common.item.lens;

import net.minecraft.item.ItemStack;
import vazkii.botania.api.mana.BurstProperties;

public class LensMessenger extends Lens {

	@Override
	public void apply(ItemStack stack, BurstProperties props) {
		props.maxMana /= 5;
		props.ticksBeforeManaLoss *= 3;
		props.motionModifier *= 3F;
	}

}
