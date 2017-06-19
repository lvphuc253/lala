package GUI;

import java.awt.event.*;
import java.util.List;

import logic.*;

public class PiecesDragAndDropListener implements MouseListener, MouseMotionListener {

    private List<GuiPiece> guiPieces;
    private PlayWithComputer playWithComputer;
    private PlayWithPersonServer playWithPersonServer;
    private PlayWithPersonClient playWithPersonClient;

    private int dragOffsetX;
    private int dragOffsetY;


    public PiecesDragAndDropListener(List<GuiPiece> guiPieces, PlayWithComputer chessGui) {
            this.guiPieces = guiPieces;
            this.playWithComputer = chessGui;
    }
    
    public PiecesDragAndDropListener(List<GuiPiece> guiPieces,  PlayWithPersonServer chessGui) {
            this.guiPieces = guiPieces;
            this.playWithPersonServer = chessGui;
    }
    public PiecesDragAndDropListener(List<GuiPiece> guiPieces, PlayWithPersonClient chessGui) {
            this.guiPieces = guiPieces;
            this.playWithPersonClient = chessGui;
    }

    @Override
    public void mousePressed(MouseEvent evt) {
        
        try
        { 
            int x = evt.getPoint().x;
            int y = evt.getPoint().y;
            if( !playWithComputer.isDraggingGamePiecesEnabled()){
                return;
            }
            for (int i = this.guiPieces.size()-1; i >= 0; i--) {
                GuiPiece guiPiece = this.guiPieces.get(i);
                if(mouseOverPiece(guiPiece,x,y)){
                    if( (this.playWithComputer.getGameState() == ChessGame.GAME_STATE_WHITE&& guiPiece.getColor() == Piece.COLOR_WHITE) ||
                        (this.playWithComputer.getGameState() == ChessGame.GAME_STATE_BLACK&& guiPiece.getColor() == Piece.COLOR_BLACK)){
                        this.dragOffsetX = x - guiPiece.getX();
                        this.dragOffsetY = y - guiPiece.getY();
                        this.playWithComputer.setDragPiece(guiPiece);
                        this.playWithComputer.repaint();
                        break;
                    }
                }
            }
           
        }
        catch(Exception e){ }
        try
        { 
            int x = evt.getPoint().x;
            int y = evt.getPoint().y;
            if( !this.playWithPersonClient.isDraggingGamePiecesEnabled()){
               return;
                }
             for (int i = this.guiPieces.size()-1; i >= 0; i--) {
                 GuiPiece guiPiece = this.guiPieces.get(i);
                 if(mouseOverPiece(guiPiece,x,y)){
                     if( (this.playWithPersonClient.getGameState() == ChessGame.GAME_STATE_WHITE&& guiPiece.getColor() == Piece.COLOR_WHITE) ||
                         (this.playWithPersonClient.getGameState() == ChessGame.GAME_STATE_BLACK&& guiPiece.getColor() == Piece.COLOR_BLACK)){
                         this.dragOffsetX = x - guiPiece.getX();
                         this.dragOffsetY = y - guiPiece.getY();
                         this.playWithPersonClient.setDragPiece(guiPiece);
                         this.playWithPersonClient.repaint();
                         break;
                     }
                 }
            }  
        }
        catch(Exception e){} 
         try
        { 
            int x = evt.getPoint().x;
            int y = evt.getPoint().y;
            if( !this.playWithPersonServer.isDraggingGamePiecesEnabled()){
               return;
            }
            for (int i = this.guiPieces.size()-1; i >= 0; i--) {
                GuiPiece guiPiece = this.guiPieces.get(i);
                if(mouseOverPiece(guiPiece,x,y)){
                    if( (this.playWithPersonServer.getGameState() == ChessGame.GAME_STATE_WHITE&& guiPiece.getColor() == Piece.COLOR_WHITE) ||
                       (this.playWithPersonServer.getGameState() == ChessGame.GAME_STATE_BLACK&& guiPiece.getColor() == Piece.COLOR_BLACK)){
                        this.dragOffsetX = x - guiPiece.getX();
                        this.dragOffsetY = y - guiPiece.getY();
                        this.playWithPersonServer.setDragPiece(guiPiece);
                        this.playWithPersonServer.repaint();
                        break;
                     }
                 }
            }  
        }
        catch(Exception e){}   
    }

    //Kieem tra co phai dang nhan vo hinh nay hay k
    private boolean mouseOverPiece(GuiPiece guiPiece, int x, int y) {

            return guiPiece.getX() <= x 
                && guiPiece.getX()+guiPiece.getWidth() >= x
                && guiPiece.getY() <= y
                && guiPiece.getY()+guiPiece.getHeight() >= y;
    }

    @Override
    public void mouseReleased(MouseEvent evt) {
        int x = evt.getPoint().x - this.dragOffsetX;
        int y = evt.getPoint().y - this.dragOffsetY;
        try
        {
             if( this.playWithComputer.getDragPiece() != null){
                

                // set game piece to the new location if possible
                //
                playWithComputer.setNewPieceLocation(this.playWithComputer.getDragPiece(), x, y);
                this.playWithComputer.repaint();
                this.playWithComputer.setDragPiece(null);
            }
        }
        catch(Exception e)
        {
            
        }
       try
        {
             if( this.playWithPersonClient.getDragPiece() != null){
  

                // set game piece to the new location if possible
                //
                playWithPersonClient.setNewPieceLocation(this.playWithPersonClient.getDragPiece(), x, y);
                this.playWithPersonClient.repaint();
                this.playWithPersonClient.setDragPiece(null);
            }
        }
        catch(Exception e)
        {
            
        }
       try
        {
             if( this.playWithPersonServer.getDragPiece() != null){
  

                // set game piece to the new location if possible
                //
                playWithPersonServer.setNewPieceLocation(this.playWithPersonServer.getDragPiece(), x, y);
                this.playWithPersonServer.repaint();
                this.playWithPersonServer.setDragPiece(null);
            }
        }
        catch(Exception e)
        {
            
        }
        
    }

    //khi keo chuot
    @Override
    public void mouseDragged(MouseEvent evt) {
        int x = evt.getPoint().x - this.dragOffsetX;
        int y = evt.getPoint().y - this.dragOffsetY;
        try
        {
            if(this.playWithComputer.getDragPiece() != null){
            

                //tien hanh set lai x,y cho hinh anh
                GuiPiece dragPiece = this.playWithComputer.getDragPiece();
                dragPiece.setX(x);
                dragPiece.setY(y);

                this.playWithComputer.repaint();
            }	
        }
        catch(Exception e) { }
       
        try
        {
            if(this.playWithPersonClient.getDragPiece() != null){

                //tien hanh set lai x,y cho hinh anh
                GuiPiece dragPiece = this.playWithPersonClient.getDragPiece();
                dragPiece.setX(x);
                dragPiece.setY(y);

                this.playWithPersonClient.repaint();
            }	
        }
        catch(Exception e) { }
         try
        {
            if(this.playWithPersonServer.getDragPiece() != null){

                //tien hanh set lai x,y cho hinh anh
                GuiPiece dragPiece = this.playWithPersonServer.getDragPiece();
                dragPiece.setX(x);
                dragPiece.setY(y);

                this.playWithPersonServer.repaint();
            }	
        }
        catch(Exception e) { }
        
    }

    @Override
    public void mouseMoved(MouseEvent arg0) {}

    @Override
    public void mouseClicked(MouseEvent arg0) {}

    @Override
    public void mouseEntered(MouseEvent arg0) {}

    @Override
    public void mouseExited(MouseEvent arg0) {}
}
