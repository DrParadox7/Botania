/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Aug 28, 2015, 10:46:20 PM (GMT)]
 */
package vazkii.botania.common.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import vazkii.botania.api.lexicon.ILexiconable;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.common.Botania;
import vazkii.botania.common.lexicon.LexiconData;
import vazkii.botania.common.lib.LibBlockNames;

public class BlockBifrostPerm extends BlockMod implements ILexiconable {

	public BlockBifrostPerm() {
		super(Material.glass);
		setBlockName(LibBlockNames.BIFROST_PERM);
		setLightOpacity(0);
		setHardness(0.3F);
		setLightLevel(1F);
		setStepSound(soundTypeGlass);
		setTickRandomly(true);
	}

	@Override
	public IIcon getIcon(int side, int meta) {
		return ModBlocks.bifrost.getIcon(side, meta);
	}

	@Override
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		// NO-OP
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	public boolean shouldSideBeRendered1(IBlockAccess worldIn, int x, int y, int z, int side) {
		Block block = worldIn.getBlock(x, y, z);

		return block == this ? false : super.shouldSideBeRendered(worldIn, x, y, z, side);
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess worldIn, int x, int y, int z, int side) {
		return shouldSideBeRendered1(worldIn, x, y, z, 1 - side);
	}

	@Override
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		if(rand.nextBoolean())
			Botania.proxy.sparkleFX(world, x + Math.random(), y + Math.random(), z + Math.random(), (float) Math.random(), (float) Math.random(), (float) Math.random(), 0.45F + 0.2F * (float) Math.random(), 6);
	}

	@Override
	public int getRenderBlockPass() {
		return 1;
	}

	@Override
	public LexiconEntry getEntry(World world, int x, int y, int z, EntityPlayer player, ItemStack lexicon) {
		return LexiconData.rainbowRod;
	}

}
