package com.propentatech.kumbaka.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import net.objecthunter.exp4j.ExpressionBuilder
import java.util.*

class CalculatorViewModel : ViewModel() {
    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression

    private val _result = MutableStateFlow("")
    val result: StateFlow<String> = _result

    private val _history = MutableStateFlow<List<String>>(emptyList())
    val history: StateFlow<List<String>> = _history

    private val _isDegreeMode = MutableStateFlow(true)
    val isDegreeMode: StateFlow<Boolean> = _isDegreeMode

    fun toggleMode() {
        _isDegreeMode.value = !_isDegreeMode.value
        evaluateRealTime()
    }

    fun onDigit(digit: String) {
        if (_expression.value == "0") _expression.value = ""
        _expression.value += digit
        evaluateRealTime()
    }

    fun onScientificFunction(func: String) {
        val lastInput = _expression.value.lastOrNull()
        if (lastInput != null && (lastInput.isDigit() || lastInput == ')')) {
            _expression.value += "×"
        }
        _expression.value += "$func("
        evaluateRealTime()
    }

    fun onConstant(constant: String) {
        val lastInput = _expression.value.lastOrNull()
        if (lastInput != null && (lastInput.isDigit() || lastInput == ')')) {
            _expression.value += "×"
        }
        _expression.value += constant
        evaluateRealTime()
    }

    fun onOperator(operator: String) {
        if (_expression.value.isNotEmpty()) {
            val last = _expression.value.last()
            if (last in "+-*/%^") {
                _expression.value = _expression.value.dropLast(1) + operator
            } else {
                _expression.value += operator
            }
        } else if (operator == "-") {
            _expression.value = "-"
        }
    }

    fun onClear() {
        _expression.value = ""
        _result.value = ""
    }

    fun onDelete() {
        if (_expression.value.isNotEmpty()) {
            _expression.value = _expression.value.dropLast(1)
            evaluateRealTime()
        }
    }

    fun onEqual() {
        if (_expression.value.isNotEmpty()) {
            try {
                val eval = evaluateExpression(_expression.value)
                val finalResult = formatResult(eval)
                _history.value = (_history.value + ("${_expression.value} = $finalResult")).takeLast(10)
                _expression.value = finalResult
                _result.value = ""
            } catch (e: Exception) {
                _result.value = "Erreur"
            }
        }
    }

    private fun evaluateRealTime() {
        if (_expression.value.isEmpty()) {
            _result.value = ""
            return
        }
        try {
            val eval = evaluateExpression(_expression.value)
            _result.value = formatResult(eval)
        } catch (e: Exception) {
            _result.value = ""
        }
    }

    private fun evaluateExpression(expr: String): Double {
        val sanitized = expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("π", "PI")
            .replace("e", "E")
            .replace("√", "sqrt")

        val builder = ExpressionBuilder(sanitized)
            .functions(
                listOf(
                    // Custom functions for Deg/Rad if needed, but exp4j uses Rad.
                    // We will wrap the input if in Degree mode.
                )
            )

        // Pre-process for Deg/Rad
        var finalExpr = sanitized
        if (_isDegreeMode.value) {
            finalExpr = finalExpr
                .replace("sin\\(([^)]+)\\)".toRegex(), "sin(0.01745329251 * ($1))")
                .replace("cos\\(([^)]+)\\)".toRegex(), "cos(0.01745329251 * ($1))")
                .replace("tan\\(([^)]+)\\)".toRegex(), "tan(0.01745329251 * ($1))")
        }

        return ExpressionBuilder(finalExpr).build().evaluate()
    }

    private fun formatResult(value: Double): String {
        return if (value == Math.floor(value)) {
            value.toLong().toString()
        } else {
            String.format(Locale.US, "%.4f", value).trimEnd('0').trimEnd('.')
        }
    }
}
