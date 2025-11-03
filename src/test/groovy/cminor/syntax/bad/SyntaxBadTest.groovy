package cminor.syntax.bad

import cminor.messages.CompilationMessage
import cminor.messages.MessageNumber
import cminor.syntax.SyntaxTest

class SyntaxBadTest extends SyntaxTest {

    def "Lexer - Invalid String Literal"() {
        when:
            input = '''
                        def a:String = 'hello world
                    '''
            vm.runInterpreter(input)

        then:
            error = thrown CompilationMessage
            error.msg.messageType == MessageNumber.SYNTAX_ERROR_100
    }
}
