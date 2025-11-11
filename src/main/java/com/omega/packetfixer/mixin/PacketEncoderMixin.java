package com.omega.packetfixer.mixin;
import com.omega.packetfixer.PacketFixer;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import net.minecraft.core.Registry;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.netty.buffer.ByteBuf;

@Mixin(net.minecraft.network.PacketEncoder.class)
public class PacketEncoderMixin {

    @Inject(method = "encode(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;Lio/netty/buffer/ByteBuf;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void preventBadSoundPacket(ChannelHandlerContext context, Packet<?> packet, ByteBuf byteBuf, CallbackInfo ci) {
        if (packet instanceof ClientboundSoundPacket soundPacket) {
            SoundEvent sound = soundPacket.getSound();

            if (Registry.SOUND_EVENT.getKey(sound) == null) {
                PacketFixer.LOGGER.error("==========================================================");
                PacketFixer.LOGGER.error("!!! INTERCEPTED A POTENTIAL CLIENT DISCONNECT !!!");
                PacketFixer.LOGGER.error("An unregistered SoundEvent was detected before encoding.");
                PacketFixer.LOGGER.error("The packet will be DROPPED to prevent the client from being kicked.");

                try {
                    PacketFixer.LOGGER.error("  - Sound Event object: {}", sound);
                    PacketFixer.LOGGER.error("  - Sound Location (from object): {}", sound.getLocation());
                } catch (Exception e) {
                    PacketFixer.LOGGER.error("  - Could not retrieve detailed sound info.", e);
                }

                PacketFixer.LOGGER.error("==========================================================");

                ci.cancel();
            }
        }
    }

}