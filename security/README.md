# Scansioni di sicurezza del 18 novembre 2026

Scansioni eseguite sui file locali del progetto dopo la revisione 0.1.1-demo. I report originali sono in security/raw, esclusa da Git perché contiene dati di esecuzione, percorsi del Mac e la sessione locale ZAP.

## Risultati

| Strumento | Ambito verificato | Risultato |
|---|---|---|
| Semgrep 1.176.0 | Sorgenti Android principali e simulatore. Configurazioni security-audit, owasp-top-ten e secrets. 137 regole effettivamente eseguite su 34 file, nessun errore di scansione. | 2 warning su Activity e alias esportati. |
| Gitleaks 8.30.1 | File correnti esclusi cache e report, poi cronologia Git di 1 commit. Valori oscurati nei report. | Nessun segreto rilevato. |
| OSV Scanner 2.6.0 | Inventari CycloneDX estratti dalle dipendenze effettive di Gradle. 2 coordinate nel classpath dell'app e inizialmente 42 nel classpath dei test. | Prima della correzione, 6 advisory su 2 dipendenze dei test. Dopo la correzione, 0 advisory su entrambi gli inventari; 41 coordinate nel classpath dei test. |
| Trivy 0.74.0 | Inventario del classpath applicativo, database aggiornato durante la scansione. | 0 vulnerabilità segnalate. |
| OWASP ZAP 2.17.0 | Spider e scansione attiva del solo simulatore servito su 127.0.0.1. | 2 categorie Medium, 2 Low e 1 Informational, dettagli sotto. |
| Gradle e Android Lint | Nuova risoluzione dipendenze, compilazione e 7 esecuzioni dei test Android. | Build riuscita, 7 test superati, Lint con 0 errori e 6 avvisi. |

## Advisory risolti

Robolectric introduceva Bouncy Castle 1.78.1 e Guava 33.3.1-jre nel classpath dei test. Queste coordinate non compaiono nel classpath applicativo. Sono state aggiunte constraints limitate a testImplementation per ottenere Bouncy Castle 1.85 e Guava 33.7.2-jre.

Gli advisory iniziali erano:

- Bouncy Castle: GHSA-574f-3g2m-x479 e GHSA-9pwp-9qqc-pr26, classificati Critical nel database, GHSA-p93r-85wp-75v3 e GHSA-qp49-qgx5-5m26 classificati High, GHSA-c3fc-8qff-9hwx classificato Moderate.
- Guava: GHSA-xxph-c9ww-hj94, classificato High.

La presenza di una versione segnalata nei test non dimostra che un percorso vulnerabile fosse raggiungibile dal launcher installato. L'aggiornamento elimina le corrispondenze segnalate nell'inventario dei test. Non è stata effettuata un'analisi completa di raggiungibilità del codice delle librerie.

## Valutazione dei warning Semgrep

La MainActivity deve essere avviabile dall'elenco delle applicazioni. L'alias HomeEntry deve essere raggiungibile dal sistema per la selezione come Home. Per questo entrambi sono exported; HomeEntry parte disabilitato.

Il codice esaminato non usa URI o extra dell'intent per aprire file, eseguire comandi o inoltrare dati ad altre app. Le modifiche alle preferenze e l'apertura dei collegamenti passano dai comandi dell'interfaccia. Non è stato identificato un accesso privilegiato sfruttabile attraverso questi due componenti. I warning sono conservati come rilievi verificati, senza disabilitare la regola o dichiarare che ogni Activity esportata sia innocua.

La copertura Kotlin delle regole utilizzate è limitata: nel riepilogo risultano 9 regole Kotlin applicate a 5 file. Semgrep non fornisce una prova esaustiva del comportamento Android o della sicurezza del firmware.

## Valutazione dei rilievi ZAP

Il server usato era python -m http.server, attivo esclusivamente su loopback e serviva la sola cartella simulatore. ZAP ha segnalato:

- Medium: Content Security Policy Header Not Set.
- Medium: Missing Anti-clickjacking Header.
- Low: Server Leaks Version Information via Server HTTP Response Header Field.
- Low: X-Content-Type-Options Header Missing.
- Informational: Modern Web Application.

Questi rilievi sono validi per le risposte HTTP del server di prova. La modalità ordinaria del simulatore apre un file HTML locale; l'APK usa layout XML e non incorpora un server HTTP o una WebView. Prima di pubblicare il simulatore su un sito, occorre configurare e provare CSP, frame-ancestors o X-Frame-Options, nosniff e gli header del server reale. I rilievi non sono stati soppressi.

ZAP ha usato gli add-on già installati e ha avvertito che il controllo aggiornamenti non veniva eseguito da oltre tre mesi. La scansione non certifica l'aggiornamento di ogni add-on e lo spider tradizionale non garantisce la copertura di ogni stato generato dal JavaScript. Il server temporaneo è stato arrestato al termine.

## Limiti

Gli inventari provengono dalla risoluzione Gradle: non costituiscono un'analisi binaria completa del DEX o delle dipendenze interne del plugin di compilazione. Trivy ha avvertito dei limiti degli SBOM generati da strumenti esterni. Gli scanner non verificano stack Bluetooth proprietario, MCU, telecamera, comandi al volante, avvio reale o prestazioni sulla radio. Non è stato eseguito un penetration test sul dispositivo.

## Report locali

- raw/semgrep.json
- raw/gitleaks.json e raw/gitleaks-history.json
- raw/osv-before.json e raw/osv-after.json
- raw/trivy.json
- raw/zap-report.json
- raw/debugRuntimeClasspath.cdx.json e raw/debugUnitTestRuntimeClasspath.cdx.json
- raw/remediation-build.log

La pubblicazione include le correzioni, il riepilogo delle scansioni e l’APK 0.1.1-demo. I report originali rimangono nella cartella locale raw.
