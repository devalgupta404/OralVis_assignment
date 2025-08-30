package com.example.oralvis.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.io.OutputStream

data class SessionMetadata(
    val patientName: String,
    val patientAge: String,
    val timestamp: Long
)

object MediaStoreUtils {

    private const val APP_DIR = "DCIM/OralVis/Sessions"
    private const val PREFS_NAME = "session_metadata"
    private const val SESSION_PREFIX = "session_"

    fun createImageUri(context: Context, sessionId: String): Uri? {
        val timestamp = System.currentTimeMillis()
        val filename = "IMG_${timestamp}.jpg"
        val resolver = context.contentResolver
        val collection = if (Build.VERSION.SDK_INT >= 29) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        
        val folderPath = "$APP_DIR/$sessionId"
        
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, folderPath)
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        
        return resolver.insert(collection, contentValues)
    }
    
    fun finalizeImageUri(context: Context, uri: Uri) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.IS_PENDING, 0)
        }
        context.contentResolver.update(uri, contentValues, null, null)
    }

    fun openOutputStream(context: Context, uri: Uri): OutputStream? =
        context.contentResolver.openOutputStream(uri)

    fun listImagesForSession(context: Context, sessionId: String): List<Uri> {
        val resolver = context.contentResolver
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.RELATIVE_PATH
        )
        val selection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
        val selectionArgs = arrayOf("DCIM/OralVis/Sessions/$sessionId/%")
        val uris = mutableListOf<Uri>()
        val collection = if (Build.VERSION.SDK_INT >= 29) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        
        resolver.query(collection, projection, selection, selectionArgs, "${MediaStore.Images.Media.DATE_ADDED} DESC")
            ?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    uris += Uri.withAppendedPath(collection, id.toString())
                }
            }
        return uris
    }

    fun saveSessionMetadata(context: Context, sessionId: String, patientName: String, patientAge: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val metadata = SessionMetadata(
            patientName = patientName.trim(),
            patientAge = patientAge,
            timestamp = System.currentTimeMillis()
        )
        
        val json = """
            {
                "patientName": "${metadata.patientName}",
                "patientAge": "${metadata.patientAge}",
                "timestamp": ${metadata.timestamp}
            }
        """.trimIndent()
        
        prefs.edit().putString("$SESSION_PREFIX$sessionId", json).apply()
    }

    fun getSessionMetadata(context: Context, sessionId: String): SessionMetadata? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString("$SESSION_PREFIX$sessionId", null)
        
        return if (json != null) {
            try {
                val nameMatch = Regex("\"patientName\":\\s*\"([^\"]+)\"").find(json)
                val ageMatch = Regex("\"patientAge\":\\s*\"([^\"]+)\"").find(json)
                val timestampMatch = Regex("\"timestamp\":\\s*(\\d+)").find(json)
                
                if (nameMatch != null && ageMatch != null && timestampMatch != null) {
                    SessionMetadata(
                        patientName = nameMatch.groupValues[1],
                        patientAge = ageMatch.groupValues[1],
                        timestamp = timestampMatch.groupValues[1].toLong()
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    fun hasSessionMetadata(context: Context, sessionId: String): Boolean {
        return getSessionMetadata(context, sessionId) != null
    }

    fun getAllSessionsWithMetadata(context: Context): List<Pair<String, SessionMetadata?>> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val allSessions = mutableListOf<Pair<String, SessionMetadata?>>()
        
        val resolver = context.contentResolver
        val projection = arrayOf(MediaStore.Images.Media.RELATIVE_PATH)
        val selection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
        val selectionArgs = arrayOf("DCIM/OralVis/Sessions/%")
        val collection = if (Build.VERSION.SDK_INT >= 29) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        
        val sessionIds = mutableSetOf<String>()
        
        resolver.query(collection, projection, selection, selectionArgs, null)
            ?.use { cursor ->
                val pathCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)
                while (cursor.moveToNext()) {
                    val path = cursor.getString(pathCol)
                    val pathParts = path.split("/")
                    if (pathParts.size >= 4) {
                        sessionIds.add(pathParts[3])
                    }
                }
            }
        
        sessionIds.forEach { sessionId ->
            val metadata = getSessionMetadata(context, sessionId)
            allSessions.add(sessionId to metadata)
        }
        
        return allSessions.sortedByDescending { (_, metadata) ->
            metadata?.timestamp ?: 0L
        }
    }
}
