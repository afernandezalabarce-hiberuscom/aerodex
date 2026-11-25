package dev.afalabarce.aircrafts.core.common.platform.entities

import dev.afalabarce.aircrafts.core.common.platform.enums.PlatformType

data class PlatformData(
    val platformType: PlatformType,
    val osVersion: String
) {
    override fun toString(): String = "${platformType.name} v. $osVersion"
}
