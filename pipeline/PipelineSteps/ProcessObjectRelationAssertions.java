//INPUT: /abstat/data/datasets/system-test/organized-splitted-deduplicated    /abstat/data/summaries/system-test/min-types/min-type-results   ../data/summaries/system-test/patterns

Lavora sull'insieme di file con suffisso "_obj_properties.nt" nella directory ../datasets/system-test/organized-splitted-deduplicated.
Per ogni file:
    Preso l'insieme di triple nel file(che ha solo objectrelational assertions) calcola 
       - gli AKPs di ogni tripla e frequenze oer ogni AKP
       - le frequenze delle properties di ogni tripla
Tutte le info ricavate da ogni tripla vengono salvate in un solo oggetto counts:OverallObjectRelationsCounting

//FILES OUTPUT:  ______________________________________________________________________________________________________________________________________________________________
Scrive su file le info contenute in counts:
     - scrive il conteggio delle properties patterns/in count-object-properties.txt
     - scrive conteggio AKPs in patterns/object-akp.txt 
//______________________________________________________________________________________________________________________________________________________________________________
