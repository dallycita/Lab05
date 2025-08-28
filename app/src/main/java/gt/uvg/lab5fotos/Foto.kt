package gt.uvg.lab5fotos

import android.net.Uri

// Clase para guardar cada foto con su ruta (uri) y un título.
data class Foto(
    val uri: Uri,
    val titulo: String
)
