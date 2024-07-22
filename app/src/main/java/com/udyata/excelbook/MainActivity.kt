package com.udyata.excelbook

import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.udyata.excelbook.ui.theme.ExcelbookTheme
import androidx.lifecycle.viewmodel.compose.viewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExcelbookTheme {
                Surface {
                    ExcelReaderApp()
                }
            }
        }
    }
}

@Composable
fun ExcelReaderApp(viewModel: ExcelViewModel = viewModel()) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var tableName by remember { mutableStateOf("") }
    val columns by viewModel.columns.collectAsState()
    val columnMappings by viewModel.columnMappings.collectAsState()
    var generatedQuery by remember { mutableStateOf("") }

    val selectFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFileUri = uri
        uri?.let {
            val (columns, inferredTypes) = ExcelToSqlService(context).getColumnHeadersAndTypes(it)
            viewModel.setColumns(columns)
            viewModel.setColumnMappings(columns.zip(inferredTypes).toMap())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(onClick = { selectFileLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") }) {
            Text("Select Excel File")
        }

        OutlinedTextField(
            value = tableName,
            onValueChange = { tableName = it },
            label = { Text("Table Name") }
        )

        columns.forEach { column ->
            val type = columnMappings[column] ?: ""
            OutlinedTextField(
                value = type,
                onValueChange = {
                    viewModel.updateColumnMapping(column, it)
                },
                label = { Text("$column Type") }
            )
        }

        Button(onClick = {
            selectedFileUri?.let { uri ->
                generatedQuery = ExcelToSqlService(context).readExcelAndGenerateSql(uri, tableName, columnMappings)
            }
        }) {
            Text("Generate SQL")
        }

        Text(text = generatedQuery)

        Button(onClick = {
            clipboardManager.setText(AnnotatedString(generatedQuery))
        }) {
            Text("Copy to Clipboard")
        }
    }
}