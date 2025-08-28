@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    coil.annotation.ExperimentalCoilApi::class
)

package gt.uvg.lab5fotos

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import gt.uvg.lab5fotos.ui.theme.Lab5FotosTheme
import java.io.File
import java.io.FileOutputStream
import androidx.core.net.toUri

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Lab5FotosTheme {
                val galeria: GaleriaViewModel = viewModel()
                PantallaFotos(galeria)
            }
        }
    }
}

@Composable
fun PantallaFotos(galeria: GaleriaViewModel) {
    val fotos by galeria.fotos.collectAsStateWithLifecycle()
    val ctx = LocalContext.current

    // Selector de imágenes (Photo Picker)
    val selector = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val n = fotos.size + 1
            galeria.agregar(uri, "Foto $n")
        }
    }

    // Cámara (foto rápida como Bitmap)
    val camara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp: Bitmap? ->
        if (bmp != null) {
            val n = fotos.size + 1
            val uri = guardarBitmapEnCache(ctx.cacheDir, bmp)
            galeria.agregar(uri, "Cámara $n")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fotos") },
                actions = {
                    // Botón simple para abrir la cámara
                    TextButton(onClick = { camara.launch(null) }) {
                        Text("Cámara")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selector.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar foto")
            }
        }
    ) { innerPadding ->
        if (fotos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Aún no hay fotos. Toca + o usa la cámara.")
            }
        } else {
            ListaDeFotos(
                fotos = fotos,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun ListaDeFotos(fotos: List<Foto>, modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(fotos) { foto ->
            TarjetaFoto(foto)
        }
    }
}

@Composable
fun TarjetaFoto(foto: Foto) {
    Card {
        // Imagen con escalado y placeholder mientras carga
        SubcomposeAsyncImage(
            model = foto.uri,
            contentDescription = foto.titulo,
            contentScale = ContentScale.Crop,
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        Text(
            text = foto.titulo,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VistaPrevia() {
    Lab5FotosTheme {
        PantallaFotos(galeria = GaleriaViewModel())
    }
}

// Guarda un Bitmap en la carpeta de caché y devuelve su Uri (file://)
// Simple y suficiente para mostrarla con Coil en la app.
private fun guardarBitmapEnCache(cacheDir: File, bmp: Bitmap): android.net.Uri {
    val nombre = "foto_${System.currentTimeMillis()}.png"
    val archivo = File(cacheDir, nombre)
    FileOutputStream(archivo).use { out ->
        bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    return archivo.toUri()
}

