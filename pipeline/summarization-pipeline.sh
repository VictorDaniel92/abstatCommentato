#!/bin/bash  Questo script crea la directory ../data/summaries/system-test e richiama lo script successivo                                                                                       

function as_absolute(){
	echo `cd $1; pwd`       #ritorna il path assoluto della mia posizione SE MI SPOSTASSI nell'argomento ricevuto
}

set -e
relative_path=`dirname $0`     #contiene il percorso dello script RELATIVO alla posizione da dove è stato eseguito: se lo eseguo dalla cartella che conteine lo script relative_path=.   se lo eseguo tipo da /home/renzo con ./Scrivania/links/dir1/pipeline/boh.sh allora relative_path=/Scrivania/links/dir1/pipeline

current_directory=`cd $relative_path;pwd`  #contiene il path assoluto della mia posizione SE MI SPOSTASSI in relative_path. Contiene il percorso assoluto dello script

#NOTARE CHE SONO ANCORA NELLA POSIZIONE DA DOVE HO ESEGUITO LO SCRIPT.

dataset=$1    #guardando le cartelle credo che l'argomento ricevuto sia "system-test"

data=$(as_absolute $current_directory/../data/datasets/$dataset)   #data =  il path assoluto della cartella  .../data/datasets/system-test I DUE PUNTI TI PORTANO ALLA CARTELLA SUPERIORE
results=$current_directory/../data/summaries/$dataset              #results = il path della cartella         .../data/summaries/system-test

mkdir -p $results                                                  #crea la cartella ../data/summaries/system-test
results=$(as_absolute $results)                                    #results = il path assoluto della cartella  .../data/summaries/system-test


echo "Running the summarization pipeline"
echo "With data from $data"
echo "Saving results in $results"

cd $current_directory          #mi sposto nela cartella dello script (perchè sono ancora nella posiione da dove l'ho chiamato)
./run-summarization.sh $data $results         #eseguo questo script passanto $data(=.../data/datasets/system-test) e $results(=.../data/summaries/system-test)

echo "Done"

