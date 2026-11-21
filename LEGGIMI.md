# Peugeot Launcher

Il progetto si trova in questa cartella.

- `simulatore/` contiene l'anteprima web già approvata.
- `android/` contiene la prima app nativa Kotlin + XML.
- `apk/PeugeotLauncher-0.1.3-demo.apk` è la build di sviluppo installabile.
- `test/` contiene i controlli dell'anteprima web.

## Prima app Android

L'app parte in modalità demo. Riprende l'orologio a quadrante, i colori grafite e ambra, i controlli musicali in Home e la barra di navigazione persistente. Musica, chiamate e sessione Android Auto sono simulate. La demo è silenziosa e il suo avanzamento si sospende quando l'app va in background. Lo stato della demo viene ripristinato durante la ricreazione dell'Activity. La preferenza demo e i collegamenti scelti vengono conservati tra gli avvii.

La superficie mantiene il rapporto 1024:600 anche sul tablet, con eventuali bande. La verifica di dimensioni fisiche, leggibilità e velocità del dispositivo è ancora da effettuare.

## Modalità dispositivo

Nelle impostazioni si possono scegliere dall'elenco delle app installate i collegamenti per musica Bluetooth, telefono e proiezione Android Auto. Scegliere le applicazioni effettivamente presenti sulla radio e disattivare la demo per aprirle dai relativi pulsanti. Un'app OEM può includere tutte le funzioni in un'unica schermata, pertanto il collegamento apre la sua Activity di avvio.

In questa versione lo stato Bluetooth reale viene indicato come non verificato. I controlli multimediali, i metadati e il tastierino custom sono disponibili nella demo. L'adattatore dispositivo espone esplicitamente l'indisponibilità finché le API proprietarie non saranno verificate. Il contratto `CarBackend` isola queste integrazioni.

Il pulsante Android Auto apre l'app di proiezione selezionata. L'APK include il launcher e le schermate custom. La proiezione richiede un'app compatibile già installata e configurata sul dispositivo.

## Home Android

L'app è inizialmente disponibile dall'elenco delle applicazioni. Per renderla selezionabile come Home usare **Impostazioni → Abilita come launcher**, confermare e scegliere nelle impostazioni Android. Quando l'opzione è abilitata, il pulsante diventa **Scegli Home predefinita** e consente di riaprire la scelta, anche per tornare alla Home OEM.

La disponibilità di questa schermata e il rispetto della Home predefinita dipendono dal firmware. Il pacchetto OEM rimane installato. Il progetto non contiene modifiche al firmware, servizi di avvio o permessi per chiamate, contatti, Bluetooth e accesso a Internet.

## Compilazione su questo Mac

Aprire il progetto `android/` in Android Studio oppure eseguire `android/build-apk.command`. Lo script compila l'APK, esegue Android Lint e i test JVM Android e aggiorna la copia in `apk/`.

Sono richiesti JDK 21, Android SDK 35 e accesso alle dipendenze Maven/Google al primo avvio. Il wrapper usa Gradle 8.14.3, Android Gradle Plugin 8.9.3 e Kotlin 2.1.20. Il file locale `android/local.properties` contiene il percorso SDK di questo Mac e va adattato spostando il progetto su un altro computer.

La compatibilità minima è Android 6 / API 23. Il target è API 35 e la compatibilità con le versioni precedenti è gestita dal codice dell’app. La versione Android reale della XY AUTO deve ancora essere verificata. La build è firmata con la chiave di sviluppo locale e ha il debug disabilitato. Conservare questa chiave per aggiornare la stessa installazione in futuro.

## Verifica effettuata

La compilazione e Android Lint vengono eseguiti sul Mac. Le prove di installazione e uso su Galaxy Tab S10 FE e autoradio sono la fase successiva. Il test del simulatore web non verifica l'app Android. Le prestazioni con 1 GB RAM, i tasti fisici, la ripresa dopo accensione e le interazioni con i servizi OEM richiedono il dispositivo reale.
