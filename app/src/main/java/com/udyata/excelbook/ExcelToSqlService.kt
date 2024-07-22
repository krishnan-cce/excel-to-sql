package com.udyata.excelbook

import android.content.Context
import android.net.Uri
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.InputStream
import java.util.Locale

class ExcelToSqlService(private val context: Context) {

    fun getColumnHeadersAndTypes(uri: Uri): Pair<List<String>, List<String>> {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val workbook = XSSFWorkbook(inputStream)
        val sheet = workbook.getSheetAt(0)
        val headerRow = sheet.getRow(0)
        val columns = headerRow.map { it.stringCellValue }
        val sampleRow = sheet.getRow(1)
        val inferredTypes = sampleRow.map { inferColumnType(it) }
        return columns to inferredTypes
    }

    private fun inferColumnType(cell: Cell): String {
        return when (cell.cellType) {
            CellType.NUMERIC -> if (cell.numericCellValue % 1 == 0.0) "int" else "double"
            CellType.STRING -> "varchar"
            else -> "varchar"
        }
    }

    fun readExcelAndGenerateSql(uri: Uri, tableName: String, columnMappings: Map<String, String>): String {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val workbook = XSSFWorkbook(inputStream)
        val sheet = workbook.getSheetAt(0)
        val headerRow = sheet.getRow(0)

        val columns = headerRow.map { escapeSqlIdentifier(it.stringCellValue) }
        val dataRows = sheet.drop(1).map { row ->
            row.mapIndexed { index, cell ->
                try {
                    when (columnMappings[columns[index]]?.lowercase(Locale.ROOT)) {
                        "int" -> cell.numericCellValue.toInt().toString()
                        "double" -> cell.numericCellValue.toString()
                        "varchar" -> "'${escapeSqlValue(cell.stringCellValue)}'"
                        else -> "'${escapeSqlValue(cell.stringCellValue)}'"
                    }
                } catch (e: Exception) {
                    "'${escapeSqlValue(cell.stringCellValue)}'"
                }
            }
        }

        val sql = StringBuilder("INSERT INTO ${escapeSqlIdentifier(tableName)} (${columns.joinToString(",")}) VALUES ")
        dataRows.forEachIndexed { index, row ->
            sql.append("(${row.joinToString(",")})")
            if (index != dataRows.size - 1) {
                sql.append(",")
            }
        }
        sql.append(";")
        return sql.toString()
    }

    private fun escapeSqlIdentifier(identifier: String): String {
        return identifier.replace("'", "''")
    }

    private fun escapeSqlValue(value: String): String {
        return value.replace("'", "''")
    }
}
