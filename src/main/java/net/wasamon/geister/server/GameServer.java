package net.wasamon.geister.server;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.wasamon.geister.utils.Direction;
import net.wasamon.geister.utils.ItemColor;
import net.wasamon.geister.utils.Board;
import net.wasamon.geister.utils.Item;
import net.wasamon.geister.utils.Constant;

/**
 * 
 * @author miyo
 *
 */
public class GameServer {

	public enum STATE {
		WAIT_FOR_INITIALIZATION, WAIT_FOR_PLAYER_0, WAIT_FOR_PLAYER_1, GAME_END
	}

	private STATE state;
	private Board board;
	private int winner;
	private boolean[] init_flags;
	private int turn_counter = 0;

	public GameServer() {
		init();
	}

	public int getWinner() {
		return winner;
	}

	public STATE getState() {
		return state;
	}

	public void init() {
		this.board = new Board();
		this.state = STATE.WAIT_FOR_INITIALIZATION;
		this.winner = -1;
		init_flags = new boolean[] { false, false };
		turn_counter = 0;
	}

	private Pattern SET_COMMAND = Pattern.compile("^SET:(\\w*)");
	private Pattern MOVE_COMMAND = Pattern.compile("^MOV:(\\w*),(\\w*)");

	/**
	 * Command parse
	 * 
	 * @param mesg
	 *            received message from client
	 * @param pid
	 *            player id
	 * @return success or failure
	 */
	public boolean parse(String mesg, int pid) {
		System.out.println("receive: " + mesg);
		boolean flag = false;
		if (state == STATE.WAIT_FOR_INITIALIZATION) {
			Matcher m = SET_COMMAND.matcher(mesg);
			if (m.matches() && m.group(1).length() == 4 && init_flags[pid] == false) {
				board.getPlayer(pid).setItemsColor("ABCDEFGH", ItemColor.BLUE); // clear
				board.getPlayer(pid).setItemsColor(m.group(1).toUpperCase(), ItemColor.RED);
				init_flags[pid] = true;
				if (init_flags[0] && init_flags[1]) {
					state = STATE.WAIT_FOR_PLAYER_0;
				}
				flag = true;
			} else {
				System.out.println("expected SET command, but " + mesg);
				flag = false;
			}
		} else if ((state == STATE.WAIT_FOR_PLAYER_0 && pid == 0) || (state == STATE.WAIT_FOR_PLAYER_1 && pid == 1)) {
			Matcher m = MOVE_COMMAND.matcher(mesg);
			if (m.matches()) {
				char k = m.group(1).toUpperCase().charAt(0);
				Direction d = Direction.dir(m.group(2).toUpperCase());
				if (d != null && 'A' <= k && k <= 'H') {
					flag = board.getPlayer(pid).move(new Character(k).toString(), d);
					if (flag) {
						turn_counter++;
						boolean judge = judgement();
						System.out.println("judge: " + judge);
						if (judge) {
							state = STATE.GAME_END;
						} else {
							state = state == STATE.WAIT_FOR_PLAYER_0 ? STATE.WAIT_FOR_PLAYER_1
									: STATE.WAIT_FOR_PLAYER_0;
						}
					}
				} else {
					System.out.println("Invalid arguments: dir=" + d + ", key=" + k);
					flag = false;
				}
			} else {
				System.out.println("expected MOVE command, but " + mesg);
				flag = false;
			}
		} else {
			flag = false;
		}
		System.out.println("result: " + flag);
		System.out.println("next: " + state);
		return flag;
	}

	private boolean judgement() {
		for (int pid = 0; pid < 2; pid++) {
			int taken_blue = 0;
			int taken_red = 0;
			for (Item item : board.getPlayer(pid).getItems()) {
				if (item.isEscaped()) {
					winner = pid;
					return true;
				} else if (item.isTaken()) {
					if (item.getColor() == ItemColor.RED) {
						taken_red += 1;
					} else if (item.getColor() == ItemColor.BLUE) {
						taken_blue += 1;
					}
				}
			}
			if (taken_blue == 4) {
				winner = pid == 0 ? 1 : 0; // opposite player won
				return true;
			} else if (taken_red == 4) {
				winner = pid;
				return true;
			}
		}
		if (turn_counter == Constant.MAX_TURN_COUNT) {
			winner = Constant.DRAW_MARK;
			return true;
		}
		return false;
	}

	/**
	 * print board information by 2nd player's viewing 1st player 0 1 2 3 4 5 0
	 * h g f e 1 d c b a 2 3 4 A B C D 5 E F G H 2nd player
	 */
	public void pp() {
		// print board
		System.out.println("  1st player");
		System.out.println(board.getBoardMap(1));
		System.out.println("  2nd player");

		// print all items
		System.out.print("1st player's items:");
		for (Item i : board.getPlayer(0).getItems()) {
			System.out.print(" " + i.getName().toLowerCase() + ":" + i.getColor().getSymbol());
		}
		System.out.println();
		System.out.print("2nd player's items:");
		for (Item i : board.getPlayer(1).getItems()) {
			System.out.print(" " + i.getName() + ":" + i.getColor().getSymbol());
		}
		System.out.println("");

		// print taken items
		System.out.print("taken 1st player's items:");
		for (Item i : board.getPlayer(0).getTakenItems()) {
			System.out.print(" " + i.getName().toLowerCase() + ":" + i.getColor().getSymbol());
		}
		System.out.println("");
		System.out.print("taken 2nd player's items:");
		for (Item i : board.getPlayer(1).getTakenItems()) {
			System.out.print(" " + i.getName() + ":" + i.getColor().getSymbol());
		}
		System.out.println("");

		// print escaped items
		System.out.print("escaped 1st player's items:");
		for (Item i : board.getPlayer(0).getEscapedItems()) {
			System.out.print(" " + i.getName().toLowerCase() + ":" + i.getColor().getSymbol());
		}
		System.out.println("");
		System.out.print("escaped 2nd player's items:");
		for (Item i : board.getPlayer(1).getEscapedItems()) {
			System.out.print(" " + i.getName().toLowerCase() + ":" + i.getColor().getSymbol());
		}
		System.out.println("");
		System.out.println("1st player's view:" + board.getEncodedBoard(0));
		System.out.println("2nd player's view:" + board.getEncodedBoard(1));
		System.out.println("");
	}

	public String getEncodedBoard(int viewer) {
		return board.getEncodedBoard(viewer);
	}

	public String getEncodedBoard(int viewer, boolean globalView) {
		return board.getEncodedBoard(viewer, globalView);
	}

}
