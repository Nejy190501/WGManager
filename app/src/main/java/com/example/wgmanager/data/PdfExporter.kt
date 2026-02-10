package com.example.wgmanager.data

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfExporter {

    private fun createPdf(context: Context, title: String, lines: List<String>, fileName: String): File? {
        return try {
            val document = PdfDocument()
            val pageWidth = 595 // A4
            val pageHeight = 842

            var pageNumber = 1
            var yPos = 60f

            fun newPage(): PdfDocument.Page {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber++).create()
                return document.startPage(pageInfo)
            }

            var page = newPage()
            var canvas = page.canvas

            val titlePaint = Paint().apply {
                textSize = 22f; isFakeBoldText = true; color = android.graphics.Color.BLACK
            }
            val headerPaint = Paint().apply {
                textSize = 14f; isFakeBoldText = true; color = android.graphics.Color.DKGRAY
            }
            val textPaint = Paint().apply {
                textSize = 12f; color = android.graphics.Color.BLACK
            }
            val lightPaint = Paint().apply {
                textSize = 11f; color = android.graphics.Color.GRAY
            }

            // Title
            canvas.drawText(title, 40f, yPos, titlePaint)
            yPos += 16f
            val dateStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.GERMANY).format(Date())
            canvas.drawText("Erstellt: $dateStr", 40f, yPos, lightPaint)
            yPos += 30f

            // Separator line
            val linePaint = Paint().apply { color = android.graphics.Color.LTGRAY; strokeWidth = 1f }
            canvas.drawLine(40f, yPos, (pageWidth - 40).toFloat(), yPos, linePaint)
            yPos += 20f

            for (line in lines) {
                if (yPos > pageHeight - 60f) {
                    document.finishPage(page)
                    page = newPage()
                    canvas = page.canvas
                    yPos = 60f
                }
                val paint = when {
                    line.startsWith("##") -> headerPaint
                    line.startsWith("---") -> { canvas.drawLine(40f, yPos, (pageWidth - 40).toFloat(), yPos, linePaint); yPos += 15f; continue }
                    else -> textPaint
                }
                val displayLine = line.removePrefix("## ")
                canvas.drawText(displayLine, 40f, yPos, paint)
                yPos += if (paint == headerPaint) 22f else 18f
            }

            document.finishPage(page)

            val dir = File(context.cacheDir, "exports")
            dir.mkdirs()
            val file = File(dir, "$fileName.pdf")
            FileOutputStream(file).use { document.writeTo(it) }
            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun sharePdf(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "PDF teilen / Share PDF"))
        } catch (e: Exception) {
            Toast.makeText(context, "Export fehlgeschlagen", Toast.LENGTH_SHORT).show()
        }
    }

    fun exportCalendar(context: Context) {
        val events = DataStore.events
        val lines = mutableListOf<String>()
        lines.add("## Alle Termine")
        lines.add("")
        events.sortedBy { it.date }.forEach { event ->
            lines.add("${event.emoji}  ${event.title}")
            lines.add("   Datum: ${event.date}   Typ: ${event.type.name}")
            lines.add("   Erstellt von: ${event.createdBy}")
            lines.add("")
        }
        val file = createPdf(context, "📅 WG Kalender", lines, "wg_kalender_${System.currentTimeMillis()}")
        if (file != null) sharePdf(context, file)
    }

    fun exportShoppingList(context: Context) {
        val items = DataStore.shoppingItems
        val pending = items.filter { it.status == ShoppingStatus.PENDING }
        val bought = items.filter { it.status == ShoppingStatus.BOUGHT }
        val lines = mutableListOf<String>()

        lines.add("## Offene Artikel (${pending.size})")
        lines.add("")
        pending.forEach { lines.add("${it.emoji}  ${it.name}  —  ${"%.2f".format(it.price)}€") }
        lines.add("")
        lines.add("---")
        lines.add("## Gekaufte Artikel (${bought.size})")
        lines.add("")
        bought.forEach { lines.add("${it.emoji}  ${it.name}  —  ${"%.2f".format(it.price)}€  (${it.boughtBy})") }
        lines.add("")
        lines.add("---")
        lines.add("Gesamt ausgegeben: ${"%.2f".format(bought.sumOf { it.price })}€")

        val file = createPdf(context, "🛒 Einkaufsliste", lines, "einkaufsliste_${System.currentTimeMillis()}")
        if (file != null) sharePdf(context, file)
    }

    fun exportCostReport(context: Context) {
        val costs = DataStore.recurringCosts
        val lines = mutableListOf<String>()
        val total = DataStore.getRecurringCostTotal()
        val perPerson = DataStore.getRecurringCostPerPerson()

        lines.add("## Zusammenfassung")
        lines.add("Gesamt monatlich: ${"%.2f".format(total)}€")
        lines.add("Pro Person: ${"%.2f".format(perPerson)}€")
        lines.add("")
        lines.add("---")
        lines.add("## Alle Fixkosten")
        lines.add("")
        costs.forEach { cost ->
            val status = if (cost.isActive) "✓" else "✗"
            lines.add("$status  ${cost.emoji} ${cost.name}  —  ${"%.2f".format(cost.totalAmount)}€/Monat")
            lines.add("   Bezahlt von: ${cost.paidBy}")
            lines.add("")
        }

        val file = createPdf(context, "💸 Fixkosten-Bericht", lines, "fixkosten_${System.currentTimeMillis()}")
        if (file != null) sharePdf(context, file)
    }

    fun exportBilanz(context: Context) {
        val bought = DataStore.shoppingItems.filter { it.status == ShoppingStatus.BOUGHT }
        val allUsers = DataStore.users
        val totalSpent = bought.sumOf { it.price }
        val fairShare = if (allUsers.isNotEmpty()) totalSpent / allUsers.size else 0.0
        val lines = mutableListOf<String>()

        lines.add("## Bilanz Übersicht")
        lines.add("Gesamtausgaben: ${"%.2f".format(totalSpent)}€")
        lines.add("Fairer Anteil: ${"%.2f".format(fairShare)}€ pro Person")
        lines.add("")
        lines.add("---")
        lines.add("## Pro Mitglied")
        lines.add("")
        allUsers.forEach { member ->
            val paid = bought.filter { it.boughtBy == member.name }.sumOf { it.price }
            val balance = paid - fairShare
            val sign = if (balance >= 0) "+" else ""
            lines.add("${member.avatarEmoji} ${member.name}")
            lines.add("   Bezahlt: ${"%.2f".format(paid)}€   Bilanz: $sign${"%.2f".format(balance)}€")
            lines.add("")
        }

        val file = createPdf(context, "📊 WG Bilanz", lines, "bilanz_${System.currentTimeMillis()}")
        if (file != null) sharePdf(context, file)
    }
}
