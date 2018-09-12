import scala.io.Source
import edu.holycross.shot.scm._
import edu.holycross.shot.cite._
import edu.holycross.shot.ohco2._
import edu.holycross.shot.seqcomp._

//val cexData = Source.fromFile("/Users/cblackwell/Dropbox/CITE/digital-libraries/hmt-archive/releases-cex/hmt-2018b.cex").getLines.mkString("\n")
val cexData = Source.fromFile("data/plutarch_women.cex").getLines.mkString("\n")
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


