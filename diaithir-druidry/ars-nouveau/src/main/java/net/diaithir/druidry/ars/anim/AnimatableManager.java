package net.diaithir.druidry.ars.anim;

import java.util.LinkedHashMap;
import java.util.Map;

public class AnimatableManager<T> {
    private final Map<String, AnimationController<T>> controllers = new LinkedHashMap<>();
    public Map<String, AnimationController<T>> getAnimationControllers() { return controllers; }
    public static class ControllerRegistrar {
        private final Map<String, AnimationController<?>> map;
        @SuppressWarnings({"unchecked", "rawtypes"})
        public ControllerRegistrar(AnimatableManager m) { this.map = (Map) m.controllers; }
        public ControllerRegistrar add(AnimationController<?>... cs) { for (AnimationController<?> c : cs) map.put(c.getName(), c); return this; }
    }
}
