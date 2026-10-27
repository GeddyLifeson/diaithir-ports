package net.id.paradise_lost.platform.services;


public interface IRegistrationHelper {
    void setFlammable(Object block, int encouragement, int flammability);

    void registerTillable(Object block, Object farmlandResult);

    void registerFlattenable(Object block, Object pathResult);

    void registerCreativeTabEntries();
}
