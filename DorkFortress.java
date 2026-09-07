import java.util.Scanner;
import java.util.Random;
import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.Collections;



//I had too much fun with this...


//Systems by Systems approach:

//make starting display (done)
//Make maze (Done) <-- too unpredictable <-- Fixed with Maze algorithm <-- Dragon dude keeps camping exit <-- Fixed to have multiple ways to exit  AND you can decide which type to use!
//Use
//Make Dragon AI (Done... but it's boring) <-- Fixed (Still boring) <-- Made harder by adding a chase feature and smarter AI <-- EVEN HARDER by stealing code segment from old code... Now you can decide between difficulty and made WAYY harder (I have yet to beat him on Nightmare)!




public class DorkFortress {

    // Globals (Or as I like to call them, Lazy Variables)
    static Scanner scanner = new Scanner(System.in);
    static Random random = new Random();

    static int dorkRow = -1, dorkCol = -1;
    static int trogdorRow = -1, trogdorCol = -1;

    static int SIZE;
    static String[][] fortress;
    static boolean gameOver = false;
    static final String WALL    = "🧱 ";
    static final String PATH    = "⬛ ";
    static final String DORK    = "🤓 ";
    static final String TROGDOR = "🐉 ";
    static final String EXIT    = "🚪 ";
    static int difficulty = 1;           // default medium
    static double smartChance = 0.6;     // will be set according to difficulty

    static final int AGGRO_DISTANCE = 7;
    static final boolean USE_ASTAR_ON_NIGHTMARE = true;

    // Maze style flag
    static int mazeStyle = 0;  // 0=classic random, 1=backtracker, 2=kruskal

    //Thank goodness for Linux Ascii art programs...
    static final String TITLE = """
        
        ╔═════════════════════════════════════════════════════╗
        ║               WELCOME TO...                         ║
        ║                                                     ║
        ║   ██████╗ ███████╗ ██████╗ ██╗  ██╗███████╗ ██████╗ ║
        ║   ██╔══██╗██╔══██╗ ██╔══██╗██║  ██║██╔════╝██╔═══██╗║
        ║   ██║  ██║███████╔╝██████╔ ███████║█████╗  ██║   ██║║
        ║   ██║  ██║██╔══██╗ ██╔══██╗██╔══██║██╔══╝  ██║   ██║║
        ║   ██████╔╝██║  ██║ ██║  ██║██║  ██║███████╗╚██████╔╝║
        ║   ╚═════╝ ╚═╝  ╚═╝ ╚═╝  ╚═╝╚═╝  ╚═╝╚══════╝ ╚═════╝ ║
        ║         Escape from learning the London             ║
        ╚═════════════════════════════════════════════════════╝
        Logo hectically made with Kali Linux
        You are the Dork (🤓 ) trying to escape the fortress
        where Trogdor the Burninator (🐉 ) is trying to catch you!
        
        Run before he forces you to play the Chess London System! 
        
        Reach the exit (🚪 ) without being caught!
        
        Controls:
          W = up    A = left
          S = down  D = right
        
        """;
    //I kind of forgot why I made this about the Chess London
    public static int displayIntroGetSIZE() {
        System.out.print(TITLE);
        System.out.println("Choose difficulty level:");
        System.out.println("  0 = Easy       (Trogdor is quite slow and dumb. He's busy watching Gothamchess)");
        System.out.println("  1 = Medium     (sometimes smart, sometimes not, He's scared of the Indian game)");
        System.out.println("  2 = Hard       (mostly tries to chase you, He's thinking of the London System)");
        System.out.println("  3 = Extreme    (rarely makes bad moves ready to teach you the Chess London System)");
        System.out.println("  4 = Nightmare  (Run, HE IS PLAYING THE LONDON!)");
        System.out.print("Enter difficulty (0–4): ");
        while (true) {
            if (scanner.hasNextInt()) {
                int diff = scanner.nextInt();
                if (diff >= 0 && diff <= 4) {
                    difficulty = diff;
                    switch (difficulty) {
                        case 0 -> smartChance = 0.80;
                        case 1 -> smartChance = 0.60;
                        case 2 -> smartChance = 0.30;   // as you requested
                        case 3 -> smartChance = 0.10;
                        case 4 -> smartChance = 0.00;   // always smart / A* and that's scary!!
                    }
                    break;
                }
            }
            System.out.print("Please enter 0, 1, 2, 3 or 4: ");
            scanner.nextLine();
        }

        // Maze generation style choice
        System.out.println("\nMaze generation style:");
        System.out.println("  0 = Classic random (Random required method – may have dead ends & isolated spots)");
        System.out.println("  1 = Perfect maze (recursive backtracker – always connected, no isolated areas)");
        System.out.println("  2 = Kruskal's algorithm (multiply-connected with loops – many ways to the exit)");
        System.out.print("Choose style (0–2): ");

        while (true) {//Scary always true loop
            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();
                scanner.nextLine();
                if (choice >= 0 && choice <= 2) {
                    mazeStyle = choice;
                    break;
                }
            }
            System.out.print("Please enter 0, 1 or 2: "); //idiot
        }

        System.out.print("\nFortress size (7–25, 11 recommended): ");
        while (true) {
            if (scanner.hasNextInt()) {
                int size = scanner.nextInt();
                scanner.nextLine();
                if (size >= 7 && size <= 25) return size;
                System.out.print("Please choose between 7 and 25: ");
            } else {
                System.out.print("That's not a number. Try again: ");//Idiot
                scanner.nextLine();
            }
        }
    }

    public static void generateFortressGrid() {
        fortress = new String[SIZE][SIZE];

        if (mazeStyle == 1) {
            generatePerfectMaze();
        } else if (mazeStyle == 2) {
            generateKruskalMaze();
        } else {
            // Original random generation that I was opposed to make (until I got tired of being trapped in corners)
            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    fortress[r][c] = WALL;
                }
            }

            double pathProbability = (difficulty >= 3) ? 0.68 : 0.75; //I would change this but I don't want to make a 67 reference.


            //Creating the Array
            for (int r = 1; r < SIZE - 1; r++) {
                for (int c = 1; c < SIZE - 1; c++) {
                    if (random.nextDouble() < pathProbability) {
                        fortress[r][c] = PATH;
                    }
                }
            }
        }

        // Post-processing: add more loops + occasional small pockets
        // Applied to all styles — stronger effect on larger maps
        //Makes things better to play because fun...
        double extraHoleChance = 0.06 + (SIZE / 300.0);   // 0.08–0.14 range on 25–40 math or something
        double pocketChance    = 0.008 + (SIZE / 1200.0); // 0.01–0.04 range

        for (int r = 1; r < SIZE - 1; r++) {
            for (int c = 1; c < SIZE - 1; c++) {
                if (fortress[r][c].equals(WALL)) {
                    // Count path neighbors (up/down/left/right)
                    int pathNeighbors = 0;
                    if (r > 0   && fortress[r-1][c].equals(PATH)) pathNeighbors++;
                    if (r < SIZE-1 && fortress[r+1][c].equals(PATH)) pathNeighbors++;
                    if (c > 0   && fortress[r][c-1].equals(PATH)) pathNeighbors++;
                    if (c < SIZE-1 && fortress[r][c+1].equals(PATH)) pathNeighbors++;

                    // Create shortcut/loop if touching ≥2 paths. This just adds extra holes. Small chance it does happen though
                    if (pathNeighbors >= 2 && random.nextDouble() < extraHoleChance) {
                        fortress[r][c] = PATH; //Turning that place into a path
                    }
                }
                // Occasionally create small dead end pockets/islands
                else if (fortress[r][c].equals(PATH) && random.nextDouble() < pocketChance) {
                    // Block 1 random neighbor -> creates tiny culdesacs like a suburban neighborhood
                    int blockDir = random.nextInt(4);
                    int dr = (blockDir == 0 ? -1 : blockDir == 1 ? 1 : 0);
                    int dc = (blockDir == 2 ? -1 : blockDir == 3 ? 1 : 0);
                    int nr = r + dr, nc = c + dc;
                    if (nr >= 1 && nr < SIZE-1 && nc >= 1 && nc < SIZE-1 &&
                            fortress[nr][nc].equals(PATH)) {
                        fortress[nr][nc] = WALL;
                    }
                }
            }
        }
        // End of post-processing (Marking, so I'm getting confused on what is and what isn't part of post-processing)
    }

    //  Perfect maze (recursive backtracker algorithm)
    private static void generatePerfectMaze() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                fortress[r][c] = WALL;
            }
        }

        int startR = 1 + random.nextInt((SIZE - 2) / 2) * 2;
        int startC = 1 + random.nextInt((SIZE - 2) / 2) * 2;

        carveMaze(startR, startC);
    }

    //I forgot what this was...
    private static void carveMaze(int r, int c) {
        fortress[r][c] = PATH;

        int[][] dirs = {{-2, 0}, {0, 2}, {2, 0}, {0, -2}};
        shuffleArray(dirs);

        for (int[] dir : dirs) {
            int nr = r + dir[0];
            int nc = c + dir[1];

            if (nr >= 1 && nr < SIZE - 1 && nc >= 1 && nc < SIZE - 1 &&
                    fortress[nr][nc].equals(WALL)) {
                fortress[r + dir[0]/2][c + dir[1]/2] = PATH;
                carveMaze(nr, nc);
            }
        }
    }
    //I forgot which maze algorithm was which... Future me will probably know though

    //Kruskal's randomized maze (multiply-connected with loops)
    private static void generateKruskalMaze() {
        // Fill with walls initially to make the maze
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                fortress[r][c] = WALL;
            }
        }

        // Treat odd rows/columns as "cells" so the algorithm can do its magic
        int cellsR = (SIZE - 1) / 2;
        int cellsC = (SIZE - 1) / 2;

        // Union-Find structure for cells (basically
        int[] parent = new int[cellsR * cellsC];
        for (int i = 0; i < parent.length; i++) parent[i] = i;

        // List of all possible walls (edges between adjacent cells)
        ArrayList<int[]> edges = new ArrayList<>();

        // Horizontal walls (or as we call them floors)
        for (int r = 0; r < cellsR; r++) {
            for (int c = 0; c < cellsC - 1; c++) {
                int cell1 = r * cellsC + c;
                int cell2 = r * cellsC + (c + 1);
                edges.add(new int[]{cell1, cell2, r*2+1, c*2+2});
            }
            //I'ma be honest IDK how this works but it works and that's what matters
        }

        // Vertical walls (Or just walls...)
        for (int r = 0; r < cellsR - 1; r++) {
            for (int c = 0; c < cellsC; c++) {
                int cell1 = r * cellsC + c;
                int cell2 = (r + 1) * cellsC + c;
                edges.add(new int[]{cell1, cell2, r*2+2, c*2+1});
            }
        }

        // Randomize the order of walls
        Collections.shuffle(edges, random);

        // Process edges. This is at the edge of my understanding and from here I am trying until it works. <-- (Thank you Stack Overflow)
        for (int[] edge : edges) {
            int cell1 = edge[0];
            int cell2 = edge[1];
            int wallR = edge[2];
            int wallC = edge[3];

            if (find(parent, cell1) != find(parent, cell2)) {
                union(parent, cell1, cell2);
                fortress[wallR][wallC] = PATH;

                int cell1r = (cell1 / cellsC) * 2 + 1;
                int cell1c = (cell1 % cellsC) * 2 + 1;
                int cell2r = (cell2 / cellsC) * 2 + 1;
                int cell2c = (cell2 % cellsC) * 2 + 1;
                fortress[cell1r][cell1c] = PATH;
                fortress[cell2r][cell2c] = PATH;
            }
        }
    }

    //end of Nerd Maze algorithm

    private static int find(int[] parent, int x) {
        if (parent[x] != x) {
            parent[x] = find(parent, parent[x]);
        }
        return parent[x];
    }

    private static void union(int[] parent, int x, int y) {
        int rx = find(parent, x);
        int ry = find(parent, y);
        if (rx != ry) {
            parent[rx] = ry;
        }
    }
    //End

    private static void shuffleArray(int[][] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int[] temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }

    public static void placeExit() {//Gotta get out somehow
        while (true) {
            int r = random.nextInt(SIZE - 2) + 1;
            int c = random.nextInt(SIZE - 2) + 1;
            if (fortress[r][c].equals(PATH)) {
                fortress[r][c] = EXIT;
                return;

             }
        }
    }

    public static void placeDork() {
        int attempts = 0;
        final int MAX_RANDOM_ATTEMPTS = 5000;

        while (attempts < MAX_RANDOM_ATTEMPTS) {
            attempts++;
            int r = random.nextInt(SIZE - 4) + 2;
            int c = random.nextInt(SIZE - 4) + 2;

            if (fortress[r][c].equals(PATH)) {
                dorkRow = r;
                dorkCol = c;

                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        int nr = r + dr;
                        int nc = c + dc;
                        if (nr >= 1 && nr < SIZE-1 && nc >= 1 && nc < SIZE-1) {
                            fortress[nr][nc] = PATH;
                        }
                    }
                }
                return;
            }
        }
    }

    public static void placeTrogdor() {
        while (true) {
            int r = random.nextInt(SIZE - 2) + 1;
            int c = random.nextInt(SIZE - 2) + 1;
            if (fortress[r][c].equals(PATH) &&
                    !(r == dorkRow && c == dorkCol) &&
                    !fortress[r][c].equals(EXIT)) {
                trogdorRow = r;
                trogdorCol = c;
                return;
            }
        }
    }


    public static void printFortressGrid() {
        System.out.print("\033[H\033[2J"); //It didn't clear the screen... sad :c
        System.out.flush();

        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (r == dorkRow && c == dorkCol) {
                    System.out.print(DORK);
                } else if (r == trogdorRow && c == trogdorCol) {
                    System.out.print(TROGDOR);
                } else {
                    System.out.print(fortress[r][c]);
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    public static void placeObjects() {
        placeExit();
        placeDork();
        placeTrogdor();
    }

    public static boolean isValidMove(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    public static void moveDork() {
        while (true) {
            System.out.print("Move (WASD): ");
            String input = scanner.nextLine().trim().toUpperCase();

            if (input.length() != 1) {
                System.out.println("Please enter exactly one character (W/A/S/D)");
                continue;
            }
            char move = input.charAt(0);
            int newRow = dorkRow;
            int newCol = dorkCol;

            switch (move) {
                case 'W': newRow--; break;
                case 'S': newRow++; break;
                case 'A': newCol--; break;
                case 'D': newCol++; break;
                default:
                    System.out.println("Invalid direction! Use W, A, S, or D");
                    continue;
            }

            if (!isValidMove(newRow, newCol)) {
                System.out.println("You hit the edge of the fortress!");
                continue;
            }

            String target = fortress[newRow][newCol];

            if (target.equals(WALL)) {
                System.out.println("You can't move through walls!");
                continue;
            }
            dorkRow = newRow;
            dorkCol = newCol;

            if (target.equals(EXIT)) {
                printFortressGrid();
                System.out.println("You reached the exit of the fortress! You win! 🎉");
                gameOver = true;
            }

            return;
        }
    }

    //he moves, as he should
    public static void moveTrogdor() {
        if (trogdorRow == dorkRow && trogdorCol == dorkCol) return; //Yes, you can return void and just return void
        boolean beSmart = (Math.random() < smartChance);
        if (difficulty == 4 && USE_ASTAR_ON_NIGHTMARE) { //Checking if Nightmare difficulty is on
            moveTrogdorWithAStar();//Scary!
        } else if (beSmart) {
            moveTrogdorGreedy();
        } else {
            moveTrogdorRandom();
        }

        if (trogdorRow == dorkRow && trogdorCol == dorkCol) {
            printFortressGrid();
            System.out.println("TROGDOR HAS CAUGHT YOU! YOU ARE FORCED TO LEARN CHESS! ☠️");
            gameOver = true;
        }
    }


    //He moves randomly, like an idiot
    private static void moveTrogdorRandom() {
        int[] dr = {-1, 0, 1, 0};
        int[] dc = {0, 1, 0, -1};

        for (int attempt = 0; attempt < 20; attempt++) {
            int dir = random.nextInt(4);
            int nr = trogdorRow + dr[dir];
            int nc = trogdorCol + dc[dir];
            if (isValidMove(nr, nc) && !fortress[nr][nc].equals(WALL)) {
                trogdorRow = nr;
                trogdorCol = nc;
                return;
            }
        }
    }

    //This is the complex algorithm that I've cannibalized from my high school project to make him move towards the player
    //No, I didn't get this from Highschool I've just taken this from Minecraft and applied it here... Thanks MineCraft
    private static void moveTrogdorGreedy() {
        int[][] dirs = {{-1,0}, {1,0}, {0,-1}, {0,1}};

        java.util.Arrays.sort(dirs, (a, b) -> {
            int da = Math.abs(trogdorRow + a[0] - dorkRow) + Math.abs(trogdorCol + a[1] - dorkCol);
            int db = Math.abs(trogdorRow + b[0] - dorkRow) + Math.abs(trogdorCol + b[1] - dorkCol);
            return Integer.compare(da, db);
        });

        int manhattan = Math.abs(trogdorRow - dorkRow) + Math.abs(trogdorCol - dorkCol);
        boolean close = (manhattan <= AGGRO_DISTANCE); //Check if he will "go agro" and chase the player (Scary)

        for (int i = 0; i < dirs.length; i++) {
            if (!close && i == 0 && Math.random() < 0.18) continue;

            int nr = trogdorRow + dirs[i][0];
            int nc = trogdorCol + dirs[i][1];

            if (isValidMove(nr, nc) && !fortress[nr][nc].equals(WALL)) {
                trogdorRow = nr;
                trogdorCol = nc;
                return;
            }
        }
        moveTrogdorRandom();
    }
    //This is the complex algorithm that I've cannibalized from my high school project to make him move towards the player. I kinda forgot how it works but a few variable name changes and it does!
    private static void moveTrogdorWithAStar() {
        PriorityQueue<Node> open = new PriorityQueue<>((a, b) -> Integer.compare(a.f, b.f));
        boolean[][] visited = new boolean[SIZE][SIZE];

        int initH = Math.abs(trogdorRow - dorkRow) + Math.abs(trogdorCol - dorkCol);
        open.add(new Node(trogdorRow, trogdorCol, 0, initH, null));
        visited[trogdorRow][trogdorCol] = true;

        int[][] dirs = {{-1,0}, {1,0}, {0,-1}, {0,1}};
        Node goal = null;

        while (!open.isEmpty()) {
            Node cur = open.poll();

            if (cur.r == dorkRow && cur.c == dorkCol) {
                goal = cur;
                break;
            }

            for (int[] d : dirs) {
                int nr = cur.r + d[0];
                int nc = cur.c + d[1];
                if (!isValidMove(nr, nc) || fortress[nr][nc].equals(WALL) || visited[nr][nc]) continue;

                visited[nr][nc] = true;
                int ng = cur.g + 1;
                int nh = Math.abs(nr - dorkRow) + Math.abs(nc - dorkCol);
                open.add(new Node(nr, nc, ng, nh, cur));
            }
        }

        if (goal != null && goal.parent != null) {
            Node step = goal;
            while (step.parent != null && step.parent.parent != null) {
                step = step.parent;
            }
            trogdorRow = step.r;
            trogdorCol = step.c;
        }
    }

    public static void beginGamePlay() {//Try to outsource things to other functions if possible to keep my "systems by systems" approach.
        while (!gameOver) {
            printFortressGrid();
            moveDork();

            if (gameOver) break;

            moveTrogdor();

            if (difficulty >= 2 && Math.random() < 0.35) {
                moveTrogdor();
            }

            try {
                Thread.sleep(10 * (int)(Math.random())); //I realize when this is cast to int it just turns into 0. But also I don't really care.
            } catch (InterruptedException ignored) {}
        }
    }
    public static void main(String[] args) { //Try to keep this as short as possible as I make this.
        SIZE = displayIntroGetSIZE();
        generateFortressGrid();
        placeObjects();
        beginGamePlay();

        scanner.close();
        System.out.println("\nGame over. Thanks for playing DorkFortress!");
    }
    static class Node {//I honestly have gotten to the point where I forgot what this does and why it exists but it's probably important
        int r, c, g, h, f;
        Node parent;
        Node(int rr, int cc, int gg, int hh, Node p) {
            r=rr; c=cc; g=gg; h=hh; f=g+h; parent=p;
        }
    }
}