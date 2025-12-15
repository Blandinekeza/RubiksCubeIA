# RubiksCubeIA

## Présentation du projet

Ce projet développe un programme pour résoudre le Rubik’s Cube 3×3×3 en utilisant divers algorithmes d'intelligence artificielle. Le cube figure en deux dimensions et peut se résoudre automatiquement avec des méthodes de recherche différentes. Le but principal est de comparer les performances de plusieurs algorithmes IA pour le même problème de résolution, en examinant le temps d’exécution et le nombre de nœuds parcourus et la longueur des solutions obtenues.

---

##  Équipe du projet

Le projet a été réalisé par :

- **KEZA Blandine**
- **KINKOLO Paulina**
- **RONOROHANTA Mino**

---

## Démarche et travail réalisé

Le projet s’est déroulé en plusieurs étapes principales :

### Recherche et choix du code de base

Nous avons commencé en cherchant sur GitHub et d’autres sources pour trouver une implémentation existante du Rubik’s Cube. Créer un solveur complet depuis zéro étant trop compliqué vu nos connaissances et le temps disponible, nous avons décidé de partir d'un code déjà existant et fonctionnel.   

### Analyse et compréhension du code choisi

Après avoir sélectionné le code, nous avons pris le temps de comprendre sa structure, le rôle de chaque classe et comment le cube et les mouvements sont représentés. Cette étape était essentielle pour modifier le code sans en altérer le fonctionnement. 

### Implémentation et adaptation des algorithmes IA

Après avoir compris le code, nous avons implémenté et adapté plusieurs algorithmes de résolution du cube:

- IDA* avec différentes heuristiques
- A* avec différentes heuristiques
- Greedy Search

Ces algorithmes ont été implementés de manière à pouvoir être comparés entre eux sur un même scramble  

### Adaptation de l’interface et des fonctionnalités

En même temps, nous avons changé l’interface et le fonctionnement général du programme pour qu’il réponde à nos objectifs, y compris l’ajout d’un mode "compétition IA" qui permet de lancer automatiquement plusieurs algorithmes et d’afficher leurs résultats.
  

---

## Structure réelle du projet

Le projet est organisé selon la structure suivante :

RubiksCubeIA/
├── src/
│   ├── Main.java
│   ├── Solver.java
│   └── org/kociemba/twophase/
│       ├── Color.java
│       ├── CompetitionIA.java
│       ├── CoordCube.java
│       ├── Corner.java
│       ├── CubieCube.java
│       ├── Edge.java
│       ├── FaceCube.java
│       ├── Facelet.java
│       ├── RubikIA.java
│       ├── Search.java
│       └── Tools.java
│
├── F2L.txt
├── OLL.txt
├── PLL.txt
├── input.txt
│
├── source/
├── Guide Git & GitHub
└── PROJECT_DETAILS.md

-Le package **org.kociemba.twophase** contient les classes liées à la modélisation du cube et aux algorithmes de résolution
-**RubikIA** implémente les différents algorithmes IA
-**CompetitionIA** permet de comparer automatiquement les algorithmes
-Les fichiers **.txt** contiennent des données utilisées par certains algorithmes de résolution comme CFOP
 

---

## Outils utilisés

- **Langage** : Java  
- **IDE** : Eclipse

---

## Algorithmes implémentés

Les algorithmes suivants ont été implémentés et testés dans le projet :

### IDA*
- heuristique des pièces mal placées  
- heuristique Manhattan  
- heuristique de Korf  

### A*
- heuristique Manhattan  
- heuristique des pièces mal placées  

### Greedy Search
- heuristique des pièces mal placées  

Ces algorithmes sont comparés selon plusieurs critères de performance  

---

## Objectif final du projet

Le programme final permet de :

- représenter un Rubik’s Cube 3×3×3  
- appliquer des scrambles de différentes difficultés  
- lancer automatiquement plusieurs algorithmes IA  
- comparer leurs performances en termes de :
  - temps d’exécution  
  - nombre de nœuds explorés  
  - longueur de la solution  
- identifier l’algorithme offrant le meilleur compromis global  

---

## Licence

Projet universitaire   
