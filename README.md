# PresidentGame
Il punto di partenza di questo progetto è stato il gioco di carte Presidente, scelto per le sue dinamiche semplici ma strategicamente ricche. Si tratta di un gioco a informazione incompleta, in cui i partecipanti non conoscono le carte degli avversari e devono adattare costantemente le proprie mosse alle situazioni di gioco, bilanciando rischio e prudenza.
L’obiettivo principale è stato quello di sviluppare e confrontare diversi tipi di giocatori artificiali, ognuno caratterizzato da un diverso approccio decisionale: più aggressivo, più difensivo o pragmatico. In questo modo è stato possibile osservare come differenti strategie si comportino in condizioni identiche e contro avversari di natura diversa.

Per l’implementazione è stato utilizzato il Tabletop Games Framework (TAG).
## TAG: [Tabletop Games Framework](http://www.tabletopgames.ai/)
Il [Tabletop Games Framework (TAG)](http://tabletopgames.ai) è un benchmark basato su Java per lo sviluppo di giochi da tavolo moderni per la ricerca sull'intelligenza artificiale. TAG fornisce uno scheletro comune per l'implementazione di giochi da tavolo basati su un'API comune per agenti di intelligenza artificiale, un set di componenti e classi per aggiungere facilmente nuovi giochi e un modulo di importazione per la definizione dei dati in formato JSON. Attualmente, questa piattaforma include l'implementazione di sette diversi giochi da tavolo che possono anche essere utilizzati come esempio per ulteriori sviluppi. Inoltre, TAG incorpora anche funzionalità di registrazione che consentono all'utente di eseguire un'analisi dettagliata del gioco, in termini di spazio d'azione, fattore di ramificazione, informazioni nascoste e altre misure di interesse per la ricerca sull'intelligenza artificiale nei giochi.

## Games
Giochi attualmente implementati:
- [x] Battlelore: Second Edition (Richard Borg and Robert A Kouba, 2013)
- [x] Blackjack (Uncredited, circa 1700)
- [x] Can't Stop (Sid Sackson, 1980)
- [x] Colt Express (Christophe Raimbault, 2014)
- [x] Connect 4 (Ned Strongin and Howard Wexler, 1974)
- [x] Diamant (Bruno Faidutti and Alan R. Moon, 2005)
- [x] Dominion (Donald X. Vaccarino, 2008)
- [x] Dots and Boxes (Edouard Lucas, 1889)
- [x] Exploding Kittens (Inman and others, 2015)
- [x] Love Letter (Seiji Kanai, 2012)
- [x] Pandemic (Matt Leacock, 2008)
- [x] Poker Texas Hold'em (Uncredited, 1810)
- [x] Settlers of Catan (Klaus Teuber, 2008)
- [x] Stratego (Jacques Johan Mogendorff, 1946)
- [x] Terraforming Mars (Jacob Fryxelius, 2016)
- [x] Tic-Tac-Toe (Uncredited, Unknown)
- [x] Uno (Merle Robbins, 1971)
- [x] Virus! (Cabrero and others, 2015)

Games in progress:
- [ ] Descent (Jesper Ejsing, John Goodenough, Frank Walls 2005)
- [ ] Hanabi (Antoine Bauza 2010)
- [ ] 7 Wonders (Antoine Bauza 2010)
      
## Setting up
Il progetto richiede Java con almeno la versione 8. Per eseguire il codice, è necessario scaricare il repository o clonarlo. Se si sta cercando una versione specifica, è possibile trovarla qui (https://github.com/GAIGResearch/TabletopGames/releases).

Il modo più semplice per eseguire il codice è creare un nuovo progetto in [IntelliJ IDEA](https://www.jetbrains.com/idea/) o in un IDE simile. In IntelliJ, creare un nuovo progetto da sorgenti esistenti, puntando al codice scaricato o clonato e selezionando il framework **Maven** per l'importazione. Questo processo dovrebbe configurare automaticamente l'ambiente e aggiungere anche eventuali librerie del progetto.

In alternativa, aprire il codice direttamente nell'IDE di propria scelta, fare clic con il pulsante destro del mouse sul file pom.xml e configurare il progetto con il framework Maven. Assicurarsi che src/main/java sia contrassegnato come root delle sorgenti. È possibile eseguire la classe `core.Game.java` per verificare se tutto è configurato correttamente e se la compilazione è in corso. [Questo video](https://youtu.be/-U7SCGNOcsg) include i passaggi per caricare correttamente il progetto in IntelliJ.

## Per iniziare
Per iniziare, il [sito web](http://tabletopgames.ai) fornisce diverse guide e descrizioni del framework.
Un'altra valida risorsa è il nostro articolo ["Design and Implementation of TAG: A Tabletop Games Framework"](https://arxiv.org/abs/2009.12065).
