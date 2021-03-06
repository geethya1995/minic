package minic.backend.codegen

import minic.Compiler
import minic.CompilerConfiguration
import minic.JavaTestUtils
import minic.ProcessTestUtils
import org.apache.commons.io.FilenameUtils
import org.junit.*
import kotlin.test.*
import org.junit.rules.TemporaryFolder
import java.io.File

class CodeGenerationTest {
    @Rule
    @JvmField
    val tmpFolder = TemporaryFolder()

    private fun run(programPath: String, input: String? = null): String {
        return ProcessTestUtils.run(JavaTestUtils.javaPath, args = FilenameUtils.getBaseName(programPath),
                workingDir = FilenameUtils.getFullPath(programPath), input = input)
    }

    private fun compileAndRun(code: String, input: String? = null): String {
        val outputFilePath = tmpFolder.root.absolutePath + "/Program${System.currentTimeMillis()}_${code.length}.class"

        Compiler(code, CompilerConfiguration(diagnosticChecks = true)).compile(outputFilePath)

        assertTrue(File(outputFilePath).exists(), "$outputFilePath doesn't exist")
        assertTrue(File(outputFilePath).length() > 0, "$outputFilePath is empty")

        val output = run(outputFilePath, input)
        val filteredOutput = output
                .split('\n')
                .filter { !it.startsWith("Picked up _") }
                .joinToString("\n")
                .replace(Regex("Picked up _.+$"), "") // when no println at the end
        return filteredOutput
    }

    @Test
    fun printsStrings() {
        val code = """
println("Hello world!");
print("Hello ");
print("world.");
"""
        val expectedOutput = """
Hello world!
Hello world.
""".trim()
        assertEquals(expectedOutput, compileAndRun(code))
    }

    @Test
    fun printsNumbers() {
        val code = """
println(toString(42));
println(toString(42.5));
"""
        val expectedOutput = """
42
42.5
""".trim()
        assertEquals(expectedOutput, compileAndRun(code))
    }

    @Test
    fun printsBooleans() {
        val code = """
println(toString(true));
println(toString(false));
"""
        val expectedOutput = """
true
false
""".trim()
        assertEquals(expectedOutput, compileAndRun(code))
    }

    @Test
    fun concatenatesStrings() {
        val code = """
println("Hello " + "world!");
println("Hello" + " " + "world.");
"""
        val expectedOutput = """
Hello world!
Hello world.
""".trim()
        assertEquals(expectedOutput, compileAndRun(code))
    }

    @Test
    fun calculatesUnaryMinus() {
        val code = """
println(toString( -42 ));
println(toString( -42.5 ));
println(toString( --42.5 ));
println(toString( -(--42) ));
"""
        val expectedOutput = """
-42
-42.5
42.5
-42
""".trim()
        assertEquals(expectedOutput, compileAndRun(code))
    }

    @Test
    fun calculatesArithmeticExpressions() {
        val code = """
println(toString( 2 + 2 * 2 ));
println(toString( 4.5 * 2 ));
println(toString(  1 + 2 * 3/4.0 - (5 + 6 * 7 * (-8 - 9)) ));
println(toString( 8 % 2 ));
println(toString( 9 % 2 ));
println(toString( 9.5 % 2 ));
int a = 2;
println(toString( 2 + a * 2 ));
double b = 2.0;
println(toString( 2 + b * 2 ));
"""
        val expectedOutput = """
6
9.0
711.5
0
1
1.5
6
6.0
""".trim()
        assertEquals(expectedOutput, compileAndRun(code))
    }

    @Test
    fun calculatesLogicalExpressions() {
        val code = """
bool a = true;
bool b = false;
println(toString( a && a ) + " " + toString( a && b ) + " " + toString( b && a ) + " " + toString( b && b ));
println(toString( a || a ) + " " + toString( a || b ) + " " + toString( b || a ) + " " + toString( b || b ));
println(toString( !a ) + " " + toString( !b ) + " " + toString( !!a ));
println(toString( a && (a || b) ));
println(toString( a && (!a || b) ));
"""
        val expectedOutput = """
true false false false
true true true false
false true true
true
false
""".trim()
        assertEquals(expectedOutput, compileAndRun(code))
    }

    @Test
    fun doesNotUseBitwiseAndOr() {
        val code = """
bool a = true;
int x = 0;
if (x != 0 && 42 / x > 0)
    println("0");
if (a || 42 / x > 0)
    println("1");
if (a && x != 0 && 42 / x > 0)
    println("0");
if (!a || x == 0 || 42 / x > 0)
    println("2");
"""
        val expectedOutput = """
1
2
""".trim()
        assertEquals(expectedOutput, compileAndRun(code))
    }

    @Test
    fun calculatesEqualityExpressions() {
        val code = """
int i1 = 42; int i2 = 43; int i3 = 42;
double f1 = 42.5; double f2 = 42.0; double f3 = 42.5;
bool b1 = true; bool b2 = false; bool b3 = true;
string s1 = "Hello"; string s2 = "not hello"; string s3 = "Hello";
println(toString( i1 == i1 ) + " " + toString( i1 == i2 ) + " " + toString( i2 == i1 ) + " " + toString( i1 == i3 ) + " " + toString( i3 == i1 ));
println(toString( i1 != i1 ) + " " + toString( i1 != i2 ) + " " + toString( i2 != i1 ) + " " + toString( i1 != i3 ) + " " + toString( i3 != i1 ));
println(toString( f1 == f1 ) + " " + toString( f1 == f2 ) + " " + toString( f2 == f1 ) + " " + toString( f1 == f3 ) + " " + toString( f3 == f1 ));
println(toString( f1 != f1 ) + " " + toString( f1 != f2 ) + " " + toString( f2 != f1 ) + " " + toString( f1 != f3 ) + " " + toString( f3 != f1 ));
println(toString( b1 == b1 ) + " " + toString( b1 == b2 ) + " " + toString( b2 == b1 ) + " " + toString( b1 == b3 ) + " " + toString( b3 == b1 ));
println(toString( b1 != b1 ) + " " + toString( b1 != b2 ) + " " + toString( b2 != b1 ) + " " + toString( b1 != b3 ) + " " + toString( b3 != b1 ));
println(toString( s1 == s1 ) + " " + toString( s1 == s2 ) + " " + toString( s2 == s1 ) + " " + toString( s1 == s3 ) + " " + toString( s3 == s1 ));
println(toString( s1 != s1 ) + " " + toString( s1 != s2 ) + " " + toString( s2 != s1 ) + " " + toString( s1 != s3 ) + " " + toString( s3 != s1 ));
println(toString( i1 == f2 ) + " " + toString( f2 == i1 ));
println(toString( i1 != f2 ) + " " + toString( f2 != i1 ));
"""
        val expectedOutput = """
true false false true true
false true true false false
true false false true true
false true true false false
true false false true true
false true true false false
true false false true true
false true true false false
true true
false false
""".trim()
        assertEquals(expectedOutput, compileAndRun(code))
    }

    @Test
    fun calculatesRelationExpressions() {
        val code = """
int i1 = 42; int i2 = 43; int i3 = 42;
double f1 = 42.5; double f2 = 42.6; double f3 = 42.5;
println(toString( i1 < i1 ) + " " + toString( i1 < i2 ) + " " + toString( i2 < i1 ) + " " + toString( i1 < i3 ) + " " + toString( i3 < i1 ));
println(toString( i1 > i1 ) + " " + toString( i1 > i2 ) + " " + toString( i2 > i1 ) + " " + toString( i1 > i3 ) + " " + toString( i3 > i1 ));
println(toString( i1 <= i1 ) + " " + toString( i1 <= i2 ) + " " + toString( i2 <= i1 ) + " " + toString( i1 <= i3 ) + " " + toString( i3 <= i1 ));
println(toString( i1 >= i1 ) + " " + toString( i1 >= i2 ) + " " + toString( i2 >= i1 ) + " " + toString( i1 >= i3 ) + " " + toString( i3 >= i1 ));
println(toString( f1 < f1 ) + " " + toString( f1 < f2 ) + " " + toString( f2 < f1 ) + " " + toString( f1 < f3 ) + " " + toString( f3 < f1 ));
println(toString( f1 > f1 ) + " " + toString( f1 > f2 ) + " " + toString( f2 > f1 ) + " " + toString( f1 > f3 ) + " " + toString( f3 > f1 ));
println(toString( f1 <= f1 ) + " " + toString( f1 <= f2 ) + " " + toString( f2 <= f1 ) + " " + toString( f1 <= f3 ) + " " + toString( f3 <= f1 ));
println(toString( f1 >= f1 ) + " " + toString( f1 >= f2 ) + " " + toString( f2 >= f1 ) + " " + toString( f1 >= f3 ) + " " + toString( f3 >= f1 ));
println(toString( i1 < f2 ) + " " + toString( f2 < i1 ));
println(toString( i1 > f2 ) + " " + toString( f2 > i1 ));
println(toString( i1 <= f2 ) + " " + toString( f2 <= i1 ));
println(toString( i1 >= f2 ) + " " + toString( f2 >= i1 ));
"""
        val expectedOutput = """
false true false false false
false false true false false
true true false true true
true false true true true
false true false false false
false false true false false
true true false true true
true false true true true
true false
false true
true false
false true
""".trim()
        assertEquals(expectedOutput, compileAndRun(code))
    }

    @Test
    fun handlesVariables() {
        val code = """
int a = 0;
if (true)
{
    double b = 1.0;
    int c = 2;
    string d = "hello";
    {
        double e = 4.5;
        int f = 5;

        println(toString(b));
        println(toString(c));
        println(d);
        println(toString(e));
        println(toString(f));
    }
    c = 42;
    int e = a + 6;

    println(toString(e));
    println(toString(c));
}
int b = 7;
double c = 8;
{
    int d = 9;
    println(toString(d));
}
bool e = true;

println(toString(a));
println(toString(b));
println(toString(c));
println(toString(e));
"""
        val expectedOutput = """
1.0
2
hello
4.5
5
6
42
9
0
7
8.0
true
""".trim()
        assertEquals(expectedOutput, compileAndRun(code))
    }

    @Test
    fun reusesVariablesSpace() {
        // if diagnosticChecks is set to true, then CheckClassAdapter will throw exception when
        // exceeded fixed stack/locals size (100)
        // without checks the size is computed automatically

        val code = """
${(271..301).map { "int i$it = $it;"  }.joinToString("\n")}
if (true) {
    ${(1..50).map { "int j$it = $it;"  }.joinToString("\n")}
    ${(1..50).map { "println(toString(j$it));"  }.joinToString("\n")}
}
if (true) {
    ${(51..80).map { "double r$it = $it;"  }.joinToString("\n")}
    ${(51..80).map { "println(toString(r$it));"  }.joinToString("\n")}
}
if (true) {
    ${(111..120).map { "int k$it = $it;"  }.joinToString("\n")}
    if (true) {
        ${(81..110).map { "int u$it = $it;"  }.joinToString("\n")}
        ${(81..110).map { "println(toString(u$it));"  }.joinToString("\n")}
    }
    ${(111..120).map { "println(toString(k$it));"  }.joinToString("\n")}
}
if (true) {
    ${(121..170).map { "int y$it = $it;"  }.joinToString("\n")}
    ${(121..170).map { "println(toString(y$it));"  }.joinToString("\n")}
}
if (true) {
    ${(171..220).map { "int x$it = $it;"  }.joinToString("\n")}
    ${(171..220).map { "println(toString(x$it));"  }.joinToString("\n")}
}
if (true) {
    ${(221..270).map { "int x$it = $it;"  }.joinToString("\n")}
    ${(221..270).map { "println(toString(x$it));"  }.joinToString("\n")}
}
${(271..301).map { "println(toString(i$it));"  }.joinToString("\n")}
""".trim()
        val expectedOutput = """
${(1..50).joinToString("\n")}
${(51..80).map { "$it.0" }.joinToString("\n")}
${(81..301).joinToString("\n")}
""".trim()
        val output = compileAndRun(code)
        assertEquals(expectedOutput, output)
    }

    @Test
    fun ifStatementWorks() {
        val code = """
bool flag = true;
if (flag)
    println("if1");
if (flag) {
    println("if2");
    if (!flag)
        println("if3");
    else {
        println("else1");
    }
} else {
    println("else2");
}
"""
        val expectedOutput = """
if1
if2
else1
""".trim()
        assertEquals(expectedOutput, compileAndRun(code))
    }

    @Test
    fun whileStatementWorks() {
        val code = """
bool flag = true;
int c = 0;
while (c < 3) {
    c = c + 1;
    println(toString(c));
}
while (!flag) {
    println("unreachable");
}
"""
        val expectedOutput = """
1
2
3
""".trim()
        assertEquals(expectedOutput, compileAndRun(code))
    }

    @Test(timeout = 2000)
    fun breakStatementWorks() {
        val code = """
bool flag = true;
int c = 0;
while (true) {
    c = c + 1;
    println(toString(c));
    if (c >= 3)
        break;
}
while (!flag) {
    while (true) {
        flag = false;
        break;
        println("unreachable");
    }
}
"""
        val expectedOutput = """
1
2
3
""".trim()
        assertEquals(expectedOutput, compileAndRun(code))
    }

    @Test
    fun exitStatementWorks() {
        val code = """
println("Hello");
exit();
println("unreachable");
"""
        val expectedOutput = """
Hello
""".trim()
        assertEquals(expectedOutput, compileAndRun(code))
    }

    @Test
    fun allowsEmptyStatements() {
        val code = """
;
;;;
if (true)
    ;
if (false)
    ;
else {
    while (false)
        ;
    println("Hello");
}
"""
        val expectedOutput = """
Hello
""".trim()
        assertEquals(expectedOutput, compileAndRun(code))
    }

    @Test(timeout = 2000)
    fun readsInput() {
        val code = """
print("Enter int: ");
int a = readInt();
print("Enter double: ");
double b = readDouble();
print("Enter line: ");
string c = readLine();
println("");
println("a = " + toString(a));
println("b = " + toString(b));
println("c = " + c);

a = readInt();
c = readLine();
println(toString(a) + "," + c);
"""
        val input = """
42
42.5
Hello world
1 str
""".trimStart()
        val expectedOutput = ("""
Enter int: Enter double: Enter line: """ +
"""
a = 42
b = 42.5
c = Hello world
1, str
""").trim()
        assertEquals(expectedOutput, compileAndRun(code, input = input))
    }
}
