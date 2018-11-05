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

def splitWithSplitter(text:String, splitters:String = "[.?!]"):Vector[String] = {
	val regexWithSplitter = s"(?<=${splitters})"
	text.split(regexWithSplitter).toVector
}
