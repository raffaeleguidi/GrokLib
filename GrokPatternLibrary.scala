package net.stoerr.grokconstructor

import java.io.InputStream

import scala.io.Source
import scala.util.matching.Regex
import java.io.File
import java.io.FileInputStream

/**
 * @author <a href="http://www.stoerr.net/">Hans-Peter Stoerr</a>
 * @since 17.02.13
 */
object GrokPatternLibrary {

    def getListOfFiles(dir: String): List[File] = {
      val d = new File(dir)
      if (d.exists && d.isDirectory) {
        d.listFiles.filter(_.isFile).toList
      } else {
        List[File]()
      }
    }

    def patternsInFolder(dir: String): List[String] = {
        getListOfFiles(dir).map(_.getName)
    }

    def load(dir: String, additionalPatterns: String): Map[String, String] = {
        GrokPatternLibrary.mergePatternLibraries(
            dir,
            GrokPatternLibrary.patternsInFolder(dir),
            Option(additionalPatterns)
        )
    }



  val grokpatternnames = List("firewalls", "grok-patterns", "haproxy", "java", "junos", "linux-syslog", "mcollective",
    "mcollective-patterns", "mongodb", "nagios", "postgresql", "redis", "ruby")

  def mergePatternLibraries(dir: String, libraries: List[String], extrapatterns: Option[String]): Map[String, String] = {
    val extrapatternlines: Iterator[String] = extrapatterns.map(Source.fromString(_).getLines()).getOrElse(Iterator())
    val grokPatternSources = for (grokfile <- libraries) yield grokSource(dir, grokfile).getLines()
    val allPatternLines = grokPatternSources.fold(extrapatternlines)(_ ++ _)
    readGrokPatterns(allPatternLines)
  }

  def grokSource(dir: String, location: String): Source = {
    val inputStream = new FileInputStream(new File(dir + "/" + location))
    if (null == inputStream) throw new IllegalArgumentException("Could not find " + location)
    println(location)
    Source.fromInputStream(inputStream)
  }

  /** Reads the patterns from a source */
  def readGrokPatterns(src: Iterator[String]): Map[String, String] = {
    val cleanedupLines = src.filterNot(_.trim.isEmpty).filterNot(_.startsWith("#"))
    val grokLine = "(\\w+) (.*)".r
    cleanedupLines.map {
      case grokLine(name, grokregex) => name -> grokregex
      case other => sys.error("Can't understand the following line in the additional grok patterns - \n" +
        "it doesn't seem to be a normal line for a grok pattern file consisting of " +
        "a key, a space and a definition. For example, this would be correct:\n# Some comment\nUSERNAME [a-zA-Z0-9._-]+\nUSER %{USERNAME}\n" +
        "\nThe troublesome line is:\n\n>>" + other + "<<< " )
    }.toMap
  }

  /** We replace patterns like %{BLA:name} with the definition of bla. This is done
    * (arbitrarily) 10 times to allow recursions but to not allow infinite loops. */
  def replacePatterns(grokregex: String, grokMap: Map[String, String]): String = {
    var substituted = grokregex
    val grokReference = """%\{(\w+)(?::(\w+)(?::(?:int|float))?)?\}""".r
    0 until 10 foreach {
      _ =>
        substituted = grokReference replaceAllIn(substituted, {
          m => {
            val patternName = m.group(1)
            if (!grokMap.contains(patternName)) throw new GrokPatternNameUnknownException(patternName, m.group(0))
            "(?" + Option(m.group(2)).map(Regex.quoteReplacement).map("<" + _ + ">").getOrElse(":") +
              Regex.quoteReplacement(grokMap(patternName)) + ")"
          }
        })
    }
    substituted
  }

}

class GrokPatternNameUnknownException(val patternname: String, val pattern: String) extends RuntimeException {
  override def toString: String = "Grok pattern name " + patternname + " unknown at " + pattern
}
