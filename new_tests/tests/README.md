"If it compiles most programs, then it works for me." - Daniel Levy 2024

Compiler development is hard... What's even harder is trying to figure out
what tests you actually want to write since the adversary is unpredictable...

However, it is time to start making an actual test bed since using a psuedo 
proof by induction in my head that all programs will work is no longer going 
to cut it.

As such, I have created a test bed which will better reveal the capabilities
(or really the faults...) of my compiler. This is largely based on the testing
framework Ben Zofcin devised for the Espresso compiler, so I owe him some credit
for how I chose to design my own test bed.

For C Minor, we will have 2 primary test beds:
    1) grammatical
        - This directory houses all the basic tests responsible for testing language
          features. These tests are not meant to represent large-scale programs. Instead,
          they are designed to test if individual features of the compiler are working.
            - Each test will be formatted as X_Y_Z
                - X = Test Number (000-999)
                - Y = Grammatical Construct (ENUM, FOR_STAT, WHILE_STAT, etc.)
                - Z = Specific Test Feature (FOR_STAT_INIT, WHILE_STAT_NEXT, ENUM_MULTIDECLS, etc.)
        - Within this directory, we will also have subdirectories for each phase of the compiler. 
          Each subdirectory will have a respective good_tests and bad_tests directory to ensure we
          go through a rigourous test process (and to store tests for future use). 
    2) programs
        - This directory focuses on actual C Minor programs that should be compiled and
          will execute some form of code. These will bring together multiple language
          features in the hopes of making sure htis 

