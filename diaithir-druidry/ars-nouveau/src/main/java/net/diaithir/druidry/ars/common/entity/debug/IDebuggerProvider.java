package net.diaithir.druidry.ars.common.entity.debug;

import net.diaithir.druidry.ars.common.util.Log;

public interface IDebuggerProvider {

    /**
     * Debug flag, enabled with -Dars_nouveau.debug=true (replaces the former ArsNouveau.isDebug field).
     */
    static boolean isDebugEnabled() {
        return Boolean.getBoolean("ars_nouveau.debug");
    }

    IDebugger getDebugger();

     default void addDebugEvent(DebugEvent event){
         this.addDebugEvent(event, false);
    }

    default void addDebugEvent(DebugEvent event, boolean storeDuplicate){
        getDebugger().addEntityEvent(event, storeDuplicate);
        if(isDebugEnabled()){
            Log.getLogger().debug(event.toString());
        }
    }
}
