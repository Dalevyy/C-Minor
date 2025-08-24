package cminor.modifier

import cminor.CompilationTest
import cminor.interpreter.VM
import cminor.utilities.PhaseNumber

class ModifierTest extends CompilationTest {

    def setupSpec() { vm = new VM(PhaseNumber.MOD_CHECKER) }
}
