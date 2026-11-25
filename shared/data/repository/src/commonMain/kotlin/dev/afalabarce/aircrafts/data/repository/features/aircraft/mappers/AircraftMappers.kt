package dev.afalabarce.aircrafts.data.repository.features.aircraft.mappers

import dev.afalabarce.aircrafts.domain.models.AircraftCapacity
import dev.afalabarce.aircrafts.domain.models.AircraftCategory
import dev.afalabarce.aircrafts.domain.models.AircraftDimensions
import dev.afalabarce.aircrafts.domain.models.AircraftModelDetail
import dev.afalabarce.aircrafts.domain.models.AircraftModelSummary
import dev.afalabarce.aircrafts.domain.models.AircraftPerformance
import dev.afalabarce.aircrafts.domain.models.AircraftPhoto
import dev.afalabarce.aircrafts.domain.models.AircraftPropulsion
import dev.afalabarce.aircrafts.domain.models.AircraftVariant
import dev.afalabarce.aircrafts.domain.models.AircraftWeights
import dev.afalabarce.aircrafts.models.features.aircraft.local.CachedAircraftSummaryEntity
import dev.afalabarce.aircrafts.models.features.aircraft.local.FavoriteAircraftEntity
import dev.afalabarce.aircrafts.models.features.aircraft.remote.AircraftModelDetailDto
import dev.afalabarce.aircrafts.models.features.aircraft.remote.AircraftModelSummaryDto
import dev.afalabarce.aircrafts.models.features.aircraft.remote.PhotoDto

fun PhotoDto.toDomain(): AircraftPhoto = AircraftPhoto(
    url = url,
    sourceUrl = sourceUrl,
    author = author,
    license = license
)

fun AircraftModelSummaryDto.toDomain(): AircraftModelSummary = AircraftModelSummary(
    icaoTypeCode = icaoTypeCode,
    iataTypeCode = iataTypeCode,
    manufacturer = manufacturer,
    model = model,
    name = name,
    category = AircraftCategory.fromApiValue(category),
    photo = photo?.toDomain(),
    firstFlightYear = firstFlightYear,
    engineCount = engineCount,
    engineType = engineType,
    wakeTurbulenceCategory = wakeTurbulenceCategory,
    maxPassengers = maxPassengers,
    cruiseSpeedKt = cruiseSpeedKt,
    maxSpeedKt = maxSpeedKt,
    rangeNm = rangeNm,
    rangeKm = rangeKm
)

fun AircraftModelSummary.toCacheEntity(updatedAtMs: Long): CachedAircraftSummaryEntity =
    CachedAircraftSummaryEntity(
        icaoTypeCode = icaoTypeCode,
        iataTypeCode = iataTypeCode,
        manufacturer = manufacturer,
        model = model,
        name = name,
        category = category.apiValue,
        photoUrl = photo?.url,
        photoAuthor = photo?.author,
        firstFlightYear = firstFlightYear,
        engineCount = engineCount,
        engineType = engineType,
        wakeTurbulenceCategory = wakeTurbulenceCategory,
        maxPassengers = maxPassengers,
        cruiseSpeedKt = cruiseSpeedKt,
        maxSpeedKt = maxSpeedKt,
        rangeNm = rangeNm,
        rangeKm = rangeKm,
        updatedAtMs = updatedAtMs
    )

fun CachedAircraftSummaryEntity.toDomain(): AircraftModelSummary = AircraftModelSummary(
    icaoTypeCode = icaoTypeCode,
    iataTypeCode = iataTypeCode,
    manufacturer = manufacturer,
    model = model,
    name = name,
    category = AircraftCategory.fromApiValue(category),
    photo = photoUrl?.let { AircraftPhoto(url = it, author = photoAuthor) },
    firstFlightYear = firstFlightYear,
    engineCount = engineCount,
    engineType = engineType,
    wakeTurbulenceCategory = wakeTurbulenceCategory,
    maxPassengers = maxPassengers,
    cruiseSpeedKt = cruiseSpeedKt,
    maxSpeedKt = maxSpeedKt,
    rangeNm = rangeNm,
    rangeKm = rangeKm
)

fun AircraftModelDetailDto.toDomain(): AircraftModelDetail = AircraftModelDetail(
    icaoTypeCode = icaoTypeCode,
    iataTypeCode = iataTypeCode,
    manufacturer = manufacturer,
    model = model,
    name = name,
    category = AircraftCategory.fromApiValue(category),
    militaryRoles = militaryRoles,
    countryOfOrigin = countryOfOrigin,
    icaoDescription = icaoDescription,
    aircraftClass = aircraftClass,
    wakeTurbulenceCategory = wakeTurbulenceCategory,
    firstFlightYear = firstFlightYear,
    certificationYear = certificationYear,
    typeCertificateUrl = typeCertificateUrl,
    wikipediaUrl = wikipediaUrl,
    dimensions = dimensions?.let {
        AircraftDimensions(
            lengthM = it.lengthM,
            heightM = it.heightM,
            wingspanM = it.wingspanM,
            wingAreaM2 = it.wingAreaM2,
            wingSweepDeg = it.wingSweepDeg,
            fuselageWidthM = it.fuselageWidthM,
            mainRotorDiameterM = it.mainRotorDiameterM,
            aerodromeReferenceCode = it.aerodromeReferenceCode
        )
    },
    weights = weights?.let {
        AircraftWeights(
            mtowKg = it.mtowKg,
            mlwKg = it.mlwKg,
            mzfwKg = it.mzfwKg,
            mrwKg = it.mrwKg,
            oewKg = it.oewKg
        )
    },
    performance = performance?.let {
        AircraftPerformance(
            cruiseSpeedKt = it.cruiseSpeedKt,
            cruiseMach = it.cruiseMach,
            cruiseAltitudeFt = it.cruiseAltitudeFt,
            maxSpeedKt = it.maxSpeedKt,
            maxSpeedKmh = it.maxSpeedKmh,
            maxSpeedMach = it.maxSpeedMach,
            vmoKt = it.vmoKt,
            mmo = it.mmo,
            approachSpeedKt = it.approachSpeedKt,
            serviceCeilingFt = it.serviceCeilingFt,
            rangeNm = it.rangeNm,
            rangeKm = it.rangeKm,
            fuelCapacityL = it.fuelCapacityL
        )
    },
    capacity = capacity?.let {
        AircraftCapacity(
            maxPassengers = it.maxPassengers,
            typicalPassengersLow = it.typicalPassengersLow,
            typicalPassengersHigh = it.typicalPassengersHigh
        )
    },
    propulsion = propulsion?.let {
        AircraftPropulsion(
            engineCount = it.engineCount,
            engineType = it.engineType,
            engineMount = it.engineMount,
            defaultEngine = it.defaultEngine
        )
    },
    variants = variants.map {
        AircraftVariant(
            name = it.name,
            engineManufacturer = it.engineManufacturer,
            engineModel = it.engineModel,
            certifiedAt = it.certifiedAt
        )
    },
    photos = photos.map { it.toDomain() },
    fleetCount = fleetCount,
    dataSources = dataSources
)

fun AircraftModelSummary.toFavoriteEntity(savedAtMs: Long): FavoriteAircraftEntity =
    FavoriteAircraftEntity(
        icaoTypeCode = icaoTypeCode,
        iataTypeCode = iataTypeCode,
        manufacturer = manufacturer,
        model = model,
        name = name,
        category = category.apiValue,
        photoUrl = photo?.url,
        photoAuthor = photo?.author,
        firstFlightYear = firstFlightYear,
        engineCount = engineCount,
        engineType = engineType,
        wakeTurbulenceCategory = wakeTurbulenceCategory,
        maxPassengers = maxPassengers,
        cruiseSpeedKt = cruiseSpeedKt,
        maxSpeedKt = maxSpeedKt,
        rangeNm = rangeNm,
        rangeKm = rangeKm,
        savedAtMs = savedAtMs
    )

fun FavoriteAircraftEntity.toDomain(): AircraftModelSummary = AircraftModelSummary(
    icaoTypeCode = icaoTypeCode,
    iataTypeCode = iataTypeCode,
    manufacturer = manufacturer,
    model = model,
    name = name,
    category = AircraftCategory.fromApiValue(category),
    photo = photoUrl?.let { AircraftPhoto(url = it, author = photoAuthor) },
    firstFlightYear = firstFlightYear,
    engineCount = engineCount,
    engineType = engineType,
    wakeTurbulenceCategory = wakeTurbulenceCategory,
    maxPassengers = maxPassengers,
    cruiseSpeedKt = cruiseSpeedKt,
    maxSpeedKt = maxSpeedKt,
    rangeNm = rangeNm,
    rangeKm = rangeKm
)
