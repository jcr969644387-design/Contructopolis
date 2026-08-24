package com.educalab.civilestructuras.data.local.converters

import androidx.room.TypeConverter

/**
 * Room ya soporta Boolean de forma nativa (INTEGER 0/1); este converter se
 * deja explícito para dejar constancia de la decisión y como punto de
 * extensión si en el futuro se necesitan tipos adicionales (por ejemplo,
 * enums serializados). No introduce columnas ocultas ni listas en memoria:
 * todas las relaciones del esquema usan tablas y claves foráneas reales.
 */
class BooleanConverters {
    @TypeConverter
    fun fromInt(value: Int): Boolean = value != 0

    @TypeConverter
    fun toInt(value: Boolean): Int = if (value) 1 else 0
}
