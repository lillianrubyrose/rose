package cc.sapphiretech.rose.ext

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> transaction(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }
