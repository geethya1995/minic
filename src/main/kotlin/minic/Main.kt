package minic

import org.apache.commons.io.FilenameUtils
import java.io.File
import java.io.FileInputStream

val compilerName = "minic"

fun showUsage() {
    println(
"""Usage:
$compilerName input_file [output_file] [--tokens]
    Compiles specified source code into file with JVM bytecode.
    input_file
        Path (or name) of file with Mini-C source code.
    output_file
        Optional. Path (or name) of output file with JVM bytecode.
        If not specified, input_file without extension will be used.
        .class extension is appended if not present (otherwise java will
         not run it), such as MyProgram.class.
    Use java to run it (java MyProgram).
    --tokens
        Outputs lexer tokens.
$compilerName --help (or -help, help, -h, --h)
    Prints this information.
If launched without arguments, reads input from stdin
until EOF (Ctrl+D, or Ctrl+Z for Windows), compiles and runs the program.
Examples:
    $compilerName MyProgram.mc
    $compilerName MyProgram.mc MyProgram
    $compilerName MyProgram.mc --tokens""")
}

fun main(args: Array<String>) {
    println("Mini-C compiler")

    var inputStream = System.`in`
    var outputFilePath: String? = null
    var executionMode = true

    val outputTokens = args.contains("--tokens")

    val drawAst = args.contains("--draw-ast")

    if (args.count() > 0) {
        if (args.any { listOf("help", "--help", "-help", "-h", "--h").contains(it) }) {
            showUsage()
            return
        }

        val pathsArgs = args.filter { !it.startsWith("--") }

        if (pathsArgs.count() > 2) {
            println("Incorrect arguments")
            showUsage()
            return
        }

        executionMode = false

        val inputFilePath = pathsArgs[0]

        outputFilePath = if (pathsArgs.count() > 1) pathsArgs[1] else FilenameUtils.removeExtension(inputFilePath)
        if (!outputFilePath!!.endsWith(".class"))
            outputFilePath += ".class"

        println("Input file: $inputFilePath")
        println("Output file: $outputFilePath")

        if (!File(inputFilePath).isFile) {
            println("File not found.")
            return
        }

        inputStream = FileInputStream(inputFilePath)
    } else {
        println("Enter code and press Ctrl+D (Ctrl+Z for Windows)")
        println()
    }

    val compiler = Compiler(inputStream)

    val errors = compiler.validate()
    if (errors.any()) {
        println("${errors.count()} error${if (errors.count() > 1) "s" else ""}.")
        errors.forEach {
            println("Line ${it.position.line}:${it.position.column}: ${it.message}")
        }
        return
    }

    try {
        if (executionMode) {
            compiler.execute()
        } else {
            compiler.compile(outputFilePath!!)
        }
    } catch (ex: Exception) {
        println("Code generation error")
        println(ex.message)
        ex.printStackTrace()
        return
    }

    if (outputTokens) {
        println("Tokens:")
        compiler.tokens.forEach {
            println("${it.name}: ${it.text}")
        }
    }

    if (drawAst) {
        compiler.drawAst("ast.png")
    }
}
