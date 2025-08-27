package cminor.runtime.good

import spock.lang.Shared
import cminor.runtime.RuntimeTest

// NOTE: FORMATTING MATTERS FOR THE OUTPUT RESULTS!!!!
class RuntimeGoodTest extends RuntimeTest {

    @Shared os

    def setupSpec() {
        // We will have the interpreter write to a separate stream!
        os = new ByteArrayOutputStream()
        System.setOut(new PrintStream(os))
    }

    def "Assignment Statement - Assignment Operators (Int)"() {
        when: "An assignment operator is used on Int variables."
            input = '''
                        def a:Int = 1
                        def b:Int = 2
                        def c:Int = 3
                        def d:Int = 4
                        def e:Int = 5
                        def f:Int = 6

                        set a += 2
                        set b -= 3
                        set c *= 2
                        set d /= 4
                        set e %= 2
                        set f **= 2
                        
                        cout << 'a = ' << a << endl 
                        cout << 'b = ' << b << endl 
                        cout << 'c = ' << c << endl 
                        cout << 'd = ' << d << endl 
                        cout << 'e = ' << e << endl 
                        cout << 'f = ' << f << endl 
                    '''
            vm.runInterpreter(input)

        then: "Each variable will be updated based on the assignment operator used."
            os.toString().contains(
                "a = 3\n" +
                "b = -1\n" +
                "c = 6\n" +
                "d = 1\n" +
                "e = 1\n" +
                "f = 36"
            )
    }

    def "Assignment Statement - Assignment Operators (Real)"() {
        when: "An assignment operator is used on Real variables."
            input = '''
                        def g:Real = 1.0
                        def h:Real = 2.0
                        def i:Real = 3.0
                        def j:Real = 4.0
                        def k:Real = 5.0
                        def l:Real = 6.0
                        
                        set g += 2.0
                        set h -= 3.0
                        set i *= 2.0
                        set j /= 4.0
                        set k %= 2.0
                        set l **= 2.0
                        
                        cout << 'g = ' << g << endl
                        cout << 'h = ' << h << endl
                        cout << 'i = ' << i << endl
                        cout << 'j = ' << j << endl
                        cout << 'k = ' << k << endl
                        cout << 'l = ' << l << endl 
                    '''
            vm.runInterpreter(input)

        then: "Each variable will be updated based on the assignment operator used."
            os.toString().contains(
                "g = 3.0\n" +
                "h = -1.0\n" +
                "i = 6.00\n" +
                "j = 1\n" +
                "k = 1.0\n" +
                "l = 36.00"
            )
    }

    def "Assignment Statement - Assigning Value to a Local Variable"() {
        when: "A variable is declared with an initial value and an assignment is executed."
            input = '''
                        def a:Int = 5
                        cout << 'Before Assignment = ' << a << endl
                        set a = 7
                        cout << 'After Assignment = ' << a << endl
                    '''
            vm.runInterpreter(input)

        then: "The variable should change its value."
            os.toString().contains(
                "Before Assignment = 5\n" +
                "After Assignment = 7"
            )
    }

    def "Assignment Statement - String Concatenation"() {
        when: "An assignment operator is used on a String variable."
            input = '''
                        def m:String = 'hello'
                        set m += ' world'
                        cout << m << endl
                    '''
            vm.runInterpreter(input)

        then: "The variable will now contain a concatenated String."
            os.toString().contains("hello world")
    }

    def "Binary Expression - Arithmetic Expressions (Int)"() {
        when: "A binary arithmetic expression is used for Int values."
            input = ''' 
                        cout << '2+3 = ' << 2 + 3 << endl
                        cout << '2-3 = ' << 2 - 3 << endl
                        cout << '2*3 = ' << 2 * 3 << endl
                        cout << '2/3 = ' << 2 / 3 << endl
                        cout << '2%3 = ' << 2 % 3 << endl
                        cout << '2**3 = ' << 2 ** 3 << endl
                    '''
            vm.runInterpreter(input)

        then: "All operations should be performed and the correct result is calculated."
            os.toString().contains(
                "2+3 = 5\n" +
                "2-3 = -1\n" +
                "2*3 = 6\n" +
                "2/3 = 0\n" +
                "2%3 = 2\n" +
                "2**3 = 8"
            )
    }

    def "Binary Expression - Arithmetic Operators (Real)"() {
        when: "A binary arithmetic expression is used for Real values."
            input = ''' 
                        cout << '2.1+3.2 = ' << 2.1 + 3.2 << endl
                        cout << '2.1-3.2 = ' << 2.1 - 3.2 << endl
                        cout << '2.1*3.2 = ' << 2.1 * 3.2 << endl
                        cout << '2.1/3.2 = ' << 2.1 / 3.2 << endl
                        cout << '2.1%3.2 = ' << 2.1 % 3.2 << endl
                        cout << '2.1**3.2 = ' << 2.1 ** 3.2 << endl
                    '''
            vm.runInterpreter(input)

        then: "All operations should be performed and the correct result is calculated."
            os.toString().contains(
                "2.1+3.2 = 5.3\n" +
                "2.1-3.2 = -1.1\n" +
                "2.1*3.2 = 6.72\n" +
                "2.1/3.2 = 0.65625\n" +
                "2.1%3.2 = 2.1\n" +
                "2.1**3.2 = 9.261"
            )
    }

    def "Binary Expression - Bitwise Operators (Bool)"() {
        when: "A bitwise expression is written for Bool literals."
            input = '''
                        cout << 'True & True = ' << (True & True) << endl 
                        cout << 'True | False = ' << (True | False) << endl 
                        cout << 'True ^ False = ' << (True ^ False) << endl 
                    '''
            vm.runInterpreter(input)

        then: "The expression should be correctly evaluated."
            os.toString().contains(
                "True & True = true\n" +
                "True | False = true\n" +
                "True ^ False = true"
            )
    }

    def "Binary Expression - Bitwise Operators (Char)"() {
        when: "A bitwise expression is written for Char literals."
            input = '''
                        cout << ('a' & 'd') << endl 
                        cout << ('a' | 'd') << endl 
                        cout << ('a' ^ 'd') << endl 
                    '''
        vm.runInterpreter(input)

        then: "The expression should be correctly evaluated."
            os.toString().contains(
                "96\n" +
                "101\n" +
                "5"
            )
    }

    def "Binary Expression - Bitwise Operators (Int)"() {
        when: "A bitwise expression is written for Int literals."
            input = '''
                        cout << '2 << 8 = ' << (2 << 8) << endl 
                        cout << '2 >> 8 = ' << (2 >> 8) << endl 
                        cout << '2 & 8 = ' << (2 & 8) << endl 
                        cout << '2 | 8 = ' << (2 | 8) << endl 
                        cout << '2 ^ 8 = ' << (2 ^ 8) << endl 
                    '''
            vm.runInterpreter(input)

        then: "The expression should be correctly evaluated."
            os.toString().contains(
                "2 << 8 = 512\n" +
                "2 >> 8 = 0\n" +
                "2 & 8 = 0\n" +
                "2 | 8 = 10\n" +
                "2 ^ 8 = 10"
            )
    }

    def "Binary Expression - Equality Operators (Bool)"() {
        when: "An equality expression is written for Bool literals."
            input = '''
                        cout << 'True == False = ' << (True == False) << endl
                        cout << 'True == True = ' << (True == True) << endl
                        cout << 'True != True = ' << (True != True) << endl
                        cout << 'False != True = ' << (False != True) << endl
                    '''
            vm.runInterpreter(input)

        then: "Each equality expression should be correctly evaluated."
            os.toString().contains(
                "True == False = false\n" +
                "True == True = true\n" +
                "True != True = false\n" +
                "False != True = true"
            )
    }

    def "Binary Expression - Equality Operators (Char)"() {
        when: "An equality expression is written for Char literals."
            input = '''
                        cout << ('c' == 'c') << endl
                        cout << ('c' == 'd') << endl
                        cout << ('c' != 'c') << endl
                        cout << ('c' != 'd') << endl
                    '''
            vm.runInterpreter(input)

        then: "Each equality expression should be correctly evaluated."
            os.toString().contains(
                "true\n" +
                "false\n" +
                "false\n" +
                "true"
            )
    }

    def "Binary Expression - Equality Operators (Int)"() {
        when: "An equality expression is written for Int literals."
            input = '''
                        cout << '3 == 4 = ' << (3 == 4) << endl
                        cout << '4 == 4 = ' << (4 == 4) << endl
                        cout << '4 != 4 = ' << (4 != 4) << endl
                        cout << '3 != 4 = ' << (3 != 4) << endl
                    '''
            vm.runInterpreter(input)

        then: "Each equality expression should be correctly evaluated."
            os.toString().contains(
                "3 == 4 = false\n" +
                "4 == 4 = true\n" +
                "4 != 4 = false\n" +
                "3 != 4 = true"
            )
    }

    def "Binary Expression - Equality Operators (Real)"() {
        when: "An equality expression is written for Real literals."
            input = '''
                        cout << '3.14 == 4.14 = ' << (3.14 == 4.14) << endl
                        cout << '4.14 == 4.14 = ' << (4.14 == 4.14) << endl
                        cout << '4.14 != 4.14 = ' << (4.14 != 4.14) << endl
                        cout << '3.14 != 4.14 = ' << (3.14 != 4.14) << endl
                    '''
            vm.runInterpreter(input)

        then: "Each equality expression should be correctly evaluated."
            os.toString().contains(
                "3.14 == 4.14 = false\n" +
                "4.14 == 4.14 = true\n" +
                "4.14 != 4.14 = false\n" +
                "3.14 != 4.14 = true"
            )
    }

    def "Binary Expression - Equality Operators (String)"() {
        when: "An equality expression is written for String literals."
            input = '''
                        cout << ('hello' == 'hello') << endl
                        cout << ('hello' == 'world') << endl
                        cout << ('hello' != 'hello') << endl
                        cout << ('hello' != 'world') << endl
                    '''
            vm.runInterpreter(input)

        then: "Each equality expression should be correctly evaluated."
            os.toString().contains(
                "true\n" +
                "false\n" +
                "false\n" +
                "true"
            )
    }

    def "Binary Expression - Logical Operators"() {
        when: "A logical expression is written for Bool literals."
            input = '''
                        cout << 'True and False = ' << (True and False) << endl
                        cout << 'True and True = ' << (True and True) << endl
                        cout << 'True or False = ' << (True or False) << endl
                        cout << 'False or False = ' << (False or False) << endl
                    '''
            vm.runInterpreter(input)

        then: "The expression should be correctly evaluated."
            os.toString().contains(
                "True and False = false\n" +
                "True and True = true\n" +
                "True or False = true\n" +
                "False or False = false"
            )
    }

    def "Binary Expression - Relational Operators (Int)"() {
        when: "A relational expression is used for two Int literals."
            input = '''
                        cout << '3 < 4 = ' << (3<4) << endl
                        cout << '3 <= 3 = ' << (3 <= 3) << endl
                        cout << '3 > 4 = ' << (3 > 4) << endl
                        cout << '4 >= 4 = ' << (4 >= 4) << endl
                    '''
            vm.runInterpreter(input)

        then: "The expression will be evaluated, and the correct result is printed."
            os.toString().contains(
                "3 < 4 = true\n" +
                "3 <= 3 = true\n" +
                "3 > 4 = false\n" +
                "4 >= 4 = true"
            )
    }

    def "Binary Expression - Relational Operators (Real)"() {
        when: "A relational expression is used for two Real literals."
            input = '''
                        cout << '3.14 < 4.28 = ' << (3.14<4.28) << endl
                        cout << '3.14 <= 3.14 = ' << (3.14 <= 3.14) << endl
                        cout << '3.14 > 4.28 = ' << (3.14 > 4.28) << endl
                        cout << '4.84 >= 4.28 = ' << (4.84 >= 4.28) << endl
                    '''
            vm.runInterpreter(input)

        then: "The expression will be evaluated, and the correct result is printed."
            os.toString().contains(
                "3.14 < 4.28 = true\n" +
                "3.14 <= 3.14 = true\n" +
                "3.14 > 4.28 = false\n" +
                "4.84 >= 4.28 = true"
            )
    }

    def "Binary Expression - String Concatenation"() {
        when: "Two Strings are used with the plus operator."
            input = '''
                        cout << 'Hello' + ' World' << endl
                    '''
            vm.runInterpreter(input)

        then: "The strings should be concatenated into a single String."
            os.toString().contains("Hello World")
    }

    def "Break Statement - Terminate Loop Early"() {
        when: "A break statement is found inside a loop."
            input = '''
                        def a:Bool = True
                        def b:Int = 1
                        
                        while(a) {
                            if(b==5) {
                                cout << 'Break found!' 
                                break 
                            }
                            set b += 1
                        }
                    '''
            vm.runInterpreter(input)

        then: "The break is executed, and the loop will terminate."
            os.toString().contains("Break found!")
    }

    def "Continue Statement - Reset Loop Early"() {
        when: "A continue statement is found inside a loop."
            input = '''
                        def a:Bool = True
                        def b:Int = 1
                        
                        while(a) {
                            cout << b << ' ' 
                            if(b==5) {
                                cout << 'Inside continue '
                                set b += 3
                                continue 
                            } else if(b == 8) {
                                break
                            }
                            
                            set b += 1
                        }
                    '''
            vm.runInterpreter(input)

        then: "The continue is executed, and the loop will reset sooner.."
            os.toString().contains("1 2 3 4 5 Inside continue 8")
    }

    def "Do Statement - Execute Do While Loop"() {
        when: "A do while loop is executed."
            input = '''
                        def a:Int = 1
                        
                        do {
                            cout << a << ' '
                            set a += 1
                        } while(a != 10)
                    '''
            vm.runInterpreter(input)

        then: "The do while loop should eventually terminate and print the correct values."
            os.toString().contains("1 2 3 4 5 6 7 8 9")
    }

    def "Do Statement - Nested While Loop"() {
        when: "A nested while loop is executed in a do while loop."
            input = '''
                        def a:Bool = True 
                        def b:Int = 1
                        
                        do {
                            cout << b << ' ' 
                            while(b%3 != 0) {
                                set b += 1
                            }
                        
                            if(b >= 10) {
                                break
                            }
                        
                            set b+= 1
                        } while(a)
                    '''
            vm.runInterpreter(input)

        then: "The do while loop should eventually terminate and print the correct values."
            os.toString().contains("1 4 7 10")
    }

    def "Global Declaration - Accessing Bool Variable"() {
        when: "A Bool global variable is declared."
            input = '''
                        def global a:Bool = False
                        cout << 'a = ' << a << endl
                    '''
            vm.runInterpreter(input)

        then: "The value can be accessed from the runtime stack."
            os.toString().contains("a = false")
    }

    def "Global Declaration - Accessing Char Variable"() {
        when: "A Char global variable is declared."
            input = '''
                        def global a:Char = 'c'
                        cout << 'a = ' << a << endl
                    '''
            vm.runInterpreter(input)

        then: "The value can be accessed from the runtime stack."
            os.toString().contains("a = c")
    }

    def "Global Declaration - Accessing Int Variable"() {
        when: "An Int global variable is declared."
            input = '''
                        def global a:Int = 4
                        cout << 'a = ' << a << endl
                    '''
            vm.runInterpreter(input)

        then: "The value can be accessed from the runtime stack."
            os.toString().contains("a = 4")
    }

    def "Global Declaration - Accessing Real Variable"() {
        when: "A Real global variable is declared."
            input = '''
                        def global a:Real = 3.14
                        cout << 'a = ' << a << endl
                    '''
            vm.runInterpreter(input)

        then: "The value can be accessed from the runtime stack."
            os.toString().contains("a = 3.14")
    }

    def "Global Declaration - Accessing String Variable"() {
        when: "A String global variable is declared."
            input = '''
                        def global a:String = 'hello world'
                        cout << 'a = ' << a << endl
                    '''
            vm.runInterpreter(input)

        then: "The value can be accessed from the runtime stack."
            os.toString().contains("a = hello world")
    }

    def "If Statement - Execute Elif Branch"() {
        when: "The conditional expression evaluates to be true for some elif branch."
            input = '''
                        def a:Int = 2
                        
                        if(a == 0) { cout << 'a == 0' << endl } 
                        else if(a == 1) { cout << 'a == 1' << endl } 
                        else if(a == 2) { cout << 'a == 2' << endl }
                        else if(a == 3) { cout << 'a == 3' << endl } 
                        else { cout << 'a < 0 or a >= 4' << endl }
                    '''
            vm.runInterpreter(input)

        then: "The appropriate elif branch should be executed."
            os.toString().contains("a == 2")
    }

    def "If Statement - Execute Else Branch"() {
        when: "The conditional expression evaluates to be false for an if branch."
            input = '''
                        def a:Int = 5
    
                        if(a < 5) { cout << 'This is correct.' << endl }
                        else { cout << 'This is incorrect.' << endl }
                    '''
            vm.runInterpreter(input)

        then: "The else branch should be executed."
            os.toString().contains("This is incorrect.")
    }

    def "If Statement - Execute If Branch"() {
        when: "The conditional expression evaluates to be true for an if branch."
            input = '''
                        def a:Int = 5
    
                        if(a >= 5) { cout << 'This is correct.' << endl }
                    '''
            vm.runInterpreter(input)

        then: "The if branch should be executed."
            os.toString().contains("This is correct.")
    }

    def "Invocation - Chain Method Invocation"() {
        when: "An object invokes multiple methods in a row."
            input = '''
                       class A {
                            
                           protected method last() => Void {
                               cout << 'last()' << endl
                           }
                            
                           protected method after(in a:Real) => Real {
                               cout << 'after() = ' << a << endl 
                               last()
                               return 6.78
                           }
                            
                           protected method next(in a:Int) => Void {
                               cout << 'next() = ' << a << endl 
                               cout << 'after after() = ' << after(3.14) << endl 
                           }
                            
                           public method start() => Void {
                               cout << 'start()' << endl
                               next(3)
                               cout << 'end of call sequence' << endl
                           }
                       }
        
                       def a:A = new A()
                       a.start()     
                    '''
            vm.runInterpreter(input)

        then: "All methods should be executed."
            os.toString().contains(
                "start()\n" +
                "next() = 3\n" +
                "after after() = after() = 3.14\n" +
                "last()\n" +
                "6.78\n" +
                "end of call sequence"
            )
    }

    def "Invocation - Function Invocation"() {
        when: "A function is declared and an invocation is made to it."
            input = '''
                        def func() => Void {
                            cout << 'Inside call to func()' << endl
                        }
                        
                        func()
                    '''
            vm.runInterpreter(input)

        then: "The function should be executed."
            os.toString().contains("Inside call to func()")
    }

    def "Invocation - Function Invocation with Parameters"() {
        when: "A function is invoked with arguments."
            input = '''
                        def func(in a:Int) => Void {
                            set a += 5 
                            cout << 'In func(), a = ' << a << endl
                        }
                        
                        def a:Int = 3
                        func(a)
                        cout << 'Outside func(), a = ' << a << endl
                    '''
            vm.runInterpreter(input)

        then: "The arguments should be correctly passed to the function."
            os.toString().contains(
                "In func(), a = 8\n" +
                "Outside func(), a = 3"
            )
    }

    def "Invocation - Method Invocation"() {
        when: "A method is invoked through an object."
            input = '''
                        class A {
                            protected x:Int 
                        
                            public method print() => Void {
                                cout << 'this.x = ' << x 
                            }
                        }
                        
                        def a:A = new A(x=5)
                        a.print()
                    '''
            vm.runInterpreter(input)

        then: "The correct value should be printed based on the object's state."
            os.toString().contains("this.x = 5")
    }

    def "Invocation - Method Invocation 2"() {
        when: "A method invocation is made inside a method."
            input = ''' 
                        class A {
                            public x:Int

                            protected method print2() => Void {
                                cout << 'In print2()' << endl
                            }

                            public method print() => Void {
                                cout << 'In print()' << endl
                                print2()
                            }
                        }

                        def a:A = new A()
                        a.print()
                    '''
            vm.runInterpreter(input)

        then: "The object should correctly be able to call both methods."
            os.toString().contains(
                "In print()\n" +
                "In print2()"
            )
    }

    def "Local Declaration - Accessing Bool Variable"() {
        when: "A Bool local variable is declared."
            input = '''
                        def a:Bool = False
                        cout << 'a = ' << a << endl
                    '''
            vm.runInterpreter(input)

        then: "The value can be accessed from the runtime stack."
            os.toString().contains("a = false")
    }

    def "Local Declaration - Accessing Char Variable"() {
        when: "A Char local variable is declared."
            input = '''
                        def a:Char = 'c'
                        cout << 'a = ' << a << endl
                    '''
            vm.runInterpreter(input)

        then: "The value can be accessed from the runtime stack."
            os.toString().contains("a = c")
    }

    def "Local Declaration - Accessing Int Variable"() {
        when: "An Int local variable is declared."
            input = '''
                        def a:Int = 4
                        cout << 'a = ' << a << endl
                    '''
            vm.runInterpreter(input)

        then: "The value can be accessed from the runtime stack."
            os.toString().contains("a = 4")
    }

    def "Local Declaration - Accessing Real Variable"() {
        when: "A Real local variable is declared."
            input = '''
                        def a:Real = 3.14
                        cout << 'a = ' << a << endl
                    '''
            vm.runInterpreter(input)

        then: "The value can be accessed from the runtime stack."
            os.toString().contains("a = 3.14")
    }

    def "Local Declaration - Accessing String Variable"() {
        when: "A String local variable is declared."
            input = '''
                        def a:String = 'hello world'
                        cout << 'a = ' << a << endl
                    '''
            vm.runInterpreter(input)

        then: "The value can be accessed from the runtime stack."
            os.toString().contains("a = hello world")
    }

    def "New Expression - Instantiate an Object"() {
        when: "An object is instantiated with a single field."
            input = '''
                        class A {
                            public x:Int 
                        }
                    
                        def a:A = new A(x=3)
                        cout << 'a.x = ' << a.x << endl
                    '''
            vm.runInterpreter(input)

        then: "The object should be correctly instantiated."
            os.toString().contains("a.x = 3")
    }

    def "New Expression - Instantiate an Object 2"() {
        when: "An object is instantiated with all class fields."
            input = '''
                        class A {
                            public a:Int
                            public b:Real 
                            public c:String 
                            public d:Bool 
                        }
                        
                        def a:A = new A(d=True, b=3.14, c='hello', a=1)
                        
                        cout << 'a.a = ' << a.a << endl
                        cout << 'a.b = ' << a.b << endl
                        cout << 'a.c = ' << a.c << endl
                        cout << 'a.d = ' << a.d << endl
                    '''
            vm.runInterpreter(input)

        then: "The object should be correctly instantiated with the right values."
            os.toString().contains(
                "a.a = 1\n" +
                "a.b = 3.14\n" +
                "a.c = hello\n" +
                "a.d = true"
            )
    }

    def "New Expression - Instantiate a Subtype"() {
        when: "An object is instantiated from a subtype."
            input = '''
                        class A { public x:Int }
                        class B inherits A { public y:String }
                        
                        def b:B = new B(x=5,y='hi')
                        cout << 'b.x = ' << b.x << endl
                        cout << 'b.y = ' << b.y << endl
                    '''
            vm.runInterpreter(input)

        then: "The object's fields should both be initialized correctly."
            os.toString().contains(
                "b.x = 5\n" +
                "b.y = hi"
            )
    }

    def "Return Statement - Function Returns Early"() {
        when: "A function has an explicit return statement."
            input = '''
                        def func(out a:Int) => Void {
                            if(a==2) {
                                return 
                            }
                            set a *= 2
                            return 
                        }
                        
                        def a:Int = 8
                        def b:Int = 2
                        func(a)
                        func(b)
                        
                        cout << 'a = ' << a << ', b = ' << b << endl
                    '''
            vm.runInterpreter(input)

        then: "The function should terminate early."
            os.toString().contains("a = 16, b = 2")
    }

    def "Return Statement - Function Returns Value"() {
        when: "A function has a return statement that returns a value."
            input = '''
                        def func() => Int { return 42 }
                        cout << func() << endl
                    '''
            vm.runInterpreter(input)

        then: "The value should be correctly returned to the caller."
            os.toString().contains("42")
    }

    def "Unary Expression - Bitwise Negation (Char)"() {
        when: "A unary bitwise negation is performed on a Char literal."
            input = '''
                        cout << ~'a' << endl 
                        cout << ~'A' << endl 
                    '''
            vm.runInterpreter(input)

        then: "The correct result should be printed."
            os.toString().contains(
                    "-98\n" +
                    "-66"
            )
    }

    def "Unary Expression - Bitwise Negation (Int)"() {
        when: "A unary bitwise negation is performed on an Int literal."
            input = '''
                        cout << '~1 = ' << ~1 << endl
                        cout << '~-3 = ' << ~-3 << endl
                        cout << '~30 = ' << ~30 << endl
                    '''
            vm.runInterpreter(input)

        then: "The correct result should be printed."
            os.toString().contains(
                "~1 = -2\n" +
                "~-3 = 2\n" +
                "~30 = -31"
            )
    }

    def "Unary Expression - Negation"() {
        when: "A negation is performed on a Bool literal."
            input = '''
                        cout << 'not True = ' << not True << endl
                        cout << 'not False = ' << not False << endl
                    '''
            vm.runInterpreter(input)

        then: "The correct result should be printed."
            os.toString().contains(
                "not True = false\n" +
                "not False = true"
            )
    }

    def "While Statement - Execute While Loop"() {
        when: "A while loop is executed."
            input = '''
                        def a:Int = 1
    
                        while(a != 10) {
                            cout << a << ' ' 
                            set a += 1
                        }
                    '''
            vm.runInterpreter(input)

        then: "The while loop should eventually terminate and print the correct values."
            os.toString().contains("1 2 3 4 5 6 7 8 9")
    }

    def "While Statement - Nested While Loop"() {
        when: "A nested while loop is executed."
            input = '''
                       def a:Int = 1, b:Int = 1
                    
                       while(a != 5) {
                           cout << 'Iteration #' << a << ':' << endl
                        
                           while(b != 5) {
                               cout << b << ' ' 
                               set b += 1
                           }
                           cout << endl 
                    
                           set b = 1
                           set a += 1
                      }
                    '''
            vm.runInterpreter(input)

        then: "Each loop iteration should be printed."
            os.toString().contains(
                    "Iteration #1:\n" +
                    "1 2 3 4 \n" +
                    "Iteration #2:\n" +
                    "1 2 3 4 \n" +
                    "Iteration #3:\n" +
                    "1 2 3 4 \n" +
                    "Iteration #4:\n" +
                    "1 2 3 4 "
            )
    }
}
