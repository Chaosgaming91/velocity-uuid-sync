package com.chaosgaming.velocityuuidforwarder.mixin;

import com.chaosgaming.velocityuuidforwarder.VelocityUUIDForwarder;
import com.chaosgaming.velocityuuidforwarder.util.VelocityMessageUtil;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ServerLoginNetworkHandler.class)
public abstract class ServerLoginNetworkHandlerMixin {
    
    @Shadow
    private GameProfile profile;
    
    @Shadow
    public abstract String getConnectionInfo();
    
    /**
     * Inject into the login query response handler to intercept Velocity forwarding data
     */
    @Inject(method = "onQueryResponse", at = @At("HEAD"), cancellable = true)
    private void onVelocityForwardingResponse(LoginQueryResponseC2SPacket packet, CallbackInfo ci) {
        // Check if this is a Velocity forwarding response
        if (packet.response() != null) {
            try {
                ByteBuf data = Unpooled.wrappedBuffer(packet.response());
                
                // Read and verify the Velocity forwarding data
                String secret = VelocityUUIDForwarder.getConfig().getSecret();
                
                // Check if secret is still default
                if ("CHANGE_ME".equals(secret)) {
                    VelocityUUIDForwarder.LOGGER.error("Rejecting connection from {} - Forwarding secret not configured!", getConnectionInfo());
                    ci.cancel();
                    return;
                }
                
                VelocityMessageUtil.ForwardingData forwardingData = 
                    VelocityMessageUtil.readForwardingData(data, secret);
                
                // Extract the real Mojang UUID and username
                UUID realUuid = forwardingData.getUuid();
                String username = forwardingData.getUsername();
                
                // Create a new game profile with the correct UUID
                GameProfile newProfile = new GameProfile(realUuid, username);
                
                // Copy properties (skin, cape, etc.)
                for (Property property : forwardingData.getProperties()) {
                    newProfile.getProperties().put(property.name(), property);
                }
                
                // Replace the profile
                this.profile = newProfile;
                
                if (VelocityUUIDForwarder.getConfig().isDebug()) {
                    VelocityUUIDForwarder.LOGGER.info("Applied Velocity forwarding for player {} with UUID {}", 
                        username, realUuid);
                    VelocityUUIDForwarder.LOGGER.info("Original address: {}", forwardingData.getAddress());
                }
                
                data.release();
            } catch (SecurityException e) {
                VelocityUUIDForwarder.LOGGER.error("Rejecting connection from {} - Invalid signature: {}", 
                    getConnectionInfo(), e.getMessage());
                ci.cancel();
            } catch (Exception e) {
                VelocityUUIDForwarder.LOGGER.error("Failed to process Velocity forwarding data from {}: {}", 
                    getConnectionInfo(), e.getMessage(), e);
            }
        }
    }
}
