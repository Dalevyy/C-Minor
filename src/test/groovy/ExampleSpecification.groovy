import cminor.messages.CompilationMessage
import spock.lang.Shared
import spock.lang.Specification
import cminor.interpreter.VM

class ExampleSpecification extends Specification {

    static VM
    @Shared input

    def setupSpec() {
        VM = new VM()
        CompilationMessage.setDebugMode()
        input = ""
    }

    def "Local Declaration"() {
        when:
            input = ''' def a:Int = 5 '''
            VM.runInterpreter(input)
        then:
            notThrown CompilationMessage
    }

    def "Local Declaration - Redeclaration Error"() {
        when:
            String input = '''
                                def a:Int = 5
                                def a:Int = 5
                           '''
            VM.runInterpreter(input)
        then: "hey this is cool"
            thrown CompilationMessage
    }
}
