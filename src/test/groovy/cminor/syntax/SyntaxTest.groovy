package cminor.syntax

import cminor.CompilationTest
import cminor.interpreter.VM
import cminor.utilities.PhaseNumber

class SyntaxTest extends CompilationTest {

    def setupSpec() { vm = new VM(PhaseNumber.SYNTAX_ANALYZER) }
}
