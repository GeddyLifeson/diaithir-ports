package org.dimdev.dimdoors.api.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.dimdoors.api.util.math.Equation;

import java.util.Map;

public class NbtEquations {
    private static final Logger LOGGER = LogManager.getLogger();

    public static CompoundTag solveNbtCompoundEquations(CompoundTag nbt, Map<String, Double> variableMap) {
    CompoundTag solved = new CompoundTag();
    for (String key : nbt.keySet()) {
        if (nbt.get(key) instanceof net.minecraft.nbt.StringTag && key.startsWith("equation_")) {
        try {
            double solution = Equation.parse(nbt.getStringOr(key, "")).apply(variableMap);
            key = key.substring(9);
            if (key.startsWith("int_")) {
            key = key.substring(4);
            solved.putInt(key, (int) solution);
            } else if (key.startsWith("boolean_")) {
            key = key.substring(8);
            solved.putBoolean(key, Equation.toBoolean(solution));
            } else if (key.startsWith("double_")) {
            key = key.substring(7);
            solved.putDouble(key, solution);
            } else {
            solved.putDouble(key, solution);
            }
        } catch (Equation.EquationParseException e) {
            LOGGER.error(e);
        }
        } else if (nbt.get(key) instanceof CompoundTag) {
        solved.put(key, solveNbtCompoundEquations(nbt.getCompoundOrEmpty(key), variableMap));
        } else if (nbt.get(key) instanceof ListTag) {
        solved.put(key, solveListTagEquations((ListTag) nbt.get(key), variableMap));
        } else {
        solved.put(key, nbt.get(key));
        }
    }
    return solved;
    }

    public static ListTag solveListTagEquations(ListTag ListTag, Map<String, Double> variableMap) {
    ListTag solved = new ListTag();
    for (Tag nbt : ListTag) {
        if (nbt.getId() == Tag.TAG_LIST) {
        solved.add(solveListTagEquations((ListTag) nbt, variableMap));
        } else if (nbt.getId() == Tag.TAG_COMPOUND) {
        solved.add(solveNbtCompoundEquations((CompoundTag) nbt, variableMap));
        } else {
        solved.add(nbt);
        }
    }
    return solved;
    }
}
