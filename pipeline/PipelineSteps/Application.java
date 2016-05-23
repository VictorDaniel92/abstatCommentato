QUesta classe contiene delle rotte  associate  a dei response.
Ci sono rotte che mappano testi, files(html), APIs.

Nel caso delle rotte che mappano API abbiamo:

.mapJson("/api/v1/autocomplete/concepts", new SolrAutocomplete(new SolrConnector(), "concept-suggest"))
risponderebbe a una richiesta come: http://localhost/api/v1/autocomplete/concepts?dataset=system-test&q=rs
Questa api prende in input i parametri dataset e q e li usa per fare una query al motore di ricerca full-text Solr. Questo risponde con un file json in cui elenca eventuali concetti nel dataset dato che contengono la sequenza di caratteri indicata da q.
IL metodo sendTo della classe JsonResponse, si occupa di settare alcuni header nela risposta e di copiare l'inputStream sull'outputStream.
L'inputStream è ottenuto chiamando get() di SolrAutocomplete e passandogli i parametri della richiesta(dataset, q). get() si occuperà di costruire un pezzo della query per solr, del tipo: "/solr/indexing/concept-suggest/?parametriVari". Questo pezzo di query verrà mandato a query() del connettore, il quale aggiungerà il campo host fancedo diventare la query tipo: "http://localhost:8891/solr/indexing/concept-suggest/?parametriVari". Viene infine mandata la query al motore solr e al sua risposta mandata a chi ha invocato la rotta.


.mapJson("/api/v1/autocomplete/properties", new SolrAutocomplete(new SolrConnector(), "property-suggest"))
