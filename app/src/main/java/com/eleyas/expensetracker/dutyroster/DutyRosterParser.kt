package com.eleyas.expensetracker.dutyroster

import android.graphics.Rect
import com.google.mlkit.vision.text.Text
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

internal data class RosterOcrLine(
    val text: String,
    val box: Rect
)

object DutyRosterParser {
    private val dateRegex = Regex(
        "\\b(\\d{1,2})[-/](Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\b",
        RegexOption.IGNORE_CASE
    )

    private val timeRegex = Regex(
        "\\b(\\d{1,2})[:.]?(\\d{2})\\s*[-–—]\\s*(\\d{1,2})[:.]?(\\d{2})\\b"
    )

    private val ignoreNames = setOf(
        "DAY", "DATE", "OCC", "HOSTESS", "KITCHEN", "FIRE", "FISH",
        "REEF", "OCEAN", "TO", "GILL", "OFF", "ON", "VM", "TL", "JWS",
        "SNR", "ATTENDANT", "BAR", "SNR.BAR", "DUTY", "SCHEDULE", "COLLECT"
    )

    fun parse(result: Text): List<RosterDay> {
        val lines = result.textBlocks.flatMap { block ->
            block.lines.mapNotNull { line ->
                line.boundingBox?.let {
                    RosterOcrLine(line.text.trim(), it)
                }
            }
        }

        if (lines.isEmpty()) return emptyList()

        val dateLines = lines
            .filter { dateRegex.containsMatchIn(it.text) }
            .sortedBy { it.box.centerX() }
            .distinctBy {
                dateRegex.find(it.text)?.value?.lowercase(Locale.US)
            }

        if (dateLines.size < 2) return emptyList()

        val year = Calendar.getInstance().get(Calendar.YEAR)
        val parser = SimpleDateFormat("d-MMM-yyyy", Locale.US)

        val columns = dateLines.mapNotNull { line ->
            val match = dateRegex.find(line.text) ?: return@mapNotNull null
            val day = match.groupValues[1].toIntOrNull() ?: return@mapNotNull null
            val month = match.groupValues[2].replaceFirstChar { it.uppercase() }
            val date = parser.parse("$day-$month-$year") ?: return@mapNotNull null

            Triple(
                line.box.centerX(),
                day,
                SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
            )
        }.sortedBy { it.first }

        if (columns.size < 2) return emptyList()

        val headerBottom = dateLines.maxOf { it.box.bottom }
        val firstColumnX = columns.first().first

        val nameLines = lines
            .filter {
                it.box.centerX() < firstColumnX - 35 &&
                        it.box.top > headerBottom + 20
            }
            .filter { isCandidateName(it.text) }
            .sortedBy { it.box.centerY() }
            .distinctBy { it.box.centerY() / 8 }

        if (nameLines.isEmpty()) return emptyList()

        val rowBounds = nameLines.mapIndexed { index, name ->
            val top =
                if (index == 0) {
                    headerBottom
                } else {
                    (nameLines[index - 1].box.centerY() + name.box.centerY()) / 2
                }

            val bottom =
                if (index == nameLines.lastIndex) {
                    Int.MAX_VALUE
                } else {
                    (name.box.centerY() + nameLines[index + 1].box.centerY()) / 2
                }

            name.text to (top..bottom)
        }

        return columns.map { (x, _, iso) ->
            val left = columnLeft(columns, x)
            val right = columnRight(columns, x)

            val duties = rowBounds.mapNotNull { (person, range) ->
                val cellLines = lines
                    .filter { line ->
                        val cy = line.box.centerY()
                        val cx = line.box.centerX()

                        cy in range &&
                                cx >= left &&
                                cx < right &&
                                cx > firstColumnX - 20
                    }
                    .sortedBy { it.box.top }

                val duty = cellLines
                    .joinToString(" ") { it.text }
                    .replace(Regex("\\s+"), " ")
                    .trim()

                if (duty.isBlank()) null
                else DutyEntry(normalizeName(person), normalizeDuty(duty))
            }

            RosterDay(
                dateIso = iso,
                displayDate = iso,
                duties = duties
            )
        }
    }

    private fun columnLeft(
        columns: List<Triple<Int, Int, String>>,
        x: Int
    ): Int {
        val index = columns.indexOfFirst { it.first == x }
        return if (index == 0) {
            x - 120
        } else {
            (columns[index - 1].first + x) / 2
        }
    }

    private fun columnRight(
        columns: List<Triple<Int, Int, String>>,
        x: Int
    ): Int {
        val index = columns.indexOfFirst { it.first == x }
        return if (index == columns.lastIndex) {
            x + 220
        } else {
            (x + columns[index + 1].first) / 2
        }
    }

    private fun isCandidateName(raw: String): Boolean {
        val value = raw.trim().replace(Regex("\\s+"), " ")

        if (value.length !in 2..24) return false
        if (value.any { it.isDigit() }) return false
        if (ignoreNames.contains(value.uppercase(Locale.US))) return false
        if (value.count { it.isLetter() } < 2) return false

        return value.all {
            it.isLetter() ||
                    it.isWhitespace() ||
                    it == '.' ||
                    it == '-' ||
                    it == '_'
        }
    }

    private fun normalizeName(value: String): String =
        value.trim().replace(Regex("\\s+"), " ")

    private val timePointRegex = Regex(
        "\\b(\\d{1,2})[:.]?(\\d{2})\\b"
    )

    private fun normalizeDuty(value: String): String {
        val cleaned = value
            .replace("—", "-")
            .replace("–", "-")
            .replace(
                Regex("\\b(\\d{2})(\\d{2})-(\\d{2})(\\d{2})\\b")
            ) {
                "${it.groupValues[1]}:${it.groupValues[2]}-${it.groupValues[3]}:${it.groupValues[4]}"
            }
            .replace(Regex("\\s+"), " ")
            .trim()

        // OCR can scramble a two-shift cell into something like:
        // "18:00- 07:30-11:30 22:30".
        // When four time points are present, treat them as two shifts,
        // sort them by start time, and then pair them. This restores the
        // intended Morning + Evening duty instead of creating false ranges.
        val points = timePointRegex.findAll(cleaned)
            .mapNotNull { match ->
                val hour = match.groupValues[1].toIntOrNull()
                val minute = match.groupValues[2].toIntOrNull()
                if (hour == null || minute == null || hour !in 0..23 || minute !in 0..59) {
                    null
                } else {
                    TimePoint(
                        hour = hour,
                        minute = minute,
                        raw = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
                    )
                }
            }
            .toList()

        if (points.size >= 4 && points.size % 2 == 0) {
            val ordered = points.sortedBy { it.hour * 60 + it.minute }
            val shifts = ordered.chunked(2).map { pair ->
                "${pair[0].raw}-${pair[1].raw}"
            }

            val nonTime = cleaned
                .replace(timePointRegex, " ")
                .replace(Regex("\\s+"), " ")
                .replace(Regex("\\s*[-/]\\s*"), " ")
                .trim()

            return listOf(nonTime, shifts.joinToString(" • "))
                .filter { it.isNotBlank() }
                .joinToString(" ")
        }

        return cleaned
    }

    private data class TimePoint(
        val hour: Int,
        val minute: Int,
        val raw: String
    )

    fun isOff(duty: String): Boolean =
        duty.uppercase(Locale.US).contains("OFF")

    fun isMorning(duty: String): Boolean {
        val match = timeRegex.find(duty) ?: return false
        val hour = match.groupValues[1].toIntOrNull() ?: return false
        return hour in 5..9
    }
}
