package com.google.security.zynamics.reil.translators.x64;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.python.google.common.collect.Lists;

import com.google.security.zynamics.binnavi.CConfigLoader;
import com.google.security.zynamics.binnavi.Database.CDatabase;
import com.google.security.zynamics.binnavi.Database.CJdbcDriverNames;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntConnectException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntInitializeDatabaseException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDataException;
import com.google.security.zynamics.binnavi.Database.Exceptions.CouldntLoadDriverException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidDatabaseException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidDatabaseVersionException;
import com.google.security.zynamics.binnavi.Database.Exceptions.InvalidExporterDatabaseFormatException;
import com.google.security.zynamics.binnavi.Database.Exceptions.LoadCancelledException;
import com.google.security.zynamics.binnavi.Exceptions.MaybeNullException;
import com.google.security.zynamics.binnavi.disassembly.INaviFunction;
import com.google.security.zynamics.binnavi.disassembly.INaviInstruction;
import com.google.security.zynamics.binnavi.disassembly.INaviModule;
import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilBlock;
import com.google.security.zynamics.reil.ReilFunction;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.TestHelpers;
import com.google.security.zynamics.reil.interpreter.CpuPolicyX64;
import com.google.security.zynamics.reil.interpreter.EmptyInterpreterPolicy;
import com.google.security.zynamics.reil.interpreter.Endianness;
import com.google.security.zynamics.reil.interpreter.InterpreterException;
import com.google.security.zynamics.reil.interpreter.ReilInterpreter;
import com.google.security.zynamics.reil.interpreter.ReilRegisterStatus;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.ReilTranslator;
import com.google.security.zynamics.reil.translators.StandardEnvironmentx64;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.zylib.disassembly.IInstruction;

public class SHA1Test {
  private CDatabase m_database;
  @Before
  public void setUp() throws IOException, CouldntLoadDriverException, CouldntConnectException,
      IllegalStateException, CouldntLoadDataException, InvalidDatabaseException,
      CouldntInitializeDatabaseException, InvalidExporterDatabaseFormatException,
      InvalidDatabaseVersionException, LoadCancelledException {
    final String[] parts = CConfigLoader.loadPostgreSQL();

    m_database =
        new CDatabase("None", CJdbcDriverNames.jdbcPostgreSQLDriverName, parts[0], "sha",
            parts[1], parts[2], parts[3], false, false);

    m_database.connect();
    m_database.load();
    
  }

  @After
  public void tearDown() {
    m_database.close();
  }

  @Test
  public void test() throws CouldntLoadDataException, LoadCancelledException, InternalTranslationException, InterpreterException {
    List<INaviModule> modules = m_database.getContent().getModules();
    assertNotEquals(0, modules.size());
    INaviModule module = modules.get(0);
    module.load();
    
    INaviFunction sha_compress;
    
    try{
      sha_compress = module.getContent().getFunctionContainer().getFunction("sha1_compress");
      sha_compress.load();
    }
    catch(MaybeNullException e) {
      fail();
      module.close();
      return;
    }
    
    assertNotEquals(0,sha_compress.getBasicBlockCount());
    final ReilInterpreter interpreter = new ReilInterpreter(Endianness.LITTLE_ENDIAN,
        new CpuPolicyX64(), new EmptyInterpreterPolicy());
    
    ReilTranslator<INaviInstruction> translator = new ReilTranslator<>();
    ReilFunction func = translator.translate(new StandardEnvironmentx64(), sha_compress);
    
    interpreter.setRegister("dsbase", BigInteger.valueOf(0), OperandSize.QWORD,
         ReilRegisterStatus.DEFINED);
    interpreter.setRegister("ssbase", BigInteger.valueOf(0), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);
   
    
    interpreter.setRegister("rax", TranslationHelpers.getUnsignedBigIntegerValue(0x7FFFFFFFDBC0l), OperandSize.QWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rbx", TranslationHelpers.getUnsignedBigIntegerValue(0), OperandSize.QWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rcx", TranslationHelpers.getUnsignedBigIntegerValue(0x38), OperandSize.QWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rdx", TranslationHelpers.getUnsignedBigIntegerValue(0x7FFFFFFFDB60l), OperandSize.QWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rsi", TranslationHelpers.getUnsignedBigIntegerValue(0x7FFFFFFFDB60l), OperandSize.QWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rdi", TranslationHelpers.getUnsignedBigIntegerValue(0x7FFFFFFFDBC0l), OperandSize.QWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rbp", TranslationHelpers.getUnsignedBigIntegerValue(0x7FFFFFFFDBB0l), OperandSize.QWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rsp", TranslationHelpers.getUnsignedBigIntegerValue(0x7FFFFFFFDB38l), OperandSize.QWORD, ReilRegisterStatus.DEFINED);
    
    interpreter.setMemory(0x7FFFFFFFDB38l, 0, 8);
    
    //long state_base = 0x7fffffffddf0l;
    long state_base = 0x7FFFFFFFDBC0l;
    interpreter.setMemory(state_base, 0x67452301l, 4);
    interpreter.setMemory(state_base+4, 0xEFCDAB89l, 4);
    interpreter.setMemory(state_base+8, 0x98BADCFEl, 4);
    interpreter.setMemory(state_base+12, 0x10325476l, 4);
    interpreter.setMemory(state_base+16, 0xC3D2E1F0l, 4);
    
    long message_base = 0x7FFFFFFFDB60l;
    interpreter.setMemory(message_base, 128, 1);
    for(int i = 1; i < 64; i++)
    {
      interpreter.setMemory(message_base+i, 0, 1);
    }    
    
    interpreter.interpret(func);
    
    assertNotEquals(0, interpreter.getMemorySize());
    assertEquals(BigInteger.valueOf(0xDA39A3EEl), interpreter.getMemory().load(state_base, 4));
    assertEquals(BigInteger.valueOf(0x5E6B4B0Dl), interpreter.getMemory().load(state_base+4, 4));
    assertEquals(BigInteger.valueOf(0x3255BFEFl), interpreter.getMemory().load(state_base+8, 4));
    assertEquals(BigInteger.valueOf(0x95601890l), interpreter.getMemory().load(state_base+12, 4));
    assertEquals(BigInteger.valueOf(0xAFD80709l), interpreter.getMemory().load(state_base+16, 4));
    
    
    module.close();
  }

}
