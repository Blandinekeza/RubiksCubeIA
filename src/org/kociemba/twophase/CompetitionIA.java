package org.kociemba.twophase;

import java.util.*;

public class CompetitionIA {

    private static final double ALPHA_TIME = 10.0;
    private static final double BETA_NODES = 1.0 / 10_000.0;

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        
        System.out.println("╔═════════════════════════════════════╗");
        System.out.println("║   RUBIK'S CUBE - BATTLE IA 🎮      ║");
        System.out.println("╚═════════════════════════════════════╝\n");
        
        System.out.println("Choisir la difficulté :");
        System.out.println("1. Facile (scramble court : 5 moves)");
        System.out.println("2. Moyen (scramble standard : 9 moves)");
        System.out.println("3. Difficile (scramble long : 15 moves)");
        System.out.print("\nVotre choix : ");
        
        int choix = scan.nextInt();
        lancerCompetition(choix);
        scan.close();
    }
    
    private static void lancerCompetition(int difficulte) {
        CubieCube cube;
        String scrambleStr;
        
        if (difficulte == 1) {
            // Facile : 5 mouvements
            cube = genererScramble(new int[]{0, 3, 6, 9, 12});  // U R F D L
            scrambleStr = "U R F D L";
        } else if (difficulte == 2) {
            // Moyen : 9 mouvements
            cube = genererScramble(new int[]{0, 3, 8, 13, 9, 15, 5, 1, 6});
            scrambleStr = "U R F' L2 D B R' U2 F";
        } else {
            // Difficile : 15 mouvements
            cube = genererScramble(new int[]{0, 3, 8, 13, 9, 15, 5, 1, 6, 10, 14, 2, 7, 11, 4});
            scrambleStr = "U R F' L2 D B R' U2 F D' L' R F2 D2 L";
        }
        
        System.out.println("\nScramble utilisé : " + scrambleStr);
        System.out.println("\n🏁 QUE LA COMPÉTITION COMMENCE !\n");
        System.out.println("═══════════════════════════════════════\n");
        
        // Lancer les 6 algorithmes avec affichage de progression
        RubikIA ia = new RubikIA();
        List<RubikIA.Result> resultats = new ArrayList<>();
        
        System.out.println("⏳ IDA* + hKorf en cours...");
        resultats.add(ia.joueur1_IDA_Korf(cube));
        System.out.println("   ✅ Terminé\n");
        
        System.out.println("⏳ IDA* + hMalPlaces en cours...");
        resultats.add(ia.joueur2_IDA_MalPlacees(cube));
        System.out.println("   ✅ Terminé\n");
        
        System.out.println("⏳ IDA* + hManhattan en cours...");
        resultats.add(ia.joueur3_IDA_Manhattan(cube));
        System.out.println("   ✅ Terminé\n");
        
        System.out.println("⏳ A* + hManhattan en cours...");
        resultats.add(ia.joueur4_AStar_Manhattan(cube));
        System.out.println("   ✅ Terminé\n");
        
        System.out.println("⏳ A* + hMalPlaces en cours...");
        resultats.add(ia.joueur5_AStar_MalPlacees(cube));
        System.out.println("   ✅ Terminé\n");
        
        System.out.println("⏳ Greedy + hMalPlaces en cours...");
        resultats.add(ia.joueur6_Greedy_MalPlacees(cube));
        System.out.println("   ✅ Terminé\n");

        afficherResultats(resultats);
    }

    private static void afficherResultats(List<RubikIA.Result> resultats) {

        List<RubikIA.Result> valides = new ArrayList<>(
                resultats.stream()
                         .filter(r -> r.succes)
                         .toList()
        );

        if (valides.isEmpty()) {
            System.out.println("\n⚠️  Aucun algorithme n'a résolu le cube.");
            return;
        }

        // Podium = meilleur compromis global
        valides.sort(Comparator.comparingDouble(CompetitionIA::scoreIA));

        String[] medailles = {"🥇","🥈","🥉","4️⃣","5️⃣","6️⃣"};

        System.out.println("\n╔═════════════════════════════════════════════════════╗");
        System.out.println("║                  🏆 RÉSULTATS 🏆                     ║");
        System.out.println("╚═════════════════════════════════════════════════════╝\n");

        System.out.println("┌────┬─────────────────────────┬───────┬─────────┬──────────┐");
        System.out.println("│    │ Algorithme              │ Coups │ Temps   │ Nœuds    │");
        System.out.println("├────┼─────────────────────────┼───────┼─────────┼──────────┤");

        for (int i = 0; i < valides.size(); i++) {
            RubikIA.Result r = valides.get(i);
            System.out.printf(
                    "│ %s │ %-23s │ %5d │ %7.3fs │ %8d │\n",
                    medailles[i], r.nomAlgo, r.coups, r.tempsSec, r.noeuds
            );
        }

        System.out.println("└────┴─────────────────────────┴───────┴─────────┴──────────┘");

        afficherQualificationIA(valides);
    }

    private static double scoreIA(RubikIA.Result r) {
        return r.coups + r.tempsSec * ALPHA_TIME + r.noeuds * BETA_NODES;
    }

    private static void afficherQualificationIA(List<RubikIA.Result> valides) {

        RubikIA.Result plusRapide =
                Collections.min(valides, Comparator.comparingDouble(r -> r.tempsSec));

        RubikIA.Result moinsNoeuds =
                Collections.min(valides, Comparator.comparingLong(r -> r.noeuds));

        RubikIA.Result meilleurChemin =
                Collections.min(valides, Comparator.comparingInt(r -> r.coups));

        RubikIA.Result meilleurCompromis =
                Collections.min(valides, Comparator.comparingDouble(CompetitionIA::scoreIA));

        System.out.println("\n🏆 QUALIFICATION PAR CRITÈRE\n");

        System.out.println("⚡ Plus rapide            : " + plusRapide.nomAlgo);
        System.out.println("🌲 Moins de nœuds explorés : " + moinsNoeuds.nomAlgo);
        System.out.println("🧭 Chemin le plus court   : " + meilleurChemin.nomAlgo);
        System.out.println("⚖️  Meilleur compromis IA  : " + meilleurCompromis.nomAlgo);
    }

    private static CubieCube genererScramble(int[] moves) {
        CubieCube cube = new CubieCube();
        for (int mv : moves) {
            applyMove18(cube, mv);
        }
        return cube;
    }

    private static void applyMove18(CubieCube cube, int move18) {
        int face = move18 / 3;
        int power = move18 % 3;
        int times = (power == 0) ? 1 : (power == 1) ? 2 : 3;

        for (int i = 0; i < times; i++) {
            cube.multiply(CubieCube.moveCube[face]);
        }
    }
}
