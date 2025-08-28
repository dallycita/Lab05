package gt.uvg.lab5fotos

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// este ViewModel es como la "memoria" de la galería.
// aquí guardamos todas las fotos que el usuario vaya agregando.
class GaleriaViewModel : ViewModel() {

    // lista de fotos que puede cambiar (empieza vacía).
    private val _fotos = MutableStateFlow<List<Foto>>(emptyList())
    val fotos: StateFlow<List<Foto>> = _fotos.asStateFlow()

    // agregar una nueva foto a la lista
    fun agregar(uri: Uri, titulo: String = "Foto") {
        _fotos.update { listaActual -> listaActual + Foto(uri, titulo) }
    }

    // (opcional) limpiar la galería
    fun limpiar() {
        _fotos.value = emptyList()
    }
}
