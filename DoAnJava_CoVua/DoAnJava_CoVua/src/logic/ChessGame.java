package logic;

import GUI.PlayWithComputer;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class ChessGame implements Runnable{

    public int gameState = GAME_STATE_WHITE;
    public static final int GAME_STATE_WHITE = 0;
    public static final int GAME_STATE_BLACK = 1;
    public static final int GAME_STATE_END_BLACK_WON = 2;
    public static final int GAME_STATE_END_WHITE_WON = 3;

    // 0 = bottom, size = top
    public List<Piece> pieces = new ArrayList<Piece>();
    public List<Piece> capturedPieces = new ArrayList<Piece>();
    public List<Piece> piece_oldMove = new ArrayList<Piece>();
    private MoveValidator moveValidator;
    private IPlayerHandler blackPlayerHandler;
    private IPlayerHandler whitePlayerHandler;
    private IPlayerHandler activePlayerHandler;

    /**
     * initialize game
     */
    public ChessGame() {

        // rook, knight, bishop, queen, king, bishop, knight, and rook
        createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_ROOK, Piece.ROW_1, Piece.COLUMN_A,0,1);
        createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_KNIGHT, Piece.ROW_1,
                        Piece.COLUMN_B,1,1);
        createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_BISHOP, Piece.ROW_1,
                        Piece.COLUMN_C,2,1);
        createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_QUEEN, Piece.ROW_1,
                        Piece.COLUMN_D,3,1);
        createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_KING, Piece.ROW_1, Piece.COLUMN_E,4,1);
        createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_BISHOP, Piece.ROW_1,
                        Piece.COLUMN_F,5,1);
        createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_KNIGHT, Piece.ROW_1,
                        Piece.COLUMN_G,6,1);
        createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_ROOK, Piece.ROW_1, Piece.COLUMN_H,7,1);

        // pawns
        int currentColumn = Piece.COLUMN_A;
        for (int i = 0; i < 8; i++) {
            createAndAddPiece(Piece.COLOR_WHITE, Piece.TYPE_PAWN, Piece.ROW_2,currentColumn,i+8,1);
            currentColumn++;
        }

        createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_ROOK, Piece.ROW_8, Piece.COLUMN_A,16,1);
        createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_KNIGHT, Piece.ROW_8,
                        Piece.COLUMN_B,17,1);
        createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_BISHOP, Piece.ROW_8,
                        Piece.COLUMN_C,18,1);
        createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_QUEEN, Piece.ROW_8,
                        Piece.COLUMN_D,19,1);
        createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_KING, Piece.ROW_8, Piece.COLUMN_E,20,1);
        createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_BISHOP, Piece.ROW_8,
                        Piece.COLUMN_F,21,1);
        createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_KNIGHT, Piece.ROW_8,
                        Piece.COLUMN_G,22,1);
        createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_ROOK, Piece.ROW_8, Piece.COLUMN_H,23,1);

        // pawns
        currentColumn = Piece.COLUMN_A;
        for (int i = 0; i < 8; i++) {
            createAndAddPiece(Piece.COLOR_BLACK, Piece.TYPE_PAWN, Piece.ROW_7,currentColumn,i+24,1);
            currentColumn++;
        }
        
        this.moveValidator = new MoveValidator(this);
    }

    public void startGame(){
       if(this.blackPlayerHandler != null && this.whitePlayerHandler != null){
           // bên trắng đi trước
           this.activePlayerHandler = this.whitePlayerHandler;
           // start game flow
           System.out.println("ChessGame: starting game flow");
           while(!isGameEndConditionReached()){
               waitForMove();
               swapActivePlayer();
           }
           
       }    
    }

    public void setPlayer(int pieceColor, IPlayerHandler playerHandler){
        if(pieceColor==Piece.COLOR_BLACK){
            this.blackPlayerHandler = playerHandler;
        }else{
            this.whitePlayerHandler = playerHandler;
        }
    }

    private void swapActivePlayer() {
            if( this.activePlayerHandler == this.whitePlayerHandler ){
                    this.activePlayerHandler = this.blackPlayerHandler;
            }else{
                    this.activePlayerHandler = this.whitePlayerHandler;
            }
            this.changeGameState();
    }

    /**
     * Wait for client/player move and execute it.
     * Notify all clients/players about successful execution of move.
     */
    public int capSize=0;
    private String str1,str2;
    public int num=1;
    private void waitForMove() {
        Move move = null;
        // wait for a valid move
        do{
            move = this.activePlayerHandler.getMove();
            try {Thread.sleep(100);} catch (InterruptedException e) {e.printStackTrace();}
            if( move != null && this.moveValidator.isMoveValid(move, true) ){
                    break;
            }else if( move != null && !this.moveValidator.isMoveValid(move,true)){
                System.out.println("provided move was invalid: "+move);
                move=null;
                System.exit(0);
            }
        }while(move == null);
        Piece temp1=this.getNonCapturedPieceAtLocation(move.sourceRow, move.sourceColumn);   
        Piece temp2 = new Piece(temp1.getColor(), temp1.getType(), temp1.getRow(), temp1.getColumn(), temp1.getLocation(), 1);
        this.piece_oldMove.add(temp2);
        if(num==2){
            //định dạng Type:Row-Column
            str2 = Piece.getTypeString(temp2.getType())+":"+
                Piece.getRowString(move.sourceRow)+Piece.getColumnString(move.sourceColumn)+"-"+
                Piece.getRowString(move.targetRow)+Piece.getColumnString(move.targetColumn);
        }
        if(num==1){
            str1 = Piece.getTypeString(temp2.getType())+":"+ 
                Piece.getRowString(move.sourceRow)+Piece.getColumnString(move.sourceColumn)+"-"+
                Piece.getRowString(move.targetRow)+Piece.getColumnString(move.targetColumn);
        }
        if(num==2){
           DefaultTableModel model = (DefaultTableModel) PlayWithComputer.jTableTemp.getModel();
            model.addRow(new Object[]{str1,str2}); 
            str1 = ""; str2 = ""; num = 0;
        }
        num++;
        //execute move
        boolean success = this.movePiece(move);
            
        if(success){
            this.blackPlayerHandler.moveSuccessfullyExecuted(move);
            this.whitePlayerHandler.moveSuccessfullyExecuted(move);
        }
        if(capSize<capturedPieces.size()){
            capSize=capturedPieces.size();
            Piece temp3= capturedPieces.get(capSize-1);
            Piece temp4 = new Piece(temp3.getColor(), temp3.getType(), temp3.getRow(), temp3.getColumn(), temp3.getLocation(), 0);
            piece_oldMove.add(temp4);
        }
//        System.out.println("*************************");
//        for(int i=0;i<piece_oldMove.size();i++){
//            System.out.println(piece_oldMove.get(i));
//        }
//        System.out.println("*************************");
    }

    public void createAndAddPiece(int color, int type, int row, int column, int location, int exists) {
        Piece piece = new Piece(color, type, row, column,location,exists);
        this.pieces.add(piece);
    }
    public boolean movePiece(Move move) {
        //set captured piece in move
        // this information is needed in the undoMove() method.
        move.capturedPiece = this.getNonCapturedPieceAtLocation(move.targetRow, move.targetColumn);

        Piece piece = getNonCapturedPieceAtLocation(move.sourceRow, move.sourceColumn);

        // check if the move is capturing an opponent piece
        int opponentColor = (piece.getColor() == Piece.COLOR_BLACK ? Piece.COLOR_WHITE : Piece.COLOR_BLACK);
        if (isNonCapturedPieceAtLocation(opponentColor, move.targetRow, move.targetColumn)) {
                // handle captured piece
                Piece opponentPiece = getNonCapturedPieceAtLocation(move.targetRow, move.targetColumn);
                this.pieces.remove(opponentPiece);
                this.capturedPieces.add(opponentPiece);
                opponentPiece.isCaptured(true);
        }

        // move piece to new position
        piece.setRow(move.targetRow);
        piece.setColumn(move.targetColumn);
        return true;
    }

    //Hàm thay đổi trạng thái game
    public void changeGameState() {
        //nếu game kết thúc rồi thì
        if (this.isGameEndConditionReached()) {
            //nếu trạng thái game cuối cùng là Black thì black win
            if (this.gameState == ChessGame.GAME_STATE_BLACK) {
                this.gameState = ChessGame.GAME_STATE_END_BLACK_WON;
            } else if(this.gameState == ChessGame.GAME_STATE_WHITE){
                this.gameState = ChessGame.GAME_STATE_END_WHITE_WON;
            }
        }
        else{
            this.gameState = (this.gameState == GAME_STATE_BLACK?GAME_STATE_WHITE:GAME_STATE_BLACK);
        }
    }
    
    public void undoMove(Move move){
        Piece piece = getNonCapturedPieceAtLocation(move.targetRow, move.targetColumn);

        piece.setRow(move.sourceRow);
        piece.setColumn(move.sourceColumn);

        if(move.capturedPiece != null){
                move.capturedPiece.setRow(move.targetRow);
                move.capturedPiece.setColumn(move.targetColumn);
                move.capturedPiece.isCaptured(false);
                this.capturedPieces.remove(move.capturedPiece);
                this.pieces.add(move.capturedPiece);
        }

        if(piece.getColor() == Piece.COLOR_BLACK){
                this.gameState = ChessGame.GAME_STATE_BLACK;
        }else{
                this.gameState = ChessGame.GAME_STATE_WHITE;
        }
    }
    //Hàm kiểm tra Piece có bị bắt tại vị trí row, column không
    boolean isNonCapturedPieceAtLocation(int row, int column) {
        for (Piece piece : this.pieces) {
                if (piece.getRow() == row && piece.getColumn() == column) {
                        return true;
                }
        }
        return false;
    }
    
    //Kiểm tra có Piece chưa đc đánh dấu với color? row? column tại vị trí này không
    boolean isNonCapturedPieceAtLocation(int color, int row, int column) {
            for (Piece piece : this.pieces) {
                    if (piece.getRow() == row && piece.getColumn() == column && piece.getColor() == color) {
                            return true;
                    }
            }
            return false;
    }
    //hàm trả về Piece không bị đánh dấu
    public Piece getNonCapturedPieceAtLocation(int row, int column) {
        for (Piece piece : this.pieces) {
            if (piece.getRow() == row && piece.getColumn() == column) {
                    return piece;
            }
        }
        return null;
    }
    
    private boolean isGameEndConditionReached() {
        for (Piece piece : this.capturedPieces) {
            if (piece.getType() == Piece.TYPE_KING ) {// nếu là King thì true=kết thúc
                    return true;
            } 
        }
        return false;
    }
    public int getGameState() {
            return this.gameState;
    }
    public MoveValidator getMoveValidator(){
        return this.moveValidator;
    }
    public List<Piece> getPieces() {
        return this.pieces;
    }
    @Override
    public void run() {
        this.startGame();
    }
}
