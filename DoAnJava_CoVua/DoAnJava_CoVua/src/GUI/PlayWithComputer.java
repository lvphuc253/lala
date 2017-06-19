/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import aiplayer.SimpleAiPlayerHandler;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import logic.ChessGame;
import logic.IPlayerHandler;
import logic.Move;
import logic.MoveValidator;
import logic.Piece;

/**
 *
 * @author PhucRed
 */
public class PlayWithComputer extends javax.swing.JPanel implements IPlayerHandler{

    private static final int BOARD_START_X = 60; //X bắt đầu bàn cờ
    private static final int BOARD_START_Y = 45; //Y bắt đầu bàn cờ

    private static final int SQUARE_WIDTH = 69; // độ rộng mỗi ô cờ
    private static final int SQUARE_HEIGHT = 69;// độ cao mỗi ô cờ

    private static final int PIECE_WIDTH = 69; //độ rộng mỗi quân cờ
    private static final int PIECE_HEIGHT = 69; // độ cao mỗi quân cờ

    //X bắt đầu để quân cờ vào ô cờ
    private static final int PIECES_START_X = BOARD_START_X + (int)(SQUARE_WIDTH - PIECE_WIDTH);
    //Y bắt đầu để quân cờ vào ô cờ
    private static final int PIECES_START_Y = BOARD_START_Y + (int)(SQUARE_HEIGHT - PIECE_HEIGHT);

    private static final int DRAG_TARGET_SQUARE_START_X = BOARD_START_X - (int)(PIECE_WIDTH/2.0);
    private static final int DRAG_TARGET_SQUARE_START_Y = BOARD_START_Y - (int)(PIECE_HEIGHT/2.0);

    private Image imgBackground;

    private ChessGame chessGame;
    private List<GuiPiece> guiPieces = new ArrayList<GuiPiece>();
   
    private GuiPiece dragPiece;

    private Move currentMove;

    private boolean draggingGamePiecesEnabled;
    private JFrame f;
    public static JTable jTableTemp;
    public PlayWithComputer(ChessGame chessGame) {
        initComponents();

        jTableTemp = jTable1;
        ///jLabel1.setVisible(false);
        URL urlBackgroundImg = getClass().getResource("/imgs/ChessBoard02.png");

  
        this.imgBackground = new ImageIcon(urlBackgroundImg).getImage();

        this.chessGame = chessGame;

        //Gắn hình ảnh vào mỗi đối tượng Piece
        for (Piece piece : this.chessGame.getPieces()) {
                createAndAddGuiPiece(piece);
        }

        PiecesDragAndDropListener listener = new PiecesDragAndDropListener(this.guiPieces,
                        this);
        this.addMouseListener(listener);
        this.addMouseMotionListener(listener);

        f = new JFrame();
        f.add(this);
        f.setTitle("With Computer");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(1050, 700);
        f.setResizable(false);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }


//    private String getGameStateAsText() {
//        String state = "unknown";
//        switch (this.chessGame.getGameState()) {
//            case ChessGame.GAME_STATE_BLACK: state = "black";break;
//            case ChessGame.GAME_STATE_END_WHITE_WON: state = "white won";break;
//            case ChessGame.GAME_STATE_END_BLACK_WON: state = "black won";break;
//            case ChessGame.GAME_STATE_WHITE: state = "white";break;
//        }
//        return state;
//    }


    private void createAndAddGuiPiece(Piece piece) {
        Image img = this.getImageForPiece(piece.getColor(), piece.getType());
        GuiPiece guiPiece = new GuiPiece(img, piece);
        this.guiPieces.add(guiPiece);
    }

    //load hình cờ dựa vào màu và loại cờ
    private Image getImageForPiece(int color, int type) {

        String filename = "";

        filename += (color == Piece.COLOR_WHITE ? "w" : "b");
        switch (type) {
                case Piece.TYPE_BISHOP:
                        filename += "b";
                        break;
                case Piece.TYPE_KING:
                        filename += "k";
                        break;
                case Piece.TYPE_KNIGHT:
                        filename += "n";
                        break;
                case Piece.TYPE_PAWN:
                        filename += "p";
                        break;
                case Piece.TYPE_QUEEN:
                        filename += "q";
                        break;
                case Piece.TYPE_ROOK:
                        filename += "r";
                        break;
        }
        filename += ".gif";

        URL urlPieceImg = getClass().getResource("/imgs/" + filename);
        return new ImageIcon(urlPieceImg).getImage();
    }
    
    
    @Override
    protected void paintComponent(Graphics g) {
         // draw background
        g.drawImage(this.imgBackground, 0, 0, null);

        // draw pieces
        for (GuiPiece guiPiece : this.guiPieces) {
            if( !guiPiece.isCaptured()){
                    g.drawImage(guiPiece.getImage(), guiPiece.getX(), guiPiece.getY(), null);
            }
        }

        // Vẽ vị trí mục tiêu hợp lệ, nếu người dùng đang kéo một đoạn trò chơi
        if( isUserDraggingPiece() ){

            MoveValidator moveValidator = this.chessGame.getMoveValidator();

            // Lặp lại toàn bộ bảng để kiểm tra xem vị trí đích có hợp lệ không
            for (int column = Piece.COLUMN_A; column <= Piece.COLUMN_H; column++) {
                for (int row = Piece.ROW_1; row <= Piece.ROW_8; row++) {
                    int sourceRow = this.dragPiece.getPiece().getRow();
                    int sourceColumn = this.dragPiece.getPiece().getColumn();

                    // Kiểm tra xem vị trí đích có hợp lệ không
                    if( moveValidator.isMoveValid( new Move(sourceRow, sourceColumn, row, column), false) ){
                        int highlightX = convertColumnToX(column);
                        int highlightY = convertRowToY(row);

                        // draw the highlight
                        g.setColor(new Color(102, 255, 51, 60));
                       // g.setColor(Color.GREEN);
                        g.drawRoundRect( highlightX-14, highlightY+4, SQUARE_WIDTH, SQUARE_HEIGHT,10,10); 
                        g.fillRect(highlightX-14, highlightY+4, SQUARE_WIDTH - 1, SQUARE_HEIGHT - 1);
                        
                    }
                }
            }
        }
        // draw game state label
        

        if(this.chessGame.getGameState()==ChessGame.GAME_STATE_END_BLACK_WON){
            this.jLabel1.setText("YOU LOSE");
            //this.jLabel1.setVisible(true);
        }
        if(this.chessGame.getGameState()==ChessGame.GAME_STATE_END_WHITE_WON){
            this.jLabel1.setText("YOU WIN");
            //this.jLabel1.setVisible(true);
        }
        
    }
    /**
    * check if the user is currently dragging a game piece
    * @return true - if the user is currently dragging a game piece
    */
   private boolean isUserDraggingPiece() {
        return this.dragPiece != null;
   }

   public int getGameState() {
        return this.chessGame.getGameState();
   }
   
   //X = vị trí X bắt đầu bàn cờ + độ rộng ô cờ * số cột
   public static int convertColumnToX(int column){
        return PIECES_START_X + SQUARE_WIDTH * column;
   }
   
   //Y = vị trí Y bắt đầu bàn cờ + độ rộng ô cờ * số dòng
   public static int convertRowToY(int row){
        return PIECES_START_Y + SQUARE_HEIGHT * (Piece.ROW_8 - row);
   }

   public static int convertXToColumn(int x){
        return (x - DRAG_TARGET_SQUARE_START_X)/SQUARE_WIDTH;
   }

   public static int convertYToRow(int y){
        return Piece.ROW_8 - (y - DRAG_TARGET_SQUARE_START_Y)/SQUARE_HEIGHT;
   }

   //Hàm set vị trí mới cho Piece
   public void setNewPieceLocation(GuiPiece dragPiece, int x, int y) {
        int targetRow = convertYToRow(y);
        int targetColumn = convertXToColumn(x);

        Move move = new Move(dragPiece.getPiece().getRow(), dragPiece.getPiece().getColumn()
                        , targetRow, targetColumn);
        //Nếu vị trí đích hợp lệ thì tiến hành di chuyển
        if( this.chessGame.getMoveValidator().isMoveValid(move, true) ){
                this.currentMove = move;
                
        }else{// ngược lại đưa trở về vị trí cũ
                dragPiece.resetToUnderlyingPiecePosition();
        }
   }

   /**
    * set the game piece that is currently dragged by the user
    * @param guiPiece
    */
   public void setDragPiece(GuiPiece guiPiece) {
           this.dragPiece = guiPiece;
   }

   public GuiPiece getDragPiece(){
           return this.dragPiece;
   }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btn_dilai = new javax.swing.JButton();
        btn_trove = new javax.swing.JButton();
        btn_vanmoi = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setMaximumSize(new java.awt.Dimension(1000, 700));
        setMinimumSize(new java.awt.Dimension(1000, 700));
        setPreferredSize(new java.awt.Dimension(1000, 700));

        btn_dilai.setText("Đi lại");
        btn_dilai.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_dilaiActionPerformed(evt);
            }
        });

        btn_trove.setText("Trở về");
        btn_trove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_troveActionPerformed(evt);
            }
        });

        btn_vanmoi.setText("Ván mới");
        btn_vanmoi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_vanmoiActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Yu Gothic", 0, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("YOU WON");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "White", "Black"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);
        if (jTable1.getColumnModel().getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(0).setResizable(false);
            jTable1.getColumnModel().getColumn(1).setResizable(false);
        }

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(851, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(btn_trove, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btn_dilai, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btn_vanmoi, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(36, 36, 36))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(23, 23, 23))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addComponent(jLabel1)
                .addGap(40, 40, 40)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 218, Short.MAX_VALUE)
                .addComponent(btn_vanmoi, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btn_dilai, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btn_trove, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btn_vanmoiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_vanmoiActionPerformed
        int dialogResult = JOptionPane.showConfirmDialog(this, "Bạn muốn tạo ván mới?", "Thông báo", JOptionPane.YES_NO_OPTION);
        if(dialogResult == 0) {
            f.dispose();
            ChessGame chessGame = new ChessGame();

            PlayWithComputer chessGui = new PlayWithComputer(chessGame);
            SimpleAiPlayerHandler ai1 = new SimpleAiPlayerHandler(chessGame);
            SimpleAiPlayerHandler ai2 = new SimpleAiPlayerHandler(chessGame);

            // set strength of AI
            ai1.maxDepth = 1;
            ai2.maxDepth = 2;

            chessGame.setPlayer(Piece.COLOR_BLACK, ai1);
            chessGame.setPlayer(Piece.COLOR_WHITE, chessGui);
            new Thread(chessGame).start();
        } else {
          System.out.println("No Option");
        } 
        
    }//GEN-LAST:event_btn_vanmoiActionPerformed

    private void btn_troveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_troveActionPerformed
        int dialogResult = JOptionPane.showConfirmDialog(this, "Bạn có muốn tiếp tục?", "Thông báo", JOptionPane.YES_NO_OPTION);
        if(dialogResult == 1) {
            f.dispose();
            new MainChess().setVisible(true);
        } else {
          System.out.println("No Option");
        }      
    }//GEN-LAST:event_btn_troveActionPerformed

    private void btn_dilaiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_dilaiActionPerformed
         if(this.chessGame.getGameState()==ChessGame.GAME_STATE_END_BLACK_WON ||
                 this.chessGame.getGameState()==ChessGame.GAME_STATE_END_WHITE_WON)
             return;
        int j=0;
        for(int i=this.chessGame.piece_oldMove.size()-1;i>=0;i--){
            
            int location = chessGame.piece_oldMove.get(i).getLocation();
            int column = chessGame.piece_oldMove.get(i).getColumn();
            int row = chessGame.piece_oldMove.get(i).getRow();
            
            GuiPiece guiPiece = guiPieces.get(location);
            
            if(guiPiece.getPiece().isCaptured()==true){
                guiPiece.getPiece().isCaptured(false);
                Piece capPiece = null;
                for (Piece piece : chessGame.capturedPieces){
                    if(piece.getLocation()==location){
                        capPiece=piece;
                        j--;
                        break;
                    }
                }
                chessGame.capturedPieces.remove(capPiece);
                chessGame.pieces.add(capPiece);
                chessGame.capSize--;
            }
            
            guiPiece.setX(convertColumnToX(column));
            guiPiece.setY(convertRowToY(row));
            guiPiece.getPiece().setColumn(column);
            guiPiece.getPiece().setRow(row);
            this.chessGame.piece_oldMove.remove(i);
            j++;
            if(j==2){
                j=0;
                break;
            }
            DefaultTableModel model = (DefaultTableModel) this.jTable1.getModel();
            model.removeRow(model.getRowCount()-1);
            repaint();
        }

    }//GEN-LAST:event_btn_dilaiActionPerformed

    @Override
    public Move getMove() {
        this.draggingGamePiecesEnabled = true; 
        Move moveForExecution = this.currentMove;
        this.currentMove = null;
        return moveForExecution;
    }

    @Override
    public void moveSuccessfullyExecuted(Move move) {
        // Điều chỉnh GUI mảnh
        GuiPiece guiPiece = this.getGuiPieceAt(move.targetRow, move.targetColumn);
        if( guiPiece == null){
                throw new IllegalStateException("no guiPiece at "+move.targetRow+"/"+move.targetColumn);
        }
        guiPiece.resetToUnderlyingPiecePosition();

        // Vô hiệu hóa kéo cho đến khi được hỏi bởi ChessGame cho bước tiếp theo
        this.draggingGamePiecesEnabled = false;
        
        // Sơn lại trạng thái mới
        this.repaint();
    }
    /**
	 * @return true - if the user is allowed to drag game pieces
	 */
    public boolean isDraggingGamePiecesEnabled(){
        return draggingGamePiecesEnabled;
    }

    // lấy Gui của Piece tại row?, column ?
    private GuiPiece getGuiPieceAt(int row, int column) {
        for (GuiPiece guiPiece : this.guiPieces) {
            if( guiPiece.getPiece().getRow() == row
                && guiPiece.getPiece().getColumn() == column
                && guiPiece.isCaptured() == false){
                return guiPiece;
            }
        }
        return null;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_dilai;
    private javax.swing.JButton btn_trove;
    private javax.swing.JButton btn_vanmoi;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
