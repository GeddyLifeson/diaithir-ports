package com.github.talrey.createdeco;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.Identifier;

public class CreateDecoMod {
  public static final String MOD_ID = "createdeco";
  public static final String NAME = "Create Deco";
  public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

  public static Identifier id(String path) {
    return Identifier.fromNamespaceAndPath(MOD_ID, path);
  }
}
