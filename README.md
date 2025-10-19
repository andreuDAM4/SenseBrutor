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

