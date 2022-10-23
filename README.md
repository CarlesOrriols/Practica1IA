# Practica1IA

* Link per editar l'overleaf del treball d'innovació: https://www.overleaf.com/6457256274ybdxnfbpytzs
* Link per editar la documentació de la pràctica: https://www.overleaf.com/3642936959yrxsxvczdspb


Primero de todo, modificamos la variable de entorno CLASSPATH, ejecutar en el terminal linux:
* export CLASSPATH=".:./src:./libraries/AIMA.jar:./libraries/CentralEnergia.jar"

Para compilar el programa, desde esta misma carpeta ejecutar en el terminal:<br />
* javac -g Main.java ./src/*.java

Para ejecutar el programa, ejecutar en el terminal:
* java Main (Parámetros)

Parametros:<br /><br />
seed es la semilla para la generación de las centrales:
* -sce seed<br />

seed es la semilla apra la generación de los clientes:
* -scl seed<br />

num es el número de clientes que queremos tener:
* -ncl num<br />

tA, tB y tC són el numero de centrales de cada tipo:
* -ntce tA tB tC<br />

pXG, pMG y pG són la proporcion de clientes de cada tipo(deben sumar 1):
* -pcli pXG pMG pG<br />

p es la proporcion de garantizados que queremos tener:
* -pg p<br />

seed es la semilla para la generación del estado inicial random:
* -sei seed<br />

Para escoger el tipo de algoritmo, hc para Hill Climbing y sa para Simuleted Annealing
* -hcorsa o<br />

Para escoger el tipo de estado inicial, "o" puede ser random o greedy
* -ei o<br />
