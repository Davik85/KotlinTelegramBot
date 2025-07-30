package ru.mrdavik.trainer.telegram

import org.mozilla.universalchardet.UniversalDetector
import java.io.File
import java.nio.charset.Charset


object EncodingUtil {
    fun readFileAutoEncoding(file: File): List<String> {
        val buf = file.readBytes()
        val detector = UniversalDetector(null)
        detector.handleData(buf, 0, buf.size)
        detector.dataEnd()
        val charset = detector.detectedCharset ?: "UTF-8"
        detector.reset()
        return file.readLines(Charset.forName(charset))
    }
}
