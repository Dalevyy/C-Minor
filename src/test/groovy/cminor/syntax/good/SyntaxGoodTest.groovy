package cminor.syntax.good

import cminor.messages.CompilationMessage
import cminor.syntax.SyntaxTest

class SyntaxGoodTest extends SyntaxTest {

    def "Comment - Single Line"() {
        when:
            input = '''
                        // This is a single line comment
                        // The parser should ignore comments
                        // And no errors will be generated
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

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

    def "Global Declaration"() {
        when:
            input = '''
                        def global a:Int 
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

}
