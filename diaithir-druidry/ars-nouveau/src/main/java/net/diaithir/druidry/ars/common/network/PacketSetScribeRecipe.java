package net.diaithir.druidry.ars.common.network;

import net.diaithir.druidry.ars.ArsNouveau;
import net.diaithir.druidry.ars.common.block.tile.ScribesTile;
import net.diaithir.druidry.ars.common.crafting.recipes.GlyphRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;

public class PacketSetScribeRecipe extends AbstractPacket {
    public static final Type<PacketSetScribeRecipe> TYPE = new Type<>(ArsNouveau.prefix("set_scribe_recipe"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketSetScribeRecipe> CODEC = StreamCodec.ofMember(PacketSetScribeRecipe::toBytes, PacketSetScribeRecipe::new);
    BlockPos scribePos;
    Identifier recipeID;

    public PacketSetScribeRecipe(RegistryFriendlyByteBuf buf) {
        this.scribePos = buf.readBlockPos();
        this.recipeID = buf.readIdentifier();
    }

    //Encoder
    public void toBytes(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(scribePos);
        buf.writeIdentifier(recipeID);
    }

    public PacketSetScribeRecipe(BlockPos scribesPos, Identifier resourceLocation) {
        this.scribePos = scribesPos;
        this.recipeID = resourceLocation;
    }

    @Override
    public void onServerReceived(MinecraftServer minecraftServer, ServerPlayer player) {
        if (player.level().getBlockEntity(scribePos) instanceof ScribesTile scribesTile) {
            var key = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE, recipeID);
            var found = minecraftServer.getRecipeManager().byKey(key);
            if (found.isPresent() && found.get().value() instanceof GlyphRecipe) {
                @SuppressWarnings("unchecked")
                RecipeHolder<GlyphRecipe> recipe = (RecipeHolder<GlyphRecipe>) found.get();
                scribesTile.setRecipe(recipe, player);
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
