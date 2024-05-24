package cc.sapphiretech.rose.ext

import org.koin.java.KoinJavaComponent

// This should only be used outside Ktor scopes like in services.
inline fun <reified T : Any> lazyInject() =
    KoinJavaComponent.inject<T>(T::class.java)