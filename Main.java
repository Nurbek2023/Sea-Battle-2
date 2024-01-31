import java.util.Scanner;
import java.util.Random;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;



public class Main {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";

    private static final String FILE_NAME = "player_data.txt";

    public static void main(String[] args){

        Scanner scanner = new Scanner(System.in);

        List <Player> playerList = new ArrayList<>();

        readFromFile(playerList);



        // Get player's name
        String playerName = userName(scanner);

        // Initialize sea and place ships
        int[][] sea = new int[7][7];
        initializeSea(sea);

        placeShips(sea, 3, 1);
        placeShips(sea, 2, 2);
        placeShips(sea, 1, 4);

        System.out.println(" ");

        // Main game loop
        int shots = 0;
        boolean gameOver = false;

        while (!gameOver) {
            clearScreen();

            printSea(sea, false);
            System.out.println(" ");
            System.out.println("Enter coordinates for your shot (row and column, e.g., A3): ");
            String shotCoordinates = scanner.nextLine().toUpperCase();


            // Convert user input to row and column indices
            int row = shotCoordinates.charAt(0) - 'A';
            int col = Integer.parseInt(shotCoordinates.substring(1)) - 1;
            System.out.println(" ");

            // Process the player's shot
            shots++;
            processShot(sea, row, col);
            printSea(sea, false);

            System.out.println(" ");
            System.out.print("click 'Enter' ");
            String request = scanner.nextLine().toUpperCase();
            if (request.equals("Yes")){
                clearScreen();
            }

            // Check if all ships are sunk
            if (areAllShipsSunk(sea)) {
                gameOver = true;

                System.out.println("Congratulations, " + playerName + "! You sunk all the ships in " + shots + " shots.");
                Player player = new Player(playerName, shots);
                playerList.add(player);

                Collections.sort(playerList, Comparator.comparingInt(Player::getShots));

                // Ask if the player wants to play again
                System.out.println("Do you want to play again? (yes/no): ");
                String playAgain = scanner.nextLine().toLowerCase();
                if (playAgain.equals("yes")) {
                    // Reset the game
                    initializeSea(sea);
                    placeShips(sea, 3, 1);
                    placeShips(sea, 2, 2);
                    placeShips(sea, 1, 4);
                    shots = 0;
                    gameOver = false;
                }
                else{


                    displayAndWriteToFile(playerList);

                }
            }
        }


        // Close the scanner
        scanner.close();
    }

    private static void readFromFile(List <Player> playersList){
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String name = parts[0].trim();
                int weight = Integer.parseInt(parts[1].trim());
                playersList.add(new Player(name, weight));
            }
        } catch (IOException e) {
            System.out.println("Error reading data from file: " + e.getMessage());
        }
    }

    private static void displayAndWriteToFile(List<Player> playersList) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            System.out.println("\nPlayers sorted by shots: ");
            int numOfPlayersToShow = Math.min(playersList.size(), 10);
            System.out.println("Top players: ");
            for (int i = 0; i < numOfPlayersToShow; i++) {
                Player player = playersList.get(i);
                System.out.println("Name: " + player.getName() + ", Shots: " + player.getShots());
                // Write player data to the file
                writer.println(player.getName() + ", " + player.getShots());
            }
        } catch (IOException e) {
            System.out.println("Error writing data to file: " + e.getMessage());
        }
    }


    private static void processShot(int[][] sea, int row, int col) {
        if (sea[row][col] == 0) {
            // Miss
            sea[row][col] = -1;
            System.out.println("Miss! ");
        } else if (sea[row][col] > 0) {
            // Hit
            int shipSize = sea[row][col];
            sea[row][col] = -2; // mark like hit

            // Checking to see if the entire ship is sunk
            if (isShipSunk(sea, shipSize, row, col)) {
                System.out.println("Hit and sunk a ship the size of " + shipSize + "!");

            } else {
                System.out.println("Hit!");
            }
        } else {
            //This position has already been hit
            System.out.println("\n" + "You've already shot here. Try again.");
        }
    }

    private static boolean isShipSunk(int[][] sea, int shipSize, int row, int col) {
        if (shipSize == 1) {
            return true; // Single cell ship is always sunk
        }
        else if (shipSize == 2) {
            if (row == 0){
                if(sea[row + 1][col] == -2){
                    return true;
                }
            }
            else if (row == (sea.length - 1)){
                if (sea[row - 1][col] == -2){
                    return true;
                }
            }
            if (col == 0){
                if(sea[row][col + 1] == -2){
                    return true;
                }
            }
            else if (col == (sea[0].length - 1)){
                if (sea[row][col -1] == -2){
                    return true;
                }
            }
            else if (row < (sea.length - 1) && row > 0){
                if(sea[row + 1][col] == -2 || sea[row - 1][col] == -2 ) {
                    return true;//
                }
            }
            else if (col < (sea[0].length - 1) && col > 0){
                if(sea[row][col + 1] == -2 || sea[row][col - 1] == -2 ) {
                    return true;//
                }
            }

        }
        for (int i = 0; i < sea.length; i++) {
            for (int j = 0; j < sea[i].length; j++) {
                if (sea[i][j] == shipSize) {
                    // As soon as a piece of a ship of a given size is found, we check whether all its parts are sunk
                    if (!isShipPartIntact(sea, i, j)) {
                        return false;
                    }
                }
            }
        }
        return true; // The whole ship is sunk
    }
    private static boolean isShipPartIntact(int[][] sea, int row, int col) {
        return sea[row][col] == -2; //We check that this part of the ship is sunk
    }
    private static void printSea(int[][] sea, boolean hideShips) {
        System.out.println(" ");
        System.out.print("   ");
        for (int i = 1; i <= sea[0].length; i++) {
            System.out.print(i + " ");
        }
        System.out.println();

        char rowLabel = 'A';
        for (int i = 0; i < sea.length; i++) {
            System.out.print(rowLabel + "|");
            for (int j = 0; j < sea[i].length; j++) {
                char symbol;
                String color = ANSI_RESET;

                if (sea[i][j] == 0 || (sea[i][j] > 0 && hideShips)) {
                    symbol = '~'; // Empty cell or hidden ship
                    color = ANSI_BLUE;
                } else if (sea[i][j] == -1) {
                    symbol = 'o'; // Miss
                    color = ANSI_YELLOW;
                } else if (sea[i][j] == -2) {
                    symbol = 'X'; // Hit
                    color = ANSI_RED;
                } else {
                    symbol = 'S'; // Ship (you can choose another symbol to hide or indicate ship)
                    color = ANSI_BLUE;
                }
                System.out.print(" " + color + symbol + ANSI_RESET);
            }
            System.out.println();
            rowLabel++;
        }
    }

    private static void initializeSea(int[][] sea){
        for(int i = 0; i < sea.length; i++){
            for(int j = 0; j<sea[i].length; j++){
                sea[i][j] = 0;
            }
        }
    }

    private static void placeShips(int[][] sea, int shipSize, int numOfShips) {
        Random random = new Random();
        for (int ship = 0; ship < numOfShips; ship++) {
            int row, col;
            boolean isHorizontally;
            do {
                isHorizontally = random.nextBoolean();
                if (isHorizontally) {
                    row = random.nextInt(sea.length);
                    col = random.nextInt(5);
                } else {
                    row = random.nextInt(5);
                    col = random.nextInt(sea[row].length);
                }

            } while (!isValidPlace(sea, row, col, shipSize, isHorizontally));

            for (int i = 0; i < shipSize; i++) {
                if (isHorizontally) {
                    sea[row][col + i] = shipSize;
                } else {
                    sea[row + i][col] = shipSize;
                }
            }

        }
    }

    private static boolean isValidPlace(int[][] sea, int startRow, int startCol, int shipSize, boolean isHorizontally) {
        // int seaRows = sea.length;
        // int seaCols = sea[0].length;
        if (isHorizontally) {
            int newStartRow = startRow - 1;
            int newStartCol = startCol - 1;
            int endCol = 0;
            for (int i = -1; i <= shipSize; i++) {
                endCol = startCol + i;
            }
            int endRow = startRow + 1;

            boolean isValid = checkAroundShipHorizontally(sea, shipSize, endCol, endRow, startRow, startCol, newStartRow, newStartCol);
            return isValid;
        } else {

            int newStartRow = startRow - 1;
            int newStartCol = startCol - 1;
            int endRow = 0;
            for (int i = -1; i <= shipSize; i++) {
                endRow = startRow + i;
            }

            int endCol = startCol + 1;
            return checkAroundShipVertically(sea, shipSize, endCol, endRow, startRow, startCol, newStartRow, newStartCol);

        }
    }

    private static boolean checkAroundShipVertically(int[][] sea, int shipSize, int endCol, int endRow, int startRow, int startCol, int newStartRow, int newStartCol) {
        int l = 0; // we apply l to check how many objects surrounded the ship
        boolean result = false;
        // when the ship is at left of the top
        if (newStartCol < 0 && newStartRow < 0) {
            for (int i = 0; i <= shipSize; i++) {
                for (int j = 0; j <= 1; j++) {
                    if (sea[startRow + i][startCol + j] != 0) {
                        l++;
                    }
                }
            }
        }
        // when the ship is at right of the top
        else if (endCol >= sea[0].length && newStartRow < 0) {
            for (int i = 0; i <= shipSize; i++) {
                for (int j = -1; j < 1; j++) {
                    if (sea[startRow + i][startCol + j] != 0) {
                        l++;
                    }
                }
            }
        }
        // when the ship is at left of the bottom *
        else if (newStartCol < 0 && endRow >= sea.length) {
            for (int i = -1; i < shipSize; i++) {
                for (int j = 0; j <= 1; j++) {
                    if (sea[startRow + i][startCol + j] != 0) {
                        l++;
                    }
                }
            }
        }
        // when the ship is at the right of the bottom
        else if (endRow >= sea.length && endCol >= sea[0].length) {
            for (int i = -1; i < shipSize; i++) {
                for (int j = -1; j < 1; j++) {
                    if (sea[startRow + i][startCol + j] != 0) {
                        l++;
                    }
                }
            }
        }
        // when the ship is at left of the sea are *
        else if (newStartCol < 0) {
            for (int i = -1; i <= shipSize; i++) {
                for (int j = 0; j <= 1; j++) {
                    if (sea[startRow + i][startCol + j] != 0) {
                        l++;
                    }
                }
            }
        }
        // when the ship is at the right of the sea are
        else if (endCol >= sea[0].length) {
            for (int i = -1; i <= shipSize; i++) {
                for (int j = -1; j < 1; j++) {
                    if (sea[startRow + i][startCol + j] != 0) {
                        l++;
                    }
                }
            }
        }
        // when the ship is at the top of the sea area
        else if (newStartRow < 0 && endRow < sea.length) {
            for (int i = 0; i <= shipSize; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (sea[startRow + i][startCol + j] != 0) {
                        l++;
                    }
                }
            }
        }
        // when the ship is at the bottom of the sea area
        else if (endRow >= sea.length) {
            for (int i = -1; i < shipSize; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (sea[startRow + i][startCol + j] != 0) {
                        l++;
                    }
                }
            }
        }
        // when the ship is at the center (not touching the borders)
        else if (sea.length > endRow) {
            for (int i = -1; i <= shipSize; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (sea[startRow + i][startCol + j] != 0) {
                        l++;
                    }
                }
            }
        } else if (l == 0) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    private static boolean checkAroundShipHorizontally(int[][] sea, int shipSize, int endCol, int endRow, int startRow, int startCol, int newStartRow, int newStartCol) {
        int l = 0; //to check how many objects surrounded the ship
        // when the ship is at left of the top
        if (newStartCol < 0 && newStartRow < 0) {
            for (int i = 0; i <= shipSize; i++) {
                for (int j = 0; j <= 1; j++) {
                    if (sea[startRow + j][startCol + i] != 0) {
                        l++;
                    }
                }
            }
        }
        // when the ship is at left of the bottom
        else if (newStartCol < 0 && endRow >= sea.length) {
            for (int i = 0; i <= shipSize; i++) {
                for (int j = -1; j < 1; j++) {
                    if (sea[startRow + j][startCol + i] != 0) {
                        l++;
                    }
                }
            }
        }
        // when the ship is at right of the top
        else if (endCol >= sea[0].length && newStartRow < 0) {
            for (int i = -1; i < shipSize; i++) {
                for (int j = 0; j <= 1; j++) {
                    if (sea[startRow + j][startCol + i] != 0) {
                        l++;
                    }
                }
            }
        }
        // when the ship is at right of the bottom
        else if (endCol >= sea[0].length && endRow >= sea.length) {
            for (int i = -1; i < shipSize; i++) {
                for (int j = -1; j < 1; j++) {
                    if (sea[startRow + j][startCol + i] != 0) {
                        l++;
                    }
                }
            }
        }
        // when ship on the left side of our sea area
        else if (newStartCol < 0) {
            for (int i = 0; i <= shipSize; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (sea[startRow + j][startCol + i] != 0) {
                        l++;
                    }
                }
            }
        }
        // when the ship is at right side of our sea area
        else if (sea[0].length <= endCol) {
            for (int i = -1; i < shipSize; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (sea[startRow + j][startCol + i] != 0) {
                        l++;
                    }
                }
            }
        }
        // when the ship is at the top of our sea area
        else if (newStartRow < 0) {
            for (int i = -1; i <= shipSize; i++) {
                for (int j = 0; j <= 1; j++) {
                    if (sea[startRow + j][startCol + i] != 0) {
                        l++;
                    }
                }
            }
        }
        // when the ship is at the bottom of our sea area
        else if (endRow >= sea.length) {
            for (int i = -1; i <= shipSize; i++) {
                for (int j = -1; j < 1; j++) {
                    if (sea[startRow + j][startCol + i] != 0) {
                        l++;
                    }
                }
            }
        }
        // when the ship is at the center (not touching the borders)
        else if (sea[0].length > endCol) {
            for (int i = -1; i <= shipSize; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (sea[startRow + j][startCol + i] != 0) {
                        l++;
                    }
                }
            }
        }

        boolean result;
        if (l == 0) {
            result = true;
        } else {
            result = false;
        }


        return result;


    }

    private static boolean areAllShipsSunk(int[][] sea) {
        for (int i = 0; i < sea.length; i++) {
            for (int j = 0; j < sea[i].length; j++) {
                if (sea[i][j] > 0) {
                    return false; // At least one ship is still afloat
                }
            }
        }
        return true; // All ships are sunk
    }

    private static String userName(Scanner scanner) {
        System.out.println("Print your name: ");
        String name = scanner.nextLine();

        System.out.println("Welcome to 'Sea Battle' game " + name + "!");
        return name;
    }
    private static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    //just checking
}

