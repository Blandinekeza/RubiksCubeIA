package org.kociemba.twophase;

import java.util.*;
import java.util.function.ToIntFunction;

/**
 * Classe qui regroupe plusieurs algorithmes de résolution du Rubik’s Cube
 * Elle permet de comparer différentes stratégies de recherche et heuristiques
 */
public class RubikIA {

	/* ======================= RESULT ======================= */

	/**
	 * Classe qui stocke le résultat produit par un algorithme IA
	 * Elle contient les informations nécessaires pour comparer les performances
	 */
	public static class Result {
		public final String nomAlgo;
		public final String solution;
		public final int coups;
		public final double tempsSec;
		public final long noeuds;
		public final boolean succes;

		/**
		 * Crée un objet résultat contenant les performances d’un algorithme
		 */
		public Result(String nomAlgo, String solution, int coups,
				double tempsSec, long noeuds, boolean succes) {
			this.nomAlgo = nomAlgo;
			this.solution = solution;
			this.coups = coups;
			this.tempsSec = tempsSec;
			this.noeuds = noeuds;
			this.succes = succes;
		}
	}

	/* ======================= PARAMÈTRES ======================= */

	/**
	 * Noms des 18 mouvements possibles du Rubik’s Cube
	 */
	private static final String[] MOVE_NAMES = {
			"U","U2","U'","R","R2","R'","F","F2","F'",
			"D","D2","D'","L","L2","L'","B","B2","B'"
	};

	/**
	 * Associe chaque mouvement à une face du cube
	 * Permet d’éviter des mouvements inutiles successifs sur la même face
	 */
	private static final int[] MOVE_FACE = {
			0,0,0, 1,1,1, 2,2,2, 3,3,3, 4,4,4, 5,5,5
	};

	/**
	 * Applique un mouvement codé sur 18 possibilités au cube
	 */
	public static void applyMove18(CubieCube cube, int move18) {
		int face = move18 / 3;
		int power = move18 % 3;

		int times = (power == 0) ? 1 : (power == 1) ? 2 : 3;

		for (int i = 0; i < times; i++) {
			cube.multiply(CubieCube.moveCube[face]);
		}
	}

	/**
	 * Nombre total de mouvements autorisés
	 */
	private static final int NB_MOVES = 18;

	/**
	 * Limites de temps pour les algorithmes IDA* et A*
	 */
	private static final long TIMEOUT_IDA_NS   = (long)(20e9);
	private static final long TIMEOUT_ASTAR_NS = (long)(30e9);

	/**
	 * Limites du nombre de nœuds explorés pour éviter les explosions combinatoires
	 */
	private static final long NODE_CAP_IDA   = 50_000_000L;
	private static final long NODE_CAP_ASTAR = 100_000_000L;

	/* ======================= TEST BUT ======================= */

	/**
	 * Vérifie si le cube est dans l’état résolu
	 */
	private boolean isSolved(CubieCube c) {
		for (int i = 0; i < 8; i++) {
			if (c.cp[i].ordinal() != i || c.co[i] != 0) return false;
		}
		for (int i = 0; i < 12; i++) {
			if (c.ep[i].ordinal() != i || c.eo[i] != 0) return false;
		}
		return true;
	}

	/* ======================= HEURISTIQUES ======================= */

	/**
	 * Heuristique qui compte les pièces mal placées et mal orientées
	 */
	private int hMalPlaces(CubieCube c) {
	    int h = 0;
	    for (int i = 0; i < 8; i++) {
	        if (c.cp[i].ordinal() != i) h++;
	        if (c.co[i] != 0) h++;
	    }
	    for (int i = 0; i < 12; i++) {
	        if (c.ep[i].ordinal() != i) h++;
	        if (c.eo[i] != 0) h++;
	    }
	    return h / 8;
	}

	/**
	 * Table de distances minimales entre positions des coins
	 * Elle est utilisée pour calculer une heuristique plus précise
	 */
	private static final int[][] DIST_COINS = {
	    {0,2,2,2,1,2,3,2},
	    {2,0,2,2,2,1,2,3},
	    {2,2,0,2,3,2,1,2},
	    {2,2,2,0,2,3,2,1},
	    {1,2,3,2,0,2,2,2},
	    {2,1,2,3,2,0,2,2},
	    {3,2,1,2,2,2,0,2},
	    {2,3,2,1,2,2,2,0}
	};

	/**
	 * Table de distances minimales entre positions des arêtes
	 */
	private static final int[][] DIST_ARETES = {
	    {0,1,1,1,2,2,2,2,1,2,2,1},
	    {1,0,1,1,2,2,2,2,1,1,2,2},
	    {1,1,0,1,2,2,2,2,2,1,1,2},
	    {1,1,1,0,2,2,2,2,2,2,1,1},
	    {2,2,2,2,0,1,1,1,1,2,2,1},
	    {2,2,2,2,1,0,1,1,1,1,2,2},
	    {2,2,2,2,1,1,0,1,2,1,1,2},
	    {2,2,2,2,1,1,1,0,2,2,1,1},
	    {1,1,2,2,1,1,2,2,0,2,2,1},
	    {2,1,1,2,2,1,1,2,2,0,1,2},
	    {2,2,1,1,2,2,1,1,2,1,0,2},
	    {1,2,2,1,1,2,2,1,1,2,2,0}
	};

	/**
	 * Heuristique basée sur la distance de Manhattan des pièces
	 */
	private int hManhattan(CubieCube c) {
	    int h = 0;

	    for (int pos = 0; pos < 8; pos++) {
	        int coin = c.cp[pos].ordinal();
	        if (coin != pos) h += DIST_COINS[pos][coin];
	        if (c.co[pos] != 0) h++;
	    }

	    for (int pos = 0; pos < 12; pos++) {
	        int arete = c.ep[pos].ordinal();
	        if (arete != pos) h += DIST_ARETES[pos][arete];
	        if (c.eo[pos] != 0) h++;
	    }

	    return h / 8;
	}

	/**
	 * Heuristique combinée prenant la valeur maximale entre deux heuristiques
	 */
	private int hKorf(CubieCube c) {
	    return Math.max(hManhattan(c), hMalPlaces(c));
	}

	/* ======================= SUCCESSEURS ======================= */

	/**
	 * Représente un état successeur obtenu après un mouvement
	 */
	private static class Successeur {
		final CubieCube cube;
		final int move;
		Successeur(CubieCube c, int m) { cube = c; move = m; }
	}

	/**
	 * Génère les successeurs d’un état en évitant les coups redondants
	 */
	private List<Successeur> successeurs(CubieCube cube, int lastMove) {
		List<Successeur> res = new ArrayList<>(NB_MOVES);

		for (int mv = 0; mv < NB_MOVES; mv++) {
			if (lastMove != -1 && MOVE_FACE[mv] == MOVE_FACE[lastMove]) continue;

			CubieCube next = cube.copy();
			applyMove18(next, mv);
			res.add(new Successeur(next, mv));
		}
		return res;
	}

	/* ======================= IDA* ======================= */

	/**
	 * Stocke les informations nécessaires pendant la recherche IDA*
	 */
	private static class IdaState {
		int depth;
		int[] path;
		long nodes;
	}

	/**
	 * Lance l’algorithme IDA* avec une heuristique donnée
	 */
	private Result lancerIDA(String nom, CubieCube start, ToIntFunction<CubieCube> h) {
		long t0 = System.nanoTime();

		if (isSolved(start)) {
			return new Result(nom, "(déjà résolu)", 0, 0.0, 1, true);
		}

		IdaState s = new IdaState();
		int[] path = new int[40];
		int bound = h.applyAsInt(start);

		while (true) {
			int t = idaDFS(start, 0, bound, -1, path, h, s, t0);
			if (t == -1) {
				double temps = (System.nanoTime() - t0) / 1e9;
				return new Result(nom, pathToString(s.path, s.depth), s.depth, temps, s.nodes, true);
			}
			if (t == Integer.MAX_VALUE) {
				double temps = (System.nanoTime() - t0) / 1e9;
				return new Result(nom, "", 0, temps, s.nodes, false);
			}
			bound = t;
		}
	}

	/**
	 * Parcours en profondeur utilisé par IDA*
	 */
	private int idaDFS(CubieCube c, int g, int bound, int lastMove,
			int[] path, ToIntFunction<CubieCube> h,
			IdaState s, long t0) {

		if (System.nanoTime() - t0 > TIMEOUT_IDA_NS || s.nodes > NODE_CAP_IDA)
			return Integer.MAX_VALUE;

		int f = g + h.applyAsInt(c);
		if (f > bound) return f;

		s.nodes++;

		if (isSolved(c)) {
			s.depth = g;
			s.path = Arrays.copyOf(path, g);
			return -1;
		}

		int min = Integer.MAX_VALUE;
		for (Successeur suc : successeurs(c, lastMove)) {
			path[g] = suc.move;
			int t = idaDFS(suc.cube, g + 1, bound, suc.move, path, h, s, t0);
			if (t == -1) return -1;
			min = Math.min(min, t);
		}
		return min;
	}

	/* ======================= A* / GREEDY ======================= */

	/**
	 * Représente un nœud utilisé dans les algorithmes A* et Greedy
	 */
	private static class Node {
		CubieCube cube;
		int g, f, lastMove;
		Node parent;
		Node(CubieCube c, int g, int f, int lm, Node p) {
			cube = c; this.g = g; this.f = f; lastMove = lm; parent = p;
		}
	}

	/**
	 * Lance l’algorithme A* ou Greedy selon le mode choisi
	 */
	private Result lancerAStar(String nom, CubieCube start,
			ToIntFunction<CubieCube> h, boolean greedy) {

		long t0 = System.nanoTime();
		long nodes = 0;

		PriorityQueue<Node> open =
				new PriorityQueue<>(Comparator.comparingInt(n -> n.f));

		int h0 = h.applyAsInt(start);
		open.add(new Node(start, 0, h0, -1, null));

		while (!open.isEmpty()) {

			if (System.nanoTime() - t0 > TIMEOUT_ASTAR_NS || nodes > NODE_CAP_ASTAR)
				return new Result(nom, "", 0,
						(System.nanoTime() - t0) / 1e9, nodes, false);

			Node n = open.poll();
			nodes++;

			if (isSolved(n.cube)) {
				List<Integer> path = new ArrayList<>();
				while (n.parent != null) {
					path.add(n.lastMove);
					n = n.parent;
				}
				Collections.reverse(path);
				return new Result(nom, pathToString(listToArray(path), path.size()),
						path.size(), (System.nanoTime() - t0) / 1e9, nodes, true);
			}

			for (Successeur suc : successeurs(n.cube, n.lastMove)) {
				int g2 = n.g + 1;
				int h2 = h.applyAsInt(suc.cube);
				int f2 = greedy ? h2 : g2 + h2;
				open.add(new Node(suc.cube, g2, f2, suc.move, n));
			}
		}

		return new Result(nom, "", 0,
				(System.nanoTime() - t0) / 1e9, nodes, false);
	}

	/* ======================= UTILITAIRES ======================= */

	/**
	 * Convertit une suite de mouvements en chaîne lisible
	 */
	private String pathToString(int[] p, int len) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i++) {
			if (i > 0) sb.append(" ");
			sb.append(MOVE_NAMES[p[i]]);
		}
		return sb.toString();
	}

	/**
	 * Convertit une liste de mouvements en tableau
	 */
	private int[] listToArray(List<Integer> l) {
		return l.stream().mapToInt(i -> i).toArray();
	}

	/* ======================= API PUBLIQUE ======================= */

	/**
	 * Lance IDA* avec l’heuristique combinée de Korf
	 */
	public Result joueur1_IDA_Korf(CubieCube c) {
		return lancerIDA("IDA* + hKorf", c.copy(), this::hKorf);
	}

	/**
	 * Lance IDA* avec l’heuristique des pièces mal placées
	 */
	public Result joueur2_IDA_MalPlacees(CubieCube c) {
		return lancerIDA("IDA* + hMalPlaces", c.copy(), this::hMalPlaces);
	}

	/**
	 * Lance IDA* avec l’heuristique Manhattan
	 */
	public Result joueur3_IDA_Manhattan(CubieCube c) {
		return lancerIDA("IDA* + hManhattan", c.copy(), this::hManhattan);
	}

	/**
	 * Lance A* avec l’heuristique Manhattan
	 */
	public Result joueur4_AStar_Manhattan(CubieCube c) {
		return lancerAStar("A* + hManhattan", c.copy(), this::hManhattan, false);
	}

	/**
	 * Lance A* avec l’heuristique des pièces mal placées
	 */
	public Result joueur5_AStar_MalPlacees(CubieCube c) {
		return lancerAStar("A* + hMalPlaces", c.copy(), this::hMalPlaces, false);
	}

	/**
	 * Lance l’algorithme Greedy avec l’heuristique des pièces mal placées
	 */
	public Result joueur6_Greedy_MalPlacees(CubieCube c) {
		return lancerAStar("Greedy + hMalPlaces", c.copy(), this::hMalPlaces, true);
	}
}
