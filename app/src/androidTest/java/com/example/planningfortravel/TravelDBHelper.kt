package com.example.planningfortravel

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class TravelRecord(
    val no: Int = 0,
    val place: String,
    val visitDate: String,
    val memo: String,
    val photoUri: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

class TravelDBHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION){
    companion object {
        const val DB_NAME = "travel.db"
        const val DB_VERSION = 2
        const val TABLE = "travel_records"
        const val COL_NO = "no"
        const val COL_PLACE = "place"
        const val COL_DATE = "visit_date"
        const val COL_MEMO = "memo"
        const val COL_PHOTO = "photo_uri"
        const val COL_LAT = "latitude"
        const val COL_LNG = "longitude"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE (
                $COL_NO INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PLACE TEXT,
                $COL_DATE TEXT,
                $COL_MEMO TEXT,
                $COL_PHOTO TEXT,
                $COL_LAT REAL,
                $COL_LNG REAL
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE ADD COLUMN $COL_LAT REAL DEFAULT 0.0")
            db.execSQL("ALTER TABLE $TABLE ADD COLUMN $COL_LNG REAL DEFAULT 0.0")
        }
    }

    fun insert(record: TravelRecord): Long {
        val cv = ContentValues().apply {
            put(COL_PLACE, record.place)
            put(COL_DATE, record.visitDate)
            put(COL_MEMO, record.memo)
            put(COL_PHOTO, record.photoUri)
            put(COL_LAT, record.latitude)
            put(COL_LNG, record.longitude)
        }
        return writableDatabase.insert(TABLE, null, cv)
    }

    fun getAll(sortBy: String): List<TravelRecord> {
        val list = mutableListOf<TravelRecord>()
        val cursor = readableDatabase.query(TABLE, null, null, null, null, null, "$sortBy DESC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    TravelRecord(
                        no = it.getInt(it.getColumnIndexOrThrow(COL_NO)),
                        place = it.getString(it.getColumnIndexOrThrow(COL_PLACE)),
                        visitDate = it.getString(it.getColumnIndexOrThrow(COL_DATE)),
                        memo = it.getString(it.getColumnIndexOrThrow(COL_MEMO)),
                        photoUri = it.getString(it.getColumnIndexOrThrow(COL_PHOTO)),
                        latitude = it.getDouble(it.getColumnIndexOrThrow(COL_LAT)),
                        longitude = it.getDouble(it.getColumnIndexOrThrow(COL_LNG))
                    )
                )
            }
        }
        return list
    }

    fun getById(no: Int): TravelRecord? {
        val cursor = readableDatabase.query(TABLE, null, "$COL_NO = ?", arrayOf(no.toString()), null, null, null)
        return cursor.use{
            if(it.moveToFirst()) TravelRecord(
                no = it.getInt(it.getColumnIndexOrThrow(COL_NO)),
                place = it.getString(it.getColumnIndexOrThrow(COL_PLACE)),
                visitDate = it.getString(it.getColumnIndexOrThrow(COL_DATE)),
                memo = it.getString(it.getColumnIndexOrThrow(COL_MEMO)),
                photoUri = it.getString(it.getColumnIndexOrThrow(COL_PHOTO)),
                latitude = it.getDouble(it.getColumnIndexOrThrow(COL_LAT)),
                longitude = it.getDouble(it.getColumnIndexOrThrow(COL_LNG))
            ) else null
        }
    }

    fun update(record: TravelRecord): Int{
        val cv = ContentValues().apply {
            put(COL_PLACE, record.place)
            put(COL_DATE, record.visitDate)
            put(COL_MEMO, record.memo)
            put(COL_PHOTO, record.photoUri)
            put(COL_LAT, record.latitude)
            put(COL_LNG, record.longitude)
        }
        return writableDatabase.update(TABLE, cv, "$COL_NO = ?", arrayOf(record.no.toString()))
    }

    fun delete(no: Int): Int = writableDatabase.delete(TABLE, "$COL_NO = ?", arrayOf(no.toString()))

    fun deleteAll(): Int = writableDatabase.delete(TABLE, null, null)
}