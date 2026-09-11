package com.stashhunter.stashhunter.mixin;

import com.stashhunter.stashhunter.StashHunter;
import com.stashhunter.stashhunter.events.PlayerDeathEvent;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Explicit priority (default is 1000, stated here for clarity): this mixin only injects a HEAD
// callback and never touches control flow another mixin on LivingEntity.die() might rely on, so
// leaving it at the default is safe - just documented so load order intent isn't ambiguous if
// another addon/mod also mixes into this method.
@Mixin(value = LivingEntity.class, priority = 1000)
public class LivingEntityMixin {
    @Inject(method = "die", at = @At("HEAD"))
    private void onDeath(DamageSource source, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof LocalPlayer) {
            StashHunter.LOG.info("Player death mixin called!");
            MeteorClient.EVENT_BUS.post(PlayerDeathEvent.get((LocalPlayer) entity));
        }
    }
}
