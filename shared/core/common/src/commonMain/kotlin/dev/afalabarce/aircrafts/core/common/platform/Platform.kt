package dev.afalabarce.aircrafts.core.common.platform

import dev.afalabarce.aircrafts.core.common.platform.entities.PlatformData

interface Platform {
    val platformData: PlatformData
}

expect fun getPlatform(): Platform
