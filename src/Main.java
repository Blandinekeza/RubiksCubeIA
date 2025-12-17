import java.awt.Color;
import java.awt.Font;
import javax.swing.*;
import java.awt.event.*;
import java.util.List;
import java.util.Arrays;


/*
 * Classe principale de l’application.
 * Elle gère l’interface graphique du Rubik’s Cube.
 * Elle affiche le cube, les boutons et capte les interactions utilisateur.
 */
class Main extends JFrame implements KeyListener, MouseListener {

	/*
	 * La classe main est le point d'entree du code. Elle permet de lancer l'interface graphique. 
	 * Elle cree les boutons (JButton[][] face)
	 * Elle crée les controles(solve, clear, fill, Scramble)
	 * Elle crée les evenements clavier/souris(KeyListener, MouseListener)
	 * Elle instancie -> Solver.java
	 */

	// Instance du solveur qui contient toute la logique du cube
	Solver solver;

	// Taille d’un carré du cube à l’écran
	int faceSize = 49;

	// Couleur actuellement sélectionnée par l’utilisateur
	Color pickedColor = Color.lightGray; 

	// Représentation graphique du cube (6 faces de 9 cases)
	JButton[][] face = new JButton[6][9];

	// Boutons servant à choisir une couleur
	JButton[] colorPicker = new JButton[6]; 

	// Boutons de contrôle de l’application
	JButton solve, clear, fill, apply, randomScramble;

	// Champ de texte pour entrer des mouvements manuellement
	JTextField moves;

	// Labels d’information pour l’interface
	JLabel colorPickerLabel, message, randomScrambleLabel, movesLabel;

	/*
	 * Constructeur de la fenêtre principale.
	 * Initialise la fenêtre, les boutons, le cube et les événements.
	 */
	Main() {
		getContentPane().setLayout(null); 
		setTitle("Solve your 3 x 3 x 3");
		setSize(620, 750);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
		addKeyListener(this);

		// Positions des faces du cube dans la fenêtre
		int[] xPos = {3, 3, 3, 9, 0, 6};
		int[] yPos = {0, 6, 3, 3, 3, 3};

		// Couleurs utilisées pour représenter les faces
		Color[] colors = {
			Color.white, 
			Color.yellow, 
			Color.green, 
			Color.blue, 
			new Color(255, 128, 0), 
			Color.red, 
			Color.lightGray
		};

		// Création des boutons de sélection de couleur
		for(int i = 0; i < 6; i++) {
			colorPicker[i] = new JButton();
			colorPicker[i].setBackground(colors[i]);
			colorPicker[i].setOpaque(true);
			colorPicker[i].setName(Integer.toString(i));
			colorPicker[i].setBounds(
				faceSize * 7 + faceSize / 5 * 4 * i, 
				faceSize, 
				faceSize / 5 * 4, 
				faceSize / 5 * 4
			);
			colorPicker[i].addKeyListener(this);

			// Lors d’un clic, la couleur choisie devient la couleur active
			colorPicker[i].addActionListener(event ->
				pickedColor = colors[
					Integer.parseInt(((JButton)event.getSource()).getName())
				]
			);
			getContentPane().add(colorPicker[i]);
		} 

		// Label indiquant la zone de sélection de couleur
		colorPickerLabel = new JLabel("Color Picker");
		colorPickerLabel.setFont(new Font("Monospace Regular", Font.PLAIN, 13));
		colorPickerLabel.setBounds(faceSize * 7, faceSize / 2, faceSize * 4, faceSize * 2 / 3);
		getContentPane().add(colorPickerLabel);

		// Label utilisé pour afficher des messages à l’utilisateur
		message = new JLabel();
		message.setFont(new Font("Monospace Bold", Font.PLAIN, 14));
		message.setBounds(10, faceSize * 11, faceSize * 12, faceSize * 3);
		getContentPane().add(message);

		// Création graphique des 6 faces du cube
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < 9; j++) {
				face[i][j] = new JButton();
				face[i][j].setBackground(Color.lightGray);
				face[i][j].setOpaque(true);
				face[i][j].setBounds(
					faceSize * xPos[i] + faceSize * (j % 3), 
					faceSize * yPos[i] + faceSize * (j / 3), 
					faceSize, 
					faceSize
				);
				getContentPane().add(face[i][j]);
			}
		}

		// Initialisation du solveur avec les composants graphiques
		solver = new Solver(face, randomScrambleLabel, message, colors);

		// Ajout des événements clavier et souris sur chaque case du cube
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < 9; j++) {
				face[i][j].addKeyListener(this);
				face[i][j].addActionListener(event -> {
					((JButton)event.getSource()).setBackground(pickedColor);
					solver.update2();
				});
			}
		}

		// Noms des faces affichés au centre de chaque face
		String[] faceNames = {"U", "D", "F", "B", "L", "R"};
		for(int i = 0; i < 6; i++) 
			face[i][4].setText(faceNames[i]);

		// Bouton permettant de générer un mélange aléatoire
		randomScramble = new JButton("<html><b>Scramble</b></html>");
		randomScramble.setFont(new Font("Monospace Regular", Font.PLAIN, 13));
		randomScramble.setForeground(new Color(0, 0, 128));
		randomScramble.setBorder(BorderFactory.createLineBorder(new Color(51, 173, 255), 2));
		randomScramble.setBounds(faceSize * 7, faceSize * 7, 2 * faceSize, faceSize * 3 / 4);
		randomScramble.addKeyListener(this);
		randomScramble.setOpaque(true);
		randomScramble.addActionListener(event -> {
			solver.generateRandomScramble();
			message.setText("");
		});
		getContentPane().add(randomScramble);

		// Bouton lançant la résolution automatique du cube
		solve = new JButton("<html><b>SOLVE</b></html>");
		solve.setFont(new Font("Monospace Regular", Font.PLAIN, 13));
		solve.setBounds(faceSize * 10, faceSize * 7, 2 * faceSize, faceSize * 3 / 4);
		solve.addKeyListener(this);
		solve.setOpaque(true);
		solve.setForeground(new Color(26, 101, 26));
		solve.setBorder(BorderFactory.createLineBorder(new Color(0, 230, 0), 2));
		solve.addActionListener(event -> {
			solver.solve();
		});
		getContentPane().add(solve);

		// Bouton permettant de réinitialiser le cube
		clear = new JButton("<html><b>Clear</b></html>");
		clear.setBorder(BorderFactory.createLineBorder(Color.lightGray, 2));
		clear.setForeground(new Color(77, 77, 77));
		clear.setFont(new Font("Monospace Regular", Font.PLAIN, 13));
		clear.setBounds(faceSize * 7, faceSize * 8, 2 * faceSize, faceSize * 3 / 4);
		clear.addKeyListener(this);
		clear.setOpaque(true);
		clear.addActionListener(event -> {
			solver.clear();
			message.setText("");
			randomScrambleLabel.setText("");
		});
		getContentPane().add(clear);

		// Bouton permettant de remplir automatiquement le cube
		fill = new JButton("<html><b>Fill</b></html>");
		fill.setForeground(new Color(0, 0, 128));
		fill.setBorder(BorderFactory.createLineBorder(new Color(51, 173, 255), 2));
		fill.setFont(new Font("Monospace Regular", Font.PLAIN, 13));
		fill.setBounds(faceSize * 10, faceSize * 8, 2 * faceSize, faceSize * 3 / 4);
		fill.addKeyListener(this);
		fill.setOpaque(true);
		fill.addActionListener(event -> {
			solver.fill();
			message.setText("");
			randomScrambleLabel.setText("");
		});
		getContentPane().add(fill);
	}

	/*
	 * Gestion des entrées clavier.
	 * Chaque touche correspond à un mouvement du Rubik’s Cube.
	 */
	@Override
	public void keyPressed(KeyEvent event) {
		char key = event.getKeyChar();

		if(key == 'u') {
			solver.u();
		} else if(key == 'd') {
			solver.d();
		} else if(key == 'f') {
			solver.f();
		} else if(key == 'b') {
			solver.b();
		} else if(key == 'l') {
			solver.l();
		} else if(key == 'r') {
			solver.r();
		} else if(key == 'm') {
			solver.m();
		} else if(key == 'e') {
			solver.e();
		} else if(key == 's') {
			solver.s();
		}

		else if(key == 'U') {
			solver.uPrime();
		} else if(key == 'D') {
			solver.dPrime();
		} else if(key == 'F') {
			solver.fPrime();
		} else if(key == 'B') {
			solver.bPrime();
		} else if(key == 'L') {
			solver.lPrime();
		} else if(key == 'R') {
			solver.rPrime();
		} else if(key == 'M') {
			solver.mPrime();
		} else if(key == 'E') {
			solver.ePrime();
		} else if(key == 'S') {
			solver.sPrime();
		}

		else if(key == 'x') {
			solver.x();
		} else if(key == 'X') {
			solver.xPrime();
		} else if(key == 'y') {
			solver.y();
		} else if(key == 'Y') {
			solver.yPrime();
		} else if(key == 'z') {
			solver.z();
		} else if(key == 'Z') {
			solver.zPrime();
		}
	}

	// Gestion du clic souris pour garder le focus clavier
	@Override
	public void mouseClicked(MouseEvent event) {
		solve.requestFocusInWindow();
	}

	@Override public void mouseExited(MouseEvent event) {}
	@Override public void mouseEntered(MouseEvent event) {}
	@Override public void mouseReleased(MouseEvent event) {}
	@Override public void mousePressed(MouseEvent event) {}
	@Override public void keyReleased(KeyEvent event) {}
	@Override public void keyTyped(KeyEvent event) {}

	// Point d’entrée du programme
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new Main());
	}
}
