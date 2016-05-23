// INPUT: /abstat/data/datasets/system-test/ontology      abstat/data/summaries/system-test/reports/tmp-data-for-computation/

Creo il modello ontologyModel:OntoModel che rappresenta l'ontologia in input

Estraggo le properties dall'ontologia e registro info su di esse in pExtract:PropertyExtractor
creo properties:Properties e ci copio tutti i dati di pExtract

Estraggo i concetti dall'ontologia e registro info su di esse in cExtract:ConceptExtractor
creo concepts:Concepts e ci copio tutti i dati di cExtract

Estraggo le relazioni di subClassOf dall'ontologia in SBExtractor:OntologySubClassOfExtractor (*1)
creo SubClassOfRelation:SubClassOf  e ci copio i dati di OntologySubClassOfExtractor

Estraggo da tutti i concetti che ho i concetti equivalenti. Li salvo in equConcepts:EqConceptExtractor.
creo equConcept:EquivalentConcepts e ci copio i dati di eqConcepts

//FILES OUTPUT:  ________________________________________________________________________________________________________________________________
- Scrivo su file l'elenco dei concetti dell'ontologia in reports/tmp-data-for-computation/Concepts.txt
- Scrivo su file l'elenco dei concetti dell'ontologia  che sono sottoclassi di altri concetti in reports/tmp-data-for-computation/SubclassOf.txt
//______________________________________________________________________________________________________________________________________________
(*1)
Per fare ciò chiama setConceptsSubclassOf(concepts, ontologyModel).
il metodo itera su ogni concetto finora trovato. Se tale concetto è definito nella nostra ontologia, trova i supertipi(se trova dei concetti nuovi li aggiunge a Concepts)
 dopodichè salva le relazioni di subClassOf nell'oggetto ConceptsSubclassOf, aggiorno il Counter di Concepts.
Se il concetto no è definito nell'ontologia facciamo una query per trovare le superclassi, dopodichè salviamo le relazioni di subClassOf. Se troviamo concetti nuovi
li aggiungiamo a Concepts.


NOTARE CHE:
ontologyModel è un oggetto ontModel crato a partire da un file ontologico. Questo oggetto permette la manipolazione dell'ontologia.Ad ogni passaggio si aggiungono nuove info a ontologyModel. Ad esempio dopo aver ricavato l'insieme delle properties guardando l'ontologia, diciamo a ontologyModel che tale insieme sono le sue properties. Analogamente per i concetti.
Le info su ontologyModel come le properies e concetti vengono poi salvate in strutture esterne a ontologyModel.



PROBLEMA1
in OntologySubClassOfExtractor riga 49: le info sulle superclassi di ogni concetto sono disponibili automaticamente?(cioè basta fornire il concetto e automaticamente so quali sono i suoi supertipi?) No perchè altrimenti non so cosa itera alla riga 47 se non ce li ho in automatico

PROBLEMA2
 OntologySubClassOfExtractor nella riga 61 controlla se il superconcetto è un nuovo concetto(lo fa controllando se il suo URI mappa qualcosa). Com'è possibile avere ancora dei nuovi concetti se in ConceptExtractor alla riga 64-65 abbiamo aggiunto a impicitClasses sia sogg che obj di ogni statement con predicato SubClassOF e successivamente implicitClasses è servito per dire a ontoloyModel quali sono i suoi concetti?

PROBLEMA3
Cosa succede se in updateCounter devo aggiornare un concetto che ha già un contesta A ma il metodo riceve come parametro B? Viene riscritto e perdo il contesto precedente? 
Ad esempio un concetto c2 può essere sottoclasse di c3 ma superclasse di c1. Quando eseguo riga 64-66 di OntologySubClassOfEztractor ad un certo punto c2 verrà aggiornato con due contsti diversi tenendono solo l'ultimo ricevuto?

DUBBIO1
OntologySubClassOfEztractor blocco catch riga 71: In che caso ho un concetto non definito nella mia ontologia? 
possibileRISP: L'elenco di concetti sono tuti quelli a cui si è fatto RIFERIMENTO(!=definito) nell'ontologia. Quindi è possibile che sia stato fatto riferimento a un concetto ma che non sia stato definito. Ciò saltafuori quando eseguendo il try soprastante genera un'eccezione

PROBLEMA4
 OntologySubClassOfExtractor query dalla riga 75: perchè fare una query sul supertipo di un concetto alla nostra ontologia se tale  non è definito nell'ontologia? 

