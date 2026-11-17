# Simulatore launcher Peugeot 207

## Avvio su Mac

Apri `index.html` nel browser con un doppio clic dal Finder. Tieni nella stessa cartella il foglio di stile e i due file JavaScript. Funziona anche offline, senza installazioni o server.

La superficie interna misura **1024 × 600 pixel CSS**. Con zoom del browser al 100% e spazio sufficiente viene mostrata a scala 1:1. In finestre più piccole si riduce mantenendo l'aspetto 128:75. Il browser e lo schermo Retina possono usare più pixel fisici per rappresentare ogni pixel CSS.

## Prova rapida

1. Usa i comandi musicali dalla **Home**. Per il cursore, apri **Musica** dalla barra inferiore. Nella schermata Musica, prova riproduzione, pausa, precedente, successivo e cursore di avanzamento.
2. In **Telefono**, componi un numero fittizio, cancella una cifra e avvia e termina la chiamata simulata.
3. In **Android Auto**, avvia e chiudi la sessione demo. La schermata rappresenta lo stato di proiezione.
4. Dall’ingranaggio in alto apri **Impostazioni**, disconnetti il telefono e riapri Musica, Telefono e Android Auto per vedere gli stati senza connessione. Riattiva la demo per tornare ai dati di esempio.

Il pulsante **Peugeot 207**, la freccia indietro ed Esc riportano alla Home. La demo parte con un telefono fittizio connesso e si azzera ricaricando la pagina. Brani e chiamate sono simulati e silenziosi.

## Passaggio ad Android

L'interfaccia web usa HTML, CSS e JavaScript senza librerie esterne. Il file `demo.js` contiene lo stato e i comandi simulati. Il porting può mantenere questo confine con layout XML nativi e un adattatore Kotlin per ciascuna integrazione. Il primo APK va aperto come app in orizzontale sul Galaxy Tab S10 FE per testare layout, touch e navigazione.

Sulla XY AUTO YT9216BJ, verificare per singola funzione quali dati, comandi e schermate sono accessibili dal firmware. Se i comandi Bluetooth o la proiezione non sono accessibili, mostrare lo stato di indisponibilità e offrire un collegamento alla schermata OEM solo quando è stato verificato che si può aprire. Mantenere il launcher OEM disponibile e verificare memoria, avvio e fluidità direttamente sull'autoradio prima di adottare il launcher personalizzato.
