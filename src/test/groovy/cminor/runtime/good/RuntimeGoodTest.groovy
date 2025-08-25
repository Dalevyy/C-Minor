package cminor.runtime.good

import cminor.messages.CompilationMessage
import cminor.runtime.RuntimeTest

class RuntimeGoodTest extends RuntimeTest {

    def "Local Declaration - Accessing Local Variables"() {
        when:
            input = '''
                        def a:Int = 4
                        def b:Char = 'c'
                        def c:Bool = False
                        def d:Real = 3.14
                        def e:String = 'hello world'
                        
                        cout << 'a = ' << a 
                        cout << 'b = ' << b
                        cout << 'c = ' << c 
                        cout << 'd = ' << d 
                        cout << 'e = ' << e 
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

}
