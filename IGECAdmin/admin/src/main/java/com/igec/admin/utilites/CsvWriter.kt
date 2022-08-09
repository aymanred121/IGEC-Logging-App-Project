package com.igec.admin.utilites

import kotlin.Throws
import android.os.Build
import android.os.Environment
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.lang.StringBuilder

class CsvWriter(vararg Values: String) {
    private var data: StringBuilder
    private val headerSize: Int
    init {
        headerSize = Values.size
        data = StringBuilder()
        addHeader(*Values)
    }
    private fun addHeader(vararg Values:String) {
        for (s in Values) {
            data.append(s).append(",")
        }
        data = data.deleteCharAt(data.length - 1)
        data.append("\n")
    }

    fun addDataRow(vararg Values: String): CsvWriter {
        require(Values.size == headerSize) { "Number of values must be equal to header size" }
        for (s in Values) {
            data.append(s).append(",")
        }
        if (data.substring(data.length - 1) == ",") {
            data = data.deleteCharAt(data.length - 1)
        }
        data.append("\n")
        return this
    }

    @Throws(IOException::class)
    fun build(fileName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val gulpfile = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                String.format("%S.csv", fileName)
            )
            val writer: FileWriter = FileWriter(gulpfile)
            writer.append(data.toString())
            writer.flush()
            writer.close()
        }
    }


}