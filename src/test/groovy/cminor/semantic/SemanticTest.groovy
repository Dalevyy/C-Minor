package cminor.semantic

import cminor.CompilationTest
import cminor.interpreter.VM
import cminor.utilities.PhaseNumber

class SemanticTest extends CompilationTest {

    def setupSpec() { vm = new VM(PhaseNumber.SEMANTIC_ANALYZER) }
}
