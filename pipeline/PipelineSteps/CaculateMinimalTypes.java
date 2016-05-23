// INPUT: /abstat/data/datasets/system-test/ontology    /abstat/data/datasets/system-test/organized-splitted-deduplicated"    /abstat/data/summaries/system-test/min-types/min-type-results

MinimalTypesCalculation minimalTypes = new MinimalTypesCalculation(ontologyModel, targetDirectory):
   Riestae le properties
   Riestrae i oncetti
   Riestrae le relazioni di SubClassOf
   DRExtractor.setConceptsDomainRange(concepts, properties):  estrazione info su domain e range di ogni property(e anche subproperties e inverse properties di questa). E' possibile anche che 
   vengano trovati dei nuovi concetti
    
   this.graph = new TypeGraph(concepts, subclassRelations): torna il typeGraph(vertici=concetti, archi=reazioni di subClassOf)a partire dai concetti e dalle relazioni di SubClassOf

new ParallelProcessing(typesDirectory, "_types.nt").process(minimalTypes):
   //FILES OUTPUT:  ______________________________________________________________________________________________________________________________________________________________
   - Scrive su abstat/data/summaries/system-test/min-types/min-type-results il file del conteggio di ogni concetto nell'ontologia. In altre parole, avremo dei file che 
   che elencheranno tutti i concetti dell'ontologia con associato un contatore. Tale contatore segna +1 su un concetto ogni volta che tra i relational assertions si fa 
   riferimento a tale concetto. Ricorda che si serve dei file in organized-splitted-deduplicated/..._types.nt, cioè i type asserts, ovvero l'output(una parte) dello script awk.
   - Scrive il file  dei concetti esterni, 
   - Scrive il file dei minimal types di ogni entity del dataset. vedi summaries/system-test/min-types/min-type-results
   //______________________________________________________________________________________________________________________________________________________________________________


   
