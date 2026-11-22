# Launcher207 – Peugeot 207

Kotlin + XML, landscape 1024×600, destinato alla radio XY AUTO.

## Versione 0.2.1-safe-preview

Risolve la criticità principale evidenziata dal blocco Google Play Protect per la versione 0.2.0: rimosso completamente `NotificationListenerService` con l'accesso sensibile alle notifiche.

- Nessun permesso per SMS, notifiche, telefonate, rubrica o accessibilità.
- I pulsanti musicali inviano i normali tasti media Android tramite `AudioManager`. **Sulla radio XY AUTO potrebbero non funzionare**, in quanto il Bluetooth è proprietario.
- Titolo/artista/durata **non sono più letti**: l'interfaccia lo segnala, senza inventare informazioni.
- Sempre disponibili i collegamenti selezionabili al modulo Bluetooth e al dialer originale.
- Android Auto apre Headunit Reloaded se presente, altrimenti l'app selezionata.
- La modalità Home è disattivata per impostazione iniziale: il launcher OEM resta disponibile.
- Modalità demo ancora opzionale nelle impostazioni.

### Importante

Play Protect può bloccare software anche per altri motivi: la rimozione del permesso elimina la causa più probabile **ma non garantisce** l'autorizzazione all'installazione. Non disabilitare Play Protect e interrompi il test se compare un altro blocco.

La CI esegue `assembleDebug`, `testDebugUnitTest` e `lintDebug`; soltanto con esito positivo pubblica il file .apk direttamente su GitHub Releases, tag `v0.2.1-safe`. Le vecchie demo rimangono nel repository.

### Prova sull'autoradio

Scarica la release 0.2.1 da Chrome dell'autoradio, installa soltanto se Play Protect non segnala problemi, apri come normale app. **Non impostarla come Home predefinita.** Imposta nelle opzioni il collegamento alla app Bluetooth OEM.

La firma dell'APK buildato in CI può differire dalle vecchie installazioni; se già installato, può essere necessario disinstallare la versione precedente.
