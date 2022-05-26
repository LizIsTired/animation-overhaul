package net.lizistired.animationoverhaul;

import net.lizistired.animationoverhaul.util.config.AOConfig;
import net.lizistired.animationoverhaul.util.config.GamePaths;
import gg.moonflower.pollen.pinwheel.api.client.render.BlockRendererRegistry;
import net.lizistired.animationoverhaul.animations.entity.CreeperPartAnimator;
import net.lizistired.animationoverhaul.animations.entity.PlayerPartAnimator;
import net.lizistired.animationoverhaul.render.*;
import net.lizistired.animationoverhaul.util.data.TimelineGroupDataLoader;
import net.lizistired.animationoverhaul.util.data.LivingEntityAnimatorRegistry;
import gg.moonflower.pollen.api.platform.Platform;
import gg.moonflower.pollen.api.registry.resource.ResourceRegistry;
import gg.moonflower.pollen.pinwheel.api.client.render.TickableBlockRenderer;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.resource.ResourceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

public class AnimationOverhaulMain {

	private static AnimationOverhaulMain instance;
	public static AnimationOverhaulMain getInstance() {
		return instance;
	}
	public AnimationOverhaulMain() {
		instance = this;
	}
	private static AOConfig config;
	public static AOConfig getConfig() {
		return config;
	}


	public static final String MOD_ID = "animation_overhaul";
	public static final Platform PLATFORM = Platform.builder(MOD_ID)
			.clientInit(AnimationOverhaulMain::onClientInit)
			.clientPostInit(AnimationOverhaulMain::onClientPostInit)
			.commonInit(AnimationOverhaulMain::onCommonInit)
			.commonPostInit(AnimationOverhaulMain::onCommonPostInit)
			.build();


	public static Logger LOGGER = LogManager.getLogger();

	//public static final PollinatedRegistry<LivingEntityAnimator> ENTITY_ANIMATORS = PollinatedRegistry.createSimple(LivingEntityAnimator.class, new ResourceLocation(MOD_ID, "entity_animators"));
	public static final LivingEntityAnimatorRegistry ENTITY_ANIMATORS = new LivingEntityAnimatorRegistry();
	public static Entity debugEntity;

	public static void onClientInit() {
		registerTimelineGroupLoader();
		Path AOFolder = GamePaths.getConfigDirectory().resolve("ao");
		config = new AOConfig(AOFolder.getParent().resolve("ao.json"), AnimationOverhaulMain.getInstance());
		config.load();
		if (getConfig().isEnableMobAnimations()){
			registerEntityAnimators();
		}
		registerEntityAnimators();
		registerBlockRenderers();
	}

	public static void onClientPostInit(Platform.ModSetupContext ctx) {
	}

	public static void onCommonInit() {
	}

	public static void onCommonPostInit(Platform.ModSetupContext ctx) {
	}

	private static void registerTimelineGroupLoader(){
		ResourceRegistry.registerReloadListener(ResourceType.CLIENT_RESOURCES, new TimelineGroupDataLoader());
	}

	private static void registerEntityAnimators(){
		ENTITY_ANIMATORS.register(EntityType.PLAYER, new PlayerPartAnimator());
		ENTITY_ANIMATORS.register(EntityType.CREEPER, new CreeperPartAnimator());
	}

	private static void registerBlockRenderers(){
		registerBlockRenderer(new PressurePlateBlockRenderer(), PressurePlateBlockRenderer.PRESSURE_PLATES);
		registerBlockRenderer(new ButtonBlockRenderer(), ButtonBlockRenderer.BUTTONS);
		registerBlockRenderer(new TrapDoorBlockRenderer(), TrapDoorBlockRenderer.TRAPDOORS);
		registerBlockRenderer(new LeverBlockRenderer(), LeverBlockRenderer.LEVERS);
		registerBlockRenderer(new EndPortalFrameBlockRenderer(), EndPortalFrameBlockRenderer.END_PORTAL_BLOCKS);
		registerBlockRenderer(new ChainedBlockRenderer(), ChainedBlockRenderer.CHAINED_BLOCKS);
		registerBlockRenderer(new FloatingPlantBlockRenderer(), FloatingPlantBlockRenderer.FLOATING_PLANTS);
	}

	private static void registerBlockRenderer(TickableBlockRenderer tickableBlockRenderer, Block[] blocks){
		for(Block block : blocks){
			BlockRendererRegistry.register(block, tickableBlockRenderer);
		}
	}
}
