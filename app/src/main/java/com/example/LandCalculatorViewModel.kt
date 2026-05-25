package com.example

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.sqrt

enum class AppTab {
    RURAL_AREA, URBAN_AREA, PRICE, DIVIDER
}

class LandCalculatorViewModel : ViewModel() {

    // Theme state
    var isDarkTheme by mutableStateOf(false)

    // Current selected Tab
    var currentTab by mutableStateOf(AppTab.RURAL_AREA)

    // Rates structure
    object RuralRates {
        const val KILLA = 43560.0
        const val VIGHA = 10890.0
        const val KANAL = 5445.0
        const val MARLA = 272.25
        const val GAJ = 9.0
        const val SQ_FT = 1.0
        const val HECTARE = 107639.1042
        const val SQ_METER = 10.7639
        const val SQ_KM = 10763910.42
        const val SQ_KARAM = 30.25
    }

    object UrbanRates {
        const val KILLA = 36000.0 // 160 * 225
        const val VIGHA = 9000.0 // 40 * 225
        const val KANAL = 4500.0 // 20 * 225
        const val MARLA = 225.0
        const val GAJ = 9.0
        const val SQ_FT = 1.0
        const val ACRE = 43560.0
        const val HECTARE = 107639.1042
        const val SQ_METER = 10.7639
        const val SQ_KM = 10763910.42
    }

    private val meterToFeet = 3.28084

    // Helper to format float values
    fun formatDouble(value: Double, precision: Int): String {
        if (value.isNaN() || value.isInfinite() || value == 0.0) return ""
        return try {
            String.format(Locale.US, "%.${precision}f", value)
                .trimEnd('0')
                .trimEnd('.')
        } catch (e: Throwable) {
            ""
        }
    }

    private fun parseInput(input: String): Double? {
        val trimmed = input.trim()
        if (trimmed.isBlank()) return null
        return trimmed.replace(",", "").toDoubleOrNull()
    }

    private fun sanitizeInput(value: String, maxLen: Int = 15): String {
        // Keep only digits, dots, and commas
        val filtered = value.filter { it.isDigit() || it == '.' || it == ',' }
        val dotIndex = filtered.indexOf('.')
        val clean = if (dotIndex != -1) {
            val before = filtered.substring(0, dotIndex + 1)
            val after = filtered.substring(dotIndex + 1).replace(".", "")
            before + after
        } else {
            filtered
        }
        return if (clean.length > maxLen) clean.substring(0, maxLen) else clean
    }

    // ==========================================
    // RURAL STATE VARIABLES
    // ==========================================
    var rKilla by mutableStateOf("")
    var rVigha by mutableStateOf("")
    var rKanal by mutableStateOf("")
    var rMarla by mutableStateOf("")
    var rGaj by mutableStateOf("")
    var rSqFt by mutableStateOf("")
    var rHectare by mutableStateOf("")
    var rSqMeter by mutableStateOf("")
    var rSqKm by mutableStateOf("")

    // Rural dimension state
    var rFeetLength by mutableStateOf("")
    var rFeetWidth by mutableStateOf("")
    var rMeterLength by mutableStateOf("")
    var rMeterWidth by mutableStateOf("")
    var rKaramLength by mutableStateOf("")
    var rKaramWidth by mutableStateOf("")

    // Rural triangle
    var rTriA by mutableStateOf("")
    var rTriB by mutableStateOf("")
    var rTriC by mutableStateOf("")
    var rTriUnit by mutableStateOf("feet") // "feet", "meters", "karams"
    var rTriError by mutableStateOf(false)

    fun onRuralValueChange(unit: String, value: String) {
        val clean = sanitizeInput(value)
        when (unit) {
            "killa" -> { rKilla = clean; runRuralCalculation("killa", clean) }
            "vigha" -> { rVigha = clean; runRuralCalculation("vigha", clean) }
            "kanal" -> { rKanal = clean; runRuralCalculation("kanal", clean) }
            "marla" -> { rMarla = clean; runRuralCalculation("marla", clean) }
            "gaj" -> { rGaj = clean; runRuralCalculation("gaj", clean) }
            "sqFt" -> { rSqFt = clean; runRuralCalculation("sqFt", clean) }
            "hectare" -> { rHectare = clean; runRuralCalculation("hectare", clean) }
            "sqMeter" -> { rSqMeter = clean; runRuralCalculation("sqMeter", clean) }
            "sqKm" -> { rSqKm = clean; runRuralCalculation("sqKm", clean) }
        }
    }

    private fun runRuralCalculation(sourceUnit: String, value: String) {
        try {
            clearRuralDimensions(exclude = "")
            val parsed = parseInput(value)
            if (parsed == null) {
                clearAllRuralFields(exclude = sourceUnit)
                return
            }

            val inSqFt = when (sourceUnit) {
                "killa" -> parsed * RuralRates.KILLA
                "vigha" -> parsed * RuralRates.VIGHA
                "kanal" -> parsed * RuralRates.KANAL
                "marla" -> parsed * RuralRates.MARLA
                "gaj" -> parsed * RuralRates.GAJ
                "sqFt" -> parsed * RuralRates.SQ_FT
                "hectare" -> parsed * RuralRates.HECTARE
                "sqMeter" -> parsed * RuralRates.SQ_METER
                "sqKm" -> parsed * RuralRates.SQ_KM
                else -> 0.0
            }

            if (inSqFt.isNaN() || inSqFt.isInfinite()) {
                clearAllRuralFields(exclude = sourceUnit)
                return
            }

            updateAllRuralFields(inSqFt, exclude = sourceUnit)
        } catch (t: Throwable) {
            clearAllRuralFields(exclude = sourceUnit)
        }
    }

    private fun updateAllRuralFields(inSqFt: Double, exclude: String) {
        if (exclude != "killa") rKilla = formatDouble(inSqFt / RuralRates.KILLA, 4)
        if (exclude != "vigha") rVigha = formatDouble(inSqFt / RuralRates.VIGHA, 4)
        if (exclude != "kanal") rKanal = formatDouble(inSqFt / RuralRates.KANAL, 4)
        if (exclude != "marla") rMarla = formatDouble(inSqFt / RuralRates.MARLA, 4)
        if (exclude != "gaj") rGaj = formatDouble(inSqFt / RuralRates.GAJ, 2)
        if (exclude != "sqFt") rSqFt = formatDouble(inSqFt / RuralRates.SQ_FT, 2)
        if (exclude != "hectare") rHectare = formatDouble(inSqFt / RuralRates.HECTARE, 5)
        if (exclude != "sqMeter") rSqMeter = formatDouble(inSqFt / RuralRates.SQ_METER, 2)
        if (exclude != "sqKm") rSqKm = formatDouble(inSqFt / RuralRates.SQ_KM, 7)
    }

    private fun clearAllRuralFields(exclude: String) {
        if (exclude != "killa") rKilla = ""
        if (exclude != "vigha") rVigha = ""
        if (exclude != "kanal") rKanal = ""
        if (exclude != "marla") rMarla = ""
        if (exclude != "gaj") rGaj = ""
        if (exclude != "sqFt") rSqFt = ""
        if (exclude != "hectare") rHectare = ""
        if (exclude != "sqMeter") rSqMeter = ""
        if (exclude != "sqKm") rSqKm = ""
    }

    fun onRuralDimensionChange(dimType: String, lengthVal: String, widthVal: String) {
        val cleanLength = sanitizeInput(lengthVal)
        val cleanWidth = sanitizeInput(widthVal)
        when (dimType) {
            "feet" -> {
                rFeetLength = cleanLength
                rFeetWidth = cleanWidth
                val l = parseInput(cleanLength)
                val w = parseInput(cleanWidth)
                if (l != null && w != null) {
                    val sqFt = l * w
                    updateAllRuralFields(sqFt, exclude = "")
                    syncRuralDimensions(l, w, source = "feet")
                } else {
                    clearAllRuralFields(exclude = "")
                    clearRuralDimensions(exclude = "feet")
                }
            }
            "meters" -> {
                rMeterLength = cleanLength
                rMeterWidth = cleanWidth
                val l = parseInput(cleanLength)
                val w = parseInput(cleanWidth)
                if (l != null && w != null) {
                    val lF = l * meterToFeet
                    val wF = w * meterToFeet
                    val sqFt = lF * wF
                    updateAllRuralFields(sqFt, exclude = "")
                    syncRuralDimensions(lF, wF, source = "meters")
                } else {
                    clearAllRuralFields(exclude = "")
                    clearRuralDimensions(exclude = "meters")
                }
            }
            "karams" -> {
                rKaramLength = cleanLength
                rKaramWidth = cleanWidth
                val l = parseInput(cleanLength)
                val w = parseInput(cleanWidth)
                if (l != null && w != null) {
                    val lF = l * 5.5
                    val wF = w * 5.5
                    val sqFt = lF * wF
                    updateAllRuralFields(sqFt, exclude = "")
                    syncRuralDimensions(lF, wF, source = "karams")
                } else {
                    clearAllRuralFields(exclude = "")
                    clearRuralDimensions(exclude = "karams")
                }
            }
        }
    }

    private fun syncRuralDimensions(lenFeet: Double, widFeet: Double, source: String) {
        if (source != "feet") {
            rFeetLength = formatDouble(lenFeet, 2)
            rFeetWidth = formatDouble(widFeet, 2)
        }
        if (source != "karams") {
            rKaramLength = formatDouble(lenFeet / 5.5, 2)
            rKaramWidth = formatDouble(widFeet / 5.5, 2)
        }
        if (source != "meters") {
            rMeterLength = formatDouble(lenFeet / meterToFeet, 2)
            rMeterWidth = formatDouble(widFeet / meterToFeet, 2)
        }
    }

    private fun clearRuralDimensions(exclude: String) {
        if (exclude != "feet") { rFeetLength = ""; rFeetWidth = "" }
        if (exclude != "meters") { rMeterLength = ""; rMeterWidth = "" }
        if (exclude != "karams") { rKaramLength = ""; rKaramWidth = "" }
        if (exclude != "triangle") {
            rTriA = ""; rTriB = ""; rTriC = ""
            rTriError = false
        }
    }

    fun onRuralTriangleChange(a: String, b: String, c: String, unit: String = rTriUnit) {
        val cleanA = sanitizeInput(a)
        val cleanB = sanitizeInput(b)
        val cleanC = sanitizeInput(c)
        rTriA = cleanA
        rTriB = cleanB
        rTriC = cleanC
        rTriUnit = unit
        clearRuralDimensions(exclude = "triangle")

        val sideA = parseInput(cleanA)
        val sideB = parseInput(cleanB)
        val sideC = parseInput(cleanC)

        if (sideA != null && sideB != null && sideC != null) {
            if (sideA + sideB > sideC && sideA + sideC > sideB && sideB + sideC > sideA) {
                val s = (sideA + sideB + sideC) / 2.0
                var area = sqrt(s * (s - sideA) * (s - sideB) * (s - sideC))

                if (unit == "meters") {
                    area *= (meterToFeet * meterToFeet)
                } else if (unit == "karams") {
                    area *= RuralRates.SQ_KARAM
                }

                rTriError = false
                updateAllRuralFields(area, exclude = "")
            } else {
                rTriError = true
                clearAllRuralFields(exclude = "")
            }
        } else {
            rTriError = false
            clearAllRuralFields(exclude = "")
        }
    }

    fun resetRural() {
        clearAllRuralFields("")
        clearRuralDimensions("")
    }

    // ==========================================
    // URBAN STATE VARIABLES
    // ==========================================
    var uKilla by mutableStateOf("")
    var uVigha by mutableStateOf("")
    var uKanal by mutableStateOf("")
    var uMarla by mutableStateOf("")
    var uGaj by mutableStateOf("")
    var uSqFt by mutableStateOf("")
    var uAcre by mutableStateOf("")
    var uHectare by mutableStateOf("")
    var uSqMeter by mutableStateOf("")
    var uSqKm by mutableStateOf("")

    // Urban dimension state
    var uFeetLength by mutableStateOf("")
    var uFeetWidth by mutableStateOf("")
    var uMeterLength by mutableStateOf("")
    var uMeterWidth by mutableStateOf("")
    var uKaramLength by mutableStateOf("")
    var uKaramWidth by mutableStateOf("")

    // Urban triangle
    var uTriA by mutableStateOf("")
    var uTriB by mutableStateOf("")
    var uTriC by mutableStateOf("")
    var uTriUnit by mutableStateOf("feet") // "feet", "meters", "karams"
    var uTriError by mutableStateOf(false)

    fun onUrbanValueChange(unit: String, value: String) {
        val clean = sanitizeInput(value)
        when (unit) {
            "killa" -> { uKilla = clean; runUrbanCalculation("killa", clean) }
            "vigha" -> { uVigha = clean; runUrbanCalculation("vigha", clean) }
            "kanal" -> { uKanal = clean; runUrbanCalculation("kanal", clean) }
            "marla" -> { uMarla = clean; runUrbanCalculation("marla", clean) }
            "gaj" -> { uGaj = clean; runUrbanCalculation("gaj", clean) }
            "sqFt" -> { uSqFt = clean; runUrbanCalculation("sqFt", clean) }
            "acre" -> { uAcre = clean; runUrbanCalculation("acre", clean) }
            "hectare" -> { uHectare = clean; runUrbanCalculation("hectare", clean) }
            "sqMeter" -> { uSqMeter = clean; runUrbanCalculation("sqMeter", clean) }
            "sqKm" -> { uSqKm = clean; runUrbanCalculation("sqKm", clean) }
        }
    }

    private fun runUrbanCalculation(sourceUnit: String, value: String) {
        try {
            clearUrbanDimensions(exclude = "")
            val parsed = parseInput(value)
            if (parsed == null) {
                clearAllUrbanFields(exclude = sourceUnit)
                return
            }

            val inSqFt = when (sourceUnit) {
                "killa" -> parsed * UrbanRates.KILLA
                "vigha" -> parsed * UrbanRates.VIGHA
                "kanal" -> parsed * UrbanRates.KANAL
                "marla" -> parsed * UrbanRates.MARLA
                "gaj" -> parsed * UrbanRates.GAJ
                "sqFt" -> parsed * UrbanRates.SQ_FT
                "acre" -> parsed * UrbanRates.ACRE
                "hectare" -> parsed * UrbanRates.HECTARE
                "sqMeter" -> parsed * UrbanRates.SQ_METER
                "sqKm" -> parsed * UrbanRates.SQ_KM
                else -> 0.0
            }

            if (inSqFt.isNaN() || inSqFt.isInfinite()) {
                clearAllUrbanFields(exclude = sourceUnit)
                return
            }

            updateAllUrbanFields(inSqFt, exclude = sourceUnit)
        } catch (t: Throwable) {
            clearAllUrbanFields(exclude = sourceUnit)
        }
    }

    private fun updateAllUrbanFields(inSqFt: Double, exclude: String) {
        if (exclude != "killa") uKilla = formatDouble(inSqFt / UrbanRates.KILLA, 4)
        if (exclude != "vigha") uVigha = formatDouble(inSqFt / UrbanRates.VIGHA, 4)
        if (exclude != "kanal") uKanal = formatDouble(inSqFt / UrbanRates.KANAL, 4)
        if (exclude != "marla") uMarla = formatDouble(inSqFt / UrbanRates.MARLA, 4)
        if (exclude != "gaj") uGaj = formatDouble(inSqFt / UrbanRates.GAJ, 2)
        if (exclude != "sqFt") uSqFt = formatDouble(inSqFt / UrbanRates.SQ_FT, 2)
        if (exclude != "acre") uAcre = formatDouble(inSqFt / UrbanRates.ACRE, 4)
        if (exclude != "hectare") uHectare = formatDouble(inSqFt / UrbanRates.HECTARE, 5)
        if (exclude != "sqMeter") uSqMeter = formatDouble(inSqFt / UrbanRates.SQ_METER, 2)
        if (exclude != "sqKm") uSqKm = formatDouble(inSqFt / UrbanRates.SQ_KM, 7)
    }

    private fun clearAllUrbanFields(exclude: String) {
        if (exclude != "killa") uKilla = ""
        if (exclude != "vigha") uVigha = ""
        if (exclude != "kanal") uKanal = ""
        if (exclude != "marla") uMarla = ""
        if (exclude != "gaj") uGaj = ""
        if (exclude != "sqFt") uSqFt = ""
        if (exclude != "acre") uAcre = ""
        if (exclude != "hectare") uHectare = ""
        if (exclude != "sqMeter") uSqMeter = ""
        if (exclude != "sqKm") uSqKm = ""
    }

    fun onUrbanDimensionChange(dimType: String, lengthVal: String, widthVal: String) {
        val cleanLength = sanitizeInput(lengthVal)
        val cleanWidth = sanitizeInput(widthVal)
        when (dimType) {
            "feet" -> {
                uFeetLength = cleanLength
                uFeetWidth = cleanWidth
                val l = parseInput(cleanLength)
                val w = parseInput(cleanWidth)
                if (l != null && w != null) {
                    val sqFt = l * w
                    updateAllUrbanFields(sqFt, exclude = "")
                    syncUrbanDimensions(l, w, source = "feet")
                } else {
                    clearAllUrbanFields(exclude = "")
                    clearUrbanDimensions(exclude = "feet")
                }
            }
            "meters" -> {
                uMeterLength = cleanLength
                uMeterWidth = cleanWidth
                val l = parseInput(cleanLength)
                val w = parseInput(cleanWidth)
                if (l != null && w != null) {
                    val lF = l * meterToFeet
                    val wF = w * meterToFeet
                    val sqFt = lF * wF
                    updateAllUrbanFields(sqFt, exclude = "")
                    syncUrbanDimensions(lF, wF, source = "meters")
                } else {
                    clearAllUrbanFields(exclude = "")
                    clearUrbanDimensions(exclude = "meters")
                }
            }
            "karams" -> {
                uKaramLength = cleanLength
                uKaramWidth = cleanWidth
                val l = parseInput(cleanLength)
                val w = parseInput(cleanWidth)
                if (l != null && w != null) {
                    val lF = l * 5.5
                    val wF = w * 5.5
                    val sqFt = lF * wF
                    updateAllUrbanFields(sqFt, exclude = "")
                    syncUrbanDimensions(lF, wF, source = "karams")
                } else {
                    clearAllUrbanFields(exclude = "")
                    clearUrbanDimensions(exclude = "karams")
                }
            }
        }
    }

    private fun syncUrbanDimensions(lenFeet: Double, widFeet: Double, source: String) {
        if (source != "feet") {
            uFeetLength = formatDouble(lenFeet, 2)
            uFeetWidth = formatDouble(widFeet, 2)
        }
        if (source != "karams") {
            uKaramLength = formatDouble(lenFeet / 5.5, 2)
            uKaramWidth = formatDouble(widFeet / 5.5, 2)
        }
        if (source != "meters") {
            uMeterLength = formatDouble(lenFeet / meterToFeet, 2)
            uMeterWidth = formatDouble(widFeet / meterToFeet, 2)
        }
    }

    private fun clearUrbanDimensions(exclude: String) {
        if (exclude != "feet") { uFeetLength = ""; uFeetWidth = "" }
        if (exclude != "meters") { uMeterLength = ""; uMeterWidth = "" }
        if (exclude != "karams") { uKaramLength = ""; uKaramWidth = "" }
        if (exclude != "triangle") {
            uTriA = ""; uTriB = ""; uTriC = ""
            uTriError = false
        }
    }

    fun onUrbanTriangleChange(a: String, b: String, c: String, unit: String = uTriUnit) {
        val cleanA = sanitizeInput(a)
        val cleanB = sanitizeInput(b)
        val cleanC = sanitizeInput(c)
        uTriA = cleanA
        uTriB = cleanB
        uTriC = cleanC
        uTriUnit = unit
        clearUrbanDimensions(exclude = "triangle")

        val sideA = parseInput(cleanA)
        val sideB = parseInput(cleanB)
        val sideC = parseInput(cleanC)

        if (sideA != null && sideB != null && sideC != null) {
            if (sideA + sideB > sideC && sideA + sideC > sideB && sideB + sideC > sideA) {
                val s = (sideA + sideB + sideC) / 2.0
                var area = sqrt(s * (s - sideA) * (s - sideB) * (s - sideC))

                if (unit == "meters") {
                    area *= (meterToFeet * meterToFeet)
                } else if (unit == "karams") {
                    area *= RuralRates.SQ_KARAM
                }

                uTriError = false
                updateAllUrbanFields(area, exclude = "")
            } else {
                uTriError = true
                clearAllUrbanFields(exclude = "")
            }
        } else {
            uTriError = false
            clearAllUrbanFields(exclude = "")
        }
    }

    fun resetUrban() {
        clearAllUrbanFields("")
        clearUrbanDimensions("")
    }

    // ==========================================
    // PRICE STATE VARIABLES
    // ==========================================
    var pKilla by mutableStateOf("")
    var pVigha by mutableStateOf("")
    var pKanal by mutableStateOf("")
    var pMarla by mutableStateOf("")
    var pGaj by mutableStateOf("")
    var pSqFt by mutableStateOf("")
    var pSqMeter by mutableStateOf("")
    var pSqKm by mutableStateOf("")

    // We can also have standard formatted displays
    fun getFormattedPrice(unit: String): String {
        val raw = when (unit) {
            "killa" -> pKilla
            "vigha" -> pVigha
            "kanal" -> pKanal
            "marla" -> pMarla
            "gaj" -> pGaj
            "sqFt" -> pSqFt
            "sqMeter" -> pSqMeter
            "sqKm" -> pSqKm
            else -> ""
        }
        val valDouble = parseInput(raw) ?: return ""
        return try {
            val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
            format.maximumFractionDigits = 2
            format.format(valDouble)
        } catch (e: Throwable) {
            ""
        }
    }

    fun onPriceValueChange(unit: String, value: String) {
        val clean = sanitizeInput(value)
        when (unit) {
            "killa" -> pKilla = clean
            "vigha" -> pVigha = clean
            "kanal" -> pKanal = clean
            "marla" -> pMarla = clean
            "gaj" -> pGaj = clean
            "sqFt" -> pSqFt = clean
            "sqMeter" -> pSqMeter = clean
            "sqKm" -> pSqKm = clean
        }
        runPriceCalculation(unit, clean)
    }

    private fun runPriceCalculation(sourceUnit: String, value: String) {
        try {
            val parsed = parseInput(value)
            if (parsed == null) {
                clearAllPriceFields(exclude = sourceUnit)
                return
            }

            // Get RuralRates factor for sourceUnit
            val factor = when (sourceUnit) {
                "killa" -> RuralRates.KILLA
                "vigha" -> RuralRates.VIGHA
                "kanal" -> RuralRates.KANAL
                "marla" -> RuralRates.MARLA
                "gaj" -> RuralRates.GAJ
                "sqFt" -> RuralRates.SQ_FT
                "sqMeter" -> RuralRates.SQ_METER
                "sqKm" -> RuralRates.SQ_KM
                else -> 1.0
            }

            val pricePerSqFt = parsed / factor
            if (pricePerSqFt.isNaN() || pricePerSqFt.isInfinite()) {
                clearAllPriceFields(exclude = sourceUnit)
                return
            }
            updateAllPriceFields(pricePerSqFt, exclude = sourceUnit)
        } catch (t: Throwable) {
            clearAllPriceFields(exclude = sourceUnit)
        }
    }

    private fun updateAllPriceFields(pricePerSqFt: Double, exclude: String) {
        if (exclude != "killa") pKilla = formatDouble(pricePerSqFt * RuralRates.KILLA, 2)
        if (exclude != "vigha") pVigha = formatDouble(pricePerSqFt * RuralRates.VIGHA, 2)
        if (exclude != "kanal") pKanal = formatDouble(pricePerSqFt * RuralRates.KANAL, 2)
        if (exclude != "marla") pMarla = formatDouble(pricePerSqFt * RuralRates.MARLA, 2)
        if (exclude != "gaj") pGaj = formatDouble(pricePerSqFt * RuralRates.GAJ, 2)
        if (exclude != "sqFt") pSqFt = formatDouble(pricePerSqFt * RuralRates.SQ_FT, 2)
        if (exclude != "sqMeter") pSqMeter = formatDouble(pricePerSqFt * RuralRates.SQ_METER, 2)
        if (exclude != "sqKm") pSqKm = formatDouble(pricePerSqFt * RuralRates.SQ_KM, 2)
    }

    private fun clearAllPriceFields(exclude: String) {
        if (exclude != "killa") pKilla = ""
        if (exclude != "vigha") pVigha = ""
        if (exclude != "kanal") pKanal = ""
        if (exclude != "marla") pMarla = ""
        if (exclude != "gaj") pGaj = ""
        if (exclude != "sqFt") pSqFt = ""
        if (exclude != "sqMeter") pSqMeter = ""
        if (exclude != "sqKm") pSqKm = ""
    }

    fun resetPrice() {
        clearAllPriceFields("")
    }

    // ==========================================
    // DIVIDER STATE VARIABLES
    // ==========================================
    var divKilla by mutableStateOf("")
    var divKanal by mutableStateOf("")
    var divMarla by mutableStateOf("")
    var divPeople by mutableStateOf("1")

    // Outputs
    var shareKilla by mutableStateOf("0")
    var shareKanal by mutableStateOf("0")
    var shareMarla by mutableStateOf("0")

    fun onDividerChange(field: String, value: String) {
        val clean = sanitizeInput(value)
        when (field) {
            "killa" -> divKilla = clean
            "kanal" -> divKanal = clean
            "marla" -> divMarla = clean
            "people" -> divPeople = clean
        }
        calculateDivision()
    }

    private fun calculateDivision() {
        try {
            val k = parseInput(divKilla) ?: 0.0
            val kn = parseInput(divKanal) ?: 0.0
            val m = parseInput(divMarla) ?: 0.0
            val people = parseInput(divPeople) ?: 1.0

            if (people <= 0.0) {
                shareKilla = "0"
                shareKanal = "0"
                shareMarla = "0"
                return
            }

            // Convert everything to Marlas for calculation: 1 Killa = 160 Marlas, 1 Kanal = 20 Marlas
            val totalMarlas = (k * 160.0) + (kn * 20.0) + m
            val shareMarlas = totalMarlas / people

            if (shareMarlas.isNaN() || shareMarlas.isInfinite()) {
                shareKilla = "0"
                shareKanal = "0"
                shareMarla = "0"
                return
            }

            val resKilla = (shareMarlas / 160.0).toInt()
            val remainderAfterKilla = shareMarlas % 160.0
            val resKanal = (remainderAfterKilla / 20.0).toInt()
            val resMarla = remainderAfterKilla % 20.0

            shareKilla = resKilla.toString()
            shareKanal = resKanal.toString()
            shareMarla = formatDouble(resMarla, 2).ifBlank { "0" }
        } catch (t: Throwable) {
            shareKilla = "0"
            shareKanal = "0"
            shareMarla = "0"
        }
    }

    fun resetDivider() {
        divKilla = ""
        divKanal = ""
        divMarla = ""
        divPeople = "1"
        shareKilla = "0"
        shareKanal = "0"
        shareMarla = "0"
    }
}
