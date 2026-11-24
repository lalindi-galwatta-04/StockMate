package com.example.stockmate.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.stockmate.data.local.relation.ProductWithCategory
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Creates a printable inventory PDF in the app's private cache directory.
 *
 * Monetary values in ProductEntity are stored in cents, so all displayed
 * LKR values are converted from cents to rupees.
 */
object InventoryPdfGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f
    private const val CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2)

    private val orange = android.graphics.Color.rgb(239, 139, 45)
    private val charcoal = android.graphics.Color.rgb(43, 43, 48)
    private val muted = android.graphics.Color.rgb(120, 120, 125)
    private val lightGrey = android.graphics.Color.rgb(240, 240, 242)
    private val white = android.graphics.Color.WHITE

    /**
     * Generates a complete inventory report and returns its shareable content URI.
     */
    fun generateInventoryReport(
        context: Context,
        products: List<ProductWithCategory>
    ): Uri {
        val reportDirectory = File(context.cacheDir, "reports").apply {
            if (!exists() && !mkdirs()) {
                throw IllegalStateException("Unable to create the reports directory.")
            }
        }

        val reportFile = File(
            reportDirectory,
            "StockMate_Inventory_Report_${System.currentTimeMillis()}.pdf"
        )

        val activeProducts = products
            .filter { it.product.isActive }
            .sortedBy { it.product.productName.lowercase(Locale.getDefault()) }

        val document = PdfDocument()
        val titlePaint = textPaint(22f, charcoal, true)
        val subtitlePaint = textPaint(10f, muted)
        val sectionPaint = textPaint(13f, charcoal, true)
        val bodyPaint = textPaint(9f, charcoal)
        val smallPaint = textPaint(8f, muted)
        val headerPaint = textPaint(8f, white, true)
        val valuePaint = textPaint(10f, charcoal, true)
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = lightGrey
            strokeWidth = 1f
        }
        val orangePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = orange
        }
        val headerBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = charcoal
        }

        val totalUnits = activeProducts.sumOf { it.product.quantityInStock.toLong() }
        val totalCostValue = activeProducts.sumOf {
            it.product.purchasePrice * it.product.quantityInStock.toLong()
        }
        val totalSellingValue = activeProducts.sumOf {
            it.product.sellingPrice * it.product.quantityInStock.toLong()
        }

        var pageNumber = 1
        var page = document.startPage(
            PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        )
        var canvas = page.canvas
        var y = 0f

        fun startPage(newPageNumber: Int) {
            document.finishPage(page)
            pageNumber = newPageNumber
            page = document.startPage(
                PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            )
            canvas = page.canvas
            y = 0f
            drawPageHeader(canvas, titlePaint, subtitlePaint, newPageNumber)
            y = 145f
            drawTableHeader(canvas, y, headerBackgroundPaint, headerPaint)
            y += 30f
        }

        fun drawProductRow(item: ProductWithCategory) {
            val product = item.product
            val rowHeight = 35f

            if (y + rowHeight > PAGE_HEIGHT - 65f) {
                startPage(pageNumber + 1)
            }

            canvas.drawLine(MARGIN, y + rowHeight, PAGE_WIDTH - MARGIN, y + rowHeight, linePaint)

            drawCell(canvas, product.productName, MARGIN + 8f, y + 13f, bodyPaint, 125f)
            drawCell(canvas, product.sku, MARGIN + 140f, y + 13f, bodyPaint, 65f)
            drawCell(
                canvas,
                item.category?.name ?: "Uncategorised",
                MARGIN + 210f,
                y + 13f,
                bodyPaint,
                90f
            )
            drawCell(
                canvas,
                product.quantityInStock.toString(),
                MARGIN + 305f,
                y + 13f,
                bodyPaint,
                35f
            )
            drawCell(
                canvas,
                formatLkr(product.purchasePrice),
                MARGIN + 345f,
                y + 13f,
                bodyPaint,
                70f
            )
            drawCell(
                canvas,
                formatLkr(product.purchasePrice * product.quantityInStock.toLong()),
                MARGIN + 420f,
                y + 13f,
                bodyPaint,
                90f
            )
            y += rowHeight
        }

        try {
            drawPageHeader(canvas, titlePaint, subtitlePaint, pageNumber)
            y = 145f

            // Summary cards
            canvas.drawRoundRect(
                MARGIN,
                y,
                PAGE_WIDTH - MARGIN,
                y + 83f,
                9f,
                9f,
                Paint(Paint.ANTI_ALIAS_FLAG).apply { color = lightGrey }
            )
            canvas.drawText("ACTIVE PRODUCTS", MARGIN + 14f, y + 22f, smallPaint)
            canvas.drawText(activeProducts.size.toString(), MARGIN + 14f, y + 45f, valuePaint)
            canvas.drawText("TOTAL UNITS", MARGIN + 180f, y + 22f, smallPaint)
            canvas.drawText(totalUnits.toString(), MARGIN + 180f, y + 45f, valuePaint)
            canvas.drawText("INVENTORY COST VALUE", MARGIN + 315f, y + 22f, smallPaint)
            canvas.drawText(formatLkr(totalCostValue), MARGIN + 315f, y + 45f, valuePaint)
            canvas.drawText(
                "Potential selling value: ${formatLkr(totalSellingValue)}",
                MARGIN + 14f,
                y + 68f,
                smallPaint
            )

            y += 108f
            canvas.drawText("Inventory Details", MARGIN, y, sectionPaint)
            y += 14f
            drawTableHeader(canvas, y, headerBackgroundPaint, headerPaint)
            y += 30f

            if (activeProducts.isEmpty()) {
                canvas.drawText(
                    "No active products are available in the inventory.",
                    MARGIN + 8f,
                    y + 15f,
                    bodyPaint
                )
            } else {
                activeProducts.forEach { drawProductRow(it) }
            }

            // Footer
            val footerPaint = textPaint(8f, muted)
            canvas.drawLine(
                MARGIN,
                PAGE_HEIGHT - 48f,
                PAGE_WIDTH - MARGIN,
                PAGE_HEIGHT - 48f,
                linePaint
            )
            canvas.drawText(
                "StockMate • Manage smarter. Stock better.",
                MARGIN,
                PAGE_HEIGHT - 30f,
                footerPaint
            )
            canvas.drawText(
                "Page $pageNumber",
                PAGE_WIDTH - MARGIN - 45f,
                PAGE_HEIGHT - 30f,
                footerPaint
            )

            document.finishPage(page)

            FileOutputStream(reportFile).use { output ->
                document.writeTo(output)
            }
        } finally {
            document.close()
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            reportFile
        )
    }

    private fun drawPageHeader(
        canvas: Canvas,
        titlePaint: Paint,
        subtitlePaint: Paint,
        pageNumber: Int
    ) {
        val orangePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = orange }
        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 9f, orangePaint)
        canvas.drawText("STOCKMATE", MARGIN, 43f, textPaint(10f, orange, true))
        canvas.drawText("Inventory Report", MARGIN, 77f, titlePaint)
        canvas.drawText(
            "Generated ${dateFormat.format(Date())}",
            MARGIN,
            98f,
            subtitlePaint
        )
        canvas.drawText(
            "Page $pageNumber",
            PAGE_WIDTH - MARGIN - 45f,
            43f,
            subtitlePaint
        )
    }

    private fun drawTableHeader(
        canvas: Canvas,
        top: Float,
        backgroundPaint: Paint,
        headerPaint: Paint
    ) {
        val bottom = top + 27f
        canvas.drawRoundRect(
            MARGIN,
            top,
            PAGE_WIDTH - MARGIN,
            bottom,
            5f,
            5f,
            backgroundPaint
        )
        val baseline = top + 17f
        canvas.drawText("PRODUCT", MARGIN + 8f, baseline, headerPaint)
        canvas.drawText("SKU", MARGIN + 140f, baseline, headerPaint)
        canvas.drawText("CATEGORY", MARGIN + 210f, baseline, headerPaint)
        canvas.drawText("QTY", MARGIN + 305f, baseline, headerPaint)
        canvas.drawText("UNIT COST", MARGIN + 345f, baseline, headerPaint)
        canvas.drawText("STOCK VALUE", MARGIN + 420f, baseline, headerPaint)
    }

    private fun drawCell(
        canvas: Canvas,
        value: String,
        x: Float,
        baseline: Float,
        paint: Paint,
        maxWidth: Float
    ) {
        val safeText = ellipsize(value, paint, maxWidth)
        canvas.drawText(safeText, x, baseline, paint)
    }

    private fun ellipsize(value: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(value) <= maxWidth) return value
        var shortened = value
        while (shortened.isNotEmpty() && paint.measureText("$shortened…") > maxWidth) {
            shortened = shortened.dropLast(1)
        }
        return "$shortened…"
    }

    private fun textPaint(
        size: Float,
        color: Int,
        bold: Boolean = false
    ) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        this.color = color
        typeface = if (bold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        else Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    private fun formatLkr(amountInCents: Long): String {
        val formatter = NumberFormat.getNumberInstance(Locale("en", "LK")).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return "LKR ${formatter.format(amountInCents / 100.0)}"
    }
}
