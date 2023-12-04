#!/bin/sh
source fxenv
PATH_TO_FX=/project2/github/steely-glint/fxess/javafx-sdk-21.0.1/lib export PATH_TO_FX
echo $PATH_TO_FX
java \
  -Xmx768m \
  -Djava.library.path=${PATH_TO_FX}\
  --module-path .:${PATH_TO_FX} \
  --add-modules javafx.controls \
  -jar target/fxess-1.0-SNAPSHOT.jar ${FOXUSER} ${FOXPASS} ${LAT} ${LONG}

