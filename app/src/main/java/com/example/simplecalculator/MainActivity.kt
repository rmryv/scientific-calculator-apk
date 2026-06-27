package com.example.simplecalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.simplecalculator.ui.theme.SimpleCalculatorTheme
import kotlin.math.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SimpleCalculatorTheme {
                CalculatorScreen()
            }
        }
    }
}

@Composable
fun CalculatorScreen() {
    var display by remember { mutableStateOf("0") }
    var memory by remember { mutableStateOf(0.0) }
    var isDegreeMode by remember { mutableStateOf(true) }
    var pendingValue by remember { mutableStateOf<Double?>(null) }
    var pendingOp by remember { mutableStateOf<String?>(null) }
    var clearOnNext by remember { mutableStateOf(false) }
    var preview by remember { mutableStateOf("") }

    fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        return if (abs(value - value.toLong()) < 1e-10) {
            value.toLong().toString()
        } else {
            val s = String.format("%.10f", value).trimEnd('0').trimEnd('.')
            if (s.length > 12) {
                String.format("%.6e", value)
            } else s
        }
    }

    fun currentValue(): Double = display.toDoubleOrNull() ?: 0.0

    fun setDisplay(value: Double) {
        display = formatResult(value)
        clearOnNext = true
    }

    fun appendDigit(d: String) {
        if (clearOnNext) {
            display = if (d == ".") "0." else d
            clearOnNext = false
        } else {
            if (display == "0" && d != ".") display = d
            else if (d == "." && display.contains(".")) return
            else display = display + d
        }
        preview = ""
    }

    fun backspace() {
        if (clearOnNext || display == "Error") {
            display = "0"
            clearOnNext = false
            return
        }
        display = if (display.length <= 1) "0" else display.dropLast(1)
    }

    fun clearAll() {
        display = "0"
        pendingValue = null
        pendingOp = null
        clearOnNext = false
        preview = ""
    }

    fun toggleSign() {
        val num = currentValue()
        setDisplay(-num)
    }

    fun toAngleUnit(rad: Double): Double = if (isDegreeMode) Math.toDegrees(rad) else rad
    fun fromAngleUnit(degOrRad: Double): Double = if (isDegreeMode) Math.toRadians(degOrRad) else degOrRad

    fun applyUnary(op: String) {
        var num = currentValue()
        var result = num
        try {
            result = when (op) {
                "sin" -> sin(fromAngleUnit(num))
                "cos" -> cos(fromAngleUnit(num))
                "tan" -> tan(fromAngleUnit(num))
                "asin" -> toAngleUnit(asin(num))
                "acos" -> toAngleUnit(acos(num))
                "atan" -> toAngleUnit(atan(num))
                "log" -> log10(num)
                "ln" -> ln(num)
                "sqrt" -> sqrt(num)
                "x²" -> num * num
                "1/x" -> if (num == 0.0) throw ArithmeticException() else 1.0 / num
                "%" -> num / 100.0
                "abs" -> abs(num)
                "e^x" -> exp(num)
                "10^x" -> 10.0.pow(num)
                "n!" -> {
                    val n = num.toInt()
                    if (n < 0 || num != n.toDouble()) throw ArithmeticException()
                    var f = 1.0
                    for (i in 2..n) f *= i
                    f
                }
                "π" -> PI
                "e" -> E
                else -> num
            }
            setDisplay(result)
        } catch (e: Exception) {
            display = "Error"
            clearOnNext = true
            pendingValue = null
            pendingOp = null
        }
    }

    fun performBinary(a: Double, op: String, b: Double): Double {
        return when (op) {
            "+" -> a + b
            "-" -> a - b
            "*" -> a * b
            "/" -> if (b == 0.0) Double.NaN else a / b
            "^" -> a.pow(b)
            else -> b
        }
    }

    fun chooseOperator(op: String) {
        val curr = currentValue()

        if (pendingOp != null && pendingValue != null && !clearOnNext) {
            val res = performBinary(pendingValue!!, pendingOp!!, curr)
            if (res.isNaN() || res.isInfinite()) {
                display = "Error"
                pendingValue = null
                pendingOp = null
                clearOnNext = true
                preview = ""
                return
            }
            display = formatResult(res)
            pendingValue = res
        } else {
            pendingValue = curr
        }

        pendingOp = op
        clearOnNext = true
        preview = "${formatResult(pendingValue!!)} ${opSymbol(op)}"
    }

    fun opSymbol(op: String): String = when (op) {
        "+" -> "+"; "-" -> "−"; "*" -> "×"; "/" -> "÷"; "^" -> "^"; else -> op
    }

    fun calculateResult() {
        if (pendingOp == null || pendingValue == null) return
        val curr = currentValue()
        val res = performBinary(pendingValue!!, pendingOp!!, curr)
        if (res.isNaN() || res.isInfinite()) {
            display = "Error"
        } else {
            display = formatResult(res)
        }
        pendingValue = null
        pendingOp = null
        clearOnNext = true
        preview = ""
    }

    fun memoryClear() { memory = 0.0 }
    fun memoryRecall() {
        setDisplay(memory)
    }
    fun memoryAdd() {
        memory += currentValue()
    }
    fun memorySubtract() {
        memory -= currentValue()
    }

    fun insertConstant(value: Double) {
        setDisplay(value)
    }

    // Button styling helpers
    val numButtonColor = Color(0xFF333333)
    val opButtonColor = Color(0xFFFF8C00)
    val sciButtonColor = Color(0xFF2E7D32)
    val memButtonColor = Color(0xFF455A64)
    val clearColor = Color(0xFFF44336)
    val equalsColor = Color(0xFF4CAF50)

    val smallButtonModifier = Modifier
        .weight(1f)
        .padding(horizontal = 3.dp, vertical = 4.dp)
        .height(46.dp)

    val funcButtonModifier = Modifier
        .weight(1f)
        .padding(horizontal = 3.dp, vertical = 4.dp)
        .height(52.dp)

    val mainButtonModifier = Modifier
        .weight(1f)
        .padding(5.dp)
        .height(64.dp)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF121212)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .windowInsetsPadding(WindowInsets.systemBars),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0D0D0D))
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    if (preview.isNotEmpty()) {
                        Text(
                            text = preview,
                            color = Color(0xFF888888),
                            fontSize = 16.sp,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = display,
                        color = Color.White,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Light,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Mode + Memory + DEL row
            Row(Modifier.fillMaxWidth()) {
                // DEG/RAD toggle
                CalculatorButton(
                    text = if (isDegreeMode) "DEG" else "RAD",
                    color = if (isDegreeMode) Color(0xFF1565C0) else Color(0xFF7B1FA2),
                    modifier = smallButtonModifier,
                    fontSize = 13.sp,
                    onClick = { isDegreeMode = !isDegreeMode }
                )
                CalculatorButton(text = "MC", color = memButtonColor, modifier = smallButtonModifier, fontSize = 13.sp, onClick = { memoryClear() })
                CalculatorButton(text = "MR", color = memButtonColor, modifier = smallButtonModifier, fontSize = 13.sp, onClick = { memoryRecall() })
                CalculatorButton(text = "M+", color = memButtonColor, modifier = smallButtonModifier, fontSize = 13.sp, onClick = { memoryAdd() })
                CalculatorButton(text = "M−", color = memButtonColor, modifier = smallButtonModifier, fontSize = 13.sp, onClick = { memorySubtract() })
                CalculatorButton(text = "⌫", color = Color(0xFF616161), modifier = smallButtonModifier, fontSize = 16.sp, onClick = { backspace() })
            }

            Spacer(Modifier.height(6.dp))

            // Scientific functions - 5 columns
            Column {
                Row(Modifier.fillMaxWidth()) {
                    CalculatorButton(text = "sin", color = sciButtonColor, modifier = funcButtonModifier, fontSize = 15.sp) { applyUnary("sin") }
                    CalculatorButton(text = "cos", color = sciButtonColor, modifier = funcButtonModifier, fontSize = 15.sp) { applyUnary("cos") }
                    CalculatorButton(text = "tan", color = sciButtonColor, modifier = funcButtonModifier, fontSize = 15.sp) { applyUnary("tan") }
                    CalculatorButton(text = "log", color = sciButtonColor, modifier = funcButtonModifier, fontSize = 15.sp) { applyUnary("log") }
                    CalculatorButton(text = "ln", color = sciButtonColor, modifier = funcButtonModifier, fontSize = 15.sp) { applyUnary("ln") }
                }
                Row(Modifier.fillMaxWidth()) {
                    CalculatorButton(text = "√", color = sciButtonColor, modifier = funcButtonModifier, fontSize = 16.sp) { applyUnary("sqrt") }
                    CalculatorButton(text = "x²", color = sciButtonColor, modifier = funcButtonModifier, fontSize = 15.sp) { applyUnary("x²") }
                    CalculatorButton(text = "x^y", color = opButtonColor, modifier = funcButtonModifier, fontSize = 15.sp) { chooseOperator("^") }
                    CalculatorButton(text = "1/x", color = sciButtonColor, modifier = funcButtonModifier, fontSize = 15.sp) { applyUnary("1/x") }
                    CalculatorButton(text = "%", color = opButtonColor, modifier = funcButtonModifier, fontSize = 16.sp) { applyUnary("%") }
                }
                Row(Modifier.fillMaxWidth()) {
                    CalculatorButton(text = "π", color = sciButtonColor, modifier = funcButtonModifier, fontSize = 16.sp) { insertConstant(PI) }
                    CalculatorButton(text = "e", color = sciButtonColor, modifier = funcButtonModifier, fontSize = 16.sp) { insertConstant(E) }
                    CalculatorButton(text = "eˣ", color = sciButtonColor, modifier = funcButtonModifier, fontSize = 15.sp) { applyUnary("e^x") }
                    CalculatorButton(text = "10ˣ", color = sciButtonColor, modifier = funcButtonModifier, fontSize = 15.sp) { applyUnary("10^x") }
                    CalculatorButton(text = "n!", color = sciButtonColor, modifier = funcButtonModifier, fontSize = 15.sp) { applyUnary("n!") }
                }
                Row(Modifier.fillMaxWidth()) {
                    CalculatorButton(text = "asin", color = sciButtonColor, modifier = funcButtonModifier, fontSize = 14.sp) { applyUnary("asin") }
                    CalculatorButton(text = "acos", color = sciButtonColor, modifier = funcButtonModifier, fontSize = 14.sp) { applyUnary("acos") }
                    CalculatorButton(text = "atan", color = sciButtonColor, modifier = funcButtonModifier, fontSize = 14.sp) { applyUnary("atan") }
                    CalculatorButton(text = "|x|", color = sciButtonColor, modifier = funcButtonModifier, fontSize = 15.sp) { applyUnary("abs") }
                    CalculatorButton(text = "±", color = numButtonColor, modifier = funcButtonModifier, fontSize = 16.sp) { toggleSign() }
                }
            }

            Spacer(Modifier.height(6.dp))

            // Main keypad
            Column {
                Row(Modifier.fillMaxWidth()) {
                    CalculatorButton(text = "C", color = clearColor, modifier = mainButtonModifier) { clearAll() }
                    CalculatorButton(text = "÷", color = opButtonColor, modifier = mainButtonModifier) { chooseOperator("/") }
                    CalculatorButton(text = "×", color = opButtonColor, modifier = mainButtonModifier) { chooseOperator("*") }
                    CalculatorButton(text = "−", color = opButtonColor, modifier = mainButtonModifier) { chooseOperator("-") }
                }
                Row(Modifier.fillMaxWidth()) {
                    CalculatorButton(text = "7", color = numButtonColor, modifier = mainButtonModifier) { appendDigit("7") }
                    CalculatorButton(text = "8", color = numButtonColor, modifier = mainButtonModifier) { appendDigit("8") }
                    CalculatorButton(text = "9", color = numButtonColor, modifier = mainButtonModifier) { appendDigit("9") }
                    CalculatorButton(text = "+", color = opButtonColor, modifier = mainButtonModifier) { chooseOperator("+") }
                }
                Row(Modifier.fillMaxWidth()) {
                    CalculatorButton(text = "4", color = numButtonColor, modifier = mainButtonModifier) { appendDigit("4") }
                    CalculatorButton(text = "5", color = numButtonColor, modifier = mainButtonModifier) { appendDigit("5") }
                    CalculatorButton(text = "6", color = numButtonColor, modifier = mainButtonModifier) { appendDigit("6") }
                    CalculatorButton(text = "^", color = opButtonColor, modifier = mainButtonModifier) { chooseOperator("^") }
                }
                Row(Modifier.fillMaxWidth()) {
                    CalculatorButton(text = "1", color = numButtonColor, modifier = mainButtonModifier) { appendDigit("1") }
                    CalculatorButton(text = "2", color = numButtonColor, modifier = mainButtonModifier) { appendDigit("2") }
                    CalculatorButton(text = "3", color = numButtonColor, modifier = mainButtonModifier) { appendDigit("3") }
                    CalculatorButton(text = ".", color = numButtonColor, modifier = mainButtonModifier) { appendDigit(".") }
                }
                Row(Modifier.fillMaxWidth()) {
                    // 0 spans two
                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .padding(5.dp)
                            .height(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(numButtonColor)
                            .clickable { appendDigit("0") },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("0", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Medium)
                    }
                    CalculatorButton(text = "=", color = equalsColor, modifier = mainButtonModifier) { calculateResult() }
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF333333),
    fontSize: androidx.compose.ui.unit.TextUnit = 22.sp,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = fontSize,
            fontWeight = FontWeight.Medium
        )
    }
}