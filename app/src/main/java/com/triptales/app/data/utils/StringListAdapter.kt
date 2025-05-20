package com.triptales.app.data.utils

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

/**
 * Adattatore personalizzato per la serializzazione/deserializzazione di liste di stringhe.
 * Gestisce diversi formati possibili:
 * - Array JSON: ["tag1", "tag2", "tag3"]
 * - Stringa singola con separatori: "tag1,tag2,tag3"
 * - Stringa JSON di un array: "["tag1", "tag2", "tag3"]"
 */
class StringListAdapter : TypeAdapter<List<String>>() {

    override fun write(out: JsonWriter, value: List<String>?) {
        if (value == null) {
            out.nullValue()
            return
        }

        // Serializza come array JSON
        out.beginArray()
        value.forEach { out.value(it) }
        out.endArray()
    }

    override fun read(reader: JsonReader): List<String>? {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull()
            return null
        }

        // Se il valore è una stringa
        if (reader.peek() == JsonToken.STRING) {
            val stringValue = reader.nextString()

            // Stringa vuota
            if (stringValue.isBlank()) {
                return emptyList()
            }

            // Prova a interpretare come stringa JSON
            if (stringValue.startsWith("[") && stringValue.endsWith("]")) {
                try {
                    // Potrebbe essere una rappresentazione testuale di un array JSON
                    // Ad esempio: "["tag1", "tag2", "tag3"]"
                    val content = stringValue.substring(1, stringValue.length - 1)
                    return content.split(",")
                        .map { it.trim() }
                        .map { it.trim('"') }
                        .filter { it.isNotBlank() }
                } catch (_: Exception) {
                    // Fallback alla divisione semplice per virgola
                    return stringValue.split(",").map { it.trim() }
                }
            }

            // Semplice stringa separata da virgole
            return stringValue.split(",").map { it.trim() }
        }

        // Se il valore è un array JSON
        if (reader.peek() == JsonToken.BEGIN_ARRAY) {
            val result = mutableListOf<String>()
            reader.beginArray()
            while (reader.hasNext()) {
                if (reader.peek() == JsonToken.STRING) {
                    result.add(reader.nextString())
                } else {
                    reader.skipValue()
                }
            }
            reader.endArray()
            return result
        }

        // Non riconosciuto, salta e ritorna lista vuota
        reader.skipValue()
        return emptyList()
    }
}