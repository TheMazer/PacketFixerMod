package com.omega.packetfixer;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod("packetfixer")
public class PacketFixer {
    public static final Logger LOGGER = LogUtils.getLogger();

    public PacketFixer() {
        LOGGER.info("[PacketFixer] Mod Loaded!");
    }
}