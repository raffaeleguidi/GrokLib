import net.stoerr.grokconstructor._

object GrokRunner {
    def main(args: Array[String]) {

        val pattern2match = """^%{IPORHOST:clientip} (?:-|%{USER:ident}) (?:-|%{USER:auth}) \[%{HTTPDATE:timestamp}\] \"(?:%{WORD:verb} %{NOTSPACE:request}(?: HTTP/%{NUMBER:httpversion})?|-)\" %{NUMBER:response} (?:-|%{NUMBER:bytes})"""
        var lines = """10.121.123.104 - - [01/Nov/2012:21:01:04 +0100] "GET /cluster HTTP/1.1" 200 1272
10.121.123.104 - -    [01/Nov/2012:21:01:17 +0100] "GET /cpc/auth.do?loginsetup=true&targetPage=%2Fcpc%2F HTTP/1.1" 302 466
10.121.123.104 - - [01/Nov/2012:21:01:18 +0100] "GET /cpc?loginsetup=true&targetPage=%252Fcpc%252F HTTP/1.1" 302 -
10.121.123.104 - - [01/Nov/2012:21:01:18 +0100] "GET /cpc/auth.do?loginsetup=true&targetPage=%25252Fcpc%25252F&loginsetup=true HTTP/1.1" 302 494
"""
        val additionalPatterns = "TEST %{GREEDYDATA}"
        val newline = "\n"
        val lib = GrokPatternLibrary.load("patterns", additionalPatterns)
        val patternGrokked = GrokPatternLibrary.replacePatterns(pattern2match, lib)
        val regex = new JoniRegex(patternGrokked)

        for (line <- lines.split(newline)) yield {
              regex.findIn(line) match {
                case None =>
                  println("NOT MATCHED: \"" + line + "\"")
                case Some(jmatch) =>
                  println("MATCHED: \"" + line + "\"")
                  for ((name, nameResult) <- jmatch.namedgroups) println("\t" + name + ": \"" + nameResult + "\"")
              }
          }
    }
}





    /*
	  def showResult(pat: String): NodeSeq = {
	    try {
	      val patternGrokked = GrokPatternLibrary.replacePatterns(pat, form.grokPatternLibrary)
	      val regex = new JoniRegex(patternGrokked)
	      try {
	        val lines: Seq[String] = form.multilineFilter(form.loglines.valueSplitToLines)
	        return <hr/> ++ <table class="bordertable narrow">
          {for (line <- lines) yield {
	            rowheader2(line) ++ {
	              regex.findIn(line) match {
	                case None =>
	                  val (jmatch, subregex) = longestMatchOfRegexPrefix(pat, line)
	                  row2(warn("NOT MATCHED")) ++
	                    row2("Longest prefix that matches", subregex) ++ {
	                    for ((name, nameResult) <- jmatch.namedgroups) yield row2(name, visibleWhitespaces(nameResult))
	                  } ++ ifNotEmpty(jmatch.before, row2("before match:", jmatch.before)) ++
	                    ifNotEmpty(jmatch.after, row2("after match: ", jmatch.after))
	                case Some(jmatch) =>
	                  row2(<b>MATCHED</b>) ++ {
	                    for ((name, nameResult) <- jmatch.namedgroups) yield row2(name, visibleWhitespaces(nameResult))
	                  } ++ ifNotEmpty(jmatch.before, row2("before match:", jmatch.before)) ++
	                    ifNotEmpty(jmatch.after, row2("after match: ", jmatch.after))
	              }
	            }
	          }}
        </table>
	      } catch {
	        case multilineSyntaxException: SyntaxException =>
	          return <hr/> ++ <p class="box error">Syntaxfehler in the pattern for the multiline filter
            {form.multilineRegex.value.get}
            :
            <br/>{multilineSyntaxException.getMessage}
          </p>
	      }
	    } catch {
	      case patternSyntaxException: SyntaxException =>
	        return <hr/> ++ <p class="box error">Syntaxfehler in the given pattern
          {pat}
          :
          <br/>{patternSyntaxException.getMessage}
        </p>
	      case patternUnknownException: GrokPatternNameUnknownException =>
	        return <hr/> ++ <p class="box error">This grok pattern has an unknown name
          {patternUnknownException.patternname}
          :
          {patternUnknownException.pattern}
        </p>
	    }
	  }
	  *
	  * */













