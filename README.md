# 🏰 DorkFortress

A terminal-based Java maze game where you play as the **Dork 🤓** and attempt to escape a fortress while avoiding **Trogdor the Burninator 🐉**.

Originally designed as a simple 2D-array maze project for **CS 121**, DorkFortress grew considerably beyond the original assignment. What started as a randomized grid with a wandering dragon eventually acquired multiple maze-generation algorithms, several difficulty levels, and increasingly aggressive Trogdor AI.

Because apparently a simple maze game was insufficient.

---

## 🎮 Game Overview

You are the **Dork 🤓**, trapped inside a fortress.

Your objective is simple:

> **Reach an exit before Trogdor catches you.**

The fortress consists of:

* 🧱 **Walls** - impassable terrain
* ⬛ **Paths** - traversable terrain
* 🤓 **Dork** - the player
* 🐉 **Trogdor** - the adversary
* 🚪 **Exit** - your way out

The maze is displayed directly in the terminal using Unicode emojis.

### Controls

| Key | Movement |
| --- | -------- |
| `W` | Up       |
| `A` | Left     |
| `S` | Down     |
| `D` | Right    |

Movement is handled one turn at a time. After the Dork moves, Trogdor gets a chance to move as well.

---

# 🧱 Maze Generation

The game supports **three different maze-generation styles**.

When starting the game, the player chooses which generation method to use.

## 0. Classic Random

The original maze-generation method.

Interior cells are randomly assigned as either walls or paths using a probability-based approach. The outer perimeter is always made entirely of walls.

The game then performs additional post-processing to create more open areas and occasional shortcuts.

### Characteristics

* Randomly generated walls and paths
* Can contain dead ends
* Can produce less predictable layouts
* Simple and closest to the original CS 121 assignment
* May occasionally produce awkward or disconnected areas

The probability of generating paths changes slightly at higher difficulty levels.

---

## 1. Perfect Maze

Uses a **recursive backtracking maze-generation algorithm**.

The algorithm begins with a grid full of walls and recursively carves passages through it.

The basic process is:

1. Select a starting cell.
2. Mark the cell as a path.
3. Randomize the four possible directions.
4. Select an unvisited neighboring cell.
5. Remove the wall between the current cell and the neighboring cell.
6. Continue recursively.
7. Backtrack when no unvisited neighboring cells remain.

This produces a maze where the generated passages are connected and generally form a traditional "perfect maze" structure.

---

## 2. Kruskal's Algorithm

The third option uses **randomized Kruskal's algorithm** to generate the maze.

The maze is initially filled with walls. The interior is treated as a collection of cells connected by potential edges.

A **Union-Find / Disjoint Set** data structure tracks which cells already belong to the same connected region.

The algorithm:

1. Creates the individual maze cells.
2. Creates possible connections between neighboring cells.
3. Randomizes those connections.
4. Processes each connection.
5. Uses Union-Find to determine whether connecting two cells would create a cycle.
6. If they belong to different sets, the wall between them is removed.
7. The two sets are then merged.

The resulting maze can contain multiple routes and loops.

This makes it less like a single winding corridor and more like an actual network of possible paths.

---

# 🐉 Trogdor AI

Trogdor started as a completely random enemy.

That was apparently too boring.

The game now contains **three different movement behaviors**, depending on difficulty.

## Random Movement

Trogdor randomly chooses one of the four cardinal directions:

* Up
* Down
* Left
* Right

If the selected location is invalid or is a wall, another direction is attempted.

---

## Greedy Movement

At higher difficulties, Trogdor can use a **greedy movement strategy**.

The possible directions are evaluated according to their **Manhattan distance** from the Dork.

Manhattan distance is calculated as:

```text
|currentRow - targetRow| + |currentColumn - targetColumn|
```

The direction that produces the smallest distance to the Dork is considered first.

Trogdor also has an **aggression distance**. When the Dork is sufficiently close, Trogdor becomes more likely to actively pursue them.

This does not perform full pathfinding. Trogdor only evaluates his immediate neighboring cells, meaning walls can still interfere with his pursuit.

---

## A* Pathfinding

Nightmare difficulty uses **A* pathfinding**.

A* searches the maze for a path between Trogdor and the Dork.

Each node uses:

```text
f(n) = g(n) + h(n)
```

Where:

* `g(n)` = distance traveled from Trogdor
* `h(n)` = estimated distance to the Dork
* `f(n)` = total estimated cost

The heuristic used by the game is Manhattan distance.

A `PriorityQueue` is used to process nodes according to their estimated total cost.

Once a path to the Dork is found, the game reconstructs the path through the nodes' parent references and moves Trogdor one step along it.

In other words:

**Nightmare Trogdor knows where you are.**

---

# ⚔️ Difficulty Levels

There are five difficulty settings.

| Level | Name      | Trogdor Behavior                                      |
| ----: | --------- | ----------------------------------------------------- |
|   `0` | Easy      | Mostly intelligent movement, with frequent randomness |
|   `1` | Medium    | Balanced random and intelligent movement              |
|   `2` | Hard      | More likely to chase the Dork                         |
|   `3` | Extreme   | Usually attempts to pursue the Dork                   |
|   `4` | Nightmare | Uses A* pathfinding                                   |

Higher difficulties can also cause Trogdor to receive an additional movement opportunity during a turn.

### Difficulty configuration

The game's `smartChance` controls how often Trogdor uses greedy movement instead of random movement.

```text
Easy       = 80% smart movement
Medium     = 60% smart movement
Hard       = 30% smart movement
Extreme    = 10% smart movement
Nightmare  = A* pathfinding
```

The naming here is admittedly somewhat backwards because the variable represents the chance of using the "smart" behavior while the lower percentage means Trogdor is more consistently pursuing the player through other logic. Human-readable difficulty systems remain one of civilization's great unsolved problems.

---

# 🗺️ Fortress Rules

Regardless of maze style, the fortress maintains an outer boundary of walls.

The perimeter consists of:

```text
🧱 🧱 🧱 🧱 🧱 🧱 🧱
🧱 ⬛ ⬛ ⬛ ⬛ ⬛ 🧱
🧱 ⬛ ⬛ 🤓 ⬛ ⬛ 🧱
🧱 ⬛ ⬛ ⬛ 🐉 ⬛ 🧱
🧱 ⬛ ⬛ ⬛ 🚪 ⬛ 🧱
🧱 🧱 🧱 🧱 🧱 🧱 🧱
```

The player and Trogdor are **not permanently written into the maze array**.

Instead, their row and column coordinates are stored separately.

When the fortress is printed, the program checks whether a particular coordinate contains the Dork or Trogdor and displays the appropriate emoji.

This means the underlying maze remains intact while the characters move around it.

---

# 🚪 Object Placement

After the maze is generated, the game places its objects in the following order:

```text
placeExit()
      ↓
placeDork()
      ↓
placeTrogdor()
```

## Exit

The exit is randomly placed on an interior path cell.

## Dork

The Dork is randomly placed on an available path cell.

The surrounding area is also opened up slightly to help prevent the player from spawning in an immediately inaccessible location.

## Trogdor

Trogdor is placed on another path cell that is not occupied by the Dork.

---

# 🕹️ Game Loop

The main game loop follows this general structure:

```text
Generate fortress
       ↓
Place exit
       ↓
Place Dork
       ↓
Place Trogdor
       ↓
Print fortress
       ↓
Player moves
       ↓
Check victory
       ↓
Trogdor moves
       ↓
Check defeat
       ↓
Repeat
```

The game continues until either:

### 🏆 Victory

The Dork moves onto the exit.

The maze is printed and a victory message is displayed.

### ☠️ Defeat

Trogdor moves onto the Dork's position.

The game ends and a game-over message is displayed.

---

# 💻 Technical Details

## Language

**Java**

## Main File

```text
DorkFortress.java
```

## Main Data Structure

The fortress itself is represented using:

```java
String[][] fortress
```

The dimensions are determined by user input.

The program currently allows fortress sizes from:

```text
7 × 7
```

through:

```text
25 × 25
```

with **11 × 11** recommended for normal play.

---

# 📦 Imports

The program uses:

```java
import java.util.Scanner;
import java.util.Random;
import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.Collections;
```

These provide:

* `Scanner` - keyboard input
* `Random` - randomized maze generation and movement
* `PriorityQueue` - A* pathfinding
* `ArrayList` - storage for Kruskal's algorithm edges
* `Collections` - randomization of the edge list

---

# 🧩 Program Structure

The main game is divided into multiple methods.

### `displayIntroGetSIZE()`

Displays the title, instructions, difficulty selection, maze-generation selection, and fortress-size selection.

Returns the selected maze size.

### `generateFortressGrid()`

Creates the fortress and selects the requested maze-generation method.

### `generatePerfectMaze()`

Creates a maze using recursive backtracking.

### `carveMaze()`

Recursively carves passages through the perfect maze.

### `generateKruskalMaze()`

Creates a maze using randomized Kruskal's algorithm.

### `find()`

Finds the representative element of a Union-Find set.

### `union()`

Combines two Union-Find sets.

### `shuffleArray()`

Randomizes an array of movement directions.

### `placeExit()`

Places the single exit.

### `placeDork()`

Places the player and records their coordinates.

### `placeTrogdor()`

Places Trogdor and records his coordinates.

### `placeObjects()`

Calls the object-placement methods in the correct order.

### `printFortressGrid()`

Prints the current fortress while dynamically displaying Dork and Trogdor.

### `isValidMove()`

Checks whether a coordinate is inside the fortress.

### `moveDork()`

Handles player input and movement.

### `moveTrogdor()`

Determines which Trogdor movement system should be used based on difficulty.

### `moveTrogdorRandom()`

Moves Trogdor randomly.

### `moveTrogdorGreedy()`

Moves Trogdor toward the Dork using local Manhattan-distance comparisons.

### `moveTrogdorWithAStar()`

Uses A* pathfinding to determine a route toward the Dork.

### `beginGamePlay()`

Controls the main gameplay loop.

### `main()`

Starts the game by initializing the fortress, placing objects, and beginning gameplay.

---

# 🧠 Concepts Demonstrated

Although the original assignment focuses on introductory Java concepts, the completed project incorporates several additional programming concepts:

* 2D arrays
* Strings
* Static variables
* Constants
* Methods
* Loops
* Conditional statements
* `switch` statements
* User input
* Random number generation
* Input validation
* Coordinate-based movement
* Recursion
* Array manipulation
* `ArrayList`
* `Collections.shuffle()`
* `PriorityQueue`
* Object-oriented node structures
* Union-Find / Disjoint Set data structures
* A* pathfinding
* Manhattan-distance heuristics
* Procedural maze generation

---

# 🐧 Running on Linux

The game is designed to run in a terminal and can be compiled using the Java compiler.

From the directory containing `DorkFortress.java`:

```bash
javac DorkFortress.java
```

Then run:

```bash
java DorkFortress
```

The game uses Unicode emoji characters, so a terminal with Unicode support is recommended.

The screen-clearing sequence used by the program relies on ANSI terminal escape codes:

```text
\033[H\033[2J
```

Terminal behavior may therefore vary depending on the terminal emulator being used.

---

# 📚 Original Assignment vs. Final Implementation

The original CS 121 assignment called for a relatively simple maze game featuring:

* A 2D `String` array
* Random interior walls
* A wall perimeter
* A randomly placed exit
* A player
* Trogdor
* WASD movement
* Random Trogdor movement
* Victory and defeat conditions

The final implementation retains those core requirements but adds several systems beyond the original specification.

### Additional features

* Five difficulty levels
* Three maze-generation styles
* Recursive-backtracking maze generation
* Randomized Kruskal maze generation
* Union-Find data structure
* Greedy Trogdor AI
* Aggression-distance behavior
* A* pathfinding
* PriorityQueue-based pathfinding
* Additional Trogdor movement on harder difficulties
* Maze post-processing for additional paths and variations
* Terminal screen clearing
* Input validation
* Adjustable fortress size

The project therefore evolved from a basic random-grid exercise into a small procedural maze and pathfinding experiment.

---

# 🎯 Objective

Survive.

Find the door.

Avoid the dragon.

And, most importantly:

**Do not get forced to learn the Chess London System.**

Good luck, Dork. 🤓
