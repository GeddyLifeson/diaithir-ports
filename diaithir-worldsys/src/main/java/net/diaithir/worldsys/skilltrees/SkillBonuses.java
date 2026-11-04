package net.diaithir.worldsys.skilltrees;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Applies level based attribute modifiers. */
final class SkillBonuses {
    private SkillBonuses() {}

    static void apply(ServerPlayer player, SkillData data) {
        set(player, Attributes.BLOCK_BREAK_SPEED, Skill.MINING, 0.02 * data.level(Skill.MINING), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        set(player, Attributes.ATTACK_DAMAGE, Skill.COMBAT, 0.1 * data.level(Skill.COMBAT), AttributeModifier.Operation.ADD_VALUE);
        set(player, Attributes.MAX_HEALTH, Skill.FARMING, 0.5 * (data.level(Skill.FARMING) / 5), AttributeModifier.Operation.ADD_VALUE);
        set(player, Attributes.MOVEMENT_SPEED, Skill.EXPLORING, 0.001 * data.level(Skill.EXPLORING), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    private static void set(ServerPlayer player, net.minecraft.core.Holder<Attribute> attr, Skill skill, double amount, AttributeModifier.Operation op) {
        AttributeInstance inst = player.getAttribute(attr);
        if (inst == null) return;
        Identifier id = Identifier.fromNamespaceAndPath("diaithirworldsys", "skill_" + skill.key());
        inst.removeModifier(id);
        if (amount > 0) inst.addPermanentModifier(new AttributeModifier(id, amount, op));
    }
}
