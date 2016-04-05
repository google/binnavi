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
import com.google.security.zynamics.reil.translators.StandardEnvironment;
import com.google.security.zynamics.reil.translators.TranslationHelpers;
import com.google.security.zynamics.zylib.disassembly.IInstruction;

public class SimpleFunctionTest {
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
    assertEquals(2, modules.size());
    INaviModule module = modules.get(1);
    module.load();
    
    INaviFunction sha_compress;
    
    try{
      sha_compress = module.getContent().getFunctionContainer().getFunction("test_many_calc");
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
    ReilFunction func = translator.translate(new StandardEnvironment(), sha_compress);
    
   // interpreter.setRegister("dsbase", BigInteger.valueOf(0), OperandSize.QWORD,
   //      ReilRegisterStatus.DEFINED);
    /*interpreter.setRegister("csbase", BigInteger.valueOf(0x33), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);*/
   interpreter.setRegister("ssbase", BigInteger.valueOf(/*0x7fffffffdfa0l*/0), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);
   /* interpreter.setRegister("fsbase", BigInteger.valueOf(0), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("esbase", BigInteger.valueOf(0), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("gsbase", BigInteger.valueOf(0), OperandSize.QWORD,
        ReilRegisterStatus.DEFINED);*/
    
    interpreter.setRegister("rax", TranslationHelpers.getUnsignedBigIntegerValue(0x40060b), OperandSize.QWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rbx", TranslationHelpers.getUnsignedBigIntegerValue(0), OperandSize.QWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rcx", TranslationHelpers.getUnsignedBigIntegerValue(0x108b), OperandSize.QWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rdx", TranslationHelpers.getUnsignedBigIntegerValue(0x181), OperandSize.QWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rsi", TranslationHelpers.getUnsignedBigIntegerValue(0x14e1), OperandSize.QWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rdi", TranslationHelpers.getUnsignedBigIntegerValue(0x1a5), OperandSize.QWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rbp", TranslationHelpers.getUnsignedBigIntegerValue(0x7fffffffdea0l), OperandSize.QWORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("rsp", TranslationHelpers.getUnsignedBigIntegerValue(0x7fffffffde78l), OperandSize.QWORD, ReilRegisterStatus.DEFINED);
//    interpreter.setRegister("r8",  TranslationHelpers.GetUnsignedBigIntegerValue(0x7ffff7dd4dd0l), OperandSize.QWORD, ReilRegisterStatus.DEFINED);
//    interpreter.setRegister("r9",  TranslationHelpers.GetUnsignedBigIntegerValue(0x7ffff7de9a20l), OperandSize.QWORD, ReilRegisterStatus.DEFINED);
//    interpreter.setRegister("r10", TranslationHelpers.GetUnsignedBigIntegerValue(0x833l), OperandSize.QWORD, ReilRegisterStatus.DEFINED);
//    interpreter.setRegister("r11", TranslationHelpers.GetUnsignedBigIntegerValue(0x7ffff7a2f950l), OperandSize.QWORD, ReilRegisterStatus.DEFINED);
//    interpreter.setRegister("r12", TranslationHelpers.GetUnsignedBigIntegerValue(0x400440l), OperandSize.QWORD, ReilRegisterStatus.DEFINED);
//    interpreter.setRegister("r13", TranslationHelpers.GetUnsignedBigIntegerValue(0x7fffffffdf80l), OperandSize.QWORD, ReilRegisterStatus.DEFINED);
//    interpreter.setRegister("r14", TranslationHelpers.GetUnsignedBigIntegerValue(0), OperandSize.QWORD, ReilRegisterStatus.DEFINED);
//    interpreter.setRegister("r15", TranslationHelpers.GetUnsignedBigIntegerValue(0), OperandSize.QWORD, ReilRegisterStatus.DEFINED);
//    interpreter.setRegister("rip", TranslationHelpers.GetUnsignedBigIntegerValue(0x4005a5), OperandSize.QWORD, ReilRegisterStatus.DEFINED);

    
    
    interpreter.setMemory(0x7fffffffde78l, 5, 8);
    
    interpreter.interpret(
        TestHelpers.createMapping(
            Lists.newArrayList(func.getGraph().getNodes().get(0).getInstructions())),
        sha_compress.getAddress().toBigInteger());
        //func.getGraph().getNodes().get(0).getAddress().toBigInteger());
    
    assertEquals(BigInteger.valueOf(502287),interpreter.getVariableValue("rax"));
    
    module.close();
  }

}
