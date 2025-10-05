package cminor.syntax.good

import cminor.messages.CompilationMessage
import cminor.syntax.SyntaxTest

class SyntaxGoodTest extends SyntaxTest {

    def "Class Declaration"() {
        when:
            input = '''
                        class A {}
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Comment - Block"() {
        when:
            input = '''
                        /* 
                            The parser should ignore
                            this block comment!
                        */
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage

    }

    def "Comments - Mixed"() {
        when:
            input = '''
                        // This is a comment

                        /*
                            This is also another comment,
                            but it's nicer to read.
                        */
                        
                        
                        // I make another comment here.
                        // And I make another here because I can
                        
                        /*
                            And now we have a multi-line comment
                            underneath the two single lines.
                        */
                        
                        // And finally the last multi-line is here
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

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

    def "Field Declaration"() {
        when:
        input = '''
                        class A { protected x:Int }
                    '''
        vm.runInterpreter(input)

        then:
        notThrown CompilationMessage
    }

    def "Field Declaration 2"() {
        when:
        input = '''
                        class A {
                            property myValue:Int
                            public myValue2:String 
                            protected myValue3:Real
                        }
                    '''
        vm.runInterpreter(input)

        then:
        notThrown CompilationMessage
    }

    def "Field Declaration 3"() {
        when:
            input = '''
                        class A { protected x:Int, y:Int, z:Int }
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

    def "Local Declaration"() {
        when:
            input = '''
                        def a:Int = 5
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Local Declaration 2"() {
        when:
            input = '''
                        def local a:Int = 5
                        def local myLetter:Char = 'b'
                        def flag:Bool = True
                        def myName:String = 'Bob'
                        def local pi:Real = 3.14
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Local Declaration 3"() {
        when:
            input = '''
                        def local a:Int = 5, b:Int = 7, c:Int = 9
                        def myName:Char = 'Bob', myName2:Char = 'Charlie'
                        def flag1:Bool = True, flag2:Bool = False, flag3:Bool = False 
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Method Declaration"() {
        when:
            input = '''
                        class A {
                            public method myMethod(in a:Int, in b:String) => Void { }
                            protected method myMethod2() => Void { }
                        }
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

}
