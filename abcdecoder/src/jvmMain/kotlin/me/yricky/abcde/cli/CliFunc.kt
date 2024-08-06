package me.yricky.abcde.cli

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.yricky.abcde.util.SelectedAbcFile
import me.yricky.abcde.util.SelectedIndexFile
import me.yricky.oh.abcd.cfm.AbcClass
import me.yricky.oh.abcd.isa.asmArgs
import me.yricky.oh.abcd.isa.asmName
import me.yricky.oh.abcd.isa.util.ExternModuleParser
import java.io.File
import java.io.OutputStream
import java.io.PrintStream

class CliFunc(
    val name:String,
    val usage:String,
    val action:(List<String>) -> Unit
)

val dumpClass = Pair(
    "--dump-class",
    CliFunc(
        "Dump class",
        "/path/to/module.abc --out=/path/to/outFile.txt"
    ){ args ->
        val iterator = args.iterator()
        var outFile: OutputStream = System.out
        var inputFile:SelectedAbcFile? = null
        while (iterator.hasNext()){
            val arg = iterator.next()
            if(arg.startsWith("--out=")){
                outFile = File(arg.removePrefix("--out=")).outputStream()
            } else {
                inputFile = SelectedAbcFile(File(arg))
            }
        }
        println("${inputFile?.file?.path}")
        if(inputFile?.valid() == true){
            val ps = PrintStream(outFile)
            inputFile.abcBuf.classes.forEach{ (k,v) ->
                ps.println(v.name)
            }
        }
    }
)

val dumpIndex = Pair(
    "--dump-index",
    CliFunc(
        "Dump index",
        "/path/to/resource.index --out=/path/to/outFile.json"
    ){ args ->
        val iterator = args.iterator()
        var outFile: OutputStream = System.out
        var inputFile:SelectedIndexFile? = null
        while (iterator.hasNext()){
            val arg = iterator.next()
            if(arg.startsWith("--out=")){
                outFile = File(arg.removePrefix("--out=")).outputStream()
            } else {
                inputFile = SelectedIndexFile(File(arg))
            }
        }
        val json = Json {
            prettyPrint = true
        }
        println("${inputFile?.file?.path}")
        val map = mutableMapOf<Int,List<Map<String,String>>>()
        if(inputFile?.valid() == true){
            val ps = PrintStream(outFile)
            inputFile.resBuf.resMap.forEach { (t, u) ->
                map[t] = u.map {
                    mapOf(
                        "type" to it.resType.toString(),
                        "param" to it.limitKey,
                        "name" to it.fileName,
                        "data" to it.data
                    )
                }
            }
            ps.print(json.encodeToString(map))
        }
    }
)

val explore = Pair(
    "--explore",
    CliFunc(
        "Explore New Function Dev",
        "/path/to/xxx.abc"
    ){ args->
        println("Hello my friend, welcome to explore mode.")
        val iterator = args.iterator()
        var inputFile:SelectedAbcFile? = null;
        val operandParser = listOf(ExternModuleParser)

        while(iterator.hasNext()) {
            val arg = iterator.next()
            inputFile = SelectedAbcFile(File(arg))
        }

        println("${inputFile?.file?.path}")
        println("Input File check : ${inputFile?.valid()}")

        if (inputFile?.valid() == true) {
            inputFile?.abcBuf?.classes?.forEach {(k, v) ->
                // println("[${v::class.simpleName}] ${v.name}")
                if (v.name.endsWith("EntryAbility")) {
                    val cls = v as AbcClass;
                    cls.methods.forEach{ method->
                        // println("${method.name}")
                        if (method.name.equals("onWindowStageCreate")) {
                            method.codeItem?.asm?.list?.map {
                                val sb = StringBuilder()
                                sb.append(it.asmName)
                                it.asmArgs(operandParser).forEach {(index, argString) ->
                                    sb.append("  ")
                                    sb.append(argString)
                                }

                                println("${it.codeOffset}  ${sb.toString()}")
                            }
                        }
                    }
                }
            }
        }
    }
)