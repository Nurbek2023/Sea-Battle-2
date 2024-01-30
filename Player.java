public class Player {
    String playerName;
    int shots;

    public Player(String playerName, int shots) {
        this.shots = shots;
        this.playerName = normalizeName(playerName);
    }

    public String getName() {
        return playerName;
    }

    public int getShots() {
        return shots;
    }

    private static String normalizeName(String input) {
        // Convert the first character to uppercase and the rest to lowercase
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
}
