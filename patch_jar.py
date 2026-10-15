# Rewrites the one UTF8 constant in WorldLoaderMixin.class (the INVOKE target) to a descriptor-less name match, then repacks the jar.
import struct, sys, zipfile
src, dst = sys.argv[1], sys.argv[2]
CLS = "dev/latvian/mods/kubejs/core/mixin/WorldLoaderMixin.class"
OLD = b"Lnet/minecraft/resources/RegistryDataLoader;load(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;Ljava/util/List;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
NEW = b"Lnet/minecraft/resources/RegistryDataLoader;load"
zin = zipfile.ZipFile(src)
data = zin.read(CLS)
needle = struct.pack(">BH", 1, len(OLD)) + OLD
assert data.count(needle) == 1
data = data.replace(needle, struct.pack(">BH", 1, len(NEW)) + NEW)
with zipfile.ZipFile(dst, "w", zipfile.ZIP_DEFLATED) as zout:
    for i in zin.infolist():
        zout.writestr(i, data if i.filename == CLS else zin.read(i.filename))
print("ok")
