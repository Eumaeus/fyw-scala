import scala.io.Source
import java.io._
import java.util.Calendar._
import Console._
import edu.holycross.shot.scm._
import edu.holycross.shot.cite._
import edu.holycross.shot.ohco2._
import edu.holycross.shot.seqcomp._
import co.theasi.plotly
import util.Random

//val cexData = Source.fromFile("/Users/cblackwell/Dropbox/CITE/digital-libraries/hmt-archive/releases-cex/hmt-2018b.cex").getLines.mkString("\n")
//val cexData = Source.fromFile("data/kjv_sampler.cex").getLines.mkString("\n")
val cexData = Source.fromFile("/cite/dls/fuCiteDX/herodotus1.cex").getLines.mkString("\n")
val library = CiteLibrary(cexData,"#",",")

val corpus = library.textRepository.get.corpus

println("")
println("=============================================")
println("Texts in Library")
println("")
for (t <- library.textRepository.get.catalog.texts) {
   println(t)
	 println(t.urn)
	 println("")
 }
println("=============================================")
println("")

def printNode(n:CitableNode):Unit = { println(s"${n.urn}\t ${n.text}") }
def printCorpus(c:Corpus) = for (cn <- c.nodes) println(s"${cn.urn}\t ${cn.text}")
def printVector(v:Vector[CitableNode]) = for (cn <- v) println(s"${cn.urn}\t ${cn.text}")
def printTexts(tr:TextRepository):Unit = {
	println("")
	println("=============================================")
	for (t <- tr.catalog.texts) {
		 println(t)
		 println(t.urn)
		 println("")
	}
	println("=============================================")
	println("")
}

def psgRef(u:CtsUrn):String = {
  val psgRef = u.passageComponentOption match {
	      case None => ""
    	  case s: Option[String] => s.get    
  }
  psgRef
}

def splitWithSplitter(text:String, splitters:String = "[.?!]"):Vector[String] = {
	val regexWithSplitter = s"(?<=${splitters})"
	text.split(regexWithSplitter).toVector
}

def propertyUrnFromPropertyName(urn:Cite2Urn, propName:String):Cite2Urn = {
		val returnUrn:Cite2Urn = {
			urn.propertyOption match {
				case Some(po) => urn // just return it!
				case None => {
					val collUrn:Cite2Urn = urn.dropSelector
					val collUrnString:String = collUrn.toString.dropRight(1) // remove colon
					urn.objectComponentOption match {
						case Some(oc) => {
							Cite2Urn(s"${collUrnString}.${propName}:${oc}")
						}
						case None => {
							Cite2Urn(s"${collUrnString}.${propName}:")
						}
					}
				}
			}
		}
		returnUrn
	}	


val splitters:String = """[;., ·:"?!()–—-]"""

def tokenizeString(str:String, splitters:String):Vector[String] = { 
	val tokenizedString:Array[String] = str.split(splitters)
	val withBlanksRemoved:Array[String] = tokenizedString.filter(_.size > 0)
	val returnVector:Vector[String] = withBlanksRemoved.toVector
	returnVector
}

def tokenizeCtsNode(node:CitableNode, splitters:String, exemplarID:String = "token"):Vector[CitableNode] = {
	try {
		val editionUrn:CtsUrn = node.urn.dropPassage

		// Check that the URN is at the Version level
		if ( editionUrn.exemplarOption != None) throw new Exception(s"The text cannot already be an exemplar! (${editionUrn})")
		// If we get here, we're fine
		val exemplarUrn:CtsUrn = editionUrn.addExemplar(exemplarID)
		val editionCitation:String = node.urn.passageComponent
		val passage:String = node.text
		val tokens:Vector[String] = tokenizeString(passage, splitters)
		val tokenizedNodes:Vector[CitableNode] = {
			tokens.zipWithIndex.map{ case (n, i) => {
				val newUrn:CtsUrn = CtsUrn(s"${exemplarUrn}${editionCitation}.${i}")
				val newNode:CitableNode = CitableNode(newUrn, n)
				newNode
			}}.toVector	
		}
		tokenizedNodes
	} catch {
		case e:Exception => throw new Exception(s"${e}")
	}
}

def tokenizeCorpus(c:Corpus, splitters:String, exemplarID:String = "token"):Corpus = {
	val nodeVector:Vector[CitableNode] = c.nodes.map( n => tokenizeCtsNode(n, splitters, exemplarID)).flatten
	val newCorpus:Corpus = Corpus(nodeVector)
	newCorpus
}

def editionFromExemplar(c:Corpus, versionId:String):Corpus = {
	val versionUrns:Vector[CtsUrn] = c.nodes.map(_.urn.collapsePassageBy(1)).distinct
	val versionVector:Vector[CitableNode] = {
		versionUrns.map( u => {
			val passageTokens:Corpus = c ~~ u	
			val newVersionUrn:CtsUrn = u.dropVersion.addVersion(versionId)
			val newPassage:String = {
				passageTokens.nodes.map(_.text).mkString(" ")
			}
			CitableNode(newVersionUrn, newPassage)
		})
	}
	Corpus(versionVector)
}

def saveString(s:String, filePath:String = "sbt_console_output.txt") = {
	val pw = new PrintWriter(new File(filePath))
	for (line <- s.lines){
		pw.append(line)
		pw.append("\n")
	}
	pw.close

}

def loadWordList(filepath:String):Vector[String] = {
	val lexData:Vector[String] = Source.fromFile(filepath).getLines.toVector
	lexData
}

val baseLexicon:Vector[String] = loadWordList("data/words.txt")
val customLexicon:Vector[String] = loadWordList("data/mywords.txt")

/* Returns a Corpus containing only citable nodes with words not present */
/* This version assumes the input Corpus is tokenized… one word per citable node. */
/* This is case-sensitive! */
def spellCheck(corp:Corpus, lex:Vector[String]):Corpus = {
	val badWordCorpus:Corpus = {
		val badWordVec:Vector[CitableNode] = corp.nodes.filter( n => {
			lex.contains(n.text) == false
		}).toVector
		Corpus(badWordVec)
	}	
	badWordCorpus
}

def fixAllCaps(s:String):String = {
	val matcher = "[A-Z]+".r
	val gotMatch:Option[String] = matcher.findFirstIn(s)		
	val returnString:String = {
		gotMatch match {
			case Some(m) => {
				if (s == m) { s.toLowerCase.capitalize }
				else { s }
			}
			case None => s
		}
	}
	returnString
}

def invalidateString(s:String):Vector[Char] = {
	val validChars = """[0-9A-Za-z.,;?:'" )(!]""".r
	val charVector:Vector[Char] = s.toVector
	val invalidChars:Vector[Char] = charVector.filter(c => {
		validChars.findAllIn(c.toString).size == 0	
	})
	invalidChars
}

def invalidateCitableNode(n:CitableNode):Option[CitableNode] = {
	val text:String = n.text
	val invalidChars:Vector[Char] = invalidateString(text)
	invalidChars.size match {
		case s if (s > 0) => {
			val invalidCharString:String = invalidChars.map(c => {
				val newString = s""""${c}""""
				newString
			}).mkString(",")
			Some(CitableNode(n.urn, invalidCharString))
		}
		case _ => None
	}			
}

def invalidateCorpus(c:Corpus):Option[Corpus] = {
	val invalidNodeOptions:Vector[Option[CitableNode]] = c.nodes.map( n => {
		invalidateCitableNode(n)
	})
	val invalidNodes:Vector[CitableNode] = invalidNodeOptions.filter( n => {
		n != None
	}).map( sn => sn.get )

	if ( invalidNodes.size > 0 ) Some(Corpus(invalidNodes)) else None
}


def drawPlot = {
	import co.theasi.plotly._
	import util.Random

	// Generate uniformly distributed x
	val xs = (0 until 100)

	// Generate random y
	val ys = (0 until 100).map { i => i + 5.0 * Random.nextDouble }

	val p = Plot().withScatter(xs, ys)

	draw(p, "basic-scatter", writer.FileOptions(overwrite=true))
	// returns  PlotFile(pbugnion:173,basic-scatter)
}



 val matthewUrn = CtsUrn("urn:cts:greekLit:tlg0031.tlg001.fu_kjv:")
 val markUrn = CtsUrn("urn:cts:greekLit:tlg0031.tlg002.fu_kjv:")
 val lukeUrn = CtsUrn("urn:cts:greekLit:tlg0031.tlg003.fu_kjv:")
 val johnUrn = CtsUrn("urn:cts:greekLit:tlg0031.tlg004.fu_kjv:")

def chunkByCitationLevel(corpus:Corpus, thisText:CtsUrn, level:Int):Vector[CtsUrn] = {
	val allReff:Vector[CtsUrn] = corpus.validReff(thisText)
	// we only want passages that are at least N levels deep!
	val filteredForDepth:Vector[CtsUrn] = {
		allReff.filter(r => { 
			r.citationDepth.filter(_ >= level ).size > 0
		})
	}
	// Drop the level of all URNs to N, then just keep the unique ones.
	val chunkedUrns:Vector[CtsUrn] = filteredForDepth.map(_.collapsePassageTo(level)).distinct
	chunkedUrns // return the vector!
}

val testChunkByCitationLevel:Vector[CtsUrn] = chunkByCitationLevel(corpus, johnUrn, 1) // should equal 21 books

def chunkIntoPieces(corpus:Corpus, thisText:CtsUrn, numChunks:Int):Vector[CtsUrn] = {
	val allReff:Vector[CtsUrn] = corpus.validReff(thisText)
	val chunkSize:Int = allReff.size / numChunks
	// divide it into `numChunks` groups, with the last possibly having fewer members
	val groupVectors:Vector[Vector[CtsUrn]] = allReff.grouped(chunkSize).toVector
	// We don't want chunks, but a single-level vector of URNs
	val urnVector:Vector[CtsUrn] = {
		groupVectors.map(v => {
			val firstUrn:CtsUrn = v.head
			val lastUrn:CtsUrn = v.last
			val endPassage:String = lastUrn.passageComponent	
			val rangeUrn:CtsUrn = CtsUrn(s"${firstUrn}-${endPassage}")
			rangeUrn
		})
	}
	urnVector
}

val testChunkIntoPieces:Vector[CtsUrn] = chunkIntoPieces(corpus, johnUrn, 110) // should have size of 8

def sliceText(corpus:Corpus, thisText:CtsUrn, index:Int, numPassages:Int):CtsUrn = {
	val allReff:Vector[CtsUrn] = corpus.validReff(thisText)
	// don't forget that index starts at zero!
	val endIndex:Int = index + numPassages
	val slicedReff:Vector[CtsUrn] = allReff.slice(index, endIndex)	
	val firstUrn:CtsUrn = slicedReff.head
	val endPassage:String = slicedReff.last.passageComponent
	val rangeUrn:CtsUrn = CtsUrn(s"${firstUrn}-${endPassage}")
	rangeUrn
}

val testSliceText1:CtsUrn = sliceText(corpus, johnUrn, 0, 4)
println(testSliceText1.toString)
val testSliceText2:CtsUrn = sliceText(corpus, johnUrn, 876, 4)
println(testSliceText2.toString)

def countEnglishWordsInNode(passage:CitableNode):Int = {
	val splitters:String = "[;., :?()]"
	val englishWords:Vector[String] = tokenizeString(passage.text, splitters)
	val howManyWords:Int = englishWords.size
	howManyWords
}

def countSomeCharsInNode(passage:CitableNode, chars:String):Int = {
	val matcher:scala.util.matching.Regex = s"[${chars}]".r
	val howMany:Int = matcher.findAllIn(passage.text).size
	howMany
}

def sumEnglishWordsInCorpus(corpus:Corpus):Int = {
	// For summing, we need, not a vector of CitableNodes, but a vector of Ints,
	// …that is, we want to replace CitableNodes with their word-counts
	val countVec:Vector[Int] = corpus.nodes.map(n => countEnglishWordsInNode(n))
	// Now we can just do a quick summation with the awesome "foldLeft" method
	val count:Int = countVec.reduceLeft( _ + _ )
	count
}

def sumSomeCharsInCorpus(corpus:Corpus, chars:String):Int = {
	// For summing, we need, not a vector of CitableNodes, but a vector of Ints,
	// …that is, we want to replace CitableNodes with their counts
	val countVec:Vector[Int] = corpus.nodes.map(n => countSomeCharsInNode(n, chars))
	// Now we can just do a quick summation with the awesome "foldLeft" method
	val count:Int = countVec.reduceLeft( _ + _ )
	count
}

def wordsPerChar(corpus:Corpus, chars:String):Double = {
	val wordCount:Int = sumEnglishWordsInCorpus(corpus)
	val charCount:Int = sumSomeCharsInCorpus(corpus, chars)
	// guard against divide-by-zero error!
	val wpc:Double = {
		if (charCount > 0) { wordCount / charCount } else { 0 }
	}
	wpc
}

def toTEI(corpus:Corpus):String = {
	val title:String = "CTS Corpus produced by the OHCO2 Library 10.9.0"
	val pubStmt:String = "Published on DATE"
	val srcStmt:String = "Generated by OHCO2 10.9.0."
	val headerStuff:String = s"""<?xml version="1.0" encoding="UTF-8"?>
<?xml-model href="http://www.tei-c.org/release/xml/tei/custom/schema/relaxng/tei_minimal.rng" type="application/xml" schematypens="http://relaxng.org/ns/structure/1.0"?>
<?xml-model href="http://www.tei-c.org/release/xml/tei/custom/schema/relaxng/tei_minimal.rng" type="application/xml"
schematypens="http://purl.oclc.org/dsdl/schematron"?>
<TEI xmlns="http://www.tei-c.org/ns/1.0">
<teiHeader>
<fileDesc>
<titleStmt>
<title>${title}</title>
</titleStmt>
<publicationStmt>
<p>${pubStmt}</p>
</publicationStmt>
<sourceDesc>
<p>${srcStmt}</p>
</sourceDesc>
</fileDesc>
</teiHeader>
<text>
<body>
"""
	val footerStuff:String = """</body> </text> </TEI> """

	val nodesToTEI:Vector[String] = {
		corpus.nodes.map{ n => {
			s"""<p n="${n.urn}" >${n.text}</p>"""
		}}
	}

	val corpusText:String = nodesToTEI.mkString("\n")
	val teiXml:String = s"${headerStuff}\n${corpusText}\n${footerStuff}"
	teiXml

}