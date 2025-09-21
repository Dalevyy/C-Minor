package cminor.modifier.good

import cminor.messages.CompilationMessage
import cminor.messages.MessageNumber
import cminor.modifier.ModifierTest

class ModifierGoodTest extends ModifierTest {

    def "Class Declaration - Abstract Class Implementation"() {
        when: "A class inherits from an abstract class."
            input = '''
                        abstr class AbstrClass {
                            public method firstMethod() => Void {}
                            public method secondMethod() => Void {}
                        }

                        class ConcreteClass inherits AbstrClass {
                            public override method firstMethod() => Void {}
                            public override method secondMethod() => Void {}
                        }

                        def obj:ConcreteClass = new ConcreteClass()
                    '''
            vm.runInterpreter(input)

        then: "If all methods from the abstract class are implemented, then no errors will be generated."
            notThrown CompilationMessage
    }

    def "Field Expression - Inherited Method is Called From Child Class"() {
        when: "A protected method is invoked from a class it was not declared in."
            input = '''
                        class A { protected method m() => Void {} }           
                        class B inherits A { public method test() => Void { m() } }
                        
                        def b:B = new B()
                        b.test()
                    '''
            vm.runInterpreter(input)

        then: "No error will occur as 'this.m()' can be invoked directly since the method is inherited."
            notThrown CompilationMessage
    }

    def "Field Expression - Accessing Public Field"() {
        when: "A field is accessed outside a class."
        input = ''' 
                        class myClass { public myField:Int }
                        
                        def a:myClass = new myClass(myField=5)
                        cout << a.myField
                    '''
        vm.runInterpreter(input)

        then: "An error is not generated as long as the field is marked public."
        notThrown CompilationMessage
    }

    def "Function Declaration - Recursive Function"() {
        when: "A recursive call is written inside a function."
            input = '''
                        def recurs factorial(in a:Int) => Int {
                            if(a == 0) { return 1 }
                            return a * factorial(a-1)
                        }
                    '''
            vm.runInterpreter(input)

        then: "As long as the function itself is marked recursive, then no error is generated."
            notThrown CompilationMessage
    }

    def "Method Declaration - Accessing Public Method"() {
        when: "A public method is invoked by an object."
            input = '''
                        class A {
                            public method test() => Void {}
                        }
                        
                        def a:A = new A()
                        a.test()
                    '''
            vm.runInterpreter(input)

        then: "Public methods can be accessed outside a class, so no errors are generated."
            notThrown CompilationMessage
    }
}
