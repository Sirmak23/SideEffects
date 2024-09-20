package com.anddevcorp.sideeffects

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import com.anddevcorp.sideeffects.ui.theme.SideEffectsTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SideEffectsTheme {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(scrollState)
                ) {
                    SideEffectExample()
                    ConfigurationExample()
                    DisposeEffectVisible()
                    ApiCallExample()
                    CounterExample()
                    CounterExampleWithSaveable()
                    DerivedStateOfExample()
                    MovableContentWithStateExample()
                    NoStateRetentionExample()
                }
            }
        }
    }
}

@Composable
fun SideEffectExample() {
    var counter by remember { mutableIntStateOf(0) }

    Button(onClick = { counter++ }) {
        Text("Click me!")
    }
    Text("Counter: $counter")

    SideEffect {
        // Her yeniden çizimde (recomposition) bu kod çalışır
        Log.d("SideEffect", "Counter value: $counter")
    }
}

@Composable
fun ConfigurationExample() {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val orientation = configuration.orientation

    Text(
        text = "Screen width: $screenWidth dp,\n" +
                "Screen orientation: ${if (orientation == Configuration.ORIENTATION_PORTRAIT) "Portrait" else "Landscape"}",
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun TimerExample() {
    var isTimerRunning by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var counter by remember { mutableIntStateOf(0) }

    DisposableEffect(isTimerRunning) {
        var job: Job? = null

        if (isTimerRunning) {
            job = coroutineScope.launch {
                while (isTimerRunning) {
                    delay(1000)
                    counter++
                    println("$counter saniye geçti.")
                }
            }
        }

        onDispose {
            job?.cancel() // Timer'ı durdur
            println("job.Cancel().")

        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = { isTimerRunning = !isTimerRunning }) {
            Text(text = if (isTimerRunning) "Durdur" else "Başlat")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = if (isTimerRunning) "Timer Çalışıyor...$counter" else "Timer Durduruldu")
    }
}


@Composable
fun DisposeEffectVisible() {
    var isVisible by remember { mutableStateOf(true) }

    Column {
        Button(onClick = { isVisible = !isVisible }) {
            Text(text = if (isVisible) "Timer Gizle" else "Timer Göster")
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isVisible) {
            TimerExample()
        }
    }
}

@Composable
fun ApiCallExample() {
    var isLoading by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = {
            isLoading = true
            result = null
        }) {
            Text(text = "Veri Çek")
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            LaunchedEffect(Unit) { // key değerini Unit verdik sadece bi kere çalışacak şekilde
                result = "veri yükleniyor..."
                delay(2000)
                result = "API'den alınan veri: Başarıyla Yüklendi."
                isLoading = false
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (result != null) {
            Text(text = result!!)
        }
    }
}

@Composable
fun CounterExample() {
    var counter by remember { mutableIntStateOf(0) }
    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = { counter++ }) {
            Text(text = "Sayacı Artır: $counter")
        }

    }
}

@Composable
fun CounterExampleWithSaveable() {
    var counter by rememberSaveable { mutableIntStateOf(0) }
    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = { counter++ }) {
            Text(text = "Sayacı Artır: $counter")
        }

    }
}

@Composable
fun DerivedStateOfExample() {
    var items by remember { mutableStateOf(listOf<String>()) }

    val itemCount by remember {
        derivedStateOf { items.size }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = { items = items + "Öğe ${items.size + 1}" }) {
            Text("Öğe Ekle")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { if (items.isNotEmpty()) items = items.dropLast(1) }) {
            Text("Öğe Çıkar")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Toplam Öğe Sayısı: $itemCount")
    }
}
@Composable
fun MovableContentWithStateExample() {
    var isMoved by remember { mutableStateOf(false) }

    val movableContent = remember {
        movableContentOf {

            var counter by remember { mutableIntStateOf(0) }

            Box(
                modifier = Modifier
                    .size(150.dp)
                    .background(Color.Blue, CircleShape)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { counter++ }) {
                        Text("Arttır $counter")
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isMoved) {
            movableContent()
        }

        Button(onClick = { isMoved = !isMoved }) {
            Text("Yer Değiştir")
        }

        if (isMoved) {
            movableContent()
        }
    }
}
@Composable
fun NoStateRetentionExample() {
    var isMoved by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!isMoved) {
            CounterBox()
        }

        Button(onClick = { isMoved = !isMoved }) {
            Text("Yer Değiştir")
        }

        if (isMoved) {
            CounterBox()
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun CounterBox() {
    var counter by mutableIntStateOf(0)

    Box(
        modifier = Modifier
            .size(150.dp)
            .background(Color.Blue, CircleShape)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { counter++ }) {
                Text("Arttır $counter")
            }
        }
    }
}

























//@Composable
//fun MovableContentExample() {
//    var cardPositions by remember { mutableStateOf(listOf(Offset(0f, 0f), Offset(0f, 150f), Offset(0f, 300f))) }
//
//    val updatePosition: (Int, Offset) -> Unit = { index, offset ->
//        cardPositions = cardPositions.toMutableList().apply { this[index] = offset }
//    }
//
//    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
//        cardPositions.forEachIndexed { index, position ->
//            DraggableCard(position, onPositionChange = { newOffset ->
//                updatePosition(index, newOffset)
//            })
//        }
//    }
//}
//
//@Composable
//fun DraggableCard(position: Offset, onPositionChange: (Offset) -> Unit) {
//    var cardOffset by remember { mutableStateOf(position) }
//
//    Box(
//        modifier = Modifier
//            .offset { cardOffset.toIntOffset() }
//            .size(100.dp)
//            .background(Color.DarkGray, RoundedCornerShape(8.dp))
//            .pointerInput(Unit) {
//                detectDragGestures { change, dragAmount ->
//                    cardOffset = Offset(
//                        x = cardOffset.x + dragAmount.x,
//                        y = cardOffset.y + dragAmount.y
//                    )
//                    change.consume()
//                    onPositionChange(cardOffset)
//                }
//            }
//    ) {
//        // Kartın içeriği
//        Text(
//            text = "Drag me!",
//            modifier = Modifier.align(Alignment.Center),
//            color = Color.White
//        )
//    }
//}
//
//fun Offset.toIntOffset(): IntOffset = IntOffset(x.toInt(), y.toInt())