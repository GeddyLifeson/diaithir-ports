#!/bin/bash
# Static mixin audit: mixin_audit.sh <mod jar> <target jars...>
# Target jars: the NeoForge-patched merged Minecraft jar (client + server), the NeoForge universal jar, JOML.
# Reports missing targets, @Inject CallbackInfo/CallbackInfoReturnable vs return type, handler args, static-ness,
# INVOKE/FIELD/NEW @At targets, WrapOperation/Redirect signatures, @Shadow, @Accessor and @Invoker members.
# Needs asm and asm-tree 9.x on the classpath: ASM_CP=<asm.jar>:<asm-tree.jar>
exec java -cp "$ASM_CP" "$(dirname "$0")/MixinAudit.java" "$@"
