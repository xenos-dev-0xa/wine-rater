package com.example.vine_rater.data

import android.content.Context
import android.net.Uri
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupManager(private val context: Context, private val repository: WineRepository) {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    
    private val listType = Types.newParameterizedType(List::class.java, Wine::class.java)
    private val jsonAdapter = moshi.adapter<List<Wine>>(listType)

    suspend fun exportBackup(outputStream: OutputStream) {
        withContext(Dispatchers.IO) {
            val wines = repository.getAllWinesList()
            val zipOut = ZipOutputStream(outputStream)

            // 1. Write JSON data
            val json = jsonAdapter.toJson(wines)
            zipOut.putNextEntry(ZipEntry("data.json"))
            zipOut.write(json.toByteArray())
            zipOut.closeEntry()

            // 2. Write images
            val imagesDir = context.filesDir
            imagesDir.listFiles()?.forEach { file ->
                if (file.isFile && file.name.endsWith(".jpg")) {
                    zipOut.putNextEntry(ZipEntry("images/${file.name}"))
                    file.inputStream().use { input ->
                        input.copyTo(zipOut)
                    }
                    zipOut.closeEntry()
                }
            }

            zipOut.close()
        }
    }

    suspend fun importBackup(inputStream: InputStream) {
        withContext(Dispatchers.IO) {
            val zipIn = ZipInputStream(inputStream)
            var entry = zipIn.nextEntry
            var wines: List<Wine>? = null

            while (entry != null) {
                when {
                    entry.name == "data.json" -> {
                        val json = zipIn.readBytes().toString(Charsets.UTF_8)
                        wines = jsonAdapter.fromJson(json)
                    }
                    entry.name.startsWith("images/") -> {
                        val fileName = entry.name.removePrefix("images/")
                        val destFile = File(context.filesDir, fileName)
                        FileOutputStream(destFile).use { output ->
                            zipIn.copyTo(output)
                        }
                    }
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
            zipIn.close()

            wines?.let { importedWines ->
                // Note: This replaces/adds to the database. 
                // For a full restore, you might want to clear the DB first.
                // Here we'll just insert all, Room will handle conflicts if IDs match (or replace).
                importedWines.forEach { wine ->
                    repository.insertWine(wine)
                }
            }
        }
    }
}
