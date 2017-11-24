package net.wasamon.geister.player;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.wasamon.geister.utils.Item;
import net.wasamon.geister.utils.ItemColor;

public class HumanGUIPlayer extends BasePlayer {

    JFrame frame;
    final P[] own = new P[8];
    final P[] enemy = new P[8];

    int takenRed = 0;
    int takenBlue = 0;

	boolean[] enemiesFlag;
	boolean[] enemiesDiffFlag;

    public HumanGUIPlayer() {
		enemiesDiffFlag = new boolean[36];
		enemiesFlag = new boolean[36];
		for(int i = 0;i < enemiesDiffFlag.length; i++){
			enemiesDiffFlag[i] = false;
			enemiesFlag[i] = false;
		}
		int cnt = 0;
		for (int y = 0; y < 2; y++) {
			for (int x = 0; x < 4; x++) {
                own[y * 4 + x] = new P(x + 1, y + 4, cnt);
                enemy[y*4+x] = new P(x+1, y+4, cnt);
				enemiesFlag[(y+4)*6+(x+1)] = true;
				cnt++;
            }
        }
    }

    private String label = "Geister Human Player -";
    
    public void makeGUI() {
        frame = new JFrame();
        frame.setTitle(label);
        frame.setResizable(false);
        frame.getContentPane().add(new Canvas(this));
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

    private void updateBoard() {
        Item[] items;
		boolean[] flags = new boolean[36];
		for(int i = 0; i < flags.length; i++) flags[i] = false;
		
        items = getOppositeItems();
        for (int i = 0; i < items.length; i++) {
            enemy[i] = new P(items[i].getX(), items[i].getY(), i);
            enemy[i].c = items[i].getColor();
			if(items[i].getX() < 6 && items[i].getY() < 6){
				flags[items[i].getX() + items[i].getY() * 6] = true;
			}else{ // special case (taken by own-side)
				
			}
        }
		
		if(enemiesFlag != null){
			for(int i = 0; i < enemiesFlag.length; i++){
				enemiesDiffFlag[i] = (enemiesFlag[i] != flags[i]);
			}
		}
		enemiesFlag = flags;

		items = getOwnItems();
        for (int i = 0; i < items.length; i++) {
            own[i] = new P(items[i].getX(), items[i].getY(), i);
            own[i].c = items[i].getColor();
			if(items[i].getX() < 6 && items[i].getY() < 6){
				enemiesDiffFlag[6-items[i].getX()-1 + (6-items[i].getY()-1) * 6] = false;
			}
        }

		
        items = getOppositeTakenItems();
        takenRed = 0;
        takenBlue = 0;
        for (int i = 0; i < items.length; i++) {
            if (items[i].getColor() == ItemColor.RED) {
                takenRed++;
            } else if (items[i].getColor() == ItemColor.BLUE) {
                takenBlue++;
            }
        }
        printBoard(); // output to stdout
        SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					frame.repaint();
				}
			});
    }

    boolean moveFlag = false;
    String moveInst = "";

	boolean turnFlag = false;

    public void start(String host, String port, String init) throws IOException{
        init(host, Integer.parseInt(port));
        System.out.println(setRedItems(init));
        
        SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					makeGUI();
				}
			});
		
        System.out.println("Waiting for an opposite player...");
		
		String boardStr;
		// wait for board information
        boardStr = waitBoardInfo();

        updateBoard();

        GAME_LOOP: while (true) {
            if (isEnded() == true)
                break GAME_LOOP;
            moveFlag = false; // accept user input
            // wait for user input
            while (true) {
				turnFlag = true;
                SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							frame.setTitle(label + " your turn");
						}
					});
                if (moveFlag)
                    break;
                try {
                    Thread.sleep(300);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            
            SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						frame.setTitle(label);
					}
				});

            // own move
            move(moveInst);
			if (isEnded() == true){ // opposite player has been dead
                break GAME_LOOP;
			}

            if(getLastTookColor().equals("R")) takenRed++;
            if(getLastTookColor().equals("B")) takenBlue++;
            SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						frame.repaint();
					}
				});
			turnFlag = false;
            // wait for board information after the opposite turn 
            String result = waitBoardInfo();
            updateBoard();
        }

        String status = "";
        if (isWinner()) {
            System.out.println("won");
            status = "won";
        } else if (isLoser()) {
            System.out.println("lost");
            status = "lost";
        } else if (isDraw()) {
            System.out.println("draw");
            status = "draw";
        }
        JOptionPane.showMessageDialog(frame, status);
        System.exit(0);
    }

    public static void main(String[] args) throws Exception {
        HumanGUIPlayer p = new HumanGUIPlayer();
        if (args.length < 3) {
            System.out.println(
							   "java -cp build/libs/geister.jar net.wasamon.geister.player.HumanGUIPlayer host 10000/10001 init");
            System.out.println(
							   "ex. java -cp build/libs/geister.jar net.wasamon.geister.player.HumanGUIPlayer localhost 10000 ABCD");
        }
        p.start(args[0], args[1], args[2]);
    }

}

class P {
    int x, y;
    ItemColor c = ItemColor.UNKNOWN;
	int id;

    public P(int x, int y, int id) {
        this.x = x;
        this.y = y;
		this.id = id;
    }
}

class Canvas extends JPanel implements MouseListener, MouseMotionListener {

    private final int DIM = 560;
    private final int dim = 560 / 8;

    private final HumanGUIPlayer player;

    private final String url_u = "net/wasamon/geister/player/geister_obj.png";
    private final String url_r = "net/wasamon/geister/player/geister_red.png";
    private final String url_b = "net/wasamon/geister/player/geister_blue.png";
    private final String url_arrow_l = "net/wasamon/geister/player/arrow_l.png";
    private final String url_arrow_r = "net/wasamon/geister/player/arrow_r.png";

    private final BufferedImage img_u, img_r, img_b;
    private final BufferedImage img_arrow_l, img_arrow_r;

    public Canvas(HumanGUIPlayer player) {
        this.player = player;
        this.setSize(DIM, DIM);
        this.setPreferredSize(new Dimension(DIM, DIM));
        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        try {
            img_u = ImageIO.read(getClass().getClassLoader().getResourceAsStream(url_u));
            img_r = ImageIO.read(getClass().getClassLoader().getResourceAsStream(url_r));
            img_b = ImageIO.read(getClass().getClassLoader().getResourceAsStream(url_b));
            img_arrow_l = ImageIO.read(getClass().getClassLoader().getResourceAsStream(url_arrow_l));
            img_arrow_r = ImageIO.read(getClass().getClassLoader().getResourceAsStream(url_arrow_r));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void paintComponent(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, DIM, DIM);
		paintTurnIndicator(g);
		paintEscape(g);
		paintDiffItem((Graphics2D)g);
        paintBoarder(g);
        paintItems((Graphics2D) g);
    }

	private void paintTurnIndicator(Graphics g){
		g.setColor(Color.RED);
		if(player.turnFlag){
			g.fillRect(0, DIM-10, DIM, 10);
		}else{
			g.fillRect(0, 0, DIM, 10);
		}
        g.setColor(Color.WHITE);
	}

	private void paintEscape(Graphics g){
		g.drawImage(img_arrow_l, convX(0), convY(0), dim, dim, null);
		g.drawImage(img_arrow_r, convX(5), convY(0), dim, dim, null);
		g.drawImage(img_arrow_l, convX(0), convY(5), dim, dim, null);
		g.drawImage(img_arrow_r, convX(5), convY(5), dim, dim, null);
	}

	private Color diffColor = new Color(253, 180, 240);
	private void paintDiffItem(Graphics2D g){
		for(int i = 0; i < player.enemiesDiffFlag.length; i++){
			if(player.enemiesDiffFlag[i]){
				g.setColor(diffColor);
				g.fillRect(convX(6-i%6-1), convY(6-i/6-1), dim, dim);
				g.setColor(Color.BLACK);
			}
		}
	}

    private Font font = new Font("Arial", Font.BOLD, 36);
    private Font font2 = new Font("Arial", Font.BOLD, 16);

    private void paintBoarder(Graphics g) {
        g.setColor(Color.BLACK);
        for (int i = 0; i < 7; i++) {
            g.drawLine(dim * (i + 1), dim, dim * (i + 1), dim * 7);
            g.drawLine(dim, dim * (i + 1), dim * 7, dim * (i + 1));
        }
        g.setFont(font);
        g.drawImage(img_r, convX(2), convY(6), dim, dim, null);
        g.drawString(String.valueOf(player.takenRed), convX(3), convY(7) - dim / 2);
        g.drawImage(img_b, convX(4), convY(6), dim, dim, null);
        g.drawString(String.valueOf(player.takenBlue), convX(5), convY(7) - dim / 2);
    }

	private String[] label = new String[]{"A", "B", "C", "D", "E", "F", "G", "H"};
	
    private void paintItems(Graphics2D g) {
        g.setFont(font2);
        final AffineTransform at = new AffineTransform();
        at.rotate(180 * Math.PI / 180.0, DIM / 2, DIM / 2);
        g.setTransform(at);
        for (int i = 0; i < player.enemy.length; i++) {
            P p = player.enemy[i];
            if(isOwnOccupied(5-p.x, 5-p.y) == false){
				BufferedImage img = p.c == ItemColor.RED ? img_r : p.c == ItemColor.BLUE ? img_b : img_u;
                g.drawImage(img, convX(p.x), convY(p.y), dim, dim, null); // to rotate
				g.drawString(label[p.id], convX(p.x)+2, convY(p.y)+16);
            }
        }
        at.rotate(180 * Math.PI / 180.0, DIM / 2, DIM / 2);
        g.setTransform(at);
        for (int i = 0; i < player.own.length; i++) {
            if (selected != i) {
                P p = player.own[i];
                BufferedImage img = p.c == ItemColor.RED ? img_r : p.c == ItemColor.BLUE ? img_b : img_u;
                g.drawImage(img, convX(p.x), convY(p.y), dim, dim, null);
				g.drawString(label[p.id], convX(p.x)+2, convY(p.y)+16);
            }
        }
        if (selected != -1) {
            P p = player.own[selected];
            BufferedImage img = p.c == ItemColor.RED ? img_r : p.c == ItemColor.BLUE ? img_b : img_u;
            g.drawImage(img, selectedX, selectedY, dim, dim, null);
			g.drawString(label[p.id], convX(selectedX)+2, convY(selectedY)+16);
        }
    }

    private int convX(int x) {
        return dim * (x + 1);
    }

    private int convY(int y) {
        return dim * (y + 1);
    }

    private boolean isInternal(P p, int x, int y) {
        int x0 = convX(p.x);
        int y0 = convY(p.y);
        return (x0 < x) && (x < x0 + dim) && (y0 < y) && (y < y0 + dim);
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
        selected = -1;
        repaint();
    }

    public void mouseClicked(MouseEvent e) {
    }

    private int selected = -1;
    private int selectedX;
    private int selectedY;

    public void mousePressed(MouseEvent e) {
        if(player.moveFlag) return; // before receiving board info., user cannot send any commands 
        for (int i = 0; i < player.own.length; i++) {
            if (isInternal(player.own[i], e.getX(), e.getY())) {
                selected = i;
                selectedX = e.getX() - dim / 2;
                selectedY = e.getY() - dim / 2;
            }
        }
    }

    private P conv(int x, int y) {
        int x0 = x - dim >= 0 ? (x - dim) / dim : -1;
        int y0 = y - dim >= 0 ? (y - dim) / dim : -1;
        return new P(x0, y0, 0);
    }

    private boolean isOwnOccupied(int x, int y) {
        //System.out.println("own check:" + x + "," + y);
        for (int i = 0; i < player.own.length; i++) {
            if (player.own[i].x == x && player.own[i].y == y) {
                //System.out.println("own occupied:" + i);
                return true;
            }
        }
        return false;
    }

    private boolean isEnemyOccupied(int x, int y) {
        //System.out.println("enemy check:" + x + "," + y);
        for (int i = 0; i < player.own.length; i++) {
            if (player.enemy[i].x == x && player.enemy[i].y == y) {
                System.out.println("enemy occupied:" + i);
                return true;
            }
        }
        return false;
    }

    private boolean isMovable(P p0, P p1) {
        System.out.printf("isMovable from (%d,%d) -> (%d,%d)\n", p0.x, p0.y, p1.x, p1.y);
        if (p0.c == ItemColor.BLUE && p0.x == 0 && p0.y == 0 && (p1.x == -1 || p1.y == -1))
            return true; // escape
        if (p0.c == ItemColor.BLUE && p0.x == 5 && p0.y == 0 && (p1.x == 6 || p1.y == -1))
            return true; // escape
        if (p1.x < 0 || p1.x >= 6 || p1.y < 0 || p1.y >= 6)
            return false; // outside
        if (p0.x == p1.x && p0.y == p1.y)
            return false; // same place
        if (p0.x != p1.x && p0.y != p1.y)
            return false; // not same row and column
        if (Math.abs(p0.x - p1.x) > 1 || Math.abs(p0.y - p1.y) > 1)
            return false; // allowed only just neighbor
        if (isOwnOccupied(p1.x, p1.y))
            return false; // own occupied
        return true;
    }

    private String moveInst(P p0, P p1) {
        String label = new String(new char[] { (char) ('A' + selected) });
        if (p0.x == p1.x && p0.y + 1 == p1.y)
            return label + ",S";
        if (p0.x == p1.x && p0.y - 1 == p1.y)
            return label + ",N";
        if (p0.x + 1 == p1.x && p0.y == p1.y)
            return label + ",E";
        if (p0.x - 1 == p1.x && p0.y == p1.y)
            return label + ",W";
        return "ERROR";
    }

    public void mouseReleased(MouseEvent e) {
        if (selected == -1) {
            repaint();
            return;
        }
        P p = conv(e.getX(), e.getY());
        if (isMovable(player.own[selected], p) == false) {
            selected = -1;
            repaint();
            return;
        }
        player.moveInst = moveInst(player.own[selected], p);
        player.moveFlag = true;
        player.own[selected].x = p.x;
        player.own[selected].y = p.y;
        selected = -1;
        repaint();
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
        if (selected != -1) {
            selectedX = e.getX() - dim / 2;
            selectedY = e.getY() - dim / 2;
            repaint();
        }
    }

}
