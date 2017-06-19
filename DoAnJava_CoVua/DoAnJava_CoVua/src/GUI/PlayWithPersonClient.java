/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package GUI;

import static GUI.PlayWithComputer.convertColumnToX;
import static GUI.PlayWithComputer.convertRowToY;
import static GUI.PlayWithComputer.convertXToColumn;
import static GUI.PlayWithComputer.convertYToRow;
import static GUI.PlayWithPersonServer.ipAddressServer;
import static GUI.PlayWithPersonServer.portServer;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import logic.ChessGame;
import logic.IPlayerHandler;
import logic.Move;
import logic.MoveValidator;
import logic.Piece;

/**
 *
 * @author USER
 */
public class PlayWithPersonClient extends javax.swing.JPanel implements IPlayerHandler{
    // Chat
    private Socket clientsock;
    private DataInputStream is;
    private DataOutputStream os;
    public String HostName;
    private Boolean isconnected = false;
    Thread ReceiveMessage;
    ////////////////////////////
    public static String ipAddressClient;
    public static String portClient;
    public static String userClient;
    
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
    private boolean draggingGamePiecesEnabled = true;
    private JFrame f;
    /**
     * Creates new form PlayWithPersonClient
     */
    public PlayWithPersonClient(ChessGame chessGame) {
        initComponents();
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
        f.setTitle("Client Chess");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(1000, 720);
       // f.setResizable(false);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
        jUser.setText(userClient);
        
    }
     private void createAndAddGuiPiece(Piece piece) {
        Image img = this.getImageForPiece(piece.getColor(), piece.getType());
        GuiPiece guiPiece = new GuiPiece(img, piece);
        this.guiPieces.add(guiPiece);
    }
    //load hình cờ dựa vào màu và loại cờ
    private Image getImageForPiece(int color, int type) {

        String filename = "";

        filename += (color == Piece.COLOR_WHITE ? "b" : "w");
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
        
//        String str= this.getGameStateAsText();
//        if(str=="black won"){
//           
//        }
    }
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
                guiNuocDi("nước đi của client");
                
        }else{// ngược lại đưa trở về vị trí cũ
                dragPiece.resetToUnderlyingPiecePosition();
        }
   }
    public void setDragPiece(GuiPiece guiPiece) {
           this.dragPiece = guiPiece;
   }

   public GuiPiece getDragPiece(){
           return this.dragPiece;
   }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jUser = new javax.swing.JLabel();
        btnLose = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        taChat = new javax.swing.JTextArea();
        txtChat = new javax.swing.JTextField();
        btnSend = new javax.swing.JButton();
        btnConnect = new javax.swing.JButton();

        jPanel2.setBackground(new java.awt.Color(253, 253, 253));
        jPanel2.setPreferredSize(new java.awt.Dimension(321, 410));

        jLabel1.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        jLabel1.setText("Các quân đã ăn");

        jLabel2.setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 0, 0));
        jLabel2.setText("Thời gian:");
        jLabel2.setToolTipText("");

        jLabel3.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 0, 51));
        jLabel3.setText("200");

        jLabel4.setFont(new java.awt.Font("Arial", 2, 18)); // NOI18N
        jLabel4.setText("Người chơi:");

        jUser.setFont(new java.awt.Font("Arial", 2, 18)); // NOI18N
        jUser.setText("Phong");

        btnLose.setBackground(new java.awt.Color(0, 255, 51));
        btnLose.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        btnLose.setText("Nhận Thua");
        btnLose.setEnabled(false);

        taChat.setColumns(20);
        taChat.setRows(5);
        taChat.setDisabledTextColor(new java.awt.Color(255, 255, 255));
        jScrollPane2.setViewportView(taChat);

        txtChat.setText("Nhập chat");
        txtChat.setEnabled(false);
        txtChat.setPreferredSize(new java.awt.Dimension(89, 21));
        txtChat.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtChatFocusGained(evt);
            }
        });

        btnSend.setBackground(new java.awt.Color(0, 255, 255));
        btnSend.setFont(new java.awt.Font("Arial", 1, 14)); // NOI18N
        btnSend.setForeground(new java.awt.Color(0, 102, 102));
        btnSend.setText("Gửi");
        btnSend.setEnabled(false);
        btnSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendActionPerformed(evt);
            }
        });

        btnConnect.setBackground(new java.awt.Color(0, 255, 255));
        btnConnect.setFont(new java.awt.Font("Arial", 1, 18)); // NOI18N
        btnConnect.setForeground(new java.awt.Color(0, 51, 255));
        btnConnect.setText("Connect Server");
        btnConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addGap(10, 10, 10)
                                        .addComponent(txtChat, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(btnSend, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 18, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jUser))
                            .addComponent(btnLose, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnConnect)
                                    .addComponent(jLabel3))))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnLose, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addGap(68, 68, 68)
                .addComponent(btnConnect)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 76, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jUser))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 243, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtChat, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSend, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 655, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 343, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 624, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void txtChatFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtChatFocusGained
        txtChat.setText("");
    }//GEN-LAST:event_txtChatFocusGained

    private void btnConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectActionPerformed
        Thread conn = new Thread(new ConnectToServer());
        if (isconnected == false) {
            isconnected = true;
            conn.start();
            btnSend.setEnabled(true);
            btnConnect.setVisible(false);
            btnConnect.setText("");
            taChat.append("IP Address: " + ipAddressClient);
            taChat.append("\nPort: " + portClient + "\n");
        } else {
            isconnected = false;
            btnSend.setEnabled(false);
            if (clientsock != null) {

                taChat.append("Disconnected!\n");
            }
            CloseConnect();
//        }
        }
    }//GEN-LAST:event_btnConnectActionPerformed

    private void btnSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendActionPerformed
        SendMessage(txtChat.getText());
    }//GEN-LAST:event_btnSendActionPerformed
    
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
    private javax.swing.JButton btnConnect;
    private javax.swing.JButton btnLose;
    private javax.swing.JButton btnSend;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel jUser;
    private javax.swing.JTextArea taChat;
    private javax.swing.JTextField txtChat;
    // End of variables declaration//GEN-END:variables
    
    private class ConnectToServer implements Runnable {

        @Override
        public void run() {
            try {
                clientsock = new Socket(ipAddressClient, Integer.parseInt(portClient));
                is = new DataInputStream(clientsock.getInputStream());
                os = new DataOutputStream(clientsock.getOutputStream());
                os.writeUTF(jUser.getText());
                os.flush();
                String t = is.readUTF();
                taChat.append(t + "\n");
                taChat.append("<=================****===================>\n");
                btnLose.setEnabled(true);
                btnSend.setEnabled(true);
                txtChat.setEnabled(true);
                HostName = is.readUTF();
            } catch (NumberFormatException | IOException e) {
            }
            ReceiveMessage = new Thread() {
                @Override
                public void run() {
                    String line;
                    String ReceiveChess;
                    try {
                        while (true) {
                            line = is.readUTF();
                            taChat.append("[" + HostName +"] : " + line + "\n");
                           
                            
                            
                        }
                    } catch (IOException ex) {
                    }
                }
            };
            ReceiveMessage.start();
        }
    }
    private void SendMessage(String mess) {
        try {
            os.writeUTF(mess);
            os.flush();
            taChat.append("[Sended] : " + mess + "\n");
        } catch (IOException ex) {
        }
    }
    private void CloseConnect() {
        if (clientsock != null) {
            try {
                clientsock.close();
            } catch (IOException ex) {
            }
        }
    }
    private void guiNuocDi(String mess)
    {
        try {
            os.writeUTF(mess);
            os.flush();
            taChat.append("[Sended] : " + mess + "\n");
        } catch (IOException ex) {
        }
    }
}
