# Steps for Making a Digital Edition

## Create a Safe Place for Your Work

- Get an account on GitHub (https://github.com).
- Create a new repository with a short descriptive name, *e.g.* `nepos_work`.
	- Create a `README.md` file in the process.
- Invite others as collaborators.
- Clone the repository to your VM at `/vagrant/`
- Set your username for that repository:

	> `git config --global user.name "Mona Lisa"`

## Understand the Git Cycle

- Start VM; log in; navigate to your project, *e.g.* `cd /vagrant/nepos_work`.
- `git pull` gets any changes anyone else has made.
- Do some work on your files.
- `git add [filename]` to mark a file as something you want to save.
- `git commit -m "short message"` commits a version of that file.
- `git push` saves your repository to the cloud on GitHub.

## Start an Editing Script

The point of using a computer is to automate things so you don't have to do every step yourself. We will make a script that will capture all the steps to edit, validate, and verify your digital edition.

- `cd /vagrant/fyw-scala` Navigate into the Scala project folder.
- `git pull` Get updates to this repository. This will add a file `editing.sc` to your directory.
- `sbt console` Load the programming environment (REPL).
- `:load editing.sc` Loads the script and runs any commands in it.

## Understand the Scripting Cycle

In `/vagrant/fyw-scala`…

- `sbt console` Load the REPL.
- `:load editing.sc` Run your script.
- Edit your script in Atom.
- `:load editing.sc` Run it again.
- `:quit` To get out of the REPL.

## Start Documenting Your Work

You should have a Git repository for your work, containing two files: `[name]-paper.md` and `[name]-tech.md`. You will document your editorial work in the latter, *e.g.* `blackwell-tech.md`.

Start this file by providing the bibliographical citation to the source of the translation, as well as a shout-out to whoever put it online, with a URL.

## Get the Text

- Make a new, blank file in `/vagrant/fyw-scala/data` named `working.txt`.
- Copy and paste the text of your essay from the web-page into this file. Save the file.

## Special Git Update

Because you played around in the file `editing.sc`, you need to replace it with an up-to-date, pristine copy of the latest version. To *replace* a file on your filesystem with an up-to-date copy, use:

    git checkout editing.sc

Then do the usual…

    git pull

(From now on, these instructions will assume you did a `cd /vagrant/fyw-scala` in the VM beforehand, unless otherwise noted.)

## Load the Text into Scala

In the Scala console (`sbt console`)…

- `:load editing.sc` This loads some libraries ( = "programming already done by other people" that you will be using. )
- Save as a **value** (`val`) the *path* to your copied-and-pasted text.

~~~
// Identify the file we are working from
val inputFile:String = "data/working.txt"
~~~

- Load that text into a big **string**.

~~~
// Load the contents of that file into a Vector of Strings
val bigString:String = Source.fromFile(inputFile).getLines.mkString(" ")
~~~


## Split the Text into Sentences

Practice in Atom: Open `working.txt` in Atom.

What marks the end of a sentence?



…
