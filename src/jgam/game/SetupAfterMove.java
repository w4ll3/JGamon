package jgam.game;

/**
 * A setup after a move has been performed. it wraps a setup and a move.
 */
class SetupAfterMove extends WrappedBoardSetup {
	SingleMove move;

	SetupAfterMove(BoardSetup setup, SingleMove move) {
		super(setup);
		this.move = move;
	}

	public int getPoint(int player, int pointnumber) {
		if (player == move.player()) {
			if (pointnumber == move.from()) {
				return board.getPoint(player, pointnumber) - 1;
			} else if (pointnumber == move.to()) {
				return board.getPoint(player, pointnumber) + 1;
			} else {
				return board.getPoint(player, pointnumber);
			}
		} else {
			if (move.to() != 0 && pointnumber == 25 - move.to()) {
				return 0;
			} else {
				return board.getPoint(player, pointnumber);
			}
		}
	}

}
