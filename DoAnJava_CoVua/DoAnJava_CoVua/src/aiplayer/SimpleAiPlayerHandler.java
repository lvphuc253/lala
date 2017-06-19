package aiplayer;

import java.util.ArrayList;
import java.util.List;

import logic.*;

public class SimpleAiPlayerHandler implements IPlayerHandler {

	private ChessGame chessGame;
	private MoveValidator validator;
	
	/**
	 * number of moves to look into the future
	 */
	public int maxDepth = 2;

	public SimpleAiPlayerHandler(ChessGame chessGame) {
		this.chessGame = chessGame;
		this.validator = this.chessGame.getMoveValidator();
	}

	@Override
	public Move getMove() {
		return getBestMove();
	}

	/**
	 * get best move for current game situation
	 * @return a valid Move instance
	 */
	private Move getBestMove() {
		System.out.println("thinking...");		
		List<Move> validMoves = generateMoves(false);
		int bestResult = Integer.MIN_VALUE;
		Move bestMove = null;
		
		for (Move move : validMoves) {
			executeMove(move);
			int evaluationResult = -1 * negaMax(this.maxDepth);
			undoMove(move);
			if( evaluationResult > bestResult){
				bestResult = evaluationResult;
				bestMove = move;
			}
		}
		System.out.println("done thinking! best move is: "+bestMove);
		return bestMove;
	}

	@Override
	public void moveSuccessfullyExecuted(Move move) {
		System.out.println("executed: "+move);
	}

	/**
	 * evaluate current game state according to nega max algorithm
	 *
	 * @param depth - current depth level (number of counter moves that still need to be evaluated)
	 * @param indent - debug string, that is placed in front of each log message
	 * @return integer score of game state after looking at "depth" counter moves
	 */
	private int negaMax(int depth) {

		if (depth <= 0|| this.chessGame.getGameState() == ChessGame.GAME_STATE_END_WHITE_WON
			|| this.chessGame.getGameState() == ChessGame.GAME_STATE_END_BLACK_WON){
                    
			return evaluateState();
		}
		
		List<Move> moves = generateMoves(false);
		int currentMax = Integer.MIN_VALUE;
		
		for(Move currentMove : moves){			
			executeMove(currentMove);
			int score = -1 * negaMax(depth - 1);
			undoMove(currentMove);
			
			if( score > currentMax){
				currentMax = score;
			}
		}
		return currentMax;
	}

	private void undoMove(Move move) {
		this.chessGame.undoMove(move);
	}

	private void executeMove(Move move) {
		this.chessGame.movePiece(move);
		this.chessGame.changeGameState();
	}

	//Tạo ra toàn bộ các nước đi hợp lệ
	private List<Move> generateMoves(boolean debug) {

            List<Piece> pieces = this.chessGame.getPieces();
            List<Move> validMoves = new ArrayList<Move>();
            Move testMove = new Move(0,0,0,0);
            int pieceColor = (this.chessGame.getGameState()==ChessGame.GAME_STATE_WHITE
                                                ?Piece.COLOR_WHITE : Piece.COLOR_BLACK);

            // Lặp lại tất cả các mảnh không bị bắt
            for (Piece piece : pieces) {
                // only look at pieces of current players color
                if (pieceColor == piece.getColor()) {
                    // start generating move
                    testMove.sourceRow = piece.getRow();
                    testMove.sourceColumn = piece.getColumn();

                    // iterate over all board rows and columns
                    for (int targetRow = Piece.ROW_1; targetRow <= Piece.ROW_8; targetRow++) {
                        for (int targetColumn = Piece.COLUMN_A; targetColumn <= Piece.COLUMN_H; targetColumn++) {
                            // finish generating move
                            testMove.targetRow = targetRow;
                            testMove.targetColumn = targetColumn;

                            if(debug) System.out.println("testing move: "+testMove);

                            // check if generated move is valid
                            if (this.validator.isMoveValid(testMove, true)) {
                                // valid move
                                validMoves.add(testMove.clone());
                            }
                        }
                    }
                }
            }
            return validMoves;
	}

	//Đánh giá trạng thái game
	private int evaluateState() {
            int scoreWhite = 0;
            int scoreBlack = 0;
            for (Piece piece : this.chessGame.getPieces()) {
                if(piece.getColor() == Piece.COLOR_BLACK){
                    scoreBlack +=getScoreForPieceType(piece.getType());
                    scoreBlack +=getScoreForPiecePosition(piece.getRow(),piece.getColumn());
                }else if( piece.getColor() == Piece.COLOR_WHITE){
                    scoreWhite +=getScoreForPieceType(piece.getType());
                    scoreWhite +=getScoreForPiecePosition(piece.getRow(),piece.getColumn());
                }
            }

            // return evaluation result depending on who's turn it is
            int gameState = this.chessGame.getGameState();
            if( gameState == ChessGame.GAME_STATE_BLACK){
                return scoreBlack - scoreWhite;
            }else if(gameState == ChessGame.GAME_STATE_WHITE){
                return scoreWhite - scoreBlack;
            }else if(gameState == ChessGame.GAME_STATE_END_WHITE_WON|| gameState == ChessGame.GAME_STATE_END_BLACK_WON){
                return Integer.MIN_VALUE + 1;
            }else{
                throw new IllegalStateException("unknown game state: "+gameState);
            }
	}
	
	// Điểm cho từng vị trí
	private int getScoreForPiecePosition(int row, int column) {
            byte[][] positionWeight =
            { {1,1,1,1,1,1,1,1}
             ,{2,2,2,2,2,2,2,2}
             ,{2,2,3,3,3,3,2,2}
             ,{2,2,3,4,4,3,2,2}
             ,{2,2,3,4,4,3,2,2}
             ,{2,2,3,3,3,3,2,2}
             ,{2,2,2,2,2,2,2,2}
             ,{1,1,1,1,1,1,1,1}
             };
            return positionWeight[row][column];
	}

	// Điểm cho từng loại quân cờ
	private int getScoreForPieceType(int type){
            switch (type) {
                case Piece.TYPE_BISHOP: return 30;
                case Piece.TYPE_KING: return 99999;
                case Piece.TYPE_KNIGHT: return 30;
                case Piece.TYPE_PAWN: return 10;
                case Piece.TYPE_QUEEN: return 90;
                case Piece.TYPE_ROOK: return 50;
                default: throw new IllegalArgumentException("unknown piece type: "+type);
            }
	}	
}
