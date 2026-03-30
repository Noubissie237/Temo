package com.propentatech.kumbaka.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.propentatech.kumbaka.ui.components.PremiumTopAppBar
import com.propentatech.kumbaka.ui.viewmodel.CalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel = viewModel(),
    onBack: () -> Unit
) {
    val expression by viewModel.expression.collectAsState()
    val result by viewModel.result.collectAsState()
    val isDegreeMode by viewModel.isDegreeMode.collectAsState()
    var isScientific by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            PremiumTopAppBar(
                title = { Text("Calculatrice Scientifique", color = Color.White, fontWeight = FontWeight.Bold) },
                onNavigateBack = onBack,
                actions = {
                    TextButton(onClick = { viewModel.toggleMode() }) {
                        Text(if (isDegreeMode) "DEG" else "RAD", color = Color(0xFFFF6B00), fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { isScientific = !isScientific }) {
                        Icon(
                            if (isScientific) Icons.Default.Calculate else Icons.Default.Functions,
                            contentDescription = "Mode",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        containerColor = Color(0xFF0A0A0A)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            // Area Display - Plus grand et flexible
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (isScientific) 0.35f else 0.5f)
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = expression.ifEmpty { "0" },
                        fontSize = if (expression.length > 20) 18.sp else 24.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.End,
                        maxLines = 2,
                        lineHeight = 26.sp,
                        fontWeight = FontWeight.Light
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result,
                        fontSize = if (result.length > 10) 32.sp else 42.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        lineHeight = 44.sp
                    )
                }
            }

            // Clavier - Ajusté pour éviter de tout masquer
            Column(
                modifier = Modifier.fillMaxWidth().weight(if (isScientific) 0.65f else 0.5f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isScientific) {
                    ScientificPanel(viewModel)
                }

                // Standard Keypad
                StandardPanel(viewModel)
            }
        }
    }
}

@Composable
fun ScientificPanel(viewModel: CalculatorViewModel) {
    val items = listOf(
        "sin", "cos", "tan", "log",
        "ln", "√", "^", "!",
        "π", "e", "(", ")"
    )
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth().height(130.dp)
    ) {
        items(items) { func ->
            Button(
                onClick = { 
                    when(func) {
                        "π", "e" -> viewModel.onConstant(func)
                        "(", ")" -> viewModel.onDigit(func)
                        "!", "^" -> viewModel.onOperator(func)
                        else -> viewModel.onScientificFunction(func)
                    }
                },
                modifier = Modifier.height(38.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(func, color = Color(0xFFFF6B00), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StandardPanel(viewModel: CalculatorViewModel) {
    val rows = listOf(
        listOf("C", "÷", "×", "DEL"),
        listOf("7", "8", "9", "-"),
        listOf("4", "5", "6", "+"),
        listOf("1", "2", "3", "="),
        listOf("0", ".", "%")
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEachIndexed { rowIndex, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { label ->
                    val weight = if (label == "0") 2f else 1f
                    CalcKey(
                        label = label,
                        modifier = Modifier.weight(weight).height(if (row.size == 3) 50.dp else 60.dp),
                        onClick = {
                            when (label) {
                                "C" -> viewModel.onClear()
                                "DEL" -> viewModel.onDelete()
                                "=", "ans" -> viewModel.onEqual()
                                "÷" -> viewModel.onOperator("÷")
                                "×" -> viewModel.onOperator("×")
                                "+", "-", "%" -> viewModel.onOperator(label)
                                else -> viewModel.onDigit(label)
                            }
                        },
                        color = when {
                            label == "C" -> Color(0xFFE53935).copy(alpha = 0.8f)
                            label == "=" -> Color(0xFFFF6B00)
                            label in "÷×+-DEL" -> Color(0xFFFF6B00).copy(alpha = 0.15f)
                            else -> Color.White.copy(alpha = 0.08f)
                        },
                        textColor = when {
                            label == "C" || label == "=" -> Color.White
                            label in "÷×+-" -> Color(0xFFFF6B00)
                            else -> Color.White
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CalcKey(
    label: String,
    modifier: Modifier = Modifier,
    color: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (label == "DEL") {
            Icon(Icons.Default.Backspace, contentDescription = null, tint = Color(0xFFFF6B00), modifier = Modifier.size(20.dp))
        } else {
            Text(
                text = label,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
