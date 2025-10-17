//Chess Game
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Chess extends JFrame {
    private JButton[][] boardButtons = new JButton[8][8];
    private Piece[][] board = new Piece[8][8];
    private boolean playerTurn = true;
    private int selectedRow = -1, selectedCol = -1;
    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private boolean[] whiteRookMoved = new boolean[2]; 
    private boolean[] blackRookMoved = new boolean[2]; 
    private int enPassantTargetCol = -1;
    private int enPassantTargetRow = -1;
    private boolean gameOver = false;

    public Chess() 
    {
        setTitle("Man VS Computer");
        setBounds(230, 5, 950,725);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(8, 8));
        initializeBoard();
        updateBoardUI();
        setVisible(true);
    }

    private class Piece 
    {
        String type;
        boolean isWhite;
        public Piece(String type, boolean isWhite) 
        {
            this.type = type;
            this.isWhite = isWhite;
        }

        public String getUnicode() 
        {
            return switch (type) 
            {
                case "K" -> isWhite ? "\u2654" : "\u265A"; 
                case "Q" -> isWhite ? "\u2655" : "\u265B"; 
                case "R" -> isWhite ? "\u2656" : "\u265C"; 
                case "B" -> isWhite ? "\u2657" : "\u265D"; 
                case "N" -> isWhite ? "\u2658" : "\u265E"; 
                case "P" -> isWhite ? "\u2659" : "\u265F"; 
                default -> "";
            };
        }
    }

    private void initializeBoard() 
    {
        board[0][0] = new Piece("R", false);
        board[0][1] = new Piece("N", false);
        board[0][2] = new Piece("B", false);
        board[0][3] = new Piece("Q", false);
        board[0][4] = new Piece("K", false);
        board[0][5] = new Piece("B", false);
        board[0][6] = new Piece("N", false);
        board[0][7] = new Piece("R", false);

        for (int i = 0; i < 8; i++) 
        board[1][i] = new Piece("P", false);
        board[7][0] = new Piece("R", true);
        board[7][1] = new Piece("N", true);
        board[7][2] = new Piece("B", true);
        board[7][3] = new Piece("Q", true);
        board[7][4] = new Piece("K", true);
        board[7][5] = new Piece("B", true);
        board[7][6] = new Piece("N", true);
        board[7][7] = new Piece("R", true);

        for (int i = 0; i < 8; i++) 
        board[6][i] = new Piece("P", true);

        for (int row = 0; row < 8; row++) 
        {
            for (int col = 0; col < 8; col++) 
            {
                JButton btn = new JButton();
                btn.setFont(new Font("SansSerif", Font.PLAIN, 50));
                btn.setOpaque(true);
                btn.setBorderPainted(false);
                final int r = row, c = col;
                btn.addActionListener(e -> handleClick(r, c));
                boardButtons[row][col] = btn;
                add(btn);
            }
        }
    }

    private void updateBoardUI() 
    {
        for (int row = 0; row < 8; row++) 
        {
            for (int col = 0; col < 8; col++) 
            {
                JButton btn = boardButtons[row][col];
                boolean isWhiteSquare = (row + col) % 2 == 0;
                
                btn.setBackground(isWhiteSquare ? new Color(240, 217, 181) : new Color(181, 136, 99));

                if (selectedRow != -1 && selectedRow == row && selectedCol == col) 
                {
                    btn.setBackground(Color.YELLOW);
                } 
                else if (selectedRow != -1 && isValidMove(selectedRow, selectedCol, row, col, board[selectedRow][selectedCol].isWhite, true)) 
                {
                    btn.setBackground(Color.GREEN); 
                }

                Piece p = board[row][col];
                btn.setText(p == null ? "" : p.getUnicode());

                if (p != null) 
                {
                    btn.setForeground(Color.BLACK);
                }
            }
        }
        if (gameOver) 
        {
            String status = "";
            if (isKingInCheckmate(true)) {
                status = "Computer Win!";
            } else if (isKingInCheckmate(false)) {
                status = "You Win!";
            } else if (isStalemate(playerTurn)) {
                status = "Draw!";
            } else {
                status = "Game is Over!"; 
            }
            JOptionPane.showMessageDialog(this, status, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleClick(int row, int col) 
    {
        if (gameOver || !playerTurn) 
        return; 

        Piece clicked = board[row][col];

        if (selectedRow == -1) 
        {
            if (clicked != null && clicked.isWhite) 
            {
                selectedRow = row;
                selectedCol = col;
                updateBoardUI();
            }
        } 
        else 
        {
            if (selectedRow == row && selectedCol == col) { 
                selectedRow = -1;
                selectedCol = -1;
                updateBoardUI();
                return;
            }
            boolean isMoveValid = isValidMove(selectedRow, selectedCol, row, col, board[selectedRow][selectedCol].isWhite, true);

            if (isMoveValid) 
            {
                makeMove(selectedRow, selectedCol, row, col);

                selectedRow = -1;
                selectedCol = -1;

                if(isGameOver()) {
                    gameOver = true;
                    updateBoardUI(); 
                } else {
                    playerTurn = false;
                    updateBoardUI(); 
                    new ComputerMoveWorker().execute();
                }
            } 
            else 
            {
                selectedRow = -1;
                selectedCol = -1;
                updateBoardUI(); 
            }
        }
    }
    
  
    private class ComputerMoveWorker extends SwingWorker<int[], Void> {

        @Override
        protected int[] doInBackground() throws Exception {
          
            return findBestMove();
        }

        @Override
        protected void done() {
         
            try {
                int[] bestMove = get(); 
                
                if (bestMove != null) {
                  
                    makeMove(bestMove[0], bestMove[1], bestMove[2], bestMove[3]);
                }

                if(isGameOver()) {
                    gameOver = true;
                } else {
                    playerTurn = true; 
                }
                
                updateBoardUI(); 
                
            } catch (Exception e) {
                e.printStackTrace();
               
                playerTurn = true; 
                updateBoardUI();
            }
        }
    }
    
   
    
    private void makeMove(int fromRow, int fromCol, int toRow, int toCol) {
        Piece piece = board[fromRow][fromCol];
        Piece captured = board[toRow][toCol];
        board[toRow][toCol] = piece;
        board[fromRow][fromCol] = null;
        
        if (piece.type.equals("K")) {
            if (piece.isWhite) {
                whiteKingMoved = true;
                if (Math.abs(fromCol - toCol) == 2) { 
                    int rookCol = (toCol == 6) ? 7 : 0;
                    int newRookCol = (toCol == 6) ? 5 : 3;
                    board[fromRow][newRookCol] = board[fromRow][rookCol];
                    board[fromRow][rookCol] = null;
                    whiteRookMoved[rookCol / 7] = true; 
                }
            } else {
                blackKingMoved = true;
                if (Math.abs(fromCol - toCol) == 2) { 
                    int rookCol = (toCol == 6) ? 7 : 0;
                    int newRookCol = (toCol == 6) ? 5 : 3;
                    board[fromRow][newRookCol] = board[fromRow][rookCol];
                    board[fromRow][rookCol] = null;
                    blackRookMoved[rookCol / 7] = true;
                }
            }
        } else if (piece.type.equals("R")) {
            if (piece.isWhite) {
                if (fromRow == 7 && fromCol == 0) whiteRookMoved[0] = true;
                if (fromRow == 7 && fromCol == 7) whiteRookMoved[1] = true;
            } else {
                if (fromRow == 0 && fromCol == 0) blackRookMoved[0] = true;
                if (fromRow == 0 && fromCol == 7) blackRookMoved[1] = true;
            }
        } else if (piece.type.equals("P")) {
            if (Math.abs(fromRow - toRow) == 2) {
                enPassantTargetRow = (fromRow + toRow) / 2;
                enPassantTargetCol = fromCol;
            } else if (toCol == enPassantTargetCol && toRow == enPassantTargetRow && captured == null) {
                if (piece.isWhite) {
                    board[toRow + 1][toCol] = null; 
                } else {
                    board[toRow - 1][toCol] = null; 
                }
            }
            
            if ((piece.isWhite && toRow == 0) || (!piece.isWhite && toRow == 7)) {
                board[toRow][toCol] = new Piece("Q", piece.isWhite); 
            }
        }
        
        if (!piece.type.equals("P") || Math.abs(fromRow - toRow) != 2) {
            if (piece.isWhite && !playerTurn) {
               
                enPassantTargetRow = -1;
                enPassantTargetCol = -1;
            }
        }
        
       
        if (!playerTurn && piece.type.equals("P") && Math.abs(fromRow - toRow) == 2) {
         
        } else if (!playerTurn) {
            enPassantTargetRow = -1;
            enPassantTargetCol = -1;
        } else if (playerTurn && piece.type.equals("P") && Math.abs(fromRow - toRow) == 2) {
          
        } else if (playerTurn) {
             enPassantTargetRow = -1;
             enPassantTargetCol = -1;
        }

    }
    
    private boolean isGameOver() {
        return isKingInCheckmate(true) || isKingInCheckmate(false) || isStalemate(true) || isStalemate(false);
    }
    
    private boolean isStalemate(boolean isWhite) {
        if (isKingInCheck(isWhite)) return false; 
        
        return generateAllValidMoves(isWhite).isEmpty();
    }
    
    private boolean isKingInCheckmate(boolean isWhite) {
        if (!isKingInCheck(isWhite)) return false; 
        
        return generateAllValidMoves(isWhite).isEmpty();
    }
    
    private boolean isKingInCheck(boolean isWhite) {
        int kingR = -1, kingC = -1;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p != null && p.type.equals("K") && p.isWhite == isWhite) {
                    kingR = r;
                    kingC = c;
                    break;
                }
            }
        }

        if (kingR == -1) return false; 

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p != null && p.isWhite != isWhite) { 
                    List<int[]> moves = getPiecePotentialMoves(r, c, !isWhite, false);
                    for (int[] move : moves) {
                        if (move[2] == kingR && move[3] == kingC) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, boolean isWhite, boolean checkSafety) 
    {
        List<int[]> validMoves = checkSafety ? generateAllValidMoves(isWhite) : getPiecePotentialMoves(fromRow, fromCol, isWhite, true);
        
        for (int[] move : validMoves) 
        {
            if (move[0] == fromRow && move[1] == fromCol && move[2] == toRow && move[3] == toCol) 
            {
                return true;
            }
        }
        return false;
    }
    
    private List<int[]> generateAllValidMoves(boolean isWhite) 
    {
        List<int[]> moves = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) 
            {
                Piece p = board[r][c];
                if (p != null && p.isWhite == isWhite) 
                {
                    List<int[]> potentialMoves = getPiecePotentialMoves(r, c, isWhite, true);
                    for (int[] move : potentialMoves) {
                        if (!isMoveIntoCheck(move[0], move[1], move[2], move[3], isWhite)) {
                            moves.add(move);
                        }
                    }
                }
            }
        }
        return moves;
    }
    
    private boolean isMoveIntoCheck(int fromR, int fromC, int toR, int toC, boolean isWhite) {
        Piece originalPiece = board[fromR][fromC];
        Piece capturedPiece = board[toR][toC];

      
        Piece enPassantVictim = null;
        int enPassantVictimR = -1;
        int enPassantVictimC = -1;
        if (originalPiece != null && originalPiece.type.equals("P") && capturedPiece == null && toR == enPassantTargetRow && toC == enPassantTargetCol) {
            enPassantVictimR = isWhite ? toR + 1 : toR - 1;
            enPassantVictimC = toC;
            enPassantVictim = board[enPassantVictimR][enPassantVictimC];
            board[enPassantVictimR][enPassantVictimC] = null;
        }

        board[toR][toC] = originalPiece;
        board[fromR][fromC] = null;
        
        boolean isCheck = isKingInCheck(isWhite);

    
        board[fromR][fromC] = originalPiece;
        board[toR][toC] = capturedPiece;

      
        if (enPassantVictim != null) {
             board[enPassantVictimR][enPassantVictimC] = enPassantVictim;
        }

        return isCheck;
    }

    private List<int[]> getPiecePotentialMoves(int r, int c, boolean isWhite, boolean includeSpecialMoves) 
    {
        List<int[]> moves = new ArrayList<>();
        Piece p = board[r][c];
        if (p == null) 
        return moves;

        switch (p.type) 
        {
            case "P":
                int direction = isWhite ? -1 : 1;
                int startRow = isWhite ? 6 : 1;

                if (r + direction >= 0 && r + direction < 8 && board[r + direction][c] == null) 
                {
                    moves.add(new int[]{r, c, r + direction, c});
                
                    if (r == startRow && board[r + 2 * direction][c] == null) 
                    {
                        moves.add(new int[]{r, c, r + 2 * direction, c});
                    }
                }

                for (int dc : new int[]{-1, 1}) 
                {
                    int nc = c + dc;
                    int nr = r + direction;
                    if (nr >= 0 && nr < 8 && nc >= 0 && nc < 8) 
                    {
                        Piece target = board[nr][nc];
                        if (target != null && target.isWhite != isWhite) 
                        {
                            moves.add(new int[]{r, c, nr, nc});
                        }
                        
                     
                        if (includeSpecialMoves && nr == enPassantTargetRow && nc == enPassantTargetCol && board[nr][nc] == null) {
                            if ((isWhite && r == 3) || (!isWhite && r == 4)) { 
                                Piece potentialVictim = board[r][nc];
                                if (potentialVictim != null && potentialVictim.type.equals("P") && potentialVictim.isWhite != isWhite) {
                                    moves.add(new int[]{r, c, nr, nc}); 
                                }
                            }
                        }
                    }
                }
                break;

            case "R":
            case "B":
            case "Q":
                int[][] directions;
                if (p.type.equals("R")) directions = new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
                else if (p.type.equals("B")) directions = new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
                else directions = new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

                for (int[] dir : directions) 
                {
                    for (int i = 1; i < 8; i++) 
                    {
                        int nr = r + dir[0] * i;
                        int nc = c + dir[1] * i;
                        if (nr >= 0 && nr < 8 && nc >= 0 && nc < 8) 
                        {
                            Piece target = board[nr][nc];
                            if (target == null) {
                                moves.add(new int[]{r, c, nr, nc});
                            } 
                            else 
                            {
                                if (target.isWhite != isWhite) 
                                {
                                    moves.add(new int[]{r, c, nr, nc}); 
                                }
                                break; 
                            }
                        } 
                        else 
                        {
                            break; 
                        }
                    }
                }
                break;

            case "N":
                int[][] knightMoves = new int[][]{{2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {1, -2}, {-1, 2}, {-1, -2}};
                for (int[] move : knightMoves) 
                {
                    int nr = r + move[0];
                    int nc = c + move[1];
                    if (nr >= 0 && nr < 8 && nc >= 0 && nc < 8) 
                    {
                        Piece target = board[nr][nc];
                        if (target == null || target.isWhite != isWhite) 
                        {
                            moves.add(new int[]{r, c, nr, nc});
                        }
                    }
                }
                break;
            case "K":
                for (int dr = -1; dr <= 1; dr++) 
                {
                    for (int dc = -1; dc <= 1; dc++) 
                    {
                        if (dr == 0 && dc == 0) continue;
                        int nr = r + dr;
                        int nc = c + dc;
                        if (nr >= 0 && nr < 8 && nc >= 0 && nc < 8) 
                        {
                            Piece target = board[nr][nc];
                            if (target == null || target.isWhite != isWhite) 
                            {
                                moves.add(new int[]{r, c, nr, nc});
                            }
                        }
                    }
                }
                
                if (includeSpecialMoves && !isKingInCheck(isWhite)) {
                    boolean kingMoved = isWhite ? whiteKingMoved : blackKingMoved;
                    boolean[] rookMoved = isWhite ? whiteRookMoved : blackRookMoved;
                    int row = isWhite ? 7 : 0;
                    
                    if (!kingMoved) {
                      
                        if (!rookMoved[1] && board[row][5] == null && board[row][6] == null && board[row][7] != null && board[row][7].type.equals("R")) { 
                            if (!isMoveIntoCheck(r, c, row, 5, isWhite) && !isMoveIntoCheck(r, c, row, 6, isWhite)) {
                                moves.add(new int[]{r, c, row, 6}); 
                            }
                        }
                       
                        if (!rookMoved[0] && board[row][1] == null && board[row][2] == null && board[row][3] == null && board[row][0] != null && board[row][0].type.equals("R")) { 
                            if (!isMoveIntoCheck(r, c, row, 3, isWhite) && !isMoveIntoCheck(r, c, row, 2, isWhite)) {
                                moves.add(new int[]{r, c, row, 2}); 
                            }
                        }
                    }
                }
                break;
        }
        return moves;
    }

    private int[] findBestMove() 
    {
        int bestValue = Integer.MIN_VALUE;
        int[] move = null;
        List<int[]> allMoves = generateAllValidMoves(false); 
        
        Collections.shuffle(allMoves); 
        
        for (int[] m : allMoves) 
        {
            Piece originalPiece = board[m[0]][m[1]];
            Piece capturedPiece = board[m[2]][m[3]];
            int originalEnPassantTargetCol = enPassantTargetCol;
            int originalEnPassantTargetRow = enPassantTargetRow;
            
          
            Piece enPassantVictim = null;
            int enPassantVictimR = -1;
            int enPassantVictimC = -1;
            boolean isEnPassantMove = false;
            
            if (originalPiece != null && originalPiece.type.equals("P") && capturedPiece == null && m[3] == originalEnPassantTargetCol && m[2] == originalEnPassantTargetRow) {
                enPassantVictimR = m[2] - 1; 
                enPassantVictimC = m[3];
                enPassantVictim = board[enPassantVictimR][enPassantVictimC];
                board[enPassantVictimR][enPassantVictimC] = null;
                isEnPassantMove = true;
            }

          
            board[m[2]][m[3]] = originalPiece;
            board[m[0]][m[1]] = null;
            
          
            boolean isCastling = originalPiece.type.equals("K") && Math.abs(m[1] - m[3]) == 2;
            Piece originalRook = null;
            int originalRookC = -1, newRookC = -1;
            if (isCastling) {
                if (m[3] == 6) {
                    originalRookC = 7; newRookC = 5;
                } else { 
                    originalRookC = 0; newRookC = 3;
                }
                originalRook = board[m[0]][originalRookC];
                board[m[0]][newRookC] = originalRook;
                board[m[0]][originalRookC] = null;
            }

         
            if (originalPiece.type.equals("P") && Math.abs(m[0] - m[2]) == 2) {
                enPassantTargetRow = (m[0] + m[2]) / 2;
                enPassantTargetCol = m[1];
            } else {
                enPassantTargetCol = -1;
                enPassantTargetRow = -1;
            }

        
            int moveValue = alphaBeta(1, Integer.MIN_VALUE, Integer.MAX_VALUE, false, 3); 

          
            if (isCastling) {
                board[m[0]][originalRookC] = originalRook;
                board[m[0]][newRookC] = null;
            }
            
           
            board[m[0]][m[1]] = originalPiece;
            board[m[2]][m[3]] = capturedPiece;
            
           
            if (isEnPassantMove) {
                board[enPassantVictimR][enPassantVictimC] = enPassantVictim;
            }
            
          
            enPassantTargetCol = originalEnPassantTargetCol;
            enPassantTargetRow = originalEnPassantTargetRow;


            if (moveValue > bestValue) 
            {
                bestValue = moveValue;
                move = m;
            }
        }
        return move;
    }

    private int alphaBeta(int depth, int alpha, int beta, boolean isMax, int maxDepth) {
        if (depth == maxDepth || isGameOver()) return evaluateBoard();

        if (isMax) {
            int best = Integer.MIN_VALUE;
            List<int[]> moves = generateAllValidMoves(false);
            
            for (int[] m : moves) 
            {
                Piece originalPiece = board[m[0]][m[1]];
                Piece capturedPiece = board[m[2]][m[3]];
                int originalEnPassantTargetCol = enPassantTargetCol;
                int originalEnPassantTargetRow = enPassantTargetRow;
                
               
                Piece enPassantVictim = null;
                int enPassantVictimR = -1;
                int enPassantVictimC = -1;
                boolean isEnPassantMove = false;
                
                if (originalPiece != null && originalPiece.type.equals("P") && capturedPiece == null && m[3] == originalEnPassantTargetCol && m[2] == originalEnPassantTargetRow) {
                    enPassantVictimR = m[2] - 1; 
                    enPassantVictimC = m[3];
                    enPassantVictim = board[enPassantVictimR][enPassantVictimC];
                    board[enPassantVictimR][enPassantVictimC] = null;
                    isEnPassantMove = true;
                }

                
                board[m[2]][m[3]] = originalPiece;
                board[m[0]][m[1]] = null;
                
              
                boolean isCastling = originalPiece.type.equals("K") && Math.abs(m[1] - m[3]) == 2;
                Piece originalRook = null;
                int originalRookC = -1, newRookC = -1;
                if (isCastling) 
                {
                    if (m[3] == 6) 
                    { 
                        originalRookC = 7; newRookC = 5;
                    } 
                    else 
                    { 
                        originalRookC = 0; newRookC = 3;
                    }
                    originalRook = board[m[0]][originalRookC];
                    board[m[0]][newRookC] = originalRook;
                    board[m[0]][originalRookC] = null;
                }
                
               
                if (originalPiece.type.equals("P") && Math.abs(m[0] - m[2]) == 2) 
                {
                    enPassantTargetRow = (m[0] + m[2]) / 2;
                    enPassantTargetCol = m[1];
                } 
                else 
                {
                    enPassantTargetCol = -1;
                    enPassantTargetRow = -1;
                }


                best = Math.max(best, alphaBeta(depth + 1, alpha, beta, false, maxDepth));

               
                if (isCastling) 
                {
                    board[m[0]][originalRookC] = originalRook;
                    board[m[0]][newRookC] = null;
                }
                
              
                board[m[0]][m[1]] = originalPiece;
                board[m[2]][m[3]] = capturedPiece;

               
                if (isEnPassantMove) 
                {
                    board[enPassantVictimR][enPassantVictimC] = enPassantVictim;
                }

                
                enPassantTargetCol = originalEnPassantTargetCol;
                enPassantTargetRow = originalEnPassantTargetRow;

                alpha = Math.max(alpha, best);
                if (beta <= alpha) 
                break;
            }
            return best;
        } 
        else 
        {
            int best = Integer.MAX_VALUE;
            List<int[]> moves = generateAllValidMoves(true); 
            for (int[] m : moves) 
            {
                Piece originalPiece = board[m[0]][m[1]];
                Piece capturedPiece = board[m[2]][m[3]];
                int originalEnPassantTargetCol = enPassantTargetCol;
                int originalEnPassantTargetRow = enPassantTargetRow;
                
                
                Piece enPassantVictim = null;
                int enPassantVictimR = -1;
                int enPassantVictimC = -1;
                boolean isEnPassantMove = false;
                
                if (originalPiece != null && originalPiece.type.equals("P") && capturedPiece == null && m[3] == originalEnPassantTargetCol && m[2] == originalEnPassantTargetRow) {
                    enPassantVictimR = m[2] + 1; 
                    enPassantVictimC = m[3];
                    enPassantVictim = board[enPassantVictimR][enPassantVictimC];
                    board[enPassantVictimR][enPassantVictimC] = null;
                    isEnPassantMove = true;
                }

                
                board[m[2]][m[3]] = originalPiece;
                board[m[0]][m[1]] = null;
                
               
                boolean isCastling = originalPiece.type.equals("K") && Math.abs(m[1] - m[3]) == 2;
                Piece originalRook = null;
                int originalRookC = -1, newRookC = -1;
                if (isCastling) 
                {
                    if (m[3] == 6) 
                    { 
                        originalRookC = 7; newRookC = 5;
                    } 
                    else 
                    { 
                        originalRookC = 0; newRookC = 3;
                    }
                    originalRook = board[m[0]][originalRookC];
                    board[m[0]][newRookC] = originalRook;
                    board[m[0]][originalRookC] = null;
                }
                
              
                if (originalPiece.type.equals("P") && Math.abs(m[0] - m[2]) == 2) 
                {
                    enPassantTargetRow = (m[0] + m[2]) / 2;
                    enPassantTargetCol = m[1];
                } 
                else 
                {
                    enPassantTargetCol = -1;
                    enPassantTargetRow = -1;
                }
                
                best = Math.min(best, alphaBeta(depth + 1, alpha, beta, true, maxDepth));

              
                if (isCastling) 
                {
                    board[m[0]][originalRookC] = originalRook;
                    board[m[0]][newRookC] = null;
                }
                
               
                board[m[0]][m[1]] = originalPiece;
                board[m[2]][m[3]] = capturedPiece;

              
                if (isEnPassantMove) 
                {
                    board[enPassantVictimR][enPassantVictimC] = enPassantVictim;
                }

               
                enPassantTargetCol = originalEnPassantTargetCol;
                enPassantTargetRow = originalEnPassantTargetRow;

                beta = Math.min(beta, best);
                if (beta <= alpha) 
                break; 
            }
            return best;
        }
    }

    private int evaluateBoard() 
    {
        if (isKingInCheckmate(true)) 
        return 1000000; 
        if (isKingInCheckmate(false)) 
        return -1000000; 
        if (isStalemate(true) || isStalemate(false)) 
        return 0;
        
        int score = 0;
        for (int r = 0; r < 8; r++) 
        {
            for (int c = 0; c < 8; c++) 
            {
                Piece p = board[r][c];
                if (p != null) 
                {
                    int val = switch (p.type) 
                    {
                        case "K" -> 900; 
                        case "Q" -> 90;
                        case "R" -> 50;
                        case "B" -> 30;
                        case "N" -> 30;
                        case "P" -> 10;
                        default -> 0;
                    };
                    
                    val += getPositionalBonus(r, c, p.type, p.isWhite);

                    score += p.isWhite ? -val : val;
                }
            }
        }
        return score;
    }
    
    private int getPositionalBonus(int r, int c, String type, boolean isWhite) 
    {
        int bonus = 0;
        
        if (type.equals("P")) 
        {
            if (isWhite) 
            {
                bonus += (7 - r) * 2; 
            } 
            else 
            {
                bonus += r * 2; 
            }
        } 
        else if (type.equals("N")) 
        {
            if (r > 2 && r < 5 && c > 2 && c < 5) bonus += 5; 
        } 
        else if (type.equals("K")) 
        {
             if (r < 2 || r > 5) bonus -= 20; 
        }
        return bonus;
    }
    public static void main(String[] args) 
    {
        SwingUtilities.invokeLater(Chess::new);
    }
}