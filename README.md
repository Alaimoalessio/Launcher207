# Launcher207

Launcher Android ultra-leggero per autoradio aftermarket XY AUTO YT9216BJ su Peugeot 207.
Kotlin, View/XML e schermo landscape 1024 × 600.

## Ramo feature/live-device-integration — versione 0.2.0-live-preview

Rispetto alla demo precedente:
- Modalità dispositivo reale predefinita, anche quando si aggiorna dalla vecchia demo.
- Ora e data del dispositivo.
- Musica: titoli, artisti, durata e comandi play/pausa/precedente/successivo dalle MediaSession Android disponibili. Lo seek è consentito solo se il player lo supporta.
- Accesso alle notifiche da autorizzare manualmente nelle Impostazioni del launcher. Questa è un'autorizzazione ampia di Android: concederla solo se desiderata. Il servizio non elabora i contenuti delle notifiche.
- Android Auto: apre Headunit Reloaded (`gb.xxy.hr`), se installato, oppure l'app scelta nelle impostazioni.
- Telefono: compone numeri nell'interfaccia e apre un dialer Android con numero precompilato tramite ACTION_DIAL, senza chiamare in automatico. Se assente, il numero viene copiato e viene tentata l'app telefono OEM selezionata.
- Bluetooth XY AUTO: accesso al modulo originale mediante app selezionata, sempre disponibile.
- La selezione come Home è opzionale; il launcher OEM originale non viene rimosso.
- Demo ancora disponibile soltanto come modalità di prova esplicita.

**Limiti**: il Bluetooth nativo XY AUTO potrebbe non pubblicare MediaSession. Il suo dialer telefonico potrebbe non supportare ACTION_DIAL. Headunit Reloaded richiede una configurazione USB/Wi-Fi funzionante indipendente dal launcher.

## Prova consigliata sul Samsung Galaxy Tab S10 FE

1. Da GitHub Actions scarica l'artefatto della build `Android APK (live preview)` dopo che il workflow risulta verde. Gli APK già nella cartella `apk/` sono VECCHIE demo.
2. Se il nuovo APK non aggiorna quello attuale per firma diversa, disinstalla la vecchia demo prima di installarlo (le preferenze andranno perse).
3. Apri Peugeot Launcher, vai in Impostazioni, tocca **Autorizza controlli multimediali** e consenti l'accesso alle notifiche. Torna all'app.
4. Apri Spotify o un altro player che esponga MediaSession e riproduci un brano. Torna al launcher e verifica i comandi.
5. In Impostazioni scegli le app OEM appropriate per telefono e musica quando testerai sulla radio.
6. Installa Headunit Reloaded soltanto se desideri provare Android Auto. Il launcher si limita ad aprirlo.
7. Non impostare ancora come Home sull'autoradio: conserva sempre la Home originale come recupero.

Per compilare su un PC con Android SDK, JDK 21: `cd android && ./gradlew assembleDebug testDebugUnitTest lintDebug`.

Il simulatore web è in `simulatore/`; i vecchi APK dimostrativi restano in `apk/`.
