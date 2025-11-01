## SenseBrutor
![SenseBrutor](src/main/resources/images/sensebrutor.png)

Andreu Anglada Torres, 2 any DAM

SenseBrutor és una aplicació amb interfície gràfica (GUI) desenvolupada en Java que permet descarregar vídeos i àudios de YouTube i altres plataformes sense publicitat, utilitzant yt-dlp com a motor de descàrrega.

##Panell inicial

-Es pot afegir una URL de YouTube per començar la descàrrega.

-Un cop introduïda la URL i sortint del camp de text, l'aplicació carregarà els formats disponibles per a aquesta URL.

-Durant la càrrega, apareix un progress bar i un label que informa de l'estat de la càrrega.

-El botó Descarregar estarà desactivat fins que els formats disponibles no hagin estat completament carregats.

##Panell de preferències

Seleccionar carpeta on es descarregarà l'arxiu.

Crear arxiu .m3u, que si està marcat, a l'hora de descarregar es posarà l'arxiu dins una llista.

Selecció de velocitat màxima de descàrrega, si es deixa a 0 no hi haurà màxim.

Ruta on tenim l’exe per executar els comands necessaris.

##Panell Descàrrega

La descàrrega només és possible si al panell de configuració s'ha fet el següent:

-Seleccionar la ruta on es vol guardar l'arxiu descarregat.

-Afegir l'arxiu a una llista .m3u si es desitja.

-Establir una velocitat màxima de descàrrega (opcional).

-Seleccionar la ruta de l'arxiu executable yt-dlp.exe.

-Un cop completada la descàrrega, es farà visible un botó que permet reproduir l'arxiu acabat de descarregar directament des de l'aplicació.

##Panell biblioteca de medis

-Permet cercar en temps real de dins la carpeta que tenim com a sortida el nom d’un arxiu.

-Permet seleccionar el tipus d’arxiu que volem filtrar (tots, àudio o vídeo).

-Llista tots els tipus d’arxius que hi tenim dins la carpeta i, a l’hora de seleccionar-ho, filtra per aquests.

-Permet actualitzar la taula per si s’han afegit arxius.

-Permet eliminar l’arxiu que tenim seleccionat.

-(EXTRA) Permet reproduir de la taula el que tenim seleccionat amb el reproductor que tenim per defecte.

##Acerca de

-Conté el logo de l’aplicació.

-Nom de l’alumne, amb curs i els recursos utilitzats.

##Funcionalitats extra

-He afegit la funcionalitat de poder triar el format de sortida segons el que la URL pugui oferir.

-He afegit un progress bar que surt a l’hora de carregar els formats i a l’hora d’iniciar la descàrrega.

-He afegit validacions per si s’intenta descarregar sense tenir definida una ruta de sortida o si no tenim guardat l’arxiu ytdlp.exe.

-He afegit que si es selecciona a la taula de biblioteca de medis un arxiu, s’habiliti un botó que permet reproduir-lo.

-He afegit que una vegada es descarrega l’arxiu, s’actualitzi la pantalla “Biblioteca de Medis” amb el nou arxiu descarregat.

-He afegit que una vegada seleccionada la carpeta de sortida de descàrregues, s’actualitzi la taula “Biblioteca de Medis” mostrant tots els arxius de la carpeta.

-He afegit funcionalitat de cerca en temps real a l’hora d’escriure el nom de l’arxiu a cercar.

-He afegit que si l’arxiu ja estava descarregat surti un missatge informatiu.

-He afegit que si el format seleccionat no està disponible mostri un missatge informatiu.

##Problemes

-No sabia gaire bé com fer per carregar els formats, m’ha duit bastant de temps però finalment ho he deixat com al principi. Una vegada es surt de posar
la URL, s’inicia la càrrega de formats.

-Problemes amb caràcters estranys o accents una vegada descarregat l’arxiu per reproduir-lo. Solucionat afegint la línia "--restrict-filenames", dins la funció d’iniciar la descàrrega.

##Recursos

-https://www.youtube.com/@spdvi7370

-https://chatgpt.com/

-https://paucasesnovescifp.cat/