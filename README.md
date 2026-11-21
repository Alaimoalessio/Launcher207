# Launcher207

Launcher Android personalizzato per la Peugeot 207 con autoradio XY AUTO YT9216BJ, ispirato alla precisione visiva della Ferrari Luce.

Interfaccia landscape 1024 × 600, colori grafite e ambra, orologio a quadrante, controlli musicali dalla Home e navigazione persistente.

## Contenuto

- [App Android](android/) in Kotlin con layout XML nativi.
- [APK di sviluppo](apk/PeugeotLauncher-0.1.3-demo.apk) pronto per le prime prove.
- [Simulatore web](simulatore/index.html), apribile localmente e utilizzabile offline.
- [Istruzioni](LEGGIMI.md) per compilazione, modalità demo e collegamenti alle app originali.
- [Test del simulatore](test/browser-test.html), apribile nel browser.
- [Revisione del codice](AUDIT.md) e [risultati degli scanner di sicurezza](security/README.md).

## Stato della versione 0.1.3-demo

L'app parte in modalità demo con brani, chiamate e proiezione simulati. In modalità dispositivo permette di aprire le app originali scelte nelle impostazioni. Metadati Bluetooth, controlli multimediali e telefonate reali richiedono la verifica delle API del firmware.

Per ridurre il lavoro a riposo, l'orologio segue gli eventi di Android al cambio del minuto e il timer della demo gira soltanto durante la riproduzione simulata, con il launcher in primo piano. Le schermate vengono create alla prima visita e riusate nei passaggi successivi. Se Android segnala poca memoria, quelle fuori schermo vengono rilasciate.

Compilazione Android e firma dell'APK verificate. Le prove su Samsung Galaxy Tab S10 FE e autoradio sono ancora da effettuare.

La selezione come Home si abilita dalle impostazioni dell'app. Il progetto conserva il launcher OEM come possibilità di recupero.
