package com.example.stockmate.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.stockmate.data.local.entity.SaleEntity
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SalesReportLine(
    val productName: String,
    val sku: String,
    val quantity: Int,
    val unitSellingPrice: Long,
    val unitCost: Long,
    val lineTotal: Long,
    val costTotal: Long,
    val grossProfit: Long
)

data class SalesReportEntry(
    val sale: SaleEntity,
    val items: List<SalesReportLine>
)

object SalesPdfGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f
    private val orange = android.graphics.Color.rgb(239, 139, 45)
    private val charcoal = android.graphics.Color.rgb(43, 43, 48)
    private val muted = android.graphics.Color.rgb(120, 120, 125)
    private val lightGrey = android.graphics.Color.rgb(240, 240, 242)
    private val white = android.graphics.Color.WHITE

    fun generateSalesReport(
        context: Context,
        entries: List<SalesReportEntry>,
        periodLabel: String
    ): Uri {
        val directory = File(context.cacheDir, "reports").apply {
            if (!exists() && !mkdirs()) {
                throw IllegalStateException("Unable to create the reports directory.")
            }
        }
        val file = File(directory, "StockMate_Sales_Report_${System.currentTimeMillis()}.pdf")

        val completed = entries
            .filter { it.sale.status == SaleEntity.STATUS_COMPLETED }
            .sortedBy { it.sale.saleDate }

        val totalRevenue = completed.sumOf { it.sale.totalAmount }
        val totalCost = completed.sumOf { it.sale.totalCost }
        val totalProfit = completed.sumOf { it.sale.grossProfit }
        val totalUnits = completed.sumOf { entry -> entry.items.sumOf { it.quantity.toLong() } }

        val document = PdfDocument()
        val titlePaint = paint(22f, charcoal, true)
        val subtitlePaint = paint(9f, muted)
        val sectionPaint = paint(13f, charcoal, true)
        val bodyPaint = paint(8f, charcoal)
        val smallPaint = paint(7f, muted)
        val boldPaint = paint(8f, charcoal, true)
        val headerPaint = paint(7f, white, true)
        val headerBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = charcoal }
        val divider = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = lightGrey
            strokeWidth = 1f
        }

        var pageNumber = 1
        var page = document.startPage(pageInfo(pageNumber))
        var canvas = page.canvas
        var y = 0f

        fun drawHeader() {
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 9f,
                Paint(Paint.ANTI_ALIAS_FLAG).apply { color = orange })
            canvas.drawText("STOCKMATE", MARGIN, 43f, paint(10f, orange, true))
            canvas.drawText("Sales Report", MARGIN, 77f, titlePaint)
            canvas.drawText("Period: $periodLabel", MARGIN, 98f, subtitlePaint)
            canvas.drawText(
                "Generated ${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())}",
                MARGIN, 114f, subtitlePaint
            )
            canvas.drawText("Page $pageNumber", PAGE_WIDTH - MARGIN - 45f, 43f, subtitlePaint)
        }

        fun finishAndStartPage() {
            document.finishPage(page)
            pageNumber++
            page = document.startPage(pageInfo(pageNumber))
            canvas = page.canvas
            drawHeader()
            y = 145f
        }

        fun ensureSpace(height: Float) {
            if (y + height > PAGE_HEIGHT - 65f) finishAndStartPage()
        }

        fun drawTransactionHeader() {
            ensureSpace(35f)
            canvas.drawText("Sale #${completed.getOrNull(0)?.sale?.saleId ?: ""}", MARGIN, y, sectionPaint)
            y += 17f
            canvas.drawRoundRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 24f, 5f, 5f, headerBg)
            canvas.drawText("PRODUCT / SKU", MARGIN + 7f, y + 15f, headerPaint)
            canvas.drawText("QTY", MARGIN + 205f, y + 15f, headerPaint)
            canvas.drawText("UNIT PRICE", MARGIN + 245f, y + 15f, headerPaint)
            canvas.drawText("LINE TOTAL", MARGIN + 335f, y + 15f, headerPaint)
            canvas.drawText("PROFIT", MARGIN + 430f, y + 15f, headerPaint)
            y += 32f
        }

        try {
            drawHeader()
            y = 145f

            // Summary panel
            canvas.drawRoundRect(
                MARGIN, y, PAGE_WIDTH - MARGIN, y + 104f, 9f, 9f,
                Paint(Paint.ANTI_ALIAS_FLAG).apply { color = lightGrey }
            )
            canvas.drawText("TOTAL REVENUE", MARGIN + 14f, y + 23f, smallPaint)
            canvas.drawText(formatLkr(totalRevenue), MARGIN + 14f, y + 45f, boldPaint)
            canvas.drawText("TOTAL COST", MARGIN + 190f, y + 23f, smallPaint)
            canvas.drawText(formatLkr(totalCost), MARGIN + 190f, y + 45f, boldPaint)
            canvas.drawText("GROSS PROFIT", MARGIN + 355f, y + 23f, smallPaint)
            canvas.drawText(formatLkr(totalProfit), MARGIN + 355f, y + 45f, boldPaint)
            canvas.drawText("TRANSACTIONS: ${completed.size}", MARGIN + 14f, y + 75f, smallPaint)
            canvas.drawText("UNITS SOLD: $totalUnits", MARGIN + 190f, y + 75f, smallPaint)
            y += 127f

            canvas.drawText("Payment Method Breakdown", MARGIN, y, sectionPaint)
            y += 18f
            val paymentMethods = listOf(
                SaleEntity.PAYMENT_CASH to "Cash",
                SaleEntity.PAYMENT_CARD to "Card",
                SaleEntity.PAYMENT_BANK_TRANSFER to "Bank Transfer",
                SaleEntity.PAYMENT_OTHER to "Other"
            )
            paymentMethods.forEach { (method, label) ->
                val amount = completed.filter { it.sale.paymentMethod == method }
                    .sumOf { it.sale.totalAmount }
                canvas.drawText(label, MARGIN + 4f, y, bodyPaint)
                canvas.drawText(formatLkr(amount), MARGIN + 180f, y, boldPaint)
                y += 15f
            }

            y += 12f
            canvas.drawText("Detailed Sales Transactions", MARGIN, y, sectionPaint)
            y += 18f

            if (completed.isEmpty()) {
                canvas.drawText("No completed sales found for this reporting period.", MARGIN, y + 12f, bodyPaint)
            } else {
                completed.forEach { entry ->
                    ensureSpace(110f)
                    canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, divider)
                    y += 15f
                    val dateText = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                        .format(Date(entry.sale.saleDate))
                    canvas.drawText("Sale #${entry.sale.saleId}  •  $dateText", MARGIN, y, boldPaint)
                    y += 14f
                    canvas.drawText("Customer: ${entry.sale.customerName}", MARGIN, y, bodyPaint)
                    canvas.drawText("Payment: ${paymentLabel(entry.sale.paymentMethod)}", MARGIN + 270f, y, bodyPaint)
                    y += 13f

                    canvas.drawRoundRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 20f, 4f, 4f, headerBg)
                    canvas.drawText("PRODUCT / SKU", MARGIN + 6f, y + 13f, headerPaint)
                    canvas.drawText("QTY", MARGIN + 205f, y + 13f, headerPaint)
                    canvas.drawText("UNIT PRICE", MARGIN + 245f, y + 13f, headerPaint)
                    canvas.drawText("LINE TOTAL", MARGIN + 335f, y + 13f, headerPaint)
                    canvas.drawText("PROFIT", MARGIN + 430f, y + 13f, headerPaint)
                    y += 26f

                    entry.items.forEach { item ->
                        ensureSpace(30f)
                        canvas.drawText(ellipsize("${item.productName} (${item.sku})", bodyPaint, 155f), MARGIN + 6f, y + 10f, bodyPaint)
                        canvas.drawText(item.quantity.toString(), MARGIN + 205f, y + 10f, bodyPaint)
                        canvas.drawText(formatLkr(item.unitSellingPrice), MARGIN + 245f, y + 10f, bodyPaint)
                        canvas.drawText(formatLkr(item.lineTotal), MARGIN + 335f, y + 10f, bodyPaint)
                        canvas.drawText(formatLkr(item.grossProfit), MARGIN + 430f, y + 10f, bodyPaint)
                        canvas.drawLine(MARGIN, y + 17f, PAGE_WIDTH - MARGIN, y + 17f, divider)
                        y += 22f
                    }

                    y += 5f
                    canvas.drawText("Cost: ${formatLkr(entry.sale.totalCost)}", MARGIN + 6f, y, smallPaint)
                    canvas.drawText("Revenue: ${formatLkr(entry.sale.totalAmount)}", MARGIN + 170f, y, boldPaint)
                    canvas.drawText("Profit: ${formatLkr(entry.sale.grossProfit)}", MARGIN + 355f, y, boldPaint)
                    y += 25f
                }
            }

            // Footer on final page
            canvas.drawLine(MARGIN, PAGE_HEIGHT - 48f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - 48f, divider)
            canvas.drawText("StockMate • Manage smarter. Stock better.", MARGIN, PAGE_HEIGHT - 30f, smallPaint)
            canvas.drawText("Page $pageNumber", PAGE_WIDTH - MARGIN - 45f, PAGE_HEIGHT - 30f, smallPaint)
            document.finishPage(page)

            FileOutputStream(file).use { document.writeTo(it) }
        } finally {
            document.close()
        }

        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    private fun pageInfo(number: Int) =
        PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, number).create()

    private fun paint(size: Float, color: Int, bold: Boolean = false) =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            this.color = color
            typeface = Typeface.create(
                Typeface.DEFAULT,
                if (bold) Typeface.BOLD else Typeface.NORMAL
            )
        }

    private fun ellipsize(value: String, paint: Paint, width: Float): String {
        if (paint.measureText(value) <= width) return value
        var result = value
        while (result.isNotEmpty() && paint.measureText("$result…") > width) {
            result = result.dropLast(1)
        }
        return "$result…"
    }

    private fun formatLkr(cents: Long): String {
        val formatter = NumberFormat.getNumberInstance(Locale("en", "LK")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return "LKR ${formatter.format(cents / 100.0)}"
    }

    private fun paymentLabel(method: String): String = when (method) {
        SaleEntity.PAYMENT_CASH -> "Cash"
        SaleEntity.PAYMENT_CARD -> "Card"
        SaleEntity.PAYMENT_BANK_TRANSFER -> "Bank Transfer"
        else -> "Other"
    }
}
