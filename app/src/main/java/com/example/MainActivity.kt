package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("LandCalcCrash", "Uncaught Exception in ${thread.name}: ${throwable.message}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
        enableEdgeToEdge()
        setContent {
            val systemViewModel: LandCalculatorViewModel = viewModel()
            
            // Central Theme wrapper using our custom dark mode toggle state
            MyApplicationTheme(darkTheme = systemViewModel.isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = systemViewModel)
                }
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: LandCalculatorViewModel) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    
    // Theme colors
    val isDark = viewModel.isDarkTheme
    val topBarBg = if (isDark) Color(0xFF131010) else Color(0xFFFDF8F6)
    
    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(topBarBg)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ਜਮੀਨ ਮਿਣਤੀ",
                            color = if (isDark) Color(0xFFFDF8F6) else Color(0xFF1D1B20),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily.SansSerif
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Land Calculator Punjabi",
                            color = if (isDark) Color(0xFFFDF8F6).copy(alpha = 0.6f) else Color(0xFF1D1B20).copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light
                        )
                    }
                    
                    // Theme Switcher Button
                    IconButton(
                        onClick = { viewModel.isDarkTheme = !viewModel.isDarkTheme },
                        modifier = Modifier
                            .background(if (isDark) Color(0xFFFDF8F6).copy(alpha = 0.08f) else Color(0xFF1D1B20).copy(alpha = 0.04f), CircleShape)
                            .size(40.dp)
                    ) {
                        Text(
                            text = if (isDark) "☀️" else "🌙",
                            fontSize = 18.sp,
                            color = if (isDark) Color(0xFFFDF8F6) else Color(0xFF1D1B20)
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(if (isDark) Color(0xFF131010) else Color(0xFFFDF8F6))
        ) {
            
            // Tab Switcher Row
            TabSwitcherRow(
                currentTab = viewModel.currentTab,
                onTabSelected = {
                    focusManager.clearFocus()
                    viewModel.currentTab = it
                },
                isDark = isDark
            )
            
            // Content Screen with scroll
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                when (viewModel.currentTab) {
                    AppTab.RURAL_AREA -> RuralAreaCalculatorContent(viewModel = viewModel, isDark = isDark)
                    AppTab.URBAN_AREA -> UrbanAreaCalculatorContent(viewModel = viewModel, isDark = isDark)
                    AppTab.PRICE -> PriceCalculatorContent(viewModel = viewModel, isDark = isDark)
                    AppTab.DIVIDER -> DividerContent(viewModel = viewModel, isDark = isDark)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Beautiful Profile Card
                CreatorProfileCard(isDark = isDark)
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun TabSwitcherRow(
    currentTab: AppTab,
    onTabSelected: (AppTab) -> Unit,
    isDark: Boolean
) {
    val items = listOf(
        AppTab.RURAL_AREA to "ਪੇਂਡੂ ਜ਼ਮੀਨ ਮਿਣਤੀ",
        AppTab.URBAN_AREA to "ਸ਼ਹਿਰੀ ਜਗ੍ਹਾ ਮਿਣਤੀ",
        AppTab.PRICE to "ਰੇਟ ਪਤਾ ਕਰੋ",
        AppTab.DIVIDER to "ਜਮੀਨ ਵੰਡੋ"
    )
    
    val containerBg = if (isDark) Color(0xFF1A1615) else Color(0xFFF3EDF7)
    val borderCol = if (isDark) Color(0xFF2E2A29) else Color(0xFFE7E0D8)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = BorderStroke(1.dp, borderCol),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.forEach { (tab, label) ->
                val isSelected = currentTab == tab
                
                val activeBgColors = when (tab) {
                    AppTab.RURAL_AREA -> if (isDark) Color(0xFF422C25) to Color(0xFFF7E0D4) else Color(0xFFF7E0D4) to Color(0xFF2D1600)
                    AppTab.URBAN_AREA -> if (isDark) Color(0xFF421D1D) to Color(0xFFFFDADA) else Color(0xFFFFDADA) to Color(0xFF410002)
                    AppTab.PRICE -> if (isDark) Color(0xFF2D233D) to Color(0xFFE8DEF8) else Color(0xFFE8DEF8) to Color(0xFF21005D)
                    AppTab.DIVIDER -> if (isDark) Color(0xFF1D2C3D) to Color(0xFFD3E4FF) else Color(0xFFD3E4FF) to Color(0xFF001D36)
                }
                
                val bgCol by animateColorAsState(
                    targetValue = if (isSelected) activeBgColors.first else Color.Transparent,
                    label = "bg"
                )
                
                val textCol by animateColorAsState(
                    targetValue = if (isSelected) activeBgColors.second else (if (isDark) Color(0xFF88848F) else Color(0xFF79747E)),
                    label = "text"
                )
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(bgCol)
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = textCol,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ==========================================
// CUSTOM CALCULATOR INPUT CARD
// ==========================================
@Composable
fun CalculatorInputCard(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    lightBg: Color,
    darkBg: Color,
    lightLabelColor: Color,
    lightTextColor: Color,
    darkTextColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    subBadge: String? = null,
    testTag: String = ""
) {
    val bgCol = if (isDark) darkBg else lightBg
    val labelCol = if (isDark) Color(0xFF88848F) else lightLabelColor
    val textCol = if (isDark) darkTextColor else lightTextColor
    val borderCol = if (isDark) Color(0xFF2E2A29) else Color(0xFFE7E0D8)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderCol, RoundedCornerShape(24.dp))
            .testTag(testTag),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bgCol),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = label.uppercase(),
                color = labelCol,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clear button inside card
                TextField(
                    value = value,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isDigit() || it == '.' || it == ',' }
                        if (filtered.length <= 15) {
                            onValueChange(filtered)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = "0",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = textCol.copy(alpha = 0.4f)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedTextColor = textCol,
                        unfocusedTextColor = textCol
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                
                if (value.isNotEmpty()) {
                    IconButton(
                        onClick = { onValueChange("") },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "ਸਾਫ਼",
                            tint = textCol.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            if (subBadge != null && subBadge.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .background(textCol.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = subBadge,
                        color = textCol,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Custom dimension field representation
@Composable
fun DimensionInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val bg = if (isDark) Color(0xFF1E1A19) else Color(0xFFFFFFFF)
    val textCol = if (isDark) Color(0xFFFDF8F6) else Color(0xFF1D1B20)
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isDark) Color(0xFF88848F) else Color(0xFF79747E),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                val filtered = newValue.filter { it.isDigit() || it == '.' || it == ',' }
                if (filtered.length <= 15) {
                    onValueChange(filtered)
                }
            },
            placeholder = { Text("0", fontSize = 14.sp) },
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = bg,
                unfocusedContainerColor = bg,
                focusedBorderColor = if (isDark) Color(0xFFFDF8F6) else Color(0xFF2D1600),
                unfocusedBorderColor = if (isDark) Color(0xFF2E2A29) else Color(0xFFE7E0D8),
                focusedTextColor = textCol,
                unfocusedTextColor = textCol
            ),
            textStyle = LocalTextStyle.current.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        )
    }
}

// Compact row for Triangle unit selections
@Composable
fun TriangleUnitSelector(
    selectedUnit: String,
    onUnitChanged: (String) -> Unit,
    isDark: Boolean
) {
    val units = listOf(
        "feet" to "ਫੁੱਟ (Ft)",
        "meters" to "ਮੀਟਰ (M)",
        "karams" to "ਕਰਮ (K)"
    )
    
    val bgCol = if (isDark) Color(0xFF1E1A19) else Color(0xFFF3EDF7)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgCol)
            .padding(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            units.forEach { (unitId, label) ->
                val isSelected = selectedUnit == unitId
                val btnBg = if (isSelected) {
                    if (isDark) Color(0xFF3C2D54) else Color(0xFFE8DEF8)
                } else Color.Transparent
                val btnText = if (isSelected) {
                    if (isDark) Color(0xFFE8DEF8) else Color(0xFF21005D)
                } else (if (isDark) Color(0xFF88848F) else Color(0xFF79747E))
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(btnBg)
                        .clickable { onUnitChanged(unitId) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = btnText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


// ==========================================
// 1. RURAL AREA CALCULATOR CONTENT
// ==========================================
@Composable
fun RuralAreaCalculatorContent(viewModel: LandCalculatorViewModel, isDark: Boolean) {
    // 9 rural cards arranged logically in 2-column layout + 1 full-width Sq Km
    
    Column(modifier = Modifier.fillMaxWidth()) {
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CalculatorInputCard(
                label = "ਕਿੱਲਾ (Killa / Acre)",
                value = viewModel.rKilla,
                onValueChange = { viewModel.onRuralValueChange("killa", it) },
                lightBg = Color(0xFFe8f5e9),
                darkBg = Color(0x1822c55e),
                lightLabelColor = Color(0xFF2e7d32),
                lightTextColor = Color(0xFF15803d),
                darkTextColor = Color(0xFF4ade80),
                isDark = isDark,
                modifier = Modifier.weight(1f),
                testTag = "rural_killa"
            )
            CalculatorInputCard(
                label = "ਵਿੱਘਾ (Bigha)",
                value = viewModel.rVigha,
                onValueChange = { viewModel.onRuralValueChange("vigha", it) },
                lightBg = Color(0xFFfff3e0),
                darkBg = Color(0x15f97316),
                lightLabelColor = Color(0xFFef6c00),
                lightTextColor = Color(0xFFd84315),
                darkTextColor = Color(0xFFfdba74),
                isDark = isDark,
                modifier = Modifier.weight(1f),
                testTag = "rural_vigha"
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CalculatorInputCard(
                label = "ਕਨਾਲ (Kanal)",
                value = viewModel.rKanal,
                onValueChange = { viewModel.onRuralValueChange("kanal", it) },
                lightBg = Color(0xFFe0f2f1),
                darkBg = Color(0x1514b8a6),
                lightLabelColor = Color(0xFF00695c),
                lightTextColor = Color(0xFF00796b),
                darkTextColor = Color(0xFF2dd4bf),
                isDark = isDark,
                modifier = Modifier.weight(1f),
                testTag = "rural_kanal"
            )
            CalculatorInputCard(
                label = "ਮਰਲਾ (Marla)",
                value = viewModel.rMarla,
                onValueChange = { viewModel.onRuralValueChange("marla", it) },
                lightBg = Color(0xFFfffde7),
                darkBg = Color(0x15eab308),
                lightLabelColor = Color(0xFF9e9d24),
                lightTextColor = Color(0xFFa16207),
                darkTextColor = Color(0xFFfef08a),
                isDark = isDark,
                modifier = Modifier.weight(1f),
                testTag = "rural_marla"
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CalculatorInputCard(
                label = "ਗਜ (Gaj)",
                value = viewModel.rGaj,
                onValueChange = { viewModel.onRuralValueChange("gaj", it) },
                lightBg = Color(0xFFf3e5f5),
                darkBg = Color(0x15a855f7),
                lightLabelColor = Color(0xFF6a1b9a),
                lightTextColor = Color(0xFF7e22ce),
                darkTextColor = Color(0xFFe9d5ff),
                isDark = isDark,
                modifier = Modifier.weight(1f),
                testTag = "rural_gaj"
            )
            CalculatorInputCard(
                label = "ਸਕੇਅਰ ਫੁੱਟ (Ft²)",
                value = viewModel.rSqFt,
                onValueChange = { viewModel.onRuralValueChange("sqFt", it) },
                lightBg = Color(0xFFeef2ff),
                darkBg = Color(0x156366f1),
                lightLabelColor = Color(0xFF3730a3),
                lightTextColor = Color(0xFF2563eb),
                darkTextColor = Color(0xFFc7d2fe),
                isDark = isDark,
                modifier = Modifier.weight(1f),
                testTag = "rural_sqFt"
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CalculatorInputCard(
                label = "ਹੈਕਟੇਅਰ (Hectare)",
                value = viewModel.rHectare,
                onValueChange = { viewModel.onRuralValueChange("hectare", it) },
                lightBg = Color(0xFFfce4ec),
                darkBg = Color(0x15ec4899),
                lightLabelColor = Color(0xFFc2185b),
                lightTextColor = Color(0xFFbe185d),
                darkTextColor = Color(0xFFfbcfe8),
                isDark = isDark,
                modifier = Modifier.weight(1f),
                testTag = "rural_hectare"
            )
            CalculatorInputCard(
                label = "ਸਕੇਅਰ ਮੀਟਰ (M²)",
                value = viewModel.rSqMeter,
                onValueChange = { viewModel.onRuralValueChange("sqMeter", it) },
                lightBg = Color(0xFFe1f5fe),
                darkBg = Color(0x150ea5e9),
                lightLabelColor = Color(0xFF0277bd),
                lightTextColor = Color(0xFF0284c7),
                darkTextColor = Color(0xFFbae6fd),
                isDark = isDark,
                modifier = Modifier.weight(1f),
                testTag = "rural_sqMeter"
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        CalculatorInputCard(
            label = "ਸਕੇਅਰ ਕਿ.ਮੀ. (Km²)",
            value = viewModel.rSqKm,
            onValueChange = { viewModel.onRuralValueChange("sqKm", it) },
            lightBg = Color(0xFFf1f5f9),
            darkBg = Color(0x10cbd5e1),
            lightLabelColor = Color(0xFF475569),
            lightTextColor = Color(0xFF334155),
            darkTextColor = Color(0xFFe2e8f0),
            isDark = isDark,
            testTag = "rural_sqKm"
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // --- DIMENSIONS (RURAL) ---
        Text(
            text = "ਮਿਣਤੀ ਫਾਰਮੂਲੇ (Dimension Calculations)",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color(0xFFFDF8F6) else Color(0xFF2D1600),
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        )
        
        // 1. By Feet
        DimensionCard(
            title = "ਫੁੱਟਾਂ ਰਾਹੀਂ (By Feet)",
            headerBg = Color(0xFF15803d),
            isDark = isDark
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DimensionInputField(
                    label = "ਲੰਬਾਈ (Length Ft)",
                    value = viewModel.rFeetLength,
                    onValueChange = { viewModel.onRuralDimensionChange("feet", it, viewModel.rFeetWidth) },
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "×",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 14.dp)
                )
                DimensionInputField(
                    label = "ਚੌੜਾਈ (Width Ft)",
                    value = viewModel.rFeetWidth,
                    onValueChange = { viewModel.onRuralDimensionChange("feet", viewModel.rFeetLength, it) },
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 2. By Meters
        DimensionCard(
            title = "ਮੀਟਰਾਂ ਰਾਹੀਂ (By Meters)",
            headerBg = Color(0xFF0284c7),
            isDark = isDark
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DimensionInputField(
                    label = "ਲੰਬਾਈ (Length M)",
                    value = viewModel.rMeterLength,
                    onValueChange = { viewModel.onRuralDimensionChange("meters", it, viewModel.rMeterWidth) },
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "×",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 14.dp)
                )
                DimensionInputField(
                    label = "ਚੌੜਾਈ (Width M)",
                    value = viewModel.rMeterWidth,
                    onValueChange = { viewModel.onRuralDimensionChange("meters", viewModel.rMeterLength, it) },
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 3. By Karams
        DimensionCard(
            title = "ਕਰਮਾਂ ਰਾਹੀਂ (By Karams)",
            headerBg = Color(0xFF7e22ce),
            isDark = isDark,
            annotation = "1 ਕਰਮ = 5.5 ਫੁੱਟ"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DimensionInputField(
                    label = "ਲੰਬਾਈ (Length Karam)",
                    value = viewModel.rKaramLength,
                    onValueChange = { viewModel.onRuralDimensionChange("karams", it, viewModel.rKaramWidth) },
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "×",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 14.dp)
                )
                DimensionInputField(
                    label = "ਚੌੜਾਈ (Width Karam)",
                    value = viewModel.rKaramWidth,
                    onValueChange = { viewModel.onRuralDimensionChange("karams", viewModel.rKaramLength, it) },
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 4. Triangle Land
        DimensionCard(
            title = "ਤਿਕੋਣੀ ਜ਼ਮੀਨ (Triangular Land)",
            headerBg = Color(0xFFdb2777),
            isDark = isDark
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                TriangleUnitSelector(
                    selectedUnit = viewModel.rTriUnit,
                    onUnitChanged = { newUnit ->
                        viewModel.onRuralTriangleChange(viewModel.rTriA, viewModel.rTriB, viewModel.rTriC, newUnit)
                    },
                    isDark = isDark
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val unitLabelSuffix = when (viewModel.rTriUnit) {
                        "meters" -> "M"
                        "karams" -> "K"
                        else -> "Ft"
                    }
                    DimensionInputField(
                        label = "ਭੁਜਾ A ($unitLabelSuffix)",
                        value = viewModel.rTriA,
                        onValueChange = { viewModel.onRuralTriangleChange(it, viewModel.rTriB, viewModel.rTriC) },
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                    DimensionInputField(
                        label = "ਭੁਜਾ B ($unitLabelSuffix)",
                        value = viewModel.rTriB,
                        onValueChange = { viewModel.onRuralTriangleChange(viewModel.rTriA, it, viewModel.rTriC) },
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                    DimensionInputField(
                        label = "ਭੁਜਾ C ($unitLabelSuffix)",
                        value = viewModel.rTriC,
                        onValueChange = { viewModel.onRuralTriangleChange(viewModel.rTriA, viewModel.rTriB, it) },
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                if (viewModel.rTriError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ਭੁਜਾਵਾਂ ਸਹੀ ਨਹੀਂ ਹਨ (Invalid Triangle)",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = Alignment.Center.let { TextAlign.Center },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Main Reset button
        Button(
            onClick = { viewModel.resetRural() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDark) Color(0xFF421D1D) else Color(0xFFFFDADA),
                contentColor = if (isDark) Color(0xFFFFDADA) else Color(0xFF410002)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "reset",
                tint = if (isDark) Color(0xFFFFDADA) else Color(0xFF410002)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("ਸਾਫ ਕਰੋ (Reset Rural)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}


// ==========================================
// 2. URBAN AREA CALCULATOR CONTENT
// ==========================================
@Composable
fun UrbanAreaCalculatorContent(viewModel: LandCalculatorViewModel, isDark: Boolean) {
    Column(modifier = Modifier.fillMaxWidth()) {
        
        // Urban Header Badge
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0x20ea580c) else Color(0xFFfff2e6)
            )
        ) {
            Text(
                text = "ਸ਼ਹਿਰੀ ਹਿਸਾਬ: 1 ਮਰਲਾ = 225 ਸਕੇਅਰ ਫੁੱਟ",
                color = if (isDark) Color(0xFFfdba74) else Color(0xFFea580c),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            )
        }
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CalculatorInputCard(
                label = "ਕਿੱਲਾ (Killa)",
                value = viewModel.uKilla,
                onValueChange = { viewModel.onUrbanValueChange("killa", it) },
                lightBg = Color(0xFFe8f5e9),
                darkBg = Color(0x1822c55e),
                lightLabelColor = Color(0xFF2e7d32),
                lightTextColor = Color(0xFF15803d),
                darkTextColor = Color(0xFF4ade80),
                isDark = isDark,
                modifier = Modifier.weight(1f),
                testTag = "urban_killa"
            )
            CalculatorInputCard(
                label = "ਵਿੱਘਾ (Bigha)",
                value = viewModel.uVigha,
                onValueChange = { viewModel.onUrbanValueChange("vigha", it) },
                lightBg = Color(0xFFfff3e0),
                darkBg = Color(0x15f97316),
                lightLabelColor = Color(0xFFef6c00),
                lightTextColor = Color(0xFFd84315),
                darkTextColor = Color(0xFFfdba74),
                isDark = isDark,
                modifier = Modifier.weight(1f),
                testTag = "urban_vigha"
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CalculatorInputCard(
                label = "ਕਨਾਲ (Kanal)",
                value = viewModel.uKanal,
                onValueChange = { viewModel.onUrbanValueChange("kanal", it) },
                lightBg = Color(0xFFe0f2f1),
                darkBg = Color(0x1514b8a6),
                lightLabelColor = Color(0xFF00695c),
                lightTextColor = Color(0xFF00796b),
                darkTextColor = Color(0xFF2dd4bf),
                isDark = isDark,
                modifier = Modifier.weight(1f),
                testTag = "urban_kanal"
            )
            CalculatorInputCard(
                label = "ਮਰਲਾ (Marla)",
                value = viewModel.uMarla,
                onValueChange = { viewModel.onUrbanValueChange("marla", it) },
                lightBg = Color(0xFFfffde7),
                darkBg = Color(0x15eab308),
                lightLabelColor = Color(0xFF9e9d24),
                lightTextColor = Color(0xFFa16207),
                darkTextColor = Color(0xFFfef08a),
                isDark = isDark,
                modifier = Modifier.weight(1f),
                testTag = "urban_marla"
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CalculatorInputCard(
                label = "ਗਜ (Gaj)",
                value = viewModel.uGaj,
                onValueChange = { viewModel.onUrbanValueChange("gaj", it) },
                lightBg = Color(0xFFf3e5f5),
                darkBg = Color(0x15a855f7),
                lightLabelColor = Color(0xFF6a1b9a),
                lightTextColor = Color(0xFF7e22ce),
                darkTextColor = Color(0xFFe9d5ff),
                isDark = isDark,
                modifier = Modifier.weight(1f),
                testTag = "urban_gaj"
            )
            CalculatorInputCard(
                label = "ਸਕੇਅਰ ਫੁੱਟ (Ft²)",
                value = viewModel.uSqFt,
                onValueChange = { viewModel.onUrbanValueChange("sqFt", it) },
                lightBg = Color(0xFFeef2ff),
                darkBg = Color(0x156366f1),
                lightLabelColor = Color(0xFF3730a3),
                lightTextColor = Color(0xFF2563eb),
                darkTextColor = Color(0xFFc7d2fe),
                isDark = isDark,
                modifier = Modifier.weight(1f),
                testTag = "urban_sqFt"
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CalculatorInputCard(
                label = "ਏਕੜ (Acre)",
                value = viewModel.uAcre,
                onValueChange = { viewModel.onUrbanValueChange("acre", it) },
                lightBg = Color(0xFFf5f3ff),
                darkBg = Color(0x15a78bfa),
                lightLabelColor = Color(0xFF5b21b6),
                lightTextColor = Color(0xFF6d28d9),
                darkTextColor = Color(0xFFddd6fe),
                isDark = isDark,
                modifier = Modifier.weight(1f),
                testTag = "urban_acre"
            )
            CalculatorInputCard(
                label = "ਹੈਕਟੇਅਰ (Hectare)",
                value = viewModel.uHectare,
                onValueChange = { viewModel.onUrbanValueChange("hectare", it) },
                lightBg = Color(0xFFfce4ec),
                darkBg = Color(0x15ec4899),
                lightLabelColor = Color(0xFFc2185b),
                lightTextColor = Color(0xFFbe185d),
                darkTextColor = Color(0xFFfbcfe8),
                isDark = isDark,
                modifier = Modifier.weight(1f),
                testTag = "urban_hectare"
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CalculatorInputCard(
                label = "ਸਕੇਅਰ ਮੀਟਰ (M²)",
                value = viewModel.uSqMeter,
                onValueChange = { viewModel.onUrbanValueChange("sqMeter", it) },
                lightBg = Color(0xFFe1f5fe),
                darkBg = Color(0x150ea5e9),
                lightLabelColor = Color(0xFF0277bd),
                lightTextColor = Color(0xFF0284c7),
                darkTextColor = Color(0xFFbae6fd),
                isDark = isDark,
                modifier = Modifier.weight(1f),
                testTag = "urban_sqMeter"
            )
            CalculatorInputCard(
                label = "ਸਕੇਅਰ ਕਿ.ਮੀ. (Km²)",
                value = viewModel.uSqKm,
                onValueChange = { viewModel.onUrbanValueChange("sqKm", it) },
                lightBg = Color(0xFFf1f5f9),
                darkBg = Color(0x10cbd5e1),
                lightLabelColor = Color(0xFF475569),
                lightTextColor = Color(0xFF334155),
                darkTextColor = Color(0xFFe2e8f0),
                isDark = isDark,
                modifier = Modifier.weight(1f),
                testTag = "urban_sqKm"
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // --- DIMENSIONS (URBAN) ---
        Text(
            text = "ਮਿਣਤੀ ਫਾਰਮੂਲੇ (Urban Dimension Calculations)",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color(0xFFFDF8F6) else Color(0xFF2D1600),
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        )
        
        // 1. By Feet
        DimensionCard(
            title = "ਫੁੱਟਾਂ ਰਾਹੀਂ (By Feet)",
            headerBg = Color(0xFFea580c),
            isDark = isDark
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DimensionInputField(
                    label = "ਲੰਬਾਈ (Length Ft)",
                    value = viewModel.uFeetLength,
                    onValueChange = { viewModel.onUrbanDimensionChange("feet", it, viewModel.uFeetWidth) },
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "×",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 14.dp)
                )
                DimensionInputField(
                    label = "ਚੌੜਾਈ (Width Ft)",
                    value = viewModel.uFeetWidth,
                    onValueChange = { viewModel.onUrbanDimensionChange("feet", viewModel.uFeetLength, it) },
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 2. By Meters
        DimensionCard(
            title = "ਮੀਟਰਾਂ ਰਾਹੀਂ (By Meters)",
            headerBg = Color(0xFF0284c7),
            isDark = isDark
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DimensionInputField(
                    label = "ਲੰਬਾਈ (Length M)",
                    value = viewModel.uMeterLength,
                    onValueChange = { viewModel.onUrbanDimensionChange("meters", it, viewModel.uMeterWidth) },
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "×",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 14.dp)
                )
                DimensionInputField(
                    label = "ਚੌੜਾਈ (Width M)",
                    value = viewModel.uMeterWidth,
                    onValueChange = { viewModel.onUrbanDimensionChange("meters", viewModel.uMeterLength, it) },
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 3. By Karams
        DimensionCard(
            title = "ਕਰਮਾਂ ਰਾਹੀਂ (By Karams)",
            headerBg = Color(0xFF7e22ce),
            isDark = isDark,
            annotation = "1 ਕਰਮ = 5.5 ਫੁੱਟ"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DimensionInputField(
                    label = "ਲੰਬਾਈ (Length Karam)",
                    value = viewModel.uKaramLength,
                    onValueChange = { viewModel.onUrbanDimensionChange("karams", it, viewModel.uKaramWidth) },
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "×",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 14.dp)
                )
                DimensionInputField(
                    label = "ਚੌੜਾਈ (Width Karam)",
                    value = viewModel.uKaramWidth,
                    onValueChange = { viewModel.onUrbanDimensionChange("karams", viewModel.uKaramLength, it) },
                    isDark = isDark,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 4. Triangle
        DimensionCard(
            title = "ਤਿਕੋਣੀ ਜ਼ਮੀਨ (Triangular Land)",
            headerBg = Color(0xFFdb2777),
            isDark = isDark
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                TriangleUnitSelector(
                    selectedUnit = viewModel.uTriUnit,
                    onUnitChanged = { newUnit ->
                        viewModel.onUrbanTriangleChange(viewModel.uTriA, viewModel.uTriB, viewModel.uTriC, newUnit)
                    },
                    isDark = isDark
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val unitLabelSuffix = when (viewModel.uTriUnit) {
                        "meters" -> "M"
                        "karams" -> "K"
                        else -> "Ft"
                    }
                    DimensionInputField(
                        label = "ਭੁਜਾ A ($unitLabelSuffix)",
                        value = viewModel.uTriA,
                        onValueChange = { viewModel.onUrbanTriangleChange(it, viewModel.uTriB, viewModel.uTriC) },
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                    DimensionInputField(
                        label = "ਭੁਜਾ B ($unitLabelSuffix)",
                        value = viewModel.uTriB,
                        onValueChange = { viewModel.onUrbanTriangleChange(viewModel.uTriA, it, viewModel.uTriC) },
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                    DimensionInputField(
                        label = "ਭੁਜਾ C ($unitLabelSuffix)",
                        value = viewModel.uTriC,
                        onValueChange = { viewModel.onUrbanTriangleChange(viewModel.uTriA, viewModel.uTriB, it) },
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                if (viewModel.uTriError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ਭੁਜਾਵਾਂ ਸਹੀ ਨਹੀਂ ਹਨ (Invalid Triangle)",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = Alignment.Center.let { TextAlign.Center },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { viewModel.resetUrban() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDark) Color(0xFF422C25) else Color(0xFFF7E0D4),
                contentColor = if (isDark) Color(0xFFF7E0D4) else Color(0xFF2D1600)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "reset",
                tint = if (isDark) Color(0xFFF7E0D4) else Color(0xFF2D1600)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("ਸਾਫ ਕਰੋ (Reset Urban)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}


// ==========================================
// 3. PRICE CALCULATOR CONTENT
// ==========================================
@Composable
fun PriceCalculatorContent(viewModel: LandCalculatorViewModel, isDark: Boolean) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0x187e22ce) else Color(0xFFfaf5ff)
            )
        ) {
            Text(
                text = "ਕਿਸੇ ਇੱਕ ਦਾ ਰੇਟ ਭਰੋ, ਬਾਕੀ ਆਪਣੇ ਆਪ ਆ ਜਾਣਗੇ",
                color = if (isDark) Color(0xFFd8b4fe) else Color(0xFF7e22ce),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            )
        }
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CalculatorInputCard(
                label = "ਰੇਟ ਪ੍ਰਤੀ ਕਿੱਲਾ (₹/Killa)",
                value = viewModel.pKilla,
                onValueChange = { viewModel.onPriceValueChange("killa", it) },
                lightBg = Color(0xFFe8f5e9),
                darkBg = Color(0x1822c55e),
                lightLabelColor = Color(0xFF2e7d32),
                lightTextColor = Color(0xFF15803d),
                darkTextColor = Color(0xFF4ade80),
                isDark = isDark,
                modifier = Modifier.weight(1f),
                subBadge = viewModel.getFormattedPrice("killa"),
                testTag = "price_killa"
            )
            CalculatorInputCard(
                label = "ਰੇਟ ਪ੍ਰਤੀ ਵਿੱਘਾ (₹/Bigha)",
                value = viewModel.pVigha,
                onValueChange = { viewModel.onPriceValueChange("vigha", it) },
                lightBg = Color(0xFFfff3e0),
                darkBg = Color(0x15f97316),
                lightLabelColor = Color(0xFFef6c00),
                lightTextColor = Color(0xFFd84315),
                darkTextColor = Color(0xFFfdba74),
                isDark = isDark,
                modifier = Modifier.weight(1f),
                subBadge = viewModel.getFormattedPrice("vigha"),
                testTag = "price_vigha"
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CalculatorInputCard(
                label = "ਰੇਟ ਪ੍ਰਤੀ ਕਨਾਲ (₹/Kanal)",
                value = viewModel.pKanal,
                onValueChange = { viewModel.onPriceValueChange("kanal", it) },
                lightBg = Color(0xFFe0f2f1),
                darkBg = Color(0x1514b8a6),
                lightLabelColor = Color(0xFF00695c),
                lightTextColor = Color(0xFF00796b),
                darkTextColor = Color(0xFF2dd4bf),
                isDark = isDark,
                modifier = Modifier.weight(1f),
                subBadge = viewModel.getFormattedPrice("kanal"),
                testTag = "price_kanal"
            )
            CalculatorInputCard(
                label = "ਰੇਟ ਪ੍ਰਤੀ ਮਰਲਾ (₹/Marla)",
                value = viewModel.pMarla,
                onValueChange = { viewModel.onPriceValueChange("marla", it) },
                lightBg = Color(0xFFfffde7),
                darkBg = Color(0x15eab308),
                lightLabelColor = Color(0xFF9e9d24),
                lightTextColor = Color(0xFFa16207),
                darkTextColor = Color(0xFFfef08a),
                isDark = isDark,
                modifier = Modifier.weight(1f),
                subBadge = viewModel.getFormattedPrice("marla"),
                testTag = "price_marla"
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CalculatorInputCard(
                label = "ਰੇਟ ਪ੍ਰਤੀ ਗਜ (₹/Gaj)",
                value = viewModel.pGaj,
                onValueChange = { viewModel.onPriceValueChange("gaj", it) },
                lightBg = Color(0xFFf3e5f5),
                darkBg = Color(0x15a855f7),
                lightLabelColor = Color(0xFF6a1b9a),
                lightTextColor = Color(0xFF7e22ce),
                darkTextColor = Color(0xFFe9d5ff),
                isDark = isDark,
                modifier = Modifier.weight(1f),
                subBadge = viewModel.getFormattedPrice("gaj"),
                testTag = "price_gaj"
            )
            CalculatorInputCard(
                label = "ਰੇਟ ਪ੍ਰਤੀ ਫੁੱਟ (₹/Ft²)",
                value = viewModel.pSqFt,
                onValueChange = { viewModel.onPriceValueChange("sqFt", it) },
                lightBg = Color(0xFFeef2ff),
                darkBg = Color(0x156366f1),
                lightLabelColor = Color(0xFF3730a3),
                lightTextColor = Color(0xFF2563eb),
                darkTextColor = Color(0xFFc7d2fe),
                isDark = isDark,
                modifier = Modifier.weight(1f),
                subBadge = viewModel.getFormattedPrice("sqFt"),
                testTag = "price_sqFt"
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CalculatorInputCard(
                label = "ਰੇਟ ਪ੍ਰਤੀ ਮੀਟਰ (₹/M²)",
                value = viewModel.pSqMeter,
                onValueChange = { viewModel.onPriceValueChange("sqMeter", it) },
                lightBg = Color(0xFFe1f5fe),
                darkBg = Color(0x150ea5e9),
                lightLabelColor = Color(0xFF0277bd),
                lightTextColor = Color(0xFF0284c7),
                darkTextColor = Color(0xFFbae6fd),
                isDark = isDark,
                modifier = Modifier.weight(1f),
                subBadge = viewModel.getFormattedPrice("sqMeter"),
                testTag = "price_sqMeter"
            )
            CalculatorInputCard(
                label = "ਰੇਟ ਪ੍ਰਤੀ ਕਿ.ਮੀ. (₹/Km²)",
                value = viewModel.pSqKm,
                onValueChange = { viewModel.onPriceValueChange("sqKm", it) },
                lightBg = Color(0xFFf1f5f9),
                darkBg = Color(0x10cbd5e1),
                lightLabelColor = Color(0xFF475569),
                lightTextColor = Color(0xFF334155),
                darkTextColor = Color(0xFFe2e8f0),
                isDark = isDark,
                modifier = Modifier.weight(1f),
                subBadge = viewModel.getFormattedPrice("sqKm"),
                testTag = "price_sqKm"
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Button(
            onClick = { viewModel.resetPrice() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDark) Color(0xFF2D233D) else Color(0xFFE8DEF8),
                contentColor = if (isDark) Color(0xFFE8DEF8) else Color(0xFF21005D)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "reset",
                tint = if (isDark) Color(0xFFE8DEF8) else Color(0xFF21005D)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("ਸਾਫ ਕਰੋ (Reset Price)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}


// ==========================================
// 4. LAND DIVIDER CONTENT
// ==========================================
@Composable
fun DividerContent(viewModel: LandCalculatorViewModel, isDark: Boolean) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0x180284c7) else Color(0xFFf0f9ff)
            )
        ) {
            Text(
                text = "ਕੁੱਲ ਜਮੀਨ ਭਰੋ ਅਤੇ ਹਿੱਸੇਦਾਰ ਦੱਸੋ",
                color = if (isDark) Color(0xFFbae6fd) else Color(0xFF0284c7),
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            )
        }
        
        CalculatorInputCard(
            label = "ਕੁੱਲ ਕਿੱਲੇ (Total Acres)",
            value = viewModel.divKilla,
            onValueChange = { viewModel.onDividerChange("killa", it) },
            lightBg = Color(0xFFe8f5e9),
            darkBg = Color(0x1822c55e),
            lightLabelColor = Color(0xFF2e7d32),
            lightTextColor = Color(0xFF15803d),
            darkTextColor = Color(0xFF4ade80),
            isDark = isDark,
            testTag = "div_killa"
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CalculatorInputCard(
                label = "ਕੁੱਲ ਕਨਾਲ (Kanal)",
                value = viewModel.divKanal,
                onValueChange = { viewModel.onDividerChange("kanal", it) },
                lightBg = Color(0xFFe0f2f1),
                darkBg = Color(0x1514b8a6),
                lightLabelColor = Color(0xFF00695c),
                lightTextColor = Color(0xFF00796b),
                darkTextColor = Color(0xFF2dd4bf),
                isDark = isDark,
                modifier = Modifier.weight(1f),
                testTag = "div_kanal"
            )
            CalculatorInputCard(
                label = "ਕੁੱਲ ਮਰਲੇ (Marla)",
                value = viewModel.divMarla,
                onValueChange = { viewModel.onDividerChange("marla", it) },
                lightBg = Color(0xFFfffde7),
                darkBg = Color(0x15eab308),
                lightLabelColor = Color(0xFF9e9d24),
                lightTextColor = Color(0xFFa16207),
                darkTextColor = Color(0xFFfef08a),
                isDark = isDark,
                modifier = Modifier.weight(1f),
                testTag = "div_marla"
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        CalculatorInputCard(
            label = "ਕਿੰਨੇ ਹਿੱਸੇਦਾਰ (Total Shareholders)",
            value = viewModel.divPeople,
            onValueChange = { viewModel.onDividerChange("people", it) },
            lightBg = Color(0xFFeef2ff),
            darkBg = Color(0x156366f1),
            lightLabelColor = Color(0xFF3730a3),
            lightTextColor = Color(0xFF2563eb),
            darkTextColor = Color(0xFFc7d2fe),
            isDark = isDark,
            testTag = "div_shareholders"
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // --- RESULT BLOCK ---
        Text(
            text = "ਹਰੇਕ ਦੇ ਹਿੱਸੇ ਆਈ ਜਮੀਨ (Each Share):",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color(0xFF88848F) else Color(0xFF79747E),
            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
        )
        
        val resultBorderColor = if (isDark) Color(0xFF2E2A29) else Color(0xFFE7E0D8)
        val resultBg = if (isDark) Color(0xFF1E1A19) else Color(0xFFFFFFFF)
        val highlightColor = if (isDark) Color(0xFFD3E4FF) else Color(0xFF001D36)
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(resultBg)
                .border(1.dp, resultBorderColor, RoundedCornerShape(24.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = viewModel.shareKilla,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = highlightColor
                    )
                    Text(
                        text = " ਕਿੱਲੇ",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (isDark) Color(0xFFFDF8F6) else Color(0xFF1D1B20),
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    
                    Text(
                        text = viewModel.shareKanal,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = highlightColor
                    )
                    Text(
                        text = " ਕਨਾਲ",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (isDark) Color(0xFFFDF8F6) else Color(0xFF1D1B20),
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    
                    Text(
                        text = viewModel.shareMarla,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = highlightColor
                    )
                    Text(
                        text = " ਮਰਲੇ",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (isDark) Color(0xFFFDF8F6) else Color(0xFF1D1B20)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { viewModel.resetDivider() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isDark) Color(0xFF1D2C3D) else Color(0xFFD3E4FF),
                contentColor = if (isDark) Color(0xFFD3E4FF) else Color(0xFF001D36)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "reset",
                tint = if (isDark) Color(0xFFD3E4FF) else Color(0xFF001D36)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("ਸਾਫ ਕਰੋ (Reset Divider)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}


// ==========================================
// ELEVATED CONTAINER FOR DIMENSION SUBSECTIONS
// ==========================================
@Composable
fun DimensionCard(
    title: String,
    headerBg: Color,
    isDark: Boolean,
    annotation: String? = null,
    content: @Composable () -> Unit
) {
    val cardBg = if (isDark) Color(0xFF1E1A19) else Color.White
    val borderCol = if (isDark) Color(0xFF2E2A29) else Color(0xFFE7E0D8)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderCol, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            val softHeaderBg = when (headerBg) {
                Color(0xFF15803d) -> if (isDark) Color(0xFF422C25) else Color(0xFFFFF7F3)  // Rural Feet (peach)
                Color(0xFF0284c7) -> if (isDark) Color(0xFF202B37) else Color(0xFFF0F5FF)  // Meters (blue)
                Color(0xFF7e22ce) -> if (isDark) Color(0xFF2E243A) else Color(0xFFFBF8FF)  // Karams (purple)
                Color(0xFFdb2777) -> if (isDark) Color(0xFF421D1D) else Color(0xFFFFF5F5)  // Triangular (pink)
                Color(0xFFea580c) -> if (isDark) Color(0xFF422C25) else Color(0xFFFFF7F3)  // Urban Feet
                else -> if (isDark) Color(0xFF1A1615) else Color(0xFFF3EDF7)
            }
            val softHeaderTextColor = when (headerBg) {
                Color(0xFF15803d) -> if (isDark) Color(0xFFF7E0D4) else Color(0xFF2D1600)
                Color(0xFF0284c7) -> if (isDark) Color(0xFFD3E4FF) else Color(0xFF001D36)
                Color(0xFF7e22ce) -> if (isDark) Color(0xFFE8DEF8) else Color(0xFF21005D)
                Color(0xFFdb2777) -> if (isDark) Color(0xFFFFDADA) else Color(0xFF410002)
                Color(0xFFea580c) -> if (isDark) Color(0xFFF7E0D4) else Color(0xFF2D1600)
                else -> if (isDark) Color.White else Color(0xFF1D1B20)
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(softHeaderBg)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = softHeaderTextColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (annotation != null) {
                        Text(
                            text = annotation,
                            color = softHeaderTextColor.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                content()
            }
        }
    }
}


// ==========================================
// SOLID PROFILE / DEVELOPER INFORMATION CARD
// ==========================================
@Composable
fun CreatorProfileCard(isDark: Boolean) {
    val context = LocalContext.current
    val cardBg = if (isDark) Color(0xFF1E1A19) else Color.White
    val borderCol = if (isDark) Color(0xFF2E2A29) else Color(0xFFE7E0D8)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderCol, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // Initials Avatar with custom background
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF5D2426) else Color(0xFFFFDADA)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "IS",
                    color = if (isDark) Color(0xFFFFDADA) else Color(0xFF410002),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Made By \"Inderjeet Singh Talwandi Sabo\"",
                color = if (isDark) Color(0xFFFDF8F6) else Color(0xFF1D1B20),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "Contact - 94644-91806",
                color = if (isDark) Color(0xFFFFDADA) else Color(0xFF410002),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Native Action: Quick dial click target
            Button(
                onClick = {
                    try {
                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:9464491806")
                        }
                        context.startActivity(dialIntent)
                    } catch (e: Throwable) {
                        // fallback
                    }
                },
                modifier = Modifier
                    .height(48.dp)
                    .fillMaxWidth(0.8f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) Color(0x20FFDADA) else Color(0xFFFFF5F5),
                    contentColor = if (isDark) Color(0xFFFFDADA) else Color(0xFF410002)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (isDark) Color(0x40FFDADA) else Color(0x60410002))
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Dial call",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ਕਾਲ ਕਰੋ (Call Now)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Version 5.3",
                color = if (isDark) Color(0xFF88848F) else Color(0xFF79747E),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
