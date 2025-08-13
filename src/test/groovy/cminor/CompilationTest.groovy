package cminor

import cminor.interpreter.VM
import cminor.messages.CompilationMessage
import spock.lang.Shared
import spock.lang.Specification

abstract class CompilationTest extends Specification {

    @Shared VM
    @Shared input
    @Shared error

    def setupSpec() {
        VM = new VM(2)
        CompilationMessage.setDebugMode()
        input = ""
        error = null
    }

    /*
        After each test, we are going to clear out the global scope. This will
        slow down our tests, but it allows for more flexibility when writing
        tests (i.e. using the same variable names in different tests).
    */
    def cleanup() { VM.globalUnit.reset() }
}
