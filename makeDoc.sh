#!/bin/bash

fullfile=$1

filename="${fullfile##*/}"

justname=${filename%%.*}

echo $justname

PD=`which pandoc`

$PD -o "output/${justname}.docx" $filename

