/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.useables;

import hellfirepvp.astralsorcery.common.entities.EntityIlluminationSpark;
import hellfirepvp.astralsorcery.common.entities.EntityNocturnalSpark;
import hellfirepvp.astralsorcery.common.item.base.IItemVariants;
import hellfirepvp.astralsorcery.common.lib.BlocksAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.registry.RegistryItems;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemUsableDust
 * Created by HellFirePvP
 * Date: 03.07.2017 / 11:27
 */
public class ItemUsableDust extends Item implements IItemVariants {

    public ItemUsableDust() {
        setMaxStackSize(64);
        setHasSubtypes(true);
        setCreativeTab(RegistryItems.creativeTabAstralSorcery);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            for (DustType type : DustType.values()) {
                items.add(type.asStack());
            }
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        DustType type = DustType.fromMeta(stack.getItemDamage());
        if (stack.isEmpty() || worldIn.isRemote || !(stack.getItem() instanceof ItemUsableDust) || type == null) {
            return EnumActionResult.SUCCESS;
        }
        type.rightClickBlock(player, worldIn, pos, stack, facing);
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);
        DustType type = DustType.fromMeta(stack.getItemDamage());
        if (stack.isEmpty() || worldIn.isRemote || !(stack.getItem() instanceof ItemUsableDust) || type == null) {
            return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
        }
        type.rightClickAir(worldIn, playerIn, stack);
        if(!playerIn.isCreative()) {
            stack.shrink(1);
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        Item i = stack.getItem();
        if(i instanceof ItemUsableDust) {
            ItemUsableDust.DustType type = ItemUsableDust.DustType.fromMeta(stack.getItemDamage());
            return super.getUnlocalizedName(stack) + "." + type.getUnlocalizedName();
        }
        return super.getUnlocalizedName(stack);
    }

    @Override
    public String[] getVariants() {
        String[] sub = new String[DustType.values().length];
        DustType[] values = DustType.values();
        for (int i = 0; i < values.length; i++) {
            DustType mt = values[i];
            sub[i] = mt.getUnlocalizedName();
        }
        return sub;
    }

    @Override
    public int[] getVariantMetadatas() {
        int[] sub = new int[DustType.values().length];
        DustType[] values = DustType.values();
        for (int i = 0; i < values.length; i++) {
            DustType mt = values[i];
            sub[i] = mt.getMeta();
        }
        return sub;
    }

    public static enum DustType {

        ILLUMINATION,
        NOCTURNAL;

        public void rightClickAir(World worldIn, EntityPlayer player, ItemStack dustStack) {
            switch (this) {
                case ILLUMINATION:
                    worldIn.spawnEntity(new EntityIlluminationSpark(worldIn, player));
                    break;
                case NOCTURNAL:
                    worldIn.spawnEntity(new EntityNocturnalSpark(worldIn, player));
                    break;
            }
        }

        public void rightClickBlock(EntityPlayer playerIn, World worldIn, BlockPos pos, ItemStack dustStack, EnumFacing facing) {
            switch (this) {
                case ILLUMINATION:
                    IBlockState iblockstate = worldIn.getBlockState(pos);
                    Block block = iblockstate.getBlock();
                    if (!block.isReplaceable(worldIn, pos)) {
                        pos = pos.offset(facing);
                    }
                    if(playerIn.canPlayerEdit(pos, facing, dustStack) && worldIn.mayPlace(BlocksAS.blockVolatileLight, pos, true, facing, null)) {
                        if (worldIn.setBlockState(pos, BlocksAS.blockVolatileLight.getDefaultState(), 3)) {
                            SoundType soundtype = worldIn.getBlockState(pos).getBlock().getSoundType(worldIn.getBlockState(pos), worldIn, pos, playerIn);
                            worldIn.playSound(playerIn, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
                            if(!playerIn.isCreative()) {
                                dustStack.shrink(1);
                            }
                        }
                    }
                    break;
                case NOCTURNAL:
                    pos = pos.offset(facing);
                    EntityNocturnalSpark noc = new EntityNocturnalSpark(worldIn, playerIn);
                    noc.setPosition(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                    noc.setSpawning();
                    worldIn.spawnEntity(noc);
                    break;
            }
        }

        public ItemStack asStack() {
            return new ItemStack(ItemsAS.useableDust, 1, getMeta());
        }

        public String getUnlocalizedName() {
            return name().toLowerCase();
        }

        public int getMeta() {
            return ordinal();
        }

        public static DustType fromMeta(int meta) {
            int ord = MathHelper.clamp(meta, 0, values().length -1);
            return values()[ord];
        }

    }

}