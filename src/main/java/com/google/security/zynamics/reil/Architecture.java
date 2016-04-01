package com.google.security.zynamics.reil;

import com.google.security.zynamics.reil.translators.ITranslationEnvironment;
import com.google.security.zynamics.reil.translators.StandardEnvironment;
import com.google.security.zynamics.reil.translators.StandardEnvironmentx64;

public class Architecture {
  public static Architecture x86 = new Architecture("x86-32", new StandardEnvironment());
  public static Architecture x86_64 = new Architecture("x86-64", new StandardEnvironmentx64());
  public static Architecture ARM = new Architecture("arm32", new StandardEnvironment());
  public static Architecture MIPS = new Architecture("mips32", new StandardEnvironment());
  public static Architecture PPC = new Architecture("PowerPC", new StandardEnvironment());
  public static Architecture REIL = new Architecture("REIL", new StandardEnvironment());

  public static Architecture getArchitecture(String arch) {
    if (arch.equalsIgnoreCase("x86-32")) {
      return Architecture.x86;
    } else if (arch.equalsIgnoreCase("x86-64")) {
      return Architecture.x86_64;
    } else if (arch.equalsIgnoreCase("ARM")) {
      return Architecture.ARM;
    } else if (arch.equalsIgnoreCase("POWERPC")) {
      return Architecture.PPC;
    } else if (arch.equalsIgnoreCase("REIL")) {
      return Architecture.REIL;
    } else if (arch.equalsIgnoreCase("MIPS")) {
      return Architecture.MIPS;
    } else
      return null;
  }

  public ITranslationEnvironment getEnviroment() {
    return environment;

  }

  @Override
  public String toString() {
    return name;
  };

  private String name;
  private ITranslationEnvironment environment;

  private Architecture(String name, ITranslationEnvironment env) {
    this.name = name;
    this.environment = env;
  }
}
