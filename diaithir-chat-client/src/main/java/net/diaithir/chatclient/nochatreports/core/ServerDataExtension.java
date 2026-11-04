// Ported from No Chat Reports (WTFPL) https://github.com/Aizistral-Studios/No-Chat-Reports
// Modifications: relocated package.
package net.diaithir.chatclient.nochatreports.core;

public interface ServerDataExtension {
    boolean preventsChatReports();

    void setPreventsChatReports(boolean prevents);
}
