package cminor

import cminor.messages.CompilationMessage
import spock.lang.Shared
import spock.lang.Specification

abstract class CompilationTest extends Specification {

    @Shared vm
    @Shared input
    @Shared error

    def setupSpec() {
        CompilationMessage.setDebugMode()
        input = ""
        error = null
    }

    /*
    After each test, we are going to clear out the global scope. This will
    slow down our tests, but it allows for more flexibility when writing
    tests (i.e. using the same variable names in different tests).
*/
    def cleanup() {
        vm.globalUnit.reset()
        vm.phaseHandler.reset()
    }
}
