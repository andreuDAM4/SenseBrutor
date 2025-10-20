##SenseBrutor

SenseBrutor és una aplicació amb interfície gràfica (GUI) desenvolupada en Java que permet descarregar vídeos i àudios de YouTube i altres plataformes sense publicitat, utilitzant yt-dlp com a motor de descàrrega.

##Panell inicial

-Es pot afegir una URL de YouTube per començar la descàrrega.

-Un cop introduïda la URL i sortint del camp de text, l'aplicació carregarà els formats disponibles per a aquesta URL.

-Durant la càrrega, apareix un progress bar i un label que informa de l'estat de la càrrega.

-El botó Descarregar estarà desactivat fins que els formats disponibles no hagin estat completament carregats.

##Descàrrega

La descàrrega només és possible si al panell de configuració s'ha fet el següent:

-Seleccionar la ruta on es vol guardar l'arxiu descarregat.

-Afegir l'arxiu a una llista .m3u si es desitja.

-Establir una velocitat màxima de descàrrega (opcional).

-Seleccionar la ruta de l'arxiu executable yt-dlp.exe.

##Finalització de la descàrrega

Un cop completada la descàrrega, es farà visible un botó que permet reproduir l'arxiu acabat de descarregar directament des de l'aplicació.

#Funcionalitats
He agregat la funcionalitat de poder elegir el format de sortida segons el que la URL pugui oferir.

He agregat un progressbar que surt a l'hora de carregar els formats i a l'hora de iniciar la descarrega.

He agregat validacions per si s'intenta descargar sense tenir definit una ruta de sortida o si no tenim guardat l'arxiu ytdlp.exe

#Probelmes
No sabia molt be com fer per carregar els formats, m'ha duit bastant de temps pero finalment ho he deixat com el principi. Una vegada es surt de posar
la URL s'inicia la carrega de formats.

Probelmes en caracters extranys o accents una vegada descarregat l'arxiu per reproduirlo.

#Recursos
https://www.youtube.com/@spdvi7370

https://chatgpt.com/

https://paucasesnovescifp.cat/

