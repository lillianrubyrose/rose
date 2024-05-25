package cc.sapphiretech.rose.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import dev.turingcomplete.textcaseconverter.StandardTextCases
import java.io.OutputStream

operator fun OutputStream.plusAssign(str: String) {
    this.write(str.toByteArray())
}

class Processor(@Suppress("unused") private val logger: KSPLogger, private val gen: CodeGenerator) : SymbolProcessor {
    private fun processGenericEnumError(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(GenericEnumError::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.ENUM_CLASS }
            .toCollection(mutableListOf())
        if (symbols.isEmpty()) {
            return emptyList()
        }

        val file =
            gen.createNewFile(
                Dependencies(false, *resolver.getAllFiles().toList().toTypedArray()),
                "cc.sapphiretech.rose.generated",
                "EnumErrorExt"
            )
        file += "package cc.sapphiretech.rose.generated\n"

        for (symbol in symbols) {
            file += "fun ${symbol.qualifiedName?.asString()}.toWebError(): cc.sapphiretech.rose.models.BasicWebResponse {\n"
            file += "\treturn when(this) {\n"

            for (variant in symbol.declarations.filterIsInstance<KSClassDeclaration>()
                .filter { it.classKind == ClassKind.ENUM_ENTRY }) {
                file += "\t\t${variant.qualifiedName?.asString()} -> cc.sapphiretech.rose.models.BasicWebResponse(\"${
                    StandardTextCases.PASCAL_CASE.convertTo(
                        StandardTextCases.SCREAMING_SNAKE_CASE,
                        variant.simpleName.asString()
                    )
                }\")\n"
            }

            file += "\t}\n}\n"
        }
        file.close()

        val unableToProcess = symbols.filterNot { it.validate() }.toList()
        return unableToProcess
    }

    override fun process(resolver: Resolver): List<KSAnnotated> = mutableListOf<KSAnnotated>().apply {
        addAll(processGenericEnumError(resolver))
    }
}