package cminor.type.good

import cminor.messages.CompilationMessage
import cminor.type.TypeTest

class TypeGoodTest extends TypeTest {

    def "List - Valid 1D List Initialization"() {
        when: "A user writes a 1D list literal by itself."
            input = '''
                       List(1,2,3,4,5)
                    '''
            vm.runInterpreter(input)

        then: "No error will occur if the list contains no type errors."
            notThrown CompilationMessage
    }

    def "List - Valid 1D List Initialization 2"() {
        when: "A local variable representing a 1D list is initialized to a 1D list."
            input = '''
                       def a:List[Int] = List(1,2,3,4,5)
                    '''
            vm.runInterpreter(input)

        then: "No errors will occur since the list is assignment compatible with the list type."
            notThrown CompilationMessage
    }

    def "List - Valid 1D List Initialization 3"() {
        when: "A 1D list is initialized to an empty 1D list."
            input = '''
                        def a:List[Int] = List()
                    '''
            vm.runInterpreter(input)

        then: "A list is allowed to be empty since it is dynamic so no errors occur."
            notThrown CompilationMessage
    }

    def "List - Valid 2D List Initialization"() {
        when: "A variable representing a 2D list is initialized to a 2D list."
            input = '''
                        def a:List[List[Int]] = List(List(1,2,3),List(4,5))
                    '''
            vm.runInterpreter(input)

        then: "No error should occur since both types are list assignment compatible."
            notThrown CompilationMessage
    }

    def "List - Valid 2D List Initialization 2"() {
        when: "A variable representing a 2D list is initialized to an empty 2D list."
            input = '''
                        def a:List[List[Int]] = List(List())
                    '''
            vm.runInterpreter(input)

        then: "No error should occur since both types are list assignment compatible."
            notThrown CompilationMessage
    }

    def "List - Valid 2D List Initialization 3"() {
        when: "A variable representing a 2D list is initialized to an empty 2D list."
            input = '''
                        def a:List[List[Int]] = List(List(), List(), List())
                    '''
            vm.runInterpreter(input)

        then: "No error should occur since both types are assignment compatible."
            notThrown CompilationMessage
    }

    def "List - Valid 2D List Initialization 4"() {
        when:
            input = '''
                        List(List(1,2,3,4,5), List(), List(6,7,8), List())
                    '''
            vm.runInterpreter(input)
        then:
            notThrown CompilationMessage
    }

    def "List - Valid 3D List Initialization"() {
        when: "A variable representing a 3D list is initialized to a 3D list."
            input = '''
                        def a:List[List[List[Int]]] = List(List(List(1,2,3), List(4,5,6)), List(List(6,7,8,9)))
                    '''
            vm.runInterpreter(input)

        then: "No error should occur since both types are list assignment compatible."
            notThrown CompilationMessage
    }

    def "List - Valid 3D List Initialization 2"() {
        when: "A 3D list is initialized to an empty 2D list and a 2D Int list."
            input = '''
                        List(List(List()), List(List(1,2,3,4),List(7,8,9)))
                    '''
            vm.runInterpreter(input)

        then: "An error is generated since "
            notThrown CompilationMessage
    }

    def "Local Declaration - Scalars"() {
        when: "Local variables are declared."
            input = '''
                        def a:Int = 5
                        def b:Real = 3.14
                        def c:Char = 'c'
                        def d:Bool = True
                        def e:String = 'hi'
                    '''
            vm.runInterpreter(input)

        then: "No errors are generated if all values were correctly typed."
            notThrown CompilationMessage
    }

    def "Local Declaration - Uninitialized Values"() {
        when: "Local variables are declared and are left uninitialized."
            input = '''
                        def a:Int = uninit
                        def b:Real = uninit
                        def c:Char = uninit
                        def d:Bool = uninit
                        def e:String = uninit
                    '''
            vm.runInterpreter(input)

        then: "A default value should be generated for each variable, and no errors will occur."
            notThrown CompilationMessage
    }
}
