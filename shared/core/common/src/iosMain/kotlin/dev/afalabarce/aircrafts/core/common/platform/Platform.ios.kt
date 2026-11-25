@file:Suppress("MatchingDeclarationName")

package dev.afalabarce.aircrafts.core.common.platform

import dev.afalabarce.aircrafts.core.common.platform.entities.PlatformData
import dev.afalabarce.aircrafts.core.common.platform.enums.PlatformType
import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override val platformData: PlatformData
        get() = PlatformData(
            platformType = PlatformType.IOS,
            osVersion = UIDevice.currentDevice.systemVersion
        )
}

actual fun getPlatform(): Platform = IOSPlatform()
