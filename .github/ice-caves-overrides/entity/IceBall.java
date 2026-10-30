package org.zerw.net.icecaves.entity;

import net.minecraft.class_1299;
import net.minecraft.class_1309;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1937;
import net.minecraft.class_239;
import net.minecraft.class_2392;
import net.minecraft.class_2394;
import net.minecraft.class_2398;
import net.minecraft.class_243;
import net.minecraft.class_3419;
import net.minecraft.class_3857;
import net.minecraft.class_3966;
import net.minecraft.class_7923;
import org.zerw.net.icecaves.registry.ModItems;
import org.zerw.net.icecaves.registry.ModSounds;

/** Snapshot IceBall/ThrowableBallProjectile behavior on the 1.21.1 API. */
public class IceBall extends class_3857 {
    public IceBall(class_1299<? extends IceBall> type, class_1937 level) {
        super(type, level);
    }

    public IceBall(class_1299<? extends IceBall> type, class_1309 thrower, class_1937 level) {
        super(type, thrower, level);
    }

    public IceBall(class_1299<? extends IceBall> type, double x, double y, double z, class_1937 level) {
        super(type, x, y, z, level);
    }

    protected class_1792 method_16942() {
        return (class_1792)class_7923.field_41178.method_10223(ModItems.ICE_BALL_ID);
    }

    private class_2394 getParticle() {
        return new class_2392(class_2398.field_11218, this.method_7495());
    }

    public void method_5711(byte id) {
        if (id == 3) {
            class_2394 particle = this.getParticle();
            for (int i = 0; i < 8; i++) {
                this.method_37908().method_8406(particle, this.method_23317(), this.method_23318(), this.method_23321(), 0.0, 0.0, 0.0);
            }
        }
    }

    protected void method_7454(class_3966 hitResult) {
        super.method_7454(hitResult);
        if (!this.method_37908().method_8608()) {
            float speed = (float)this.method_18798().method_1033();
            int damage = (int)Math.ceil(Math.clamp(speed * 1.395F, 0.0F, 2.1474836E9F));
            hitResult.method_17782().method_5643(this.method_48923().method_48811(this, this.method_24921()), damage);
        }
    }

    protected void method_7488(class_239 hitResult) {
        super.method_7488(hitResult);
        if (!this.method_37908().method_8608()) {
            this.method_37908().method_8421(this, (byte)3);
            class_243 hit = hitResult.method_17784();
            this.method_37908().method_43128(null, hit.field_1352, hit.field_1351, hit.field_1350,
                ModSounds.ENTITY_ICE_BALL_BREAK, class_3419.field_15254, 0.5F,
                0.4F / (this.field_5974.method_43057() * 0.4F + 0.8F));
            this.method_31472();
        }
    }
}
