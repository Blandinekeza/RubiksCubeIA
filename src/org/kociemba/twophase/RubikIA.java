package org.kociemba.twophase;

import java.util.*;
import java.util.function.ToIntFunction;

public class RubikIA {

	/* ======================= RESULT ======================= */

	public static class Result {
		public final String nomAlgo;
		public final String solution;
		public final int coups;
		public final double tempsSec;
		public final long noeuds;
		public final boolean succes;

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

	// 18 mouvements : U, U2, U', R, R2, R', ..., B, B2, B'
	private static final String[] MOVE_NAMES = {
			"U","U2","U'","R","R2","R'","F","F2","F'",
			"D","D2","D'","L","L2","L'","B","B2","B'"
	};

	// Face associée à chaque move (0..5), répété 3 fois
	private static final int[] MOVE_FACE = {
			0,0,0, 1,1,1, 2,2,2, 3,3,3, 4,4,4, 5,5,5
	};

	private static void applyMove18(CubieCube cube, int move18) {
		int face = move18 / 3;      // 0..5
		int power = move18 % 3;     // 0=quarter, 1=double, 2=prime (inverse)

		// power 0 -> 1 fois, power 1 -> 2 fois, power 2 -> 3 fois
		int times = (power == 0) ? 1 : (power == 1) ? 2 : 3;

		for (int i = 0; i < times; i++) {
			cube.multiply(CubieCube.moveCube[face]);
		}
	}


	// IMPORTANT : on force 18 moves, pas la longueur de CubieCube.moveCube (souvent = 6)
	private static final int NB_MOVES = 18;


	private static final long TIMEOUT_IDA_NS   = (long)(20e9);
	private static final long TIMEOUT_ASTAR_NS = (long)(30e9);

	private static final long NODE_CAP_IDA   = 50_000_000L;
	private static final long NODE_CAP_ASTAR = 100_000_000L;

	/* ======================= TEST BUT ======================= */

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
		return h / 4;
	}

	private int hManhattan(CubieCube c) {
		int h = 0;

		for (int i = 0; i < 8; i++) {
			int diff = Math.abs(c.cp[i].ordinal() - i);
			h += diff;
			if (c.co[i] != 0) h++;
		}

		for (int i = 0; i < 12; i++) {
			int diff = Math.abs(c.ep[i].ordinal() - i);
			h += diff;
			if (c.eo[i] != 0) h++;
		}

		return h / 6;
	}


	private int hKorf(CubieCube c) {
		return Math.max(hManhattan(c), hMalPlaces(c));
	}

	/* ======================= SUCCESSEURS ======================= */

	private static class Successeur {
		final CubieCube cube;
		final int move;
		Successeur(CubieCube c, int m) { cube = c; move = m; }
	}

	private List<Successeur> successeurs(CubieCube cube, int lastMove) {
		List<Successeur> res = new ArrayList<>(NB_MOVES);

		for (int mv = 0; mv < NB_MOVES; mv++) {
			if (lastMove != -1 && MOVE_FACE[mv] == MOVE_FACE[lastMove]) {
				continue; // pruning : éviter deux coups sur la même face
			}

			CubieCube next = cube.copy();
			applyMove18(next, mv);
			res.add(new Successeur(next, mv));
		}
		return res;


	}

	/* ======================= IDA* ======================= */

	private static class IdaState {
		int depth;
		int[] path;
		long nodes;
	}

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
				String sol = pathToString(s.path, s.depth);
				return new Result(nom, sol, s.depth, temps, s.nodes, true);
			}
			if (t == Integer.MAX_VALUE) {
				double temps = (System.nanoTime() - t0) / 1e9;
				return new Result(nom, "", 0, temps, s.nodes, false);
			}
			bound = t;
		}
	}

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

	private static class Node {
		CubieCube cube;
		int g, f, lastMove;
		Node parent;
		Node(CubieCube c, int g, int f, int lm, Node p) {
			cube = c; this.g = g; this.f = f; lastMove = lm; parent = p;
		}
	}

	private Result lancerAStar(String nom, CubieCube start,
			ToIntFunction<CubieCube> h, boolean greedy) {

		long t0 = System.nanoTime();
		long nodes = 0;

		PriorityQueue<Node> open =
				new PriorityQueue<>(Comparator.comparingInt(n -> n.f));

		int h0 = h.applyAsInt(start);
		open.add(new Node(start, 0, greedy ? h0 : h0, -1, null));


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
				String sol = pathToString(listToArray(path), path.size());
				double temps = (System.nanoTime() - t0) / 1e9;
				return new Result(nom, sol, path.size(), temps, nodes, true);
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

	private String pathToString(int[] p, int len) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i++) {
			if (i > 0) sb.append(" ");
			sb.append(MOVE_NAMES[p[i]]);
		}
		return sb.toString();
	}

	private int[] listToArray(List<Integer> l) {
		return l.stream().mapToInt(i -> i).toArray();
	}

	/* ======================= API PUBLIQUE ======================= */

	public Result joueur1_IDA_Korf(CubieCube c) {
		return lancerIDA("IDA* + hKorf", c.copy(), this::hKorf);
	}

	public Result joueur2_IDA_MalPlacees(CubieCube c) {
		return lancerIDA("IDA* + hMalPlaces", c.copy(), this::hMalPlaces);
	}

	public Result joueur3_IDA_Manhattan(CubieCube c) {
		return lancerIDA("IDA* + hManhattan", c.copy(), this::hManhattan);
	}

	public Result joueur4_AStar_Manhattan(CubieCube c) {
		return lancerAStar("A* + hManhattan", c.copy(), this::hManhattan, false);
	}

	public Result joueur5_AStar_MalPlacees(CubieCube c) {
		return lancerAStar("A* + hMalPlaces", c.copy(), this::hMalPlaces, false);
	}

	public Result joueur6_Greedy_MalPlacees(CubieCube c) {
		return lancerAStar("Greedy + hMalPlaces", c.copy(), this::hMalPlaces, true);
	}
}
