package cminor.scope

import cminor.CompilationTest
import cminor.interpreter.VM
import cminor.utilities.PhaseNumber

class ScopeTest extends CompilationTest {

    def setupSpec() { vm = new VM(PhaseNumber.NAME_CHECKER) }


}
