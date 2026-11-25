package dev.afalabarce.aircrafts.presentation.ui.common

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
expect annotation class CommonRunWith(val value: KClass<out CommonRunner>)

expect abstract class CommonRunner

expect class PlatformTestRunner : CommonRunner
