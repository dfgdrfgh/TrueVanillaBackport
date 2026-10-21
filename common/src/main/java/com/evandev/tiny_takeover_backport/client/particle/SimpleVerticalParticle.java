package com.evandev.tiny_takeover_backport.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;

public class SimpleVerticalParticle extends TextureSheetParticle {
    private SimpleVerticalParticle(ClientLevel level, double x, double y, double z, double xa, double ya, double za, TextureAtlasSprite sprite, boolean upwards) {
        super(level, x, y, z, xa, ya, za);
        this.setSprite(sprite);
        this.xd = xa;
        this.zd = za;
        this.yd = ya;
        this.gravity = 0.0F;
        this.yd += upwards ? 0.03 : -0.03;
        this.quadSize = this.quadSize * (this.random.nextFloat() * 0.6F + 0.5F);
        this.lifetime = 8;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class PauseMobGrowthProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public PauseMobGrowthProvider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType options, ClientLevel level, double x, double y, double z, double xAux, double yAux, double zAux
        ) {
            return new SimpleVerticalParticle(level, x, y, z, xAux, yAux, zAux, this.sprite.get(level.random), false);
        }
    }

    public static class ResetMobGrowthProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public ResetMobGrowthProvider(SpriteSet sprite) {
            this.sprite = sprite;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType options, ClientLevel level, double x, double y, double z, double xAux, double yAux, double zAux
        ) {
            return new SimpleVerticalParticle(level, x, y, z, xAux, yAux, zAux, this.sprite.get(level.random), true);
        }
    }
}
