package com.buuz135.functionalstorage;

import com.buuz135.functionalstorage.block.*;
import com.buuz135.functionalstorage.block.tile.CompactingFramedDrawerTile;
import com.buuz135.functionalstorage.block.tile.FluidDrawerTile;
import com.buuz135.functionalstorage.block.tile.FramedDrawerTile;
import com.buuz135.functionalstorage.block.tile.SimpleCompactingDrawerTile;
import com.buuz135.functionalstorage.client.*;
import com.buuz135.functionalstorage.client.loader.FramedModel;
import com.buuz135.functionalstorage.data.FunctionalStorageBlockTagsProvider;
import com.buuz135.functionalstorage.data.FunctionalStorageBlockstateProvider;
import com.buuz135.functionalstorage.data.FunctionalStorageItemTagsProvider;
import com.buuz135.functionalstorage.data.FunctionalStorageLangProvider;
import com.buuz135.functionalstorage.init.FunctionalBlocks;
import com.buuz135.functionalstorage.init.FunctionalItems;
import com.buuz135.functionalstorage.inventory.BigInventoryHandler;
import com.buuz135.functionalstorage.inventory.item.DrawerStackItemHandler;
import com.buuz135.functionalstorage.item.ConfigurationToolItem;
import com.buuz135.functionalstorage.item.LinkingToolItem;
import com.buuz135.functionalstorage.network.EnderDrawerSyncMessage;
import com.buuz135.functionalstorage.recipe.DrawerlessWoodIngredient;
import com.buuz135.functionalstorage.recipe.FramedDrawerRecipe;
import com.buuz135.functionalstorage.util.NumberUtils;
import com.buuz135.functionalstorage.util.StorageTags;
import com.buuz135.functionalstorage.util.TooltipUtil;
import com.hrznstudio.titanium.block.BasicBlock;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.datagenerator.loot.TitaniumLootTableProvider;
import com.hrznstudio.titanium.datagenerator.model.BlockItemModelGeneratorProvider;
import com.hrznstudio.titanium.event.handler.EventManager;
import com.hrznstudio.titanium.module.Feature;
import com.hrznstudio.titanium.module.Module;
import com.hrznstudio.titanium.module.ModuleController;
import com.hrznstudio.titanium.nbthandler.NBTManager;
import com.hrznstudio.titanium.network.NetworkHandler;
import com.hrznstudio.titanium.recipe.generator.TitaniumRecipeProvider;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import com.hrznstudio.titanium.tab.AdvancedTitaniumTab;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.data.SmithingRecipeBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(FunctionalStorage.MOD_ID)
public class FunctionalStorage extends ModuleController {

    public final static String MOD_ID = "functionalstorage";
    public static NetworkHandler NETWORK = new NetworkHandler(MOD_ID);

    static {
        NETWORK.registerMessage(EnderDrawerSyncMessage.class);
    }

    public static AdvancedTitaniumTab TAB = new AdvancedTitaniumTab(MOD_ID, true);

    public FunctionalStorage() {
        init();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::onClient);
        EventManager.forge(BlockEvent.BreakEvent.class).process(breakEvent -> {
            if (breakEvent.getPlayer().isCreative()) {
                if (breakEvent.getState().getBlock() instanceof DrawerBlock) {
                    int hit = ((DrawerBlock) breakEvent.getState().getBlock()).getHit(breakEvent.getState(), breakEvent.getPlayer().level, breakEvent.getPos(), breakEvent.getPlayer());
                    if (hit != -1) {
                        breakEvent.setCanceled(true);
                        ((DrawerBlock) breakEvent.getState().getBlock()).attack(breakEvent.getState(), breakEvent.getPlayer().level, breakEvent.getPos(), breakEvent.getPlayer());
                    }
                }
                if (breakEvent.getState().getBlock() instanceof CompactingDrawerBlock) {
                    int hit = ((CompactingDrawerBlock) breakEvent.getState().getBlock()).getHit(breakEvent.getState(), breakEvent.getPlayer().level, breakEvent.getPos(), breakEvent.getPlayer());
                    if (hit != -1) {
                        breakEvent.setCanceled(true);
                        ((CompactingDrawerBlock) breakEvent.getState().getBlock()).attack(breakEvent.getState(), breakEvent.getPlayer().level, breakEvent.getPos(), breakEvent.getPlayer());
                    }
                }
                if (breakEvent.getState().getBlock() instanceof EnderDrawerBlock) {
                    int hit = ((EnderDrawerBlock) breakEvent.getState().getBlock()).getHit(breakEvent.getState(), breakEvent.getPlayer().level, breakEvent.getPos(), breakEvent.getPlayer());
                    if (hit != -1) {
                        breakEvent.setCanceled(true);
                        ((EnderDrawerBlock) breakEvent.getState().getBlock()).attack(breakEvent.getState(), breakEvent.getPlayer().level, breakEvent.getPos(), breakEvent.getPlayer());
                    }
                }
                if (breakEvent.getState().getBlock() instanceof FluidDrawerBlock) {
                    int hit = ((FluidDrawerBlock) breakEvent.getState().getBlock()).getHit(breakEvent.getState(), breakEvent.getPlayer().level, breakEvent.getPos(), breakEvent.getPlayer());
                    if (hit != -1) {
                        breakEvent.setCanceled(true);
                        ((FluidDrawerBlock) breakEvent.getState().getBlock()).attack(breakEvent.getState(), breakEvent.getPlayer().level, breakEvent.getPos(), breakEvent.getPlayer());
                    }
                }
            }
        }).subscribe();
        EventManager.modGeneric(RegistryEvent.Register.class, IRecipeSerializer.class).process(register -> {
            CraftingHelper.register(DrawerlessWoodIngredient.NAME, DrawerlessWoodIngredient.SERIALIZER);
        }).subscribe();
        EventManager.modGeneric(RegistryEvent.Register.class, IRecipeSerializer.class)
                .process(register -> ((RegistryEvent.Register) register).getRegistry()
                        .registerAll(FramedDrawerRecipe.SERIALIZER.setRegistryName(new ResourceLocation(MOD_ID, "framed_recipe")))).subscribe();

        NBTManager.getInstance().scanTileClassForAnnotations(FramedDrawerTile.class);
        NBTManager.getInstance().scanTileClassForAnnotations(CompactingFramedDrawerTile.class);
        NBTManager.getInstance().scanTileClassForAnnotations(FluidDrawerTile.class);
        NBTManager.getInstance().scanTileClassForAnnotations(SimpleCompactingDrawerTile.class);
    }

    public void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        FunctionalBlocks.initBlocks(bus);
        FunctionalItems.initItems(bus);
    }

    @Override
    protected void initModules() {
        addModule(Module.builder(MOD_ID)
                .feature(Feature.builder("blocks")
                        .content(Block.class, FunctionalBlocks.OAK_1)
                        .content(Block.class, FunctionalBlocks.OAK_2)
                        .content(Block.class, FunctionalBlocks.OAK_4)

                        .content(Block.class, FunctionalBlocks.SPRUCE_1)
                        .content(Block.class, FunctionalBlocks.SPRUCE_2)
                        .content(Block.class, FunctionalBlocks.SPRUCE_4)

                        .content(Block.class, FunctionalBlocks.BIRCH_1)
                        .content(Block.class, FunctionalBlocks.BIRCH_2)
                        .content(Block.class, FunctionalBlocks.BIRCH_4)

                        .content(Block.class, FunctionalBlocks.JUNGLE_1)
                        .content(Block.class, FunctionalBlocks.JUNGLE_2)
                        .content(Block.class, FunctionalBlocks.JUNGLE_4)

                        .content(Block.class, FunctionalBlocks.ACACIA_1)
                        .content(Block.class, FunctionalBlocks.ACACIA_2)
                        .content(Block.class, FunctionalBlocks.ACACIA_4)

                        .content(Block.class, FunctionalBlocks.DARK_OAK_1)
                        .content(Block.class, FunctionalBlocks.DARK_OAK_2)
                        .content(Block.class, FunctionalBlocks.DARK_OAK_4)

                        .content(Block.class, FunctionalBlocks.CRIMSON_1)
                        .content(Block.class, FunctionalBlocks.CRIMSON_2)
                        .content(Block.class, FunctionalBlocks.CRIMSON_4)

                        .content(Block.class, FunctionalBlocks.WARPED_1)
                        .content(Block.class, FunctionalBlocks.WARPED_2)
                        .content(Block.class, FunctionalBlocks.WARPED_4)

                        .content(Block.class, FunctionalBlocks.FRAMED_1)
                        .content(Block.class, FunctionalBlocks.FRAMED_2)
                        .content(Block.class, FunctionalBlocks.FRAMED_4)
                        .content(Block.class, FunctionalBlocks.FRAMED_COMPACTING)

                        .content(Block.class, FunctionalBlocks.FLUID_1)
                        .content(Block.class, FunctionalBlocks.FLUID_2)
                        .content(Block.class, FunctionalBlocks.FLUID_4)

                        .content(Block.class, FunctionalBlocks.COMPACTING)
                        .content(Block.class, FunctionalBlocks.SIMPLE_COMPACTING)

                        .content(Block.class, FunctionalBlocks.ENDER)

                        .content(Block.class, FunctionalBlocks.ARMORY_CABINET)

                        .content(Block.class, FunctionalBlocks.CONTROLLER)
                        .content(Block.class, FunctionalBlocks.CONTROLLER_EXTENSION)
                ));
        FunctionalBlocks.DRAWERS.forEach(block -> TAB.addIconStacks(new ItemStack(block)));
    }

    @OnlyIn(Dist.CLIENT)
    public void onClient() {
        FunctionalBlocks.DRAWERS.forEach(tileEntityObject -> {
            ClientRegistry.bindTileEntityRenderer(((BasicTileBlock<?>) tileEntityObject).getTileEntityType(), DrawerRenderer::new);
        });

        ClientRegistry.bindTileEntityRenderer(FunctionalBlocks.COMPACTING.getTileEntityType(), CompactingDrawerRenderer::new);
        ClientRegistry.bindTileEntityRenderer(FunctionalBlocks.FRAMED_COMPACTING.getTileEntityType(), CompactingDrawerRenderer::new);
        ClientRegistry.bindTileEntityRenderer(FunctionalBlocks.CONTROLLER.getTileEntityType(), ControllerRenderer::new);
        ClientRegistry.bindTileEntityRenderer(FunctionalBlocks.ENDER.getTileEntityType(), EnderDrawerRenderer::new);
        ClientRegistry.bindTileEntityRenderer(FunctionalBlocks.FLUID_1.getTileEntityType(), FluidDrawerRenderer::new);
        ClientRegistry.bindTileEntityRenderer(FunctionalBlocks.FLUID_2.getTileEntityType(), FluidDrawerRenderer::new);
        ClientRegistry.bindTileEntityRenderer(FunctionalBlocks.FLUID_4.getTileEntityType(), FluidDrawerRenderer::new);
        ClientRegistry.bindTileEntityRenderer(FunctionalBlocks.SIMPLE_COMPACTING.getTileEntityType(), SimpleCompactingDrawerRenderer::new);

        EventManager.mod(ColorHandlerEvent.Item.class).process(item -> {
            item.getItemColors().register((stack, tint) -> {
                CompoundNBT tag = stack.getOrCreateTag();
                LinkingToolItem.LinkingMode linkingMode = LinkingToolItem.getLinkingMode(stack);
                LinkingToolItem.ActionMode linkingAction = LinkingToolItem.getActionMode(stack);
                if (tint != 0 && stack.getOrCreateTag().contains(LinkingToolItem.NBT_ENDER)) {
                    return new Color(44, 150, 88).getRGB();
                }
                if (tint == 3 && tag.contains(LinkingToolItem.NBT_CONTROLLER)) {
                    return Color.RED.getRGB();
                }
                if (tint == 1) {
                    return linkingMode.getColor().getValue();
                }
                if (tint == 2) {
                    return linkingAction.getColor().getValue();
                }
                return 0xffffff;
            }, FunctionalItems.LINKING_TOOL.get());
            item.getItemColors().register((stack, tint) -> {
                ConfigurationToolItem.ConfigurationAction action = ConfigurationToolItem.getAction(stack);
                if (tint == 1) {
                    return action.getColor().getValue();
                }
                return 0xffffff;
            }, FunctionalItems.CONFIGURATION_TOOL.get());
        }).subscribe();
        EventManager.mod(FMLClientSetupEvent.class).process(event -> {
            FunctionalBlocks.DRAWERS.forEach(blockObject -> {
                RenderTypeLookup.setRenderLayer(blockObject, RenderType.cutout());
            });
            RenderTypeLookup.setRenderLayer(FunctionalBlocks.COMPACTING, RenderType.cutout());
            RenderTypeLookup.setRenderLayer(FunctionalBlocks.FRAMED_COMPACTING, RenderType.cutout());
            RenderTypeLookup.setRenderLayer(FunctionalBlocks.ENDER, RenderType.cutout());
            RenderTypeLookup.setRenderLayer(FunctionalBlocks.FLUID_1, RenderType.cutout());
            RenderTypeLookup.setRenderLayer(FunctionalBlocks.FLUID_2, RenderType.cutout());
            RenderTypeLookup.setRenderLayer(FunctionalBlocks.FLUID_4, RenderType.cutout());
            RenderTypeLookup.setRenderLayer(FunctionalBlocks.SIMPLE_COMPACTING, RenderType.cutout());
        }).subscribe();
        EventManager.forge(RenderTooltipEvent.Pre.class).process(itemTooltipEvent -> {
            if (itemTooltipEvent.getStack().getItem().equals(FunctionalBlocks.ENDER.asItem()) && itemTooltipEvent.getStack().hasTag()) {
                TooltipUtil.renderItems(itemTooltipEvent.getMatrixStack(), EnderDrawerBlock.getFrequencyDisplay(itemTooltipEvent.getStack().getTag().getCompound("Tile").getString("frequency")), itemTooltipEvent.getX() + 14, itemTooltipEvent.getY() + 11);
            }
            if (itemTooltipEvent.getStack().getItem() instanceof LinkingToolItem && itemTooltipEvent.getStack().getOrCreateTag().contains(LinkingToolItem.NBT_ENDER)) {
                TooltipUtil.renderItems(itemTooltipEvent.getMatrixStack(), EnderDrawerBlock.getFrequencyDisplay(itemTooltipEvent.getStack().getOrCreateTag().getString(LinkingToolItem.NBT_ENDER)), itemTooltipEvent.getX() + 14, itemTooltipEvent.getY() + 11);
            }
            itemTooltipEvent.getStack().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(iItemHandler -> {
                if (iItemHandler instanceof DrawerStackItemHandler) {
                    int i = 0;
                    for (BigInventoryHandler.BigStack storedStack : ((DrawerStackItemHandler) iItemHandler).getStoredStacks()) {
                        TooltipUtil.renderItemAdvanced(itemTooltipEvent.getMatrixStack(), storedStack.getStack(), itemTooltipEvent.getX() + 20 + 26 * i, itemTooltipEvent.getY() + 11, 512, NumberUtils.getFormatedBigNumber(storedStack.getAmount()) + "/" + NumberUtils.getFormatedBigNumber(iItemHandler.getSlotLimit(i)));
                        ++i;
                    }
                }
            });
        }).subscribe();
        EventManager.mod(ModelRegistryEvent.class).process(modelRegistryEvent -> {
            ModelLoaderRegistry.registerLoader(new ResourceLocation(MOD_ID, "framedblock"), FramedModel.Loader.INSTANCE);
        }).subscribe();
    }

    @Override
    public void addDataProvider(GatherDataEvent event) {
        NonNullLazy<List<Block>> blocksToProcess = NonNullLazy.of(() ->
                ForgeRegistries.BLOCKS.getValues()
                        .stream()
                        .filter(basicBlock -> Optional.ofNullable(basicBlock.getRegistryName())
                                .map(ResourceLocation::getNamespace)
                                .filter(MOD_ID::equalsIgnoreCase)
                                .isPresent())
                        .collect(Collectors.toList())
        );
        if (true) {
            event.getGenerator().addProvider(new BlockItemModelGeneratorProvider(event.getGenerator(), MOD_ID, blocksToProcess));
            event.getGenerator().addProvider(new FunctionalStorageBlockstateProvider(event.getGenerator(), event.getExistingFileHelper(), blocksToProcess));
            event.getGenerator().addProvider(new TitaniumLootTableProvider(event.getGenerator(), blocksToProcess));

            event.getGenerator().addProvider(new FunctionalStorageItemTagsProvider(event.getGenerator(), new BlockTagsProvider(event.getGenerator(), MOD_ID, event.getExistingFileHelper()), MOD_ID, event.getExistingFileHelper()));
            event.getGenerator().addProvider(new FunctionalStorageLangProvider(event.getGenerator(), MOD_ID, "en_us"));
            event.getGenerator().addProvider(new FunctionalStorageBlockTagsProvider(event.getGenerator(), MOD_ID, event.getExistingFileHelper()));
            event.getGenerator().addProvider(new ItemModelProvider(event.getGenerator(), MOD_ID, event.getExistingFileHelper()) {
                @Override
                protected void registerModels() {
                    FunctionalItems.STORAGE_UPGRADE.forEach(itemObject -> {
                        item(itemObject.get());
                    });
                    item(FunctionalItems.COLLECTOR_UPGRADE.get());
                    item(FunctionalItems.PULLING_UPGRADE.get());
                    item(FunctionalItems.PUSHING_UPGRADE.get());
                    item(FunctionalItems.VOID_UPGRADE.get());
                    item(FunctionalItems.REDSTONE_UPGRADE.get());
                    item(FunctionalItems.CREATIVE_UPGRADE.get());
                }

                private void item(Item item) {
                    singleTexture(item.getRegistryName().getPath(), new ResourceLocation("minecraft:item/generated"), "layer0", new ResourceLocation(MOD_ID, "items/" + item.getRegistryName().getPath()));
                }
            });
            event.getGenerator().addProvider(new BlockModelProvider(event.getGenerator(), MOD_ID, event.getExistingFileHelper()) {
                @Override
                protected void registerModels() {
                    for (Block drawer : FunctionalBlocks.DRAWERS) {
                        if (drawer instanceof FramedDrawerBlock) {
                            continue;
                        }
                        withExistingParent(drawer.getRegistryName().getPath() + "_locked", modLoc(drawer.getRegistryName().getPath()))
                                .texture("lock_icon", modLoc("blocks/lock"));
                    }
                    withExistingParent(ForgeRegistries.BLOCKS.getKey(FunctionalBlocks.COMPACTING).getPath() + "_locked", modLoc(ForgeRegistries.BLOCKS.getKey(FunctionalBlocks.COMPACTING).getPath()))
                            .texture("lock_icon", modLoc("blocks/lock"));
                    withExistingParent(ForgeRegistries.BLOCKS.getKey(FunctionalBlocks.ENDER).getPath() + "_locked", modLoc(ForgeRegistries.BLOCKS.getKey(FunctionalBlocks.ENDER).getPath()))
                            .texture("lock_icon", modLoc("blocks/lock"));
                    withExistingParent(ForgeRegistries.BLOCKS.getKey(FunctionalBlocks.FLUID_1).getPath() + "_locked", modLoc(ForgeRegistries.BLOCKS.getKey(FunctionalBlocks.FLUID_1).getPath()))
                            .texture("lock_icon", modLoc("blocks/lock"));
                    withExistingParent(ForgeRegistries.BLOCKS.getKey(FunctionalBlocks.FLUID_2).getPath() + "_locked", modLoc(ForgeRegistries.BLOCKS.getKey(FunctionalBlocks.FLUID_2).getPath()))
                            .texture("lock_icon", modLoc("blocks/lock"));
                    withExistingParent(ForgeRegistries.BLOCKS.getKey(FunctionalBlocks.FLUID_4).getPath() + "_locked", modLoc(ForgeRegistries.BLOCKS.getKey(FunctionalBlocks.FLUID_4).getPath()))
                            .texture("lock_icon", modLoc("blocks/lock"));
                    withExistingParent(ForgeRegistries.BLOCKS.getKey(FunctionalBlocks.SIMPLE_COMPACTING).getPath() + "_locked", modLoc(ForgeRegistries.BLOCKS.getKey(FunctionalBlocks.SIMPLE_COMPACTING).getPath()))
                            .texture("lock_icon", modLoc("blocks/lock"));
                }
            });
        }
        event.getGenerator().addProvider(new TitaniumRecipeProvider(event.getGenerator()) {
            @Override
            public void register(Consumer<IFinishedRecipe> consumer) {
                blocksToProcess.get().stream().map(block -> (BasicBlock) block).forEach(basicBlock -> basicBlock.registerRecipe(consumer));

                // flint
                TitaniumShapedRecipeBuilder.shapedRecipe(FunctionalItems.FLINT_UPGRADE.get())
                        .pattern("III").pattern("CDC").pattern("III")
                        .define('I', Items.FLINT)
                        .define('D', StorageTags.DRAWER)
                        .define('C', Tags.Items.CHESTS)
                        .save(consumer);

                TitaniumShapedRecipeBuilder.shapedRecipe(FunctionalItems.FLINT_UPGRADE.get())
                        .setName(new ResourceLocation(MOD_ID, "flint_upgrade_drawer"))
                        .pattern("III").pattern("CDC").pattern("III")
                        .define('I', Items.FLINT)
                        .define('D', StorageTags.DRAWER)
                        .define('C', Items.BARREL)
                        .save(consumer);

                // obsidian
                TitaniumShapedRecipeBuilder.shapedRecipe(FunctionalItems.OBSIDIAN_UPGRADE.get())
                        .pattern("III").pattern(" D ").pattern("III")
                        .define('I', Items.OBSIDIAN)
                        .define('D', FunctionalItems.FLINT_UPGRADE.get())
                        .save(consumer);

                // iron upgrade
                TitaniumShapedRecipeBuilder.shapedRecipe(FunctionalItems.IRON_UPGRADE.get())
                        .pattern("IBI").pattern("CDC").pattern("BIB")
                        .define('I', Tags.Items.INGOTS_IRON)
                        .define('B', Tags.Items.STORAGE_BLOCKS_IRON)
                        .define('D', StorageTags.DRAWER)
                        .define('C', Tags.Items.CHESTS)
                        .save(consumer);
                TitaniumShapedRecipeBuilder.shapedRecipe(FunctionalItems.IRON_UPGRADE.get())
                        .setName(new ResourceLocation(MOD_ID, "iron_upgrade_drawer"))
                        .pattern("IBI").pattern("CDC").pattern("BIB")
                        .define('I', Tags.Items.INGOTS_IRON)
                        .define('B', Tags.Items.STORAGE_BLOCKS_IRON)
                        .define('D', StorageTags.DRAWER)
                        .define('C', Items.BARREL)
                        .save(consumer);

                // gold
                TitaniumShapedRecipeBuilder.shapedRecipe(FunctionalItems.GOLD_UPGRADE.get())
                        .pattern("IBI").pattern("CDC").pattern("BIB")
                        .define('I', Tags.Items.INGOTS_GOLD)
                        .define('B', Tags.Items.STORAGE_BLOCKS_GOLD)
                        .define('C', Tags.Items.CHESTS_WOODEN)
                        .define('D', FunctionalItems.IRON_UPGRADE.get())
                        .save(consumer);
                TitaniumShapedRecipeBuilder.shapedRecipe(FunctionalItems.GOLD_UPGRADE.get())
                        .setName(new ResourceLocation(MOD_ID, "gold_upgrade_drawer"))
                        .pattern("IBI").pattern("CDC").pattern("BIB")
                        .define('I', Tags.Items.INGOTS_GOLD)
                        .define('B', Tags.Items.STORAGE_BLOCKS_GOLD)
                        .define('C', Items.BARREL)
                        .define('D', FunctionalItems.IRON_UPGRADE.get())
                        .save(consumer);

                // diamond
                TitaniumShapedRecipeBuilder.shapedRecipe(FunctionalItems.DIAMOND_UPGRADE.get())
                        .setName(new ResourceLocation(MOD_ID, "diamond_upgrade_drawer"))
                        .pattern("IBI").pattern("CDC").pattern("IBI")
                        .define('I', Tags.Items.GEMS_DIAMOND)
                        .define('B', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                        .define('C', Tags.Items.CHESTS_WOODEN)
                        .define('D', FunctionalItems.GOLD_UPGRADE.get())
                        .save(consumer);
                TitaniumShapedRecipeBuilder.shapedRecipe(FunctionalItems.DIAMOND_UPGRADE.get())
                        .pattern("IBI").pattern("CDC").pattern("IBI")
                        .define('I', Tags.Items.GEMS_DIAMOND)
                        .define('B', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                        .define('C', Items.BARREL)
                        .define('D', FunctionalItems.GOLD_UPGRADE.get())
                        .save(consumer);

                // netherite
                SmithingRecipeBuilder.smithing(Ingredient.of(FunctionalItems.DIAMOND_UPGRADE.get()), Ingredient.of(Items.NETHERITE_INGOT), FunctionalItems.NETHERITE_UPGRADE.get())
                        .unlocks("has_netherite_ingot", has(Items.NETHERITE_INGOT))
                        .save(consumer, ForgeRegistries.ITEMS.getKey(FunctionalItems.NETHERITE_UPGRADE.get()));

                TitaniumShapedRecipeBuilder.shapedRecipe(FunctionalItems.VOID_UPGRADE.get())
                        .pattern("III").pattern("IDI").pattern("III")
                        .define('I', Tags.Items.OBSIDIAN)
                        .define('D', StorageTags.DRAWER)
                        .save(consumer);
                TitaniumShapedRecipeBuilder.shapedRecipe(FunctionalItems.CONFIGURATION_TOOL.get())
                        .pattern("PPG").pattern("PDG").pattern("PEP")
                        .define('P', Items.PAPER)
                        .define('G', Tags.Items.INGOTS_GOLD)
                        .define('D', StorageTags.DRAWER)
                        .define('E', Items.EMERALD)
                        .save(consumer);
                TitaniumShapedRecipeBuilder.shapedRecipe(FunctionalItems.LINKING_TOOL.get())
                        .pattern("PPG").pattern("PDG").pattern("PEP")
                        .define('P', Items.PAPER)
                        .define('G', Tags.Items.INGOTS_GOLD)
                        .define('D', StorageTags.DRAWER)
                        .define('E', Items.DIAMOND)
                        .save(consumer);
                TitaniumShapedRecipeBuilder.shapedRecipe(FunctionalItems.REDSTONE_UPGRADE.get())
                        .pattern("IBI").pattern("CDC").pattern("IBI")
                        .define('I', Items.REDSTONE)
                        .define('B', Items.REDSTONE_BLOCK)
                        .define('C', Items.COMPARATOR)
                        .define('D', StorageTags.DRAWER)
                        .save(consumer);
                TitaniumShapedRecipeBuilder.shapedRecipe(FunctionalBlocks.ARMORY_CABINET)
                        .pattern("ICI").pattern("CDC").pattern("IBI")
                        .define('I', Tags.Items.STONE)
                        .define('B', Tags.Items.INGOTS_NETHERITE)
                        .define('C', StorageTags.DRAWER)
                        .define('D', Items.COMPARATOR)
                        .save(consumer);
                TitaniumShapedRecipeBuilder.shapedRecipe(FunctionalItems.PULLING_UPGRADE.get())
                        .pattern("ICI").pattern("IDI").pattern("IBI")
                        .define('I', Tags.Items.STONE)
                        .define('B', Tags.Items.DUSTS_REDSTONE)
                        .define('C', Items.HOPPER)
                        .define('D', StorageTags.DRAWER)
                        .save(consumer);
                TitaniumShapedRecipeBuilder.shapedRecipe(FunctionalItems.PUSHING_UPGRADE.get())
                        .pattern("IBI").pattern("IDI").pattern("IRI")
                        .define('I', Tags.Items.STONE)
                        .define('B', Tags.Items.DUSTS_REDSTONE)
                        .define('R', Items.HOPPER)
                        .define('D', StorageTags.DRAWER)
                        .save(consumer);
                TitaniumShapedRecipeBuilder.shapedRecipe(FunctionalItems.COLLECTOR_UPGRADE.get())
                        .pattern("IBI").pattern("RDR").pattern("IBI")
                        .define('I', Tags.Items.STONE)
                        .define('B', Items.HOPPER)
                        .define('R', Tags.Items.DUSTS_REDSTONE)
                        .define('D', StorageTags.DRAWER)
                        .save(consumer);
                TitaniumShapedRecipeBuilder.shapedRecipe(FunctionalBlocks.ENDER)
                        .pattern("PPP").pattern("LCL").pattern("PPP")
                        .define('P', ItemTags.PLANKS)
                        .define('C', Tags.Items.CHESTS_ENDER)
                        .define('L', StorageTags.DRAWER)
                        .save(consumer);
            }
        });
    }
}
