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

    def "Array Expression - 1D Array"() {
        when: "A 1D array is declared with initial values."
            input = '''
                        def a:Array[Int] = Array(1,2,3,4,5)

                        cout << a[2] << endl
                        cout << a[4] << endl
                        cout << a[1] << endl
                    '''
            vm.runInterpreter(input)

        then: "Each value should be stored in the correct position in the array."
            os.toString().contains(
                "2\n" +
                "4\n" +
                "1"
            )
    }

    def "Array Expression - 2D Array"() {
        when: "A 2D array is declared with initial values."
            input = '''
                        def a:Array[Array[Int]] = Array[2][3](Array(1,2,3),Array(4,5,6))

                        cout << a[1][3] << endl
                        cout << a[2][2] << endl
                        cout << a[1][1] << endl
                        cout << a[2][1] << endl
                    '''
            vm.runInterpreter(input)

        then: "Each value should be stored in the correct position in the array."
            os.toString().contains(
                "3\n" +
                "5\n" +
                "1\n" +
                "4"
            )
    }

    def "Array Expression - 3D Array"() {
        when: "A 3D array is declared with initial values."
            input = '''
                        def a:Array[Array[Array[Int]]] = Array[2][3][4](
                                                         Array(Array(1,2,3,4),Array(5,6,7,8),Array(9,10,11,12)), 
                                                         Array(Array(13,14,15,16),Array(17,18,19,20),Array(21,22,23,24))
                                                         )
                        cout << a[1][3][3] << endl
                        cout << a[2][2][2] << endl
                        cout << a[2][3][4] << endl
                        cout << a[1][2][3] << endl
                    '''
            vm.runInterpreter(input)

        then: "Each value should be stored in the correct position in the array."
            os.toString().contains(
                "11\n" +
                "18\n" +
                "24\n" +
                "7"
            )
    }

    def "Assignment Statement - Assign to 1D Array"() {
        when: "An element in an array is assigned a new value."
            input = '''
                        def a:Array[Int] = Array(1,2,3,4,5)
                
                        set a[3] = 9
                        cout << a[3] << endl
                        
                        set a[1] = 12
                        cout << a[1] << endl
                    '''
            vm.runInterpreter(input)

        then: "The array should be correctly updated with the new values assigned."
            os.toString().contains(
                "9\n" +
                "12"
            )
    }

    def "Assignment Statement - Assign to 2D Array"() {
        when: "An element in a 2D array is assigned a new value."
            input = '''
                        def a:Array[Array[Int]] = Array[2][2](Array(1,2),Array(3,4))
                        
                        set a[1][2] = 10
                        cout << a[1][2] << endl
                        
                        set a[2][1] = 35
                        cout << a[2][1] << endl
                    '''
            vm.runInterpreter(input)

        then: "The 2D array should be correctly updated with the new values assigned."
            os.toString().contains(
                "10\n" +
                "35"
            )
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

    def "Assignment Statement - Field Expressions"() {
        when: "An assignment is made on an object's field."
            input = '''
                        class A { public x:Int }
                        class B { public a:A }
                        class C { public b:B }
                        
                        def c:C = new C(b=new B(a=new A(x=5)))
                        
                        set c.b.a.x = 10
                        cout << c.b.a.x << endl
                        
                        set c.b.a.x += 25
                        cout << c.b.a.x << endl 
                    '''
            vm.runInterpreter(input)

        then: "The field should be properly updated with the appropriate value."
            os.toString().contains(
                "10\n" +
                "35"
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

    def "Do Statement - Multiple Break Statements"() {
        when: "A do while loop has multiple break keywords written."
            input = '''
                        def a:Int = 0
                        do {
                            while(a != 10) {
                                set a += 1
                                if(a%5 == 0){ break }
                            }
                        
                            cout << 'The value of a is ' << a << endl
                            if(a == 10) { break }
                        
                        } while(True)
                    '''
            vm.runInterpreter(input)

        then: "Each break statement should correctly terminate the appropriate loop."
            os.toString().contains(
                "The value of a is 5\n" +
                "The value of a is 10"
            )
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

    def "Field Expression - Accessing Array of Objects"() {
        when: "An object is instantiated with an array of objects."
            input = '''
                        class A {
                            protected x:Int 
                            public method print() => Void {
                                cout << 'x = ' << x << endl 
                            }
                        }
                    
                        class B { public arr:Array[A]  }
                    
                        def b:B = new B(arr=Array(new A(x=1),new A(x=2)))
                        b.arr[2].print()
                    '''
            vm.runInterpreter(input)

        then: "An object in the array should be able to be accessed."
            os.toString().contains("x = 2")
    }

    def "Field Expression - Evaluate Complex Field Expression"() {
        when: "A complex field expression is used."
            input = '''
                        class A {
                            public method create() => A { return new A() }
                            public method print() => Void { cout << 'Hi there' << endl }
                        }
                        
                        def a:A = new A()
                        a.create().print() 
                    '''
            vm.runInterpreter(input)

        then: "The returned object should correctly call the appropriate method."
            os.toString().contains("Hi there")

    }

    def "Field Expression - Evaluate Complex Field Expression 2"() {
        when: "A complex field expression is used."
            input = '''
                        class B {
                            protected x:Int
                            public method create() => B { 
                                return new B(x=5) 
                            }
                            public method print() => Void { 
                                cout << 'x = ' << x << endl 
                            }
                        }
                        
                        def b:B = new B(x=3)        
                        b.print()
                        b.create().print()
                    '''
            vm.runInterpreter(input)

        then: "The returned object should correctly call the appropriate method."
            os.toString().contains(
                "x = 3\n" +
                "x = 5"
            )
    }

    def "Field Expression - Evaluate Complex Field Expression 3"() {
        when: "A complex field expression is used."
            input = '''
                        class E {
                            public x:Int
                            public method create() => E { return new E(x=20) }
                        }
                        
                        def e:E = new E(x=5)
                        cout << 'x = ' << e.x << endl
                        cout << 'x = ' << e.create().x << endl
                    '''
            vm.runInterpreter(input)

        then: "The returned object should correctly call the appropriate method."
            os.toString().contains(
                "x = 5\n" +
                "x = 20"
            )
    }

    def "Field Expression - Evaluate Complex Field Expression 4"() {
        when: "A complex field expression is used."
            input = '''
                        class D {
                            public a:Array[Int]
                        
                            public method create() => D { return new D(a=Array(4,5,6)) }
                            public method print() => Void { 
                                cout << 'a = '
                                for(def i:Int in 1..3) {
                                    cout << a[i] << ' '
                                }
                            }
                        }
                        
                        def d:D = new D(a=Array(1,2,3))
                        d.create().print()
                    '''
            vm.runInterpreter(input)

        then: "The returned object should correctly call the appropriate method."
            os.toString().contains("a = 4 5 6")
    }

    //TODO: Output CORRECT, but formatting is not correct????? Dark magic.
/*
    def "Field Expression - Evaluate Complex Field Expression 5"() {
        when: "A complex field expression is used."
            input = '''
                        class C {
                            protected x:Int
                            protected a:Array[Int]
                        
                            public method create() => C { return new C(x=7, a=Array(4,5,6)) }
                            public method print() => Void { 
                                cout << 'x = ' << x << ', a = ' 
                                for(def i:Int in 1..3) {
                                    cout << a[i] << ' '
                                }
                                cout << '\n'
                            }
                        }
                        
                        def c:C = new C(x=2, a=Array(1,2,3))
                        c.print()
                        c.create().print()
                    '''
            vm.runInterpreter(input)

        then: "The returned object should correctly call the appropriate method."
            os.toString().contains(
                "x = 2, a = 1 2 3\n" +
                "x = 7, a = 4 5 6"
            )
    }
*/

    def "Field Expression - Evaluate Complex Field Expression 6"() {
        when: "A complex field expression is used."
            input = '''
                        class A { public x:Int }
                        class B { public a:A }
                        class C { public b:B }
                        
                        def c:C = new C(b=new B(a=new A(x=5)))
                        
                        cout << c.b.a.x
                    '''
            vm.runInterpreter(input)

        then: "The returned object should correctly access the appropriate field."
            os.toString().contains("5")
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

    def "Invocation - In Mode Passing"() {
        when: "An invocation passes argument to a function with In parameters."
            input = '''    
                        def func1(in a:Int, in b:Real) => Void {
                            set a += 5
                            set b += 2.18
                        }
                        
                        def val:Int = 3  
                        def val2:Real = 3.14 
                        
                        cout << 'Before func1 call: val = ' << val << ', val2 = ' << val2 << endl                
                        func1(val,val2)                   
                        cout << 'After func1 call: val = ' << val << ', val2 = ' << val2 << endl          
                    '''
            vm.runInterpreter(input)

        then: "The arguments should not be affected by the execution of the function."
            os.toString().contains(
                "Before func1 call: val = 3, val2 = 3.14\n" +
                "After func1 call: val = 3, val2 = 3.14"
            )
    }

    def "Invocation - Out Mode Passing"() {
        when: "An invocation passes argument to a function with Out parameters."
            input = '''    
                        def func1(out a:Int, out b:Real) => Void {
                            set a += 5
                            set b += 2.18
                        }
                        
                        def val:Int = 3  
                        def val2:Real = 3.14 
                        
                        cout << 'Before func1 call: val = ' << val << ', val2 = ' << val2 << endl                
                        func1(val,val2)                   
                        cout << 'After func1 call: val = ' << val << ', val2 = ' << val2 << endl          
                    '''
            vm.runInterpreter(input)

        then: "The arguments should not be affected by the execution of the function."
            os.toString().contains(
                "Before func1 call: val = 3, val2 = 3.14\n" +
                "After func1 call: val = 8, val2 = 5.32"
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

    def "Invocation - Objects are Returned"() {
        when: "An invocation is made to a method that returns an object."
            input = '''
                        class A { public x:Char  }
                        
                        class B {
                            public y:Real
                            
                            public method createA() => A { return new A(x='c') }
                        }
                        
                        class C {
                            public z:Int
                            
                            public method createA() => A { return new A(x='d') }
                            public method createB() => B { return new B(y=3.14) }
                        }
                        
                        class D { public method createC() => C { return new C(z=4) } }
                        
                        def c:C = new C(z=4)
                        cout << 'c.z = ' << c.z << endl
                        
                        def b:B = c.createB()
                        cout << 'b.y = ' << b.y << endl
                        
                        def a:A = c.createB().createA()
                        cout << 'a.x = ' << a.x << endl
                    '''
            vm.runInterpreter(input)

        then: "The returned object should call the appropriate method."
            os.toString().contains(
                "c.z = 4\n" +
                "b.y = 3.14\n" +
                "a.x = c"
            )
    }

    def "Invocation - Parent Invocation From Child Class"() {
        when: "A method from the parent is invoked from a child class."
            input = '''              
                        class A {
                            protected method hi() => Void { cout << 'in A!' << endl }
                        }
                        
                        class B inherits A {
                            protected method hey() => Void { cout << 'in B!' << endl }
                            public method yo() => Void {
                                hey()
                                hi()
                            }
                        }
                        
                        def b:B = new B()
                        b.yo()  
                    '''
            vm.runInterpreter(input)

        then: "The method should correctly be invoked from the child class."
            os.toString().contains(
                "in B!\n" +
                "in A!"
            )
    }

    def "Invocation - Parent Invocation From Child Class 2"() {
        when: "Methods from inherited parent classes are invoked from a child class."
            input = '''              
                        class A {
                            protected method printA() => Void { cout << 'In A!\n' }
                        }
                        
                        class B inherits A {
                            protected method printB() => Void { cout << 'In B!\n' }
                        }
                        
                        class C inherits B {
                            protected method printC() => Void { cout << 'In C!\n' }
                        }
                        
                        class D inherits C {
                            protected method printD() => Void { cout << 'In D!\n' }
                            public method print() => Void {
                                printD()
                                printC()
                                printB()
                                printA()
                            }
                        }
                        
                        def d:D = new D()
                        d.print()
                    '''
            vm.runInterpreter(input)

        then: "All methods should correctly be invoked by a child object."
            os.toString().contains(
                "In D!\n" +
                "In C!\n" +
                "In B!\n" +
                "In A!"
        )
    }

    def "Invocation - Parent Invocation From Child Class 3"() {
        when: "A method from an inherited parent class is invoked using the parent keyword."
            input = '''              
                        class A {
                            public method print() => Void {
                                cout << 'In A!' << endl 
                            }
                        }
                        
                        class B inherits A {
                            public override method print() => Void {
                                parent.print()
                                cout << 'In B!' << endl 
                            }
                        }
                        
                        def b:B = new B()
                        b.print()
                    '''
            vm.runInterpreter(input)

        then: "The parent method should correctly be invoked from the child class."
            os.toString().contains(
                "In A!\n" +
                "In B!"
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

    def "New Expression - Instantiate a Chain Subtype"() {
        when: "An object is instantiated from multiple subtypes."
            input = '''
                        class A { public x:Int }   
                        class B inherits A { public y:Real }
                        class C inherits B { public z:Bool }
                        class D inherits C { public a:A }
                        
                        def a:D = new D(x=5, y=3.14, z=True, a=new A(x=8))
                        cout << 'a.x = ' << a.x << endl
                        cout << 'a.y = ' << a.y << endl
                        cout << 'a.z = ' << a.z << endl
                        cout << 'a.a.x = ' << a.a.x << endl
                    '''
            vm.runInterpreter(input)

        then: "The object's fields should all be initialized correctly."
            os.toString().contains(
                "a.x = 5\n" +
                "a.y = 3.14\n" +
                "a.z = true\n" +
                "a.a.x = 8"
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

    def "Operator Overloading - Binary Minus Overload"() {
        when: "A binary overload on the minus operator is defined in a class."
            input = '''
                        class A {
                            property x:Int 
                        
                            public operator-(in val:Int) => Int { return x - val }
                            public method print() => Void { cout << x << endl }
                        }
                        
                        def a:A = new A(x=1)
                        def b:Int = uninit 
                        set b = a - 1
                        cout << 'After overload: ' << b << endl
                    '''
            vm.runInterpreter(input)

        then: "The overload should be executed and the correct result returned."
            os.toString().contains("After overload: 0")
    }

    def "Operator Overloading - Binary Plus Overload"() {
        when: "A binary overload on the plus operator is defined in a class."
            input = '''
                        class A {
                            property x:Int 
                        
                            public operator+(in obj:A) => A {
                                def y:Int = x + obj.getx() 
                                return new A(x=y)
                            }
                        
                            public method print() => Void { cout << x << endl }
                        }
                        
                        def a1:A = new A(x=1)
                        def a2:A = new A(x=2)
                        
                        def a3:A = a1 + a2
                        a3.print()
                    '''
            vm.runInterpreter(input)

        then: "The overload should be executed and the correct result returned."
            os.toString().contains("3")
    }

    def "Operator Overloading - Unary Negation Overload"() {
        when: "An overload is made on the negation operator."
            input = '''
                        class A {
                            protected x:Bool
                        
                            public operator not() => Void {
                                set x = not x 
                            }
                        
                            public method print() => Void {
                                cout << 'Value of x = ' << x << endl 
                            }
                        }
                        
                        def a:A = new A(x=False)
                        a.print()
                        not a 
                        a.print()
                    '''
            vm.runInterpreter(input)

        then: "The value should correctly be updated inside the object."
            os.toString().contains(
                "Value of x = false\n" +
                "Value of x = true"
            )
    }

    def "Operator Overloading - Unary Overload on Multitype"() {
        when: "A unary operator overload is executed for a multityped variable."
            input = '''
                        class A {
                            protected x:Bool
                        
                            public operator not() => Void { set x = not x }
                            public method print() => Void { cout << 'Value of x = ' << x << endl }
                        }
                        
                        class B inherits A { }
                        
                        def a:A = uninit
                        if(6 < 5) { retype a = new A(x=False) } 
                        else { retype a = new B(x=True) }
                        
                        a.print()
                        not a 
                        a.print()
                        '''
            vm.runInterpreter(input)

        then: "The interpreter will be able to execute the overload, and the results will be printed."
            os.toString().contains(
                "Value of x = true\n" +
                "Value of x = false"
            )
    }

    def "Operator Overloading - Unary Overload on Subtype"() {
        when: "An overload is made on the negation operator."
            input = '''
                        class A {
                            protected x:Bool

                            public operator not() => Void { set x = not x }
                            public method print() => Void { cout << 'Value of x = ' << x << endl }
                        }

                        class B inherits A { }

                        def b:B = new B(x=False)
                        b.print()
                        not b
                        b.print()
                    '''
            vm.runInterpreter(input)

        then: "The value should correctly be updated inside the object."
            os.toString().contains(
                "Value of x = false\n" +
                "Value of x = true"
            )
    }

    //TODO: Formatting is also off here...
//    def "Output Statement - Correct Output"() {
//        when: "Different output statements are written."
//            input = '''
//                        class A { public x:Bool }
//                        class B { public x:Int }
//
//                        def func() => Void { cout << 'hello there' }
//
//                        def a:A = new A(x=True)
//                        def b:B = new B(x=5)
//                        def c:Array[Int] = Array(1,2,3,4,5)
//
//                        cout << 'hello' << ' is this working?' << endl << 'yes it is' << endl
//
//                        cout << (3+(4*3)-2) << endl
//                        cout << 5*8-3/3*(3+9) << endl\t
//                        cout << 5*8-3+2*(3+4-3) << ' ' << 3+2*8 << endl << 8+3-8
//                        cout << (5+3-1*3) << ' ' << (5-3*2+4) << endl << (5+3-3*8)
//
//                        cout << False or True
//                        cout << False or True << endl
//                        cout << True or False and True or False and True << endl
//                        cout << False or True << endl << True or False
//                        cout << False or True << endl << True or False << endl << True and False << endl
//                        cout << True and True and False << endl << 'hello there\'
//
//                        cout << a.x and True or False
//                        cout << 'a.x = ' << a.x << endl << 'hi there\'
//                        cout << a.x and True or False and a.x << endl << 'hi there\'
//
//                        cout << b.x + 5 * 2 << ' hi there' << endl << b.x - 3 * 2 << endl
//
//                        cout << 'before func call' << endl << func() << endl << 'after func call' << endl
//
//                        cout << c[3] + 4 * 5 << ' ' << (c[1]+4)*5 << endl
//                    '''
//            vm.runInterpreter(input)
//
//        then: "The correct output should be printed for each one."
//            os.toString().contains(
//                "hello is this working?\n" +
//                "yes it is\n" +
//                "13\n" +
//                "28\n" +
//                "45 19\n" +
//                "3\n" +
//                "5 3\n" +
//                "-16\n" +
//                "true\n" +
//                "true\n" +
//                "true\n" +
//                "true\n" +
//                "true\n" +
//                "true\n" +
//                "true\n" +
//                "false\n" +
//                "false\n" +
//                "hello there\n" +
//                "true\n" +
//                "a.x = true\n" +
//                "hi there\n" +
//                "true\n" +
//                "hi there\n" +
//                "15 hi there\n" +
//                "-1\n" +
//                "\n" +
//                "before func call\n" +
//                "hello therehello there\n" +
//                "after func call\n" +
//                "\n" +
//                "23 25"
//            )
//    }

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

    def "Retype Statement - Numerous Control Flow Executions"() {
        when: "A more complex class structure is used with retype statements."
            input = '''
                        class A {
                            protected x:Int 
                            public method print() => Void { 
                                cout << 'x = ' << x << endl 
                            }
                        }
                        
                        class B inherits A { 
                            protected y:Real
                            public override method print() => Void { 
                                parent.print()
                                cout << 'y = ' << y << endl 
                            }
                        }
                        
                        class C inherits A {
                            protected z:Bool 
                            public override method print() => Void { 
                                parent.print()
                                cout << 'z = ' << z << endl 
                                }
                        }
                        
                        class D inherits B {
                            protected n:String
                            public override method print() => Void { 
                                parent.print()
                                cout << 'n = ' << n << endl 
                            }
                        }
                        
                        def a:A = uninit
                        def i:Int = 0
                        
                        while(True) {
                            if(i >= 5) { retype a = new D(x=7,y=3.1415,n='hello') } 
                            else {
                                def b:Int = 5
                                retype a = new C(z=True,x=10)
                                while(b != 10) {
                                    retype a = new B(y=7.28035,x=15)
                                    set b += 1
                                }
                            }
                        
                            if(a instanceof D) { break }
                            set i += 1
                        }
                        
                        a.print()
                    '''
            vm.runInterpreter(input)

        then: "The correct method should be executed based on the expected runtime type."
            os.toString().contains(
                "x = 7\n" +
                "y = 3.1415\n" +
                "n = hello"
            )
    }

    def "Retype Statement - Retype in Control Flow"() {
        when: "A retype statement is found inside an if statement."
            input = '''
                        class A { public method print() => Void { cout << 'in A' } }
                        class B inherits A { public method hi() => Void { cout << 'Hi' } }
                        class C inherits A { public method hey() => Void { cout << 'Hey' } }
                        
                        def a:A = uninit
                        
                        if(3 < 5) { retype a = new B() } 
                        else { retype a = new C() }
                        
                        a.hi()
                    '''
            vm.runInterpreter(input)

        then: "The correct function will be called during runtime, and no runtime exceptions are generated."
            os.toString().contains("Hi")
    }

    def "Retype Statement - Retype in Control Flow 2"() {
        when: "A retype statement is found inside a while statement."
            input = '''
                        class A { public method print() => Void { cout << 'in A' } }
                        class B inherits A { public method hi() => Void { cout << 'Hi' } }
                        class C inherits A { public method hey() => Void { cout << 'Hey' } }
                        class D inherits A { public method yo() => Void { cout << 'Yo' } }

                        def a:A = uninit 
                        def i:Int = 0
                        
                        while(True) {
                            if(i < 5) {
                                retype a = new B()
                            } else {
                                def b:Int = 5
                                retype a = new C()
                                while(b != 10) {
                                    retype a = new D()
                                    set b += 1
                                }
                            }
                        
                            if(a instanceof B) {
                                break 
                            }
                        
                            set i += 1
                        }
                        a.hi()
                    '''
            vm.runInterpreter(input)

        then: "The correct function will be called during runtime, and no runtime exceptions are generated."
            os.toString().contains("Hi")
    }

    def "Retype Statement - Retype in Control Flow 3"() {
        when: "A retype statement is found in an if statement."
            input = '''
                    class A { public x:Int  }    
                    class B inherits A { public y:Real }   
                    class C inherits A { public z:Bool }
                    
                    def a:A = uninit 
                    if(3<5) { retype a = new B(x=5,y=3.14) } 
                    else { retype a = new C(z=True,x=3) }
                    
                    cout << 'a.y = ' << a.y 
                    '''
            vm.runInterpreter(input)

        then: "The correct field should be able to be accessed based on the expected runtime type."
            os.toString().contains("a.y = 3.14")
    }

    def "Retype Statement - Retype in Control Flow 4"() {
        when: "Multiple retype statements are found inside nested control flow structures."
            input = '''     
                        class A { public x:Int  }
                        class B inherits A { public y:Real }
                        class C inherits A { public z:Bool }
                        
                        def a:A = uninit
                        for(def i:Int in 1..10) {
                            if(i%2 == 0) { 
                                retype a = new B(x=3,y=3.14) 
                                cout << 'a.y = ' << a.y
                            } else {
                                retype a = new C(x=4,z=True)
                                cout << 'a.z = ' << a.z
                            }
                        
                            cout << ', a.x = ' << a.x << endl 
                        }
                    '''
            vm.runInterpreter(input)

        then: "The correct fields should be accessed and no runtime exceptions are generated."
            os.toString().contains(
                "a.z = true, a.x = 4\n" +
                "a.y = 3.14, a.x = 3\n" +
                "a.z = true, a.x = 4\n" +
                "a.y = 3.14, a.x = 3\n" +
                "a.z = true, a.x = 4\n" +
                "a.y = 3.14, a.x = 3\n" +
                "a.z = true, a.x = 4\n" +
                "a.y = 3.14, a.x = 3\n" +
                "a.z = true, a.x = 4\n" +
                "a.y = 3.14, a.x = 3"
            )

    }

    def "Retype Statement - Retype in Control Flow 5"() {
        when: "Multiple retype statements are found inside nested control flow structures."
            input = '''     
                        class A { public method print() => Void { cout << 'In A' << endl } }
                        class B inherits A { public override method print() => Void { cout << 'In B' << endl } }
                        class C inherits B { public override method print() => Void { cout << 'In C' << endl } }
                        
                        def a:A = uninit
                        for(def i:Int in 1..10) {
                            if(i%2 == 0) { retype a = new B() } 
                            else { retype a = new C() }
                            a.print()
                        }
                    '''
            vm.runInterpreter(input)

        then: "The correct methods should be called and no runtime exceptions are generated."
            os.toString().contains(
                "In C\n" +
                "In B\n" +
                "In C\n" +
                "In B\n" +
                "In C\n" +
                "In B\n" +
                "In C\n" +
                "In B\n" +
                "In C\n" +
                "In B"
            )
    }

    def "Retype Statement - Retype Object"() {
        when: "An object is retyped from a supertype to a subtype."
            input = '''
                        class A { public x:Int }
                        class B inherits A { public y:Real }
                        
                        def a:A = new A(x=5)
                        retype a = new B(x=3,y=3.14)
                        
                        cout << 'a.x = ' << a.x << endl
                        cout << 'a.y = ' << a.y << endl
                    '''
            vm.runInterpreter(input)

        then: "The correct fields should be accessible based on the new typing."
            os.toString().contains(
                "a.x = 3\n" +
                "a.y = 3.14"
            )
    }

    def "Retype Statement - Retype Object 2"() {
        when: "An object is retyped to its initial type"
            input = '''
                        class A { public x:Int }
                        class B inherits A { public y:Real }
                        class C inherits B { public z:Bool }
                        
                        def a:A = uninit 
                        retype a = new B()
                        retype a = new C()
                        retype a = new A(x=5)
                        
                        cout << 'a.x = ' << a.x << endl
                    '''
            vm.runInterpreter(input)

        then: "The correct fields should be accessible based on the new typing."
            os.toString().contains("a.x = 5")
    }

    def "Retype Statement - Retype Object 3"() {
        when: "A retype statement is executed for the same object."
            input = '''
                        class A { public method print() => Void { cout << 'Hi' << endl } }
                        class B inherits A { public override method print() => Void { cout << 'Hey' << endl } }
                        
                        def a:A = new A()
                        a.print()
                        retype a = new B()
                        a.print()
                        retype a = new A()
                        a.print()
                    '''
            vm.runInterpreter(input)

        then: "The correct method should be called based on the current type of the object."
            os.toString().contains(
                "Hi\n" +
                "Hey\n" +
                "Hi"
            )
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
