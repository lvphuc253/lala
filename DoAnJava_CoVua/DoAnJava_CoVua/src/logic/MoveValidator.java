package logic;

/**
 * reference
 *   a  b  c  d  e  f  g  h  
 *  +--+--+--+--+--+--+--+--+
 * 8|BR|BN|BB|BQ|BK|BB|BN|BR|8
 * +--+--+--+--+--+--+--+--+
 * 7|BP|BP|BP|BP|BP|BP|BP|BP|7
 *  +--+--+--+--+--+--+--+--+
 * ..
 * 2|WP|WP|WP|WP|WP|WP|WP|WP|2
 *  +--+--+--+--+--+--+--+--+
 * 1|WR|WN|WB|WQ|WK|WB|WN|WR|1
 *  +--+--+--+--+--+--+--+--+
 *   a  b  c  d  e  f  g  h  
 *
 */
public class MoveValidator {

    private ChessGame chessGame;
    private Piece sourcePiece;
    private Piece targetPiece;
    //private boolean debug;

    public MoveValidator(ChessGame chessGame){
            this.chessGame = chessGame;
    }
    /**
    * Kiểm tra nếu di chuyển được chỉ định hợp lệ
    * @param di chuyển để xác nhận
    * Gỡ lỗi @param
    * @return true nếu di chuyển là hợp lệ, false nếu di chuyển không hợp lệ
    */
    public boolean isMoveValid(Move move, boolean debug) {
        //this.debug = debug;
        int sourceRow = move.sourceRow;
        int sourceColumn = move.sourceColumn;
        int targetRow = move.targetRow;
        int targetColumn = move.targetColumn;

        sourcePiece = this.chessGame.getNonCapturedPieceAtLocation(sourceRow, sourceColumn);
        targetPiece = this.chessGame.getNonCapturedPieceAtLocation(targetRow, targetColumn);

        if( sourcePiece == null ){
            return false;
        }

        // Mảnh nguồn có đúng màu?
        if( sourcePiece.getColor() == Piece.COLOR_WHITE
                        && this.chessGame.getGameState() == ChessGame.GAME_STATE_WHITE){
                // ok
        }else if( sourcePiece.getColor() == Piece.COLOR_BLACK
                        && this.chessGame.getGameState() == ChessGame.GAME_STATE_BLACK){
                // ok
        }else{
            // it's not your turn
            return false;
        }

        // Kiểm tra xem vị trí mục tiêu trong phạm vi ranh giới
        if( targetRow < Piece.ROW_1 || targetRow > Piece.ROW_8 || targetColumn < Piece.COLUMN_A || targetColumn > Piece.COLUMN_H){
            return false;
        }

        // Xác nhận các nguyên tắc chuyển động mảnh
        boolean validPieceMove = false;
        switch (sourcePiece.getType()) {
            case Piece.TYPE_BISHOP:
                    validPieceMove = isValidBishopMove(sourceRow,sourceColumn,targetRow,targetColumn);break;
            case Piece.TYPE_KING:
                    validPieceMove = isValidKingMove(sourceRow,sourceColumn,targetRow,targetColumn);break;
            case Piece.TYPE_KNIGHT:
                    validPieceMove = isValidKnightMove(sourceRow,sourceColumn,targetRow,targetColumn);break;
            case Piece.TYPE_PAWN:
                    validPieceMove = isValidPawnMove(sourceRow,sourceColumn,targetRow,targetColumn);break;
            case Piece.TYPE_QUEEN:
                    validPieceMove = isValidQueenMove(sourceRow,sourceColumn,targetRow,targetColumn);break;
            case Piece.TYPE_ROOK:
                    validPieceMove = isValidRookMove(sourceRow,sourceColumn,targetRow,targetColumn);break;
            default: break;
        }
        if( !validPieceMove){
            return false;
        }
        return true;
    }
    
    //nước đi hợp lệ của quân Tượng
    private boolean isValidBishopMove(int sourceRow, int sourceColumn, int targetRow, int targetColumn) {

        boolean isValid = false;
        // Kiểm tra vị trí đích có trống hay không, và có bắt được Piece hay không
        if( isTargetLocationFree() || isTargetLocationCaptureable()){
            // first lets check if the path to the target is diagonally at all
            int diffRow = targetRow - sourceRow;
            int diffColumn = targetColumn - sourceColumn;

            if( diffRow==diffColumn && diffColumn > 0){
                    // di chuyển lên phải
                    isValid = !arePiecesBetweenSourceAndTarget(sourceRow,sourceColumn,targetRow,targetColumn,+1,+1);

            }else if( diffRow==-diffColumn && diffColumn > 0){
                    // di chuyển xuống phải
                    isValid = !arePiecesBetweenSourceAndTarget(sourceRow,sourceColumn,targetRow,targetColumn,-1,+1);

            }else if( diffRow==diffColumn && diffColumn < 0){
                    // di chuyển lên xuống trái
                    isValid = !arePiecesBetweenSourceAndTarget(sourceRow,sourceColumn,targetRow,targetColumn,-1,-1);

            }else if( diffRow==-diffColumn && diffColumn < 0){
                    // di chuyển lên trái
                    isValid = !arePiecesBetweenSourceAndTarget(sourceRow,sourceColumn,targetRow,targetColumn,+1,-1);

            }else{
                    isValid = false;
            }
        }else{
            isValid = false;
        }      
        return isValid;
    }

    //nước đi hợp lệ của quân Hậu = Xe + Tượng
    private boolean isValidQueenMove(int sourceRow, int sourceColumn, int targetRow, int targetColumn) {

        return isValidBishopMove(sourceRow, sourceColumn, targetRow, targetColumn) || 
                isValidRookMove(sourceRow, sourceColumn, targetRow, targetColumn);
    }

    //xem lai
    private boolean isValidPawnMove(int sourceRow, int sourceColumn, int targetRow, int targetColumn) {

        boolean isValid = false;
        // The pawn may move forward to the unoccupied square immediately in front
        // of it on the same file, or on its first move it may advance two squares
        // along the same file provided both squares are unoccupied
        if( isTargetLocationFree() ){
            if( sourceColumn == targetColumn){
                // same column
                if(  sourcePiece.getColor() == Piece.COLOR_WHITE ){
                    if(sourceRow==1){
                        if( sourceRow+1 == targetRow ){
                            // move one up
                            isValid = true;
                        }
                        if (sourceRow+2 == targetRow){
                            //moving one up
                            isValid = true;
                        }
                    }else{
                            // white
                        if( sourceRow+1 == targetRow ){
                            // move one up
                            isValid = true;
                        }else{
                            //not moving one up
                            isValid = false;
                        }
                    }

                }
                else
                {
                    if(sourceRow==6){
                        if( sourceRow-1 == targetRow ){
                            // move one up
                            isValid = true;
                        }
                        if (sourceRow-2 == targetRow){
                            //moving one up
                            isValid = true;
                        }
                    }else{
                        // black
                        if( sourceRow-1 == targetRow ){
                            // move one down
                            isValid = true;
                        }else{
                            //not moving one down
                            isValid =  false;
                        }
                    }
                    
                }
            }else{
                // not staying in the same column
                isValid = false;
            }

        // or it may move
        // to a square occupied by an opponent�s piece, which is diagonally in front
        // of it on an adjacent file, capturing that piece. 
        }else if( isTargetLocationCaptureable() ){

            if( sourceColumn+1 == targetColumn || sourceColumn-1 == targetColumn){
                // one column to the right or left
                if(  sourcePiece.getColor() == Piece.COLOR_WHITE ){
                    // white
                    if( sourceRow+1 == targetRow ){
                            // move one up
                            isValid = true;
                    }else{
                            //not moving one up
                            isValid = false;
                    }
                }else{
                    // black
                    if( sourceRow-1 == targetRow ){
                            // move one down
                            isValid = true;
                    }else{
                            //not moving one down
                            isValid = false;
                    }
                }
            }else{
                // note one column to the left or right
                isValid = false;
            }
        }
        return isValid;
    }

    private boolean isValidKnightMove(int sourceRow, int sourceColumn, int targetRow, int targetColumn) {
        // The knight moves to any of the closest squares which are not on the same rank,
        // file or diagonal, thus the move forms an "L"-shape two squares long and one
        // square wide. The knight is the only piece which can leap over other pieces.

        // target location possible?
        if( isTargetLocationFree() || isTargetLocationCaptureable()){
                //ok
        }else{
                //target location not free and not captureable
                return false;
        }

        if( sourceRow+2 == targetRow && sourceColumn+1 == targetColumn){
                // move up up right
                return true;
        }else if( sourceRow+1 == targetRow && sourceColumn+2 == targetColumn){
                // move up right right
                return true;
        }else if( sourceRow-1 == targetRow && sourceColumn+2 == targetColumn){
                // move down right right
                return true;
        }else if( sourceRow-2 == targetRow && sourceColumn+1 == targetColumn){
                // move down down right
                return true;
        }else if( sourceRow-2 == targetRow && sourceColumn-1 == targetColumn){
                // move down down left
                return true;
        }else if( sourceRow-1 == targetRow && sourceColumn-2 == targetColumn){
                // move down left left
                return true;
        }else if( sourceRow+1 == targetRow && sourceColumn-2 == targetColumn){
                // move up left left
                return true;
        }else if( sourceRow+2 == targetRow && sourceColumn-1 == targetColumn){
                // move up up left
                return true;
        }else{
                return false;
        }
    }

    private boolean isValidKingMove(int sourceRow, int sourceColumn, int targetRow, int targetColumn) {

            // target location possible?
            if( isTargetLocationFree() || isTargetLocationCaptureable()){
                    //ok
            }else{
                    //target location not free and not captureable
                    return false;
            }

            // The king moves one square in any direction, the king has also a special move which is
            // called castling and also involves a rook.
            boolean isValid = true;
            if( sourceRow+1 == targetRow && sourceColumn == targetColumn){
                    //up
                    isValid = true;
            }else if( sourceRow+1 == targetRow && sourceColumn+1 == targetColumn){
                    //up right
                    isValid = true;
            }else if( sourceRow == targetRow && sourceColumn+1 == targetColumn){
                    //right
                    isValid = true;
            }else if( sourceRow-1 == targetRow && sourceColumn+1 == targetColumn){
                    //down right
                    isValid = true;
            }else if( sourceRow-1 == targetRow && sourceColumn == targetColumn){
                    //down
                    isValid = true;
            }else if( sourceRow-1 == targetRow && sourceColumn-1 == targetColumn){
                    //down left
                    isValid = true;
            }else if( sourceRow == targetRow && sourceColumn-1 == targetColumn){
                    //left
                    isValid = true;
            }else if( sourceRow+1 == targetRow && sourceColumn-1 == targetColumn){
                    //up left
                    isValid = true;
            }else{
                    //moving too far
                    isValid = false;
            }

            // castling
            // ..

            return isValid;
    }

    //nước đi hợp lệ của xe
    private boolean isValidRookMove(int sourceRow, int sourceColumn, int targetRow, int targetColumn) {
        boolean isValid = false;
        if( isTargetLocationFree() || isTargetLocationCaptureable()){
            
            int diffRow = targetRow - sourceRow;
            int diffColumn = targetColumn - sourceColumn;

            if( diffRow == 0 && diffColumn > 0){//đang kéo sang phải
                    // kiểm tra phía trước có bị chặn hay hợp lệ hay không
                    isValid = !arePiecesBetweenSourceAndTarget(sourceRow,sourceColumn,targetRow,targetColumn,0,+1);

            }else if( diffRow == 0 && diffColumn < 0){ // đang kéo sang trái
                    isValid = !arePiecesBetweenSourceAndTarget(sourceRow,sourceColumn,targetRow,targetColumn,0,-1);

            }else if( diffRow > 0 && diffColumn == 0){//Kéo lên trên
                    isValid = !arePiecesBetweenSourceAndTarget(sourceRow,sourceColumn,targetRow,targetColumn,+1,0);

            }else if( diffRow < 0 && diffColumn == 0){//kéo xuống dưới
                    isValid = !arePiecesBetweenSourceAndTarget(sourceRow,sourceColumn,targetRow,targetColumn,-1,0);
            }else{
                    isValid = false;
            }
        }else{
                isValid = false;
        }
        return isValid;
    }


    private boolean arePiecesBetweenSourceAndTarget(int sourceRow, int sourceColumn,
                    int targetRow, int targetColumn, int rowIncrementPerStep, int columnIncrementPerStep) {

            int currentRow = sourceRow + rowIncrementPerStep;
            int currentColumn = sourceColumn + columnIncrementPerStep;
            while(true){
                //vị trí đích có tồn tại Piece
                if(currentRow == targetRow && currentColumn == targetColumn){
                        break;
                }
                //nếu nằm ngoài phạm vi bàn cờ
                if( currentRow < Piece.ROW_1 || currentRow > Piece.ROW_8 || currentColumn < Piece.COLUMN_A || currentColumn > Piece.COLUMN_H){
                        break;
                }
                //vị trí row, column hiện tại rỗng thì
                if( this.chessGame.isNonCapturedPieceAtLocation(currentRow, currentColumn)){
                        // pieces in between source and target
                        return true;
                }

                currentRow += rowIncrementPerStep;
                currentColumn += columnIncrementPerStep;
            }
            return false;
    }
        //Là vị trí mục tiêu có thể thu được
    private boolean isTargetLocationCaptureable() {
        if( targetPiece == null ){
            return false;
        }else if( targetPiece.getColor() != sourcePiece.getColor()){
            return true;
        }else{
            return false;
        }
    }

    private boolean isTargetLocationFree() {
        return targetPiece == null;
    }

}
