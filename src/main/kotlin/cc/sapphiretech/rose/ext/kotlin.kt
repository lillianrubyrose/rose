package cc.sapphiretech.rose.ext

fun <T : Any> T?.orThrow(throwable: () -> Throwable): T = this ?: throw throwable()
