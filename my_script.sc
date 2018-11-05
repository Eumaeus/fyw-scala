// Identify the file we are working from
val inputFile:String = "data/working.txt"

// Load the contents of that file into a Vector of Strings
val bigString:String = Source.fromFile(inputFile).getLines.mkString(" ")
