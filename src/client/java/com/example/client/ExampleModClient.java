package com.example.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.UUID;

public class ExampleModClient implements ClientModInitializer {

    // Aim lock settings
    public static boolean enabled = false;
    public static int range = 20;            // aim lock distance (1..50)

    // Reach settings
    public static boolean reachEnabled = false;
    public static double reach = 6.0;        // blocks (3..10)

    private static final double DEFAULT_ENTITY_REACH = 3.0;
    private static final double DEFAULT_BLOCK_REACH = 4.5;

    private static KeyBinding openKey;

    @Override
    public void onInitializeClient() {
        KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("modid", "troll"));
        openKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.troll.open", GLFW.GLFW_KEY_RIGHT_SHIFT, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.wasPressed()) {
                client.setScreen(new MenuScreen());
            }

            ClientPlayerEntity player = client.player;
            if (player == null || client.world == null) return;

            if (enabled && client.currentScreen == null) {
                aim(client, player);
            }
            applyReach(client, player);
        });
    }

    private static void aim(MinecraftClient client, ClientPlayerEntity player) {
        Entity best = null;
        double bestDist = (double) range * range;

        for (Entity e : client.world.getEntities()) {
            if (!(e instanceof LivingEntity) || e == player || !e.isAlive()) continue;
            double d = player.squaredDistanceTo(e);
            if (d < bestDist) {
                bestDist = d;
                best = e;
            }
        }
        if (best == null) return;

        Vec3d diff = best.getEyePos().subtract(player.getEyePos());
        double flat = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
        float yaw = (float) (Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0);
        float pitch = (float) (-Math.toDegrees(Math.atan2(diff.y, flat)));
        player.setYaw(yaw);
        player.setPitch(pitch);
    }

    private static void applyReach(MinecraftClient client, ClientPlayerEntity player) {
        double entityRange = reachEnabled ? reach : DEFAULT_ENTITY_REACH;
        double blockRange = reachEnabled ? Math.max(reach, DEFAULT_BLOCK_REACH) : DEFAULT_BLOCK_REACH;

        setReach(player, entityRange, blockRange);

        // Singleplayer / LAN host: the integrated server checks reach too
        var server = client.getServer();
        if (server != null) {
            UUID id = player.getUuid();
            server.execute(() -> {
                ServerPlayerEntity sp = server.getPlayerManager().getPlayer(id);
                if (sp != null) setReach(sp, entityRange, blockRange);
            });
        }
    }

    private static void setReach(PlayerEntity p, double entityRange, double blockRange) {
        EntityAttributeInstance ei = p.getAttributeInstance(EntityAttributes.ENTITY_INTERACTION_RANGE);
        if (ei != null && ei.getBaseValue() != entityRange) ei.setBaseValue(entityRange);
        EntityAttributeInstance bi = p.getAttributeInstance(EntityAttributes.BLOCK_INTERACTION_RANGE);
        if (bi != null && bi.getBaseValue() != blockRange) bi.setBaseValue(blockRange);
    }
}
