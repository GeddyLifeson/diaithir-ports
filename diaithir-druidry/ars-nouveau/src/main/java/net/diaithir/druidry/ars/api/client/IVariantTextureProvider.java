package net.diaithir.druidry.ars.api.client;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

public interface IVariantTextureProvider<T> {

    @Deprecated(forRemoval = true)
    default Identifier getTexture(LivingEntity entity){
        return null;
    }

    default Identifier getTexture(T entity){
        return getTexture((LivingEntity)entity);
    }

}
