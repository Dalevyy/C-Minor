package cminor.syntax.good

import cminor.messages.CompilationMessage
import cminor.syntax.SyntaxTest

class SyntaxGoodTest extends SyntaxTest {

    def "Array Expression"() {
        when:
            input = '''
                        def const SIZE:Int = 10
                        def lst:Array[Int] = Array[SIZE](1,2,3,4,5,6,7,8,9,10)
                        
                        lst[0]
                        lst[1]
                        lst[1+3/4]
                        lst[(3+4)-3/12]
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Assignment Statement"() {
        when:
            input = '''
                        def local num:Int = 10
    
                        set a = 5
                        set a += 1
                        set a -= 34
                        set a *= 3
                        set a /= 2
                        set a %= 4
                        set a **= 6
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Binary Expression - Arithmetic"() {
        when:
            input = '''
                        def local a:Int = 5, b:Int = 6
                        
                        a + b
                        a - b
                        a * b
                        a / b
                        a % b
                        a ** b
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Binary Expression - Logical"() {
        when:
            input = '''
                        def local c:Bool = True, d:Bool = False
                    
                        c and d
                        c or d
                    
                        not c or not d
                        not c and not d
                    
                        not c and d
                        c and not d
                    
                        not c or d
                        c or not d
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Binary Expression - Relational"() {
        when:
            input = '''
                       def local a:Int = 10, b:Int = 8
            
                        a == b
                        a != b
                        a < b
                        a <= b
                        a > b
                        a >= b
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Binary Expression - Shift"() {
        when:
            input = '''
                        def local e:Int = 2, f:Int = 6

                        e << f
                        e >> f
                    
                        e & f
                        e ^ f
                        e | f
                    
                        (e & f ^ f)
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Cast Expression"() {
        when:
            input = '''
                        Int(3.14)
                        Char('a')
                        Real(5)
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Choice Statement"() {
        when:
            input = '''
                        def local a:Int = 5
                        
                        choice a {
                            on 1 { cout << '1' << endl }
                            on 2 { cout << '2' << endl }
                            other { cout << 'None' << endl }
                        }
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Class Declaration"() {
        when:
            input = '''
                        class A {}
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Class Declaration 2"() {
        when:
            input = '''
                        class A {}
                        final class B inherits A { }
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Class Declaration 3"() {
        when:
            input = '''     
                        abstr class A {
                            protected method use() => Void {}
                            protected method find() => Void {}
                        }
                        
                        class B inherits A {
                            protected override method use() => Void {}
                            protected override method find() => Void {}
                        }
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Class Declaration 4"() {
        when:
            input = '''
                        final class A<discr t, class s, scalar u> {}
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Class Declaration 5"() {
        when:
            input = '''
                        abstr class A<discr t, discr s> {}
                        final class B inherits A<Int,Int> {}
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

    def "Do Statement"() {
        when:
            input = '''
                        def local a:Int = 5
                        
                        do {
                            cout << 'While loop running' << endl
                            set a += 1
                        } while a != 10
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

    def "For Statement"() {
        when:
            input = '''
                        for(def i:Int in 1..5) { }
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Function Declaration"() {
        when:
            input = '''
                        def func() => Void {}
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Function Declaration 2"() {
        when:
            input = '''
                        def func(in a:Int, out b:Int) => Void { }
                        def func2(inout a:Int, inout b:real, ref c:Array[Int]) => Void { }
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Function Declaration 3"() {
        when:
            input = '''
                    def pure myFunction<discr t>() => Void { }
                    def pure myFunction2<scalar t, class t2>() => Void {}
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Global Declaration"() {
        when:
            input = '''
                        def global a:Int = uninit
                        def const b:Real = uninit
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Global Declaration 2"() {
        when:
            input = '''
                        def global a:Int = uninit, b:Int = uninit, c:Int = uninit
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Global Declaration 3"() {
        when:
            input = '''
                        def global a:Array[Int] = uninit
                        def global b:Array[Array[Int]] = uninit
                        def global c:List[Real] = uninit
                        def global d:List[List[List[String]]] = uninit
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "If Statement"() {
        when:
            input = '''
                        def local a:Int = 5, b:Int = 10
                        
                        if(a < b) { cout << 'This is a true statement.' << endl }
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "If Statement 2"() {
        when:
            input = '''
                        def local a:Int = 5, b:Int = 10
                        
                        if(a < b) {
                            cout << 'This is a true statement.'
                        }
                        else {
                            cout << 'This is not a true statement.'
                        }
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "If Statement 3"() {
        when:
            input = '''
                        def local a:Int = 5, b:Int = 10
                        
                        if a < b {
                            cout << 'a is less than b'
                        }
                        else if (a > b) {
                            cout << 'a is greater than b'
                        }
                        else {
                            cout << 'a is exactly equal to b'
                        }
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "If Statement 4"() {
        when:
            input = '''
                        def local a:Int = 5, b:Int = 10
                        if a < b {
                            if(a == 5) { cout << 'Congrats, you won a reward!' }
                            else { cout << 'You lost...' }
                        } 
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Input Statement"() {
        when:
            input = '''
                        def a:Int = uninit
                        cin >> a
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Invocation"() {
        when:
            input = '''
                    def myFunction1() => Void { }
                    def myFunction2(in a:Int, in b:Int) => Int {  return a+b }

                    myFunction1()
                    myFunction2(3,5)
                    myFunction2(3242+4232,5/3)
                    myFunction2(a+b-c,'a')
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Literal"() {
        when:
            input = '''
                        5
                        3
                        4
                        ~3
                        ~20
                        ~39
                        
                        'c'
                        'a'
                        
                        'hello there'
                        'Burger\nHam\nCheese\n'
                        '"This is a sentence"'
                        
                        True
                        False
                        True True
                        True True
                        False True
                        False True
                        
                        3.1415926
                        1000.120211
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

    def "Local Declaration 4"() {
        when:
            input = '''
                        def lst:Array[Array[Int]] = Array[2][2](Array(1,2),Array(3,4))
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Local Declaration 5"() {
        when:
            input = '''
                        def lst:List[Int] = List(1,2,3,4,5)
                        def lst2:List[Real] = List(1.0,2.0,3.0,4.0,5.0)
                        def lst3:List[String] = List('hi', 'my name is', 'george')
                    
                        lst3[1]
                        lst2[3+4/2]
                        lst['c']
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

    def "New Expression"() {
        when:
            input = '''
                        class A { 
                            protected firstName:String, lastName:String
                        }
                        
                        def a:A = new A()
                        def a2:A = new A(firstName='ben',lastName='franklin')
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Operator Overload"() {
        when:
            input = '''
                        class A {
                            public operator==(in a:Int, in b:String) => Void { }
                            protected operator<=() => Void { }
                        }
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Output Statement"() {
        when:
            input = '''
                        cout << 'Hi' << 'Checking if this works :)' << endl
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Return Statement"() {
        when:
            input = '''     
                        return 3
                        return a
                        return ~50
                        return 'my name is'
                        return 3+5/32+a-myName
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Stop Statement"() {
        when:
            input = '''
                        stop
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "Unary Expression"() {
        when:
            input = '''
                        def a:Int = 5
                        def b:Bool = False
                        
                        ~a 
                        not b
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }

    def "While Statement"() {
        when:
            input = '''
                        def local a:Int = 5
                        
                        while a != 10 {
                            cout << 'While loop running' << endl
                            set a += 1
                        }
                    '''
            vm.runInterpreter(input)

        then:
            notThrown CompilationMessage
    }
}
