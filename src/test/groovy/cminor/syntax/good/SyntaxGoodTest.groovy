package cminor.syntax.good

import cminor.messages.CompilationMessage
import cminor.syntax.SyntaxTest

class SyntaxGoodTest extends SyntaxTest {

    def "Enum Declaration"() {
        when:
            input = '''
                        def WEEKS type = { MON }
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Enum Declaration 2"() {
        when:
            input = '''
                        def WEEKS type = { MON, TUES, WEDS }
                        def NAMES type = { ALICE, BOB, CHARLIE }
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }
}
