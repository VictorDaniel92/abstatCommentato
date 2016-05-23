cd ..abstat/summarization

JAVA_HOME="/usr" #Server: /usr/lib/jvm/java-6-sun
debug=1 #0: Disabled, 1:Enabled
dbgCmd="/usr/bin/time -f \"COMMAND: %C\nTIME: %E real\t%U user\t%S sys\nCPU: %P Percentage of the CPU that this job got\nMEMORY: %M maximum resident set size in kilobytes\n\n\" "

DataDirectory=$1            #../data/datasets/system-test
ResultsDirectory=$2         #../data/summaries/system-test
AwkScriptsDirectory=awk-scripts
TripleFile=dataset.nt


OntologyFile="$DataDirectory/ontology/"                    #../data/datasets/system-test/ontology
ReportDirectory="$ResultsDirectory/reports/"               #../data/summaries/system-test/reports
TmpDatasetFileResult="$ResultsDirectory/reports/tmp-data-for-computation/"    ##../data/summaries/system-test/reports/tmp-data-for-computation/


DatasetFile="$DataDirectory/triples"                                       #contiene il dataset  ../data/datasets/system-test/triples 
tmpDatasetFile="$DataDirectory/organized-splitted-deduplicated-tmp-file"   
orgDatasetFile="$DataDirectory/organized-splitted-deduplicated"

minTypeResult="$ResultsDirectory/min-types/min-type-results"

log_file="../data/logs/summarization/log.txt" 

IFS=',' read -a splitters <<< "0,1,2,3,4,5,6,7,8,9,a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,%,_,others" #splitters[] è un array e contiene tutti questi caratteri
NProc=4
NUM=0
QUEUE=""

#aggiunge il processo ricevuto in QUEUE
function queue {
	QUEUE="$QUEUE $1"
	NUM=$(($NUM+1))
}

#aggiorna la coda: prima la salva in OLDREQUEUE poi svuota QUEUEe poi ritravasa in QUEUE i processi ancora esistenti
function regeneratequeue {}

#controlla la coda: se scandendo QUEUE trova un processo che non esiste più fa l'aggiornamento
function checkqueue {}


rm -rf ${TmpDatasetFileResult}*  #rimuovo $ResultsDirectory/reports/tmp-data-for-computation/
mkdir -p $TmpDatasetFileResult   #lo ricreo
rm -f $log_file    
touch $log_file


{ 
echo "---Start: Ontology Report---"

	Chiama la classe ProcessOntology con due argomenti: "$OntologyFile" "$TmpDatasetFileResult"

echo "---End: Ontology Report---"
} &>> $log_file               #registra tutto nel log_file


{ 
	echo "---Start: Organize and Split files---"   #Divido i file da organizzare in NProc parti uguali l'uno così da parallelizzare l'organizzazione
	
	rm -rf $tmpDatasetFile 
	mkdir -p $tmpDatasetFile 

	#Il dataset deve essere splittato. datasetBlockSize1 contiene la dimensione che i vari pezzi devono avere.
	dataSize1=$(stat --printf="%s" $DatasetFile/$TripleFile) #contiene le dimensioni in byte di dataset.nt
	let "dataBlockSize1=($dataSize1/$NProc)+10000000" 

	#Processo da eseguire per lo splittaggio: splitFile contiene il comando che "splitta dataset.nt e salva i pezzi in $tmpDatasetFile"
	splitFile="split -u -C $dataBlockSize1 $DatasetFile/$TripleFile $tmpDatasetFile/1_lod_part_" 
                                  #dimensioni            file input          file output(1_lod_part_ è il prefisso di ogni pezzo)

	eval ${dbgCmd}""${splitFile}  #avvio processo che splitta il file 


	#Creo le stringhe contenenti i file da organizzare
	filePartCount=0
	stringFile[0]="aa"
	stringFile[1]="ab"
	stringFile[2]="ac"
	stringFile[3]="ad"
	for (( i=0; i<${#stringFile[@]}; i++ ))
	do
		filePart=""
		if [ -f $tmpDatasetFile/1_lod_part_${stringFile[$i]} ];  #se esiste un pezzo di dataset che si chiama 1_lod_part_${stringFile[$i]
		then
			if [ filePart == "" ]
			then
				filePart="$tmpDatasetFile/1_lod_part_${stringFile[$i]}"
			else
				filePart="${filePart} $tmpDatasetFile/1_lod_part_${stringFile[$i]}"
			fi
		fi
		filePartCom[$filePartCount]=${filePart}   //credo che alla fine contiene tutti i path dei pezzi dei 4 pezzi di dataset
		filePartCount=$(($filePartCount+1))	
	done

	rm -f $orgDatasetFile/*.nt  2>/dev/null 
	mkdir -p $orgDatasetFile

	#Processi che dividono il dataset in type assertion dai relational assertion- L'output è un insieme di file con un suffisso a seconda se sono typeAssertion(es: 1b_types.nt), relationalAssertion con oggetto una risorsa(es 1b_obj_properties.nt) e relationalAssertion con  relational con oggeto un letterale(es 1b_dt_properties). Per ogni pezzo di dataset(4) c'è un processo
	orgFile[0]="gawk -f $AwkScriptsDirectory/organize_data.awk -v prefix=1 -v destinatioDirectory=\"${orgDatasetFile}\" ${filePartCom[0]}"
	orgFile[1]="gawk -f $AwkScriptsDirectory/organize_data.awk -v prefix=2 -v destinatioDirectory=\"${orgDatasetFile}\" ${filePartCom[1]}"
	orgFile[2]="gawk -f $AwkScriptsDirectory/organize_data.awk -v prefix=3 -v destinatioDirectory=\"${orgDatasetFile}\" ${filePartCom[2]}"
	orgFile[3]="gawk -f $AwkScriptsDirectory/organize_data.awk -v prefix=4 -v destinatioDirectory=\"${orgDatasetFile}\" ${filePartCom[3]}"


	#Avvio l'esecuzione parallela dei processi
	for (( proc=0; proc<${#orgFile[@]}; proc++ ))  #for the rest of the arguments
	do
		eval ${dbgCmd}""${orgFile[$proc]} &
		PID=$!
		queue $PID  #aggiunge il processo alla coda
		while [ $NUM -ge $NProc ]; do  #se num>= numero di processi allora controlla la coda di processi
			checkqueue
		done
	done


	#Rimuovo i singoli pezzi di dataset
	rm -f $tmpDatasetFile/1_lod_part_aa
	rm -f $tmpDatasetFile/1_lod_part_ab
	rm -f $tmpDatasetFile/1_lod_part_ac
	rm -f $tmpDatasetFile/1_lod_part_ad
	rm -rf $tmpDatasetFile/ #Rimuovo la directory con i file temporanei, non più utili

	echo "---End: Organize and Split files---"
	echo ""
	echo "---Start: Deduplication of files---"
	
	#Creo i processi che andranno ad unire i file (2>/dev/null per non scrivere errori di file non esistenti, perchè il cat funziona comunque su tutti quelli che ci sono)
	numMerge=0
	for element in "${splitters[@]}"
	do
	   #Unisco i file types
	   mergeFile[$numMerge]="cat ${orgDatasetFile}/1${element}_types.nt ${orgDatasetFile}/2${element}_types.nt ${orgDatasetFile}/3${element}_types.nt ${orgDatasetFile}/4${element}_types.nt > ${orgDatasetFile}/${element}_types.nt 2>/dev/null"
	   numMerge=$(($numMerge+1))
	   #Unisco i file obj_properties
	   mergeFile[$numMerge]="cat ${orgDatasetFile}/1${element}_obj_properties.nt ${orgDatasetFile}/2${element}_obj_properties.nt ${orgDatasetFile}/3${element}_obj_properties.nt ${orgDatasetFile}/4${element}_obj_properties.nt > ${orgDatasetFile}/${element}_obj_properties.nt 2>/dev/null"
	   numMerge=$(($numMerge+1))
	   #Unisco i file dt_properties
	   mergeFile[$numMerge]="cat ${orgDatasetFile}/1${element}_dt_properties.nt ${orgDatasetFile}/2${element}_dt_properties.nt ${orgDatasetFile}/3${element}_dt_properties.nt ${orgDatasetFile}/4${element}_dt_properties.nt > ${orgDatasetFile}/${element}_dt_properties.nt 2>/dev/null"
	   numMerge=$(($numMerge+1))
	done

	#Unisco i file: Avvio l'esecuzione parallela dei processi
	for (( proc=0; proc<${#mergeFile[@]}; proc++ ))
	do
		eval ${dbgCmd}""${mergeFile[$proc]} &
		PID=$!
		queue $PID
		while [ $NUM -ge $NProc ]; do
			checkqueue
		done
	done

	#Rimuovo le singole parti (Separato perchè in parallelo si crea dipendenza tra coppie di processi, ed essendo la rimozione veloce si può gestire senza problemi così)
	for i in 1 2 3 4
	do
		for element in "${splitters[@]}"
		do	   
		   rm -f ${orgDatasetFile}/${i}${element}"_types.nt" #Elimino i file types  
		   rm -f ${orgDatasetFile}/${i}${element}"_obj_properties.nt" #Elimino i file obj_properties 
		   rm -f ${orgDatasetFile}/${i}${element}"_dt_properties.nt" #Elimino i file dt_properties
		done
	done

	#Creo i processi che andranno a rimuovere i duplicati
	numDedupl=0
	for element in "${splitters[@]}"
	do
	   #Elimino i duplicati dai file types
	   if [ -f ${orgDatasetFile}/${element}_types.nt ];
	   then
		   deduplFile[$numDedupl]="sort -u ${orgDatasetFile}/${element}_types.nt -o ${orgDatasetFile}/${element}_types.nt"
		   numDedupl=$(($numDedupl+1))
	   fi
	   #Elimino i duplicati dai file obj_properties
	   if [ -f ${orgDatasetFile}/${element}_obj_properties.nt ];
	   then
		   deduplFile[$numDedupl]="sort -u ${orgDatasetFile}/${element}_obj_properties.nt -o ${orgDatasetFile}/${element}_obj_properties.nt"
		   numDedupl=$(($numDedupl+1))
	   fi
	   #Elimino i duplicati dai file dt_properties
	   if [ -f ${orgDatasetFile}/${element}_dt_properties.nt ];
	   then
		   deduplFile[$numDedupl]="sort -u ${orgDatasetFile}/${element}_dt_properties.nt -o ${orgDatasetFile}/${element}_dt_properties.nt"
		   numDedupl=$(($numDedupl+1))
	   fi
	done


	#Avvio l'esecuzione parallela dei processi
	for (( proc=0; proc<${#deduplFile[@]}; proc++ )) # for the rest of the arguments
	do
		#echo ${deduplFile[$proc]}
		eval ${dbgCmd}""${deduplFile[$proc]} &
		PID=$!
		queue $PID
		while [ $NUM -ge $NProc ]; do
			checkqueue
		done
	done

	echo "---End: Deduplication of files---"
} &>> $log_file

echo "---Start: Counting---"
	rm -rf $minTypeResult
	mkdir -p $minTypeResult

	rm -rf $ResultsDirectory/patterns
	mkdir -p $ResultsDirectory/patterns

	Chiama CalculateMinimalTypes con argomenti: "$OntologyFile" "$orgDatasetFile" "$minTypeResult"
	Chama it.unimib.disco.summarization.export.AggregateConceptCounts con argomenti: "$minTypeResult" "$ResultsDirectory/patterns/"
	Chiama it.unimib.disco.summarization.export.ProcessDatatypeRelationAssertions con argomenti: "${orgDatasetFile}" "$minTypeResult" "$ResultsDirectory/patterns/"
	Chiama it.unimib.disco.summarization.export.ProcessObjectRelationAssertions con argomenti: "${orgDatasetFile}" "$minTypeResult" "$ResultsDirectory/patterns/"

	echo "---End: Counting---"
} &>> $log_file

{ 
	echo "---Start: Cleaning---"
	rm -f ${orgDatasetFile}/*_types.nt
	rm -f ${minTypeResult}/*_uknHierConcept.txt
	endBlock=$SECONDS

	echo "---End: Cleaning---"
} &>> $log_file


