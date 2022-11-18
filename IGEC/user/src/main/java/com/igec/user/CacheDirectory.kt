package com.igec.user

import android.content.Context
import java.io.*

object CacheDirectory {
    @JvmStatic
    fun readAllCachedText(context: Context, filename: String): String? {
        val file = File(context.cacheDir, filename)
        return readAllText(file)
    }

    fun readAllResourceText(context: Context, resourceId: Int): String? {
        val inputStream = context.resources.openRawResource(resourceId)
        return readAllText(inputStream)
    }

    fun readAllFileText(file: String?): String? {
        return try {
            val inputStream = FileInputStream(file)
            readAllText(inputStream)
        } catch (ex: Exception) {
            null
        }
    }

    fun readAllText(file: File?): String? {
        return try {
            val inputStream = FileInputStream(file)
            readAllText(inputStream)
        } catch (ex: Exception) {
            null
        }
    }

    fun readAllText(inputStream: InputStream?): String? {
        val inputreader = InputStreamReader(inputStream)
        val buffreader = BufferedReader(inputreader)
        var line: String?
        val text = StringBuilder()
        try {
            while (buffreader.readLine().also { line = it } != null) {
                text.append(line)
                text.append('\n')
            }
        } catch (e: IOException) {
            return null
        }
        return text.toString()
    }
    @JvmStatic
    fun writeAllCachedText(context: Context, filename: String, text: String): Boolean {
        val file = File(context.cacheDir, filename)
        return writeAllText(file, text)
    }

    fun writeAllFileText(filename: String, text: String): Boolean {
        return try {
            val outputStream = FileOutputStream(filename)
            writeAllText(outputStream, text)
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }

    fun writeAllText(file: File?, text: String): Boolean {
        return try {
            val outputStream = FileOutputStream(file)
            writeAllText(outputStream, text)
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }

    fun writeAllText(outputStream: OutputStream, text: String): Boolean {
        val outputWriter = OutputStreamWriter(outputStream)
        val bufferedWriter = BufferedWriter(outputWriter)
        var success = false
        try {
            bufferedWriter.write(text)
            success = true
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            try {
                bufferedWriter.close()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        return success
    }
}