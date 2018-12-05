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

def splitWithSplitter(text:String, splitters:String = "[. ?!]"):Vector[String] = {
	val regexWithSplitter = s"(?<=${splitters})"
	text.split(regexWithSplitter).toVector.filter(_.size > 0)
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
		//val tokens:Vector[String] = splitWithSplitter(passage, splitters)
		val tokens:Vector[String] = passage.split(splitters).toVector.filter(_.size > 0)
		val tokenizedNodes:Vector[CitableNode] = {
			tokens.zipWithIndex.map{ case (n, i) => {
				val newUrn:CtsUrn = CtsUrn(s"${exemplarUrn}${editionCitation}.${i}")
				val newNode:CitableNode = CitableNode(newUrn, n)
				newNode
			}}.filter(_.text != " ").filter(_.text.size > 0).toVector
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
