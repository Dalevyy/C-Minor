package cminor.runtime

import cminor.CompilationTest
import cminor.interpreter.VM
import cminor.utilities.PhaseNumber

class RuntimeTest extends CompilationTest {

    def setupSpec() { vm = new VM(PhaseNumber.INTERPRETER) }
}
