# Revisione del 18 novembre 2026

Revisione dei sorgenti Android e web, dei componenti esportati, dei collegamenti esterni, del ripristino dello stato e della distribuzione. Le conclusioni riguardano la versione locale 0.1.1-demo. L'APK 0.1.0-demo conservato nel progetto appartiene alla build precedente.

## Problemi individuati e corretti

| Priorità | Problema | Correzione |
|---|---|---|
| Alta per l'uso come Home | Un avvio HOME con stato salvato poteva ripristinare Impostazioni o un'altra pagina. Il tasto Indietro dalla Home poteva chiudere l'Activity. | L'intent HOME ora prevale sullo stato della pagina. Indietro resta in Home quando l'app è aperta come Home. Gestiti anche i callback Android 13+. |
| Media per la distribuzione | L'APK 0.1.0 era debuggable. Chi ha accesso al dispositivo tramite strumenti di sviluppo può ispezionare più facilmente l'app. | Debug disabilitato nel pacchetto 0.1.1, con la stessa chiave locale per consentire l'aggiornamento. Rimane una build demo firmata con chiave di sviluppo. |
| Media per firmware proprietari | L'abilitazione dell'alias HOME poteva terminare con SecurityException. La schermata di scelta HOME può mancare sulla ROM. | Errore gestito. Se la schermata HOME è assente, apertura delle impostazioni generali con indicazione per l'utente. |
| Media per i collegamenti | Le app selezionate possono essere rimosse, disabilitate o cambiare accessibilità. | Ricontrollo di attività esportata e abilitata prima dell'apertura. Gestione di app mancanti e accesso negato. |
| Bassa | Il ripristino della demo su un oggetto già utilizzato poteva mantenere una chiamata precedente quando il nuovo numero era invalido. | Azzeramento di chiamata e proiezione prima del ripristino. |
| Bassa | Dopo un cambio di fuso orario l'orologio poteva usare il vecchio fuso. | Aggiornamento del fuso dei formattatori e del calendario al refresh. |
| Preventiva | Il simulatore interpolava i metadati in innerHTML. Oggi sono dati mock locali, ma futuri metadati esterni avrebbero potuto diventare HTML eseguibile. | Escape dei testi inseriti nel markup e test con un titolo contenente un tag con event handler. |
| Media per i controlli | Il test HTML web incorporava una copia dei sorgenti e poteva verificare codice diverso dall'app corrente. | Il test carica direttamente i sorgenti del simulatore. |
| Preventiva per la compilazione | Il wrapper Gradle non specificava il checksum dell'archivio scaricato. | Checksum SHA-256 fissato usando il valore ufficiale. Il wrapper JAR locale coincide con il checksum ufficiale. |

## Superficie di sicurezza esaminata

Il manifest espone l'Activity principale e l'alias HOME, necessari per le aperture del sistema. L'alias HOME parte disabilitato. Gli intent ricevuti non vengono usati per eseguire comandi, caricare file o inoltrare extra ad altre app. I collegamenti sono componenti espliciti scelti localmente.

Nel codice applicativo Android esaminato non sono presenti WebView, bridge JavaScript, esecuzione di shell, download di codice, provider, servizi o ricevitori esportati. Il pacchetto non richiede permessi di rete, rubrica, chiamata, posizione, Bluetooth o accesso ai file condivisi. Le preferenze sono private all'app e il backup è disabilitato nel manifest. È stato esplicitato anche il divieto di traffico HTTP in chiaro.

La query OSV per org.jetbrains.kotlin:kotlin-stdlib 2.1.20 ha restituito una risposta senza advisory. Questo controllo è circoscritto a quella coordinata e versione e non costituisce un audit completo di tutte le dipendenze di compilazione, del sistema Android o del firmware.

## Limiti ancora aperti

- La modalità dispositivo apre le app selezionate. Metadati musicali, comandi Bluetooth e chiamate custom reali rimangono da implementare dopo la verifica delle API XY AUTO.
- Il firmware può forzare la Home OEM all'avvio o intercettare il tasto Home. I test del codice standard Android non dimostrano il comportamento della ROM.
- Restano da verificare sulla radio riaccensione, sospensione, priorità audio, microfono, telecamera e comandi fisici.
- FitLayout scala un layout 1024:600. Leggibilità, dimensione fisica dei tocchi, impostazioni di carattere ingrandito e accuratezza del touch richiedono una prova visiva e tattile.
- Il rendering e l'occupazione reale di memoria con 1 GB RAM non sono misurati dai test JVM. Il tablet non riproduce il limite prestazionale della radio.
- L'emulatore Pixel_8 locale non è partito perché il pacchetto immagine non contiene l'immagine di sistema necessaria. Non è stata eseguita una sessione completa di Android virtualizzato.
- Prima di una distribuzione stabile serve una chiave di firma dedicata conservata al sicuro e una procedura di aggiornamento. L'eventuale passaggio a una chiave diversa richiede una strategia specifica per le installazioni già esistenti.
- Questa revisione non dimostra l'assenza di vulnerabilità e non è un penetration test sul firmware.

## Riproduzione dei controlli

Da android, con JDK 21 e SDK configurato, eseguire:

```sh
./gradlew assembleDebug lintDebug testDebugUnitTest
```

Aprire test/browser-test.html nel browser per i flussi web e il controllo dei metadati. I test JVM usano Robolectric e risorse Android per verificare creazione delle schermate, comandi demo, ripristino dello stato e gestione della Home. Non sostituiscono un test dell'APK su dispositivo.

## Fonti di verifica

- [Android e il rischio di debuggable](https://developer.android.com/privacy-and-security/risks/android-debuggable)
- [Checksum distribuzione Gradle 8.14.3](https://services.gradle.org/distributions/gradle-8.14.3-bin.zip.sha256)
- [Checksum wrapper Gradle 8.14.3](https://services.gradle.org/distributions/gradle-8.14.3-wrapper.jar.sha256)
- [OSV](https://osv.dev/)

## Esito finale dei controlli locali

- Compilazione APK 0.1.1-demo completata.
- 7 esecuzioni di test Android, 0 fallimenti e 0 errori. Le verifiche sulle schermate sono state eseguite con risorse API 23, 28 e 35. Inclusi navigazione, chiamata demo, disconnessione, dimensioni del tastierino, HOME a caldo e a freddo, Indietro dalla HOME, ricreazione Activity, rifiuto del collegamento alla propria app e gestione di un collegamento rimosso.
- Android Lint ha riportato 0 errori e 6 avvisi. Restano avvisi su target API 35 rispetto all'ultima versione, orientamento fisso intenzionale, regole backup moderne, ridisegno dello sfondo e simmetria RTL dei margini. I numeri di Lint non costituiscono un giudizio di sicurezza.
- Test web in Chrome a 1024×600 superato, inclusa la prova di HTML nei metadati. Il test usa i file attuali.
- Firma dell'APK verificata. Il certificato coincide con quello della versione 0.1.0, così la firma permette l'aggiornamento senza disinstallare. Il versionCode è passato da 1 a 2.
- Il manifest compilato non dichiara l'app come debuggable. L'APK resta una demo con collegamenti OEM configurabili.
- SHA-256 dell'APK salvato nel file .apk.sha256 accanto al pacchetto. Non sono state modificate le versioni dello stack di compilazione, salvo l'aggiunta delle dipendenze necessarie ai test.
- Il nuovo APK è apk/PeugeotLauncher-0.1.1-demo.apk. La versione 0.1.0 rimane disponibile come build precedente.

## Integrazione con scanner installati

Eseguite successivamente scansioni Semgrep, OWASP ZAP, Gitleaks, OSV Scanner e Trivy. OSV ha rilevato sei advisory nelle dipendenze di test Bouncy Castle e Guava. Le constraints dei test sono state aggiornate e una seconda scansione OSV ha riportato zero advisory per gli inventari applicativo e di test. I sette test Android sono stati rieseguiti con esito positivo. Per warning Semgrep, rilievi HTTP ZAP, versioni e limiti dei controlli vedere security/README.md.
