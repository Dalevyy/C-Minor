package cminor.type

import cminor.CompilationTest
import cminor.interpreter.VM
import cminor.utilities.PhaseNumber

class TypeTest extends CompilationTest {

    def setupSpec() { vm = new VM(PhaseNumber.TYPE_CHECKER) }
}
