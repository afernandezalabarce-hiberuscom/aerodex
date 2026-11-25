package dev.afalabarce.aircrafts.presentation.ui.common

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
actual annotation class CommonRunWith(actual val value: KClass<out CommonRunner>)

actual abstract class CommonRunner

actual class PlatformTestRunner : CommonRunner()
