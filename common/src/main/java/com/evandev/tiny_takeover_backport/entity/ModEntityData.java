package com.evandev.tiny_takeover_backport.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.Squid;

public class ModEntityData {
    public static final EntityDataAccessor<Boolean> SQUID_BABY_ID = SynchedEntityData.defineId(Squid.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> DOLPHIN_BABY_ID = SynchedEntityData.defineId(Dolphin.class, EntityDataSerializers.BOOLEAN);

    public static void init() {
    }
}
