package org.kociemba.twophase;

import java.util.*;

/**
 * Classe qui organise une compétition entre plusieurs algorithmes
 * Elle lance les résolutions du Rubik’s Cube sur le même cube mélangé 
 * et compare leurs performances selon difféerents critères
 */

public class CompetitionIA {
	/**
	 * Poids attribué au temps d’exécution dans le score final
	 * C'est-à-dire qu' un algorithme qui va prendre 1 seconde pour résoudre le rubik's cube
	 *  va perdre 10 points
	 */

	private static final double ALPHA_TIME = 10.0;
	/**
	 * Poids attribué au nombre de nœuds explorés dans le score final
	 * C'est-à-dire qu' un algorithme qui va explorer 10 000 noeuds
	 * va perdre 1 point
	 */
	private static final double BETA_NODES = 1.0 / 10_000.0;

	/* ==================== POINT D'ENTRÉE ==================== */

	/**
	 * Affiche le menu et récupère la difficulté choisie par l’utilisateur
	 */
	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);

		System.out.println("╔═════════════════════════════════════╗");
		System.out.println("║   RUBIK'S CUBE - BATTLE IA 🎮       ║");
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

	/* ==================== LANCEMENT DE LA COMPÉTITION ==================== */

	/**
	 * Lance la compétition en fonction de la difficulté choisie
	 * Génère un scramble fixe selon la difficulté choisie et exécute tous les algorithmes IA
	 */

	private static void lancerCompetition(int difficulte) {
		CubieCube cube;
		String scrambleStr;

		if (difficulte == 1) {
			// Facile : 5 mouvements
			cube = genererScramble(new int[]{0, 3, 6, 9, 12});  // U R F D L
			scrambleStr = "U R F D L";
		} 

		else if (difficulte == 2) {
			// Moyen : 9 mouvements
			cube = genererScramble(new int[]{0, 3, 8, 13, 9, 15, 5, 1, 6});
			scrambleStr = "U R F' L2 D B R' U2 F";
		} 

		else {
			// Difficile : 15 mouvements
			cube = genererScramble(new int[]{0, 3, 8, 13, 9, 15, 5, 1, 6, 10, 14, 2, 7, 11, 4});
			scrambleStr = "U R F' L2 D B R' U2 F D' L' R F2 D2 L";
		}

		System.out.println("\nScramble utilisé : " + scrambleStr);
		System.out.println("\n🏁 QUE LA COMPÉTITION COMMENCE !\n");
		System.out.println("═══════════════════════════════════════\n");

		/** 
		 *Lancer les algorithmes avec affichage de progression
		 */
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


		afficherResultats(resultats);
	}

	/* ==================== AFFICHAGE DES RÉSULTATS ==================== */

	/**
	 * Affiche les résultats des algorithmes ayant réussi la résolution
	 * Classe les IA selon un score global
	 * score = coups + temps * ALPHA_TIME + nœuds * BETA_NODES
	 */
	private static void afficherResultats(List<RubikIA.Result> resultats) {

		// Filtrer uniquement les algorithmes qui ont réussi
		List<RubikIA.Result> valides = new ArrayList<>(
				resultats.stream()
				.filter(r -> r.succes)
				.toList()
				);

		if (valides.isEmpty()) {
			System.out.println("\n⚠️  Aucun algorithme n'a résolu le cube.");
			return;
		}
		valides.sort(Comparator.comparingDouble(CompetitionIA::scoreIA));

		String[] medailles = {"🥇","🥈","🥉","4️⃣","5️⃣"};

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

		// Afficher les qualifications par critère
		afficherQualificationIA(valides);
	}

	/**
	 * Calcule un score global pour comparer les algorithmes
	 * Combine la longueur de la solution le temps et le nombre de nœuds
	 */
	private static double scoreIA(RubikIA.Result r) {
		return r.coups + r.tempsSec * ALPHA_TIME + r.noeuds * BETA_NODES;
	}

	/**
	 * Affiche les meilleures IA selon différents critères de performance
	 */
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

	/* ==================== GÉNÉRATION DE SCRAMBLE ==================== */
	/**
	 * Génère un cube mélangé à partir d’une suite de mouvements
	 *  Utilise la méthode applyMove18 de RubikIA pour appliquer chaque
	 * mouvement codé (0-17)
	 */
	private static CubieCube genererScramble(int[] moves) {
		CubieCube cube = new CubieCube();
		for (int mv : moves) {
			RubikIA.applyMove18(cube, mv);
		}
		return cube;
	}


}
