# Simulation Project

## Overview

This project is a simulation engine implemented in Java. It processes a simulation configuration specified in a JSON file that defines recipes, building types, and buildings. After successful parsing and validation, the simulation enters an interactive command loop where the user can input commands to request items, advance simulation time, change verbosity, or finish the simulation.

## Features

- **JSON Parsing:** Uses the [org.json](https://github.com/stleary/JSON-java) library to parse configuration files.

- **Input Validation:** Comprehensive validation of recipes, building types, and buildings ensuring that required fields exist and cross-references are valid.

- Interactive Commands:

   Supports four commands:

  - `request 'ITEM' from 'BUILDING'`
  - `step N` (N must be an integer ≥ 1)
  - `finish`
  - `verbose N`
  - `rate N` (N must be an integer ≥ 0)
  - `pause`

- **Modular Design:** Clear separation of concerns across multiple classes with dedicated responsibilities.

- **Custom Exceptions:** Uses a custom `SimulationException` to signal validation and processing errors.

- **Extensive Testing:** Includes a comprehensive suite of JUnit tests covering both normal and edge cases.

## Project Structure

The source code is organized in the `simulation` package:

- Domain Classes:
  - `Recipe`, `BuildingType`, `Building` (abstract), `FactoryBuilding`, and `MineBuilding`
  - `Request` represents a production request.
- Core Engine:
  - `Simulation` (interface) and its implementation `BasicSimulation`
  - `SimulationParser` for parsing and validating JSON configuration.
  - `CommandProcessor` and related command classes (`RequestCommand`, `StepCommand`, `FinishCommand`, `VerboseCommand`) for processing user commands.
- Exception Handling:
  - `SimulationException` to signal errors during simulation setup or processing.
- Entry Point:
  - `Main` class to start the simulation and enter the interactive loop.

## Requirements

- **Java:** JDK 8 or later.
- **JSON Library:** [org.json](https://github.com/stleary/JSON-java)
- **JUnit 5:** For unit testing.

## How to Include the JSON Library

### Gradle

Add the following to your build.gradle file:

```groovy
dependencies {
    implementation 'org.json:json:20220320'
}
```



### Direct Download

Download the JAR from the Maven Central Repository and include it in your project’s classpath.

## Building the Project

### Using Gradle

Build the project with:

```shell
gradle build
```



## Running the Simulation

To run the simulation, execute the Main class and pass the JSON configuration file as an argument:

```shell
./build/install/ProductSimulation/bin/ProductSimulation src/test/resources/inputs/doors1.json
```



You will see a prompt (e.g., 0>) where you can type your commands.

## Running Tests

Run tests using:

```shell
gradle test
```

# Play the Game

## Server:

> Open ServerProgram/

Use the following command to run the server with testing input  in`phase2_1.json` in the terminal:

```bash
./gradlew NewServeMain --args='src/test/resources/inputs/phase3_demo.json'
```

For real-time play:
```bash
./gradlew NewServerMain --args='src/test/resources/inputs/phase3_demo.json real-time'
```

> Old version: 
>
> ```bash
> ./gradlew ServerMain --args='src/test/resources/inputs/phase3_demo.json'
> ./gradlew ServerMain --args='src/test/resources/inputs/phase3_demo.json real-time'
> ```

It should look like this: (The original map read from json file should be printed in the beginning.)

```bash
> Task :ServerMain
Map:
      1   2   3   4   5   6   7   8   9  10  11  12  13  14  15  16  17 
    -------------------------------------------------------------------------------------
  1| D |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
    -------------------------------------------------------------------------------------
  2|   |   |   | W |   |   |   |   |   |   |   |   |   |   |   |   |   |
    -------------------------------------------------------------------------------------
  3|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
    -------------------------------------------------------------------------------------
  4|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
    -------------------------------------------------------------------------------------
  5|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
    -------------------------------------------------------------------------------------
  6|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
    -------------------------------------------------------------------------------------
  7|   |   |   |   |   |   |   |   |   |   |   | Hi|   |   |   |   |   |
    -------------------------------------------------------------------------------------
  8|   |   |   |   |   |   |   |   |   |   |   | l |   |   |   |   |   |
    -------------------------------------------------------------------------------------
  9|   |   |   |   |   |   |   |   |   |   |   | l |   |   |   |   |   |
    -------------------------------------------------------------------------------------
 10|   |   |   |   |   |   |   |   |   |   |   | l |   |   |   |   |   |
    -------------------------------------------------------------------------------------
 11|   |   |   |   |   |   |   |   |   |   |   | l |   |   |   |   |   |
    -------------------------------------------------------------------------------------
 12|   |   |   |   |   |   |   | F | - | - | - | J |   |   |   |   |   |
    -------------------------------------------------------------------------------------
 13|   |   |   |   |   |   | Ha| L | - | - | - | - | 7 |   |   |   |   |
    -------------------------------------------------------------------------------------
 14|   | S1|   |   |   |   |   |   |   |   |   |   | L | - | - | - | 7 |
    -------------------------------------------------------------------------------------
 15|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   | l |
    -------------------------------------------------------------------------------------
 16|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   | l |
    -------------------------------------------------------------------------------------
 17|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   | l |
    -------------------------------------------------------------------------------------
 18|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   | l |
    -------------------------------------------------------------------------------------
 19|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   | M |
    -------------------------------------------------------------------------------------
     1   2   3   4   5   6   7   8   9  10  11  12  13  14  15  16  17 

Server started on port 3000.
<=========----> 75% EXECUTING [1m 16s]
> :ServerMain
```

- Quit server program by entering `C + c`.
- Pause: pause game by `pause`
- Change the speed of global time to N: `rate N`
  - N must be an non-negative integer.

## Client:

> Open ClientProgram/

Go to the root directory of the client project:

```bash
cd <whatever path>/651-simulationclient
```

Install main in the root directory of the project:
```bash
npm install   # only needed for the first time
```

Run npm in the project:
```bash
npm run dev
```

Open the url to client webpage:
```bash
#something like http://127.0.0.1:5173/
```

### Client Terminal:

Run Client in the terminal:

```bash
./gradlew ClientMain
```

It should look like this (The original map and relevant info read from the json file should be printed in the beginning):

```bash
> Task :ClientMain
======== Simulation State ========
Current Time Step: 0
Verbosity Level  : 0
Buildings        : Hi, D, W, Ha, M, S1
Recipes          : door, hinge, metal, handle, wood
Map Text         :
Map:
      1   2   3   4   5   6   7   8   9  10  11  12  13  14  15  16  17 
    -------------------------------------------------------------------------------------
  1| D |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
    -------------------------------------------------------------------------------------
  2|   |   |   | W |   |   |   |   |   |   |   |   |   |   |   |   |   |
    -------------------------------------------------------------------------------------
  3|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
    -------------------------------------------------------------------------------------
  4|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
    -------------------------------------------------------------------------------------
  5|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
    -------------------------------------------------------------------------------------
  6|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
    -------------------------------------------------------------------------------------
  7|   |   |   |   |   |   |   |   |   |   |   | Hi|   |   |   |   |   |
    -------------------------------------------------------------------------------------
  8|   |   |   |   |   |   |   |   |   |   |   | l |   |   |   |   |   |
    -------------------------------------------------------------------------------------
  9|   |   |   |   |   |   |   |   |   |   |   | l |   |   |   |   |   |
    -------------------------------------------------------------------------------------
 10|   |   |   |   |   |   |   |   |   |   |   | l |   |   |   |   |   |
    -------------------------------------------------------------------------------------
 11|   |   |   |   |   |   |   |   |   |   |   | l |   |   |   |   |   |
    -------------------------------------------------------------------------------------
 12|   |   |   |   |   |   |   | F | - | - | - | J |   |   |   |   |   |
    -------------------------------------------------------------------------------------
 13|   |   |   |   |   |   | Ha| L | - | - | - | - | 7 |   |   |   |   |
    -------------------------------------------------------------------------------------
 14|   | S1|   |   |   |   |   |   |   |   |   |   | L | - | - | - | 7 |
    -------------------------------------------------------------------------------------
 15|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   | l |
    -------------------------------------------------------------------------------------
 16|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   | l |
    -------------------------------------------------------------------------------------
 17|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   | l |
    -------------------------------------------------------------------------------------
 18|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   | l |
    -------------------------------------------------------------------------------------
 19|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   | M |
    -------------------------------------------------------------------------------------
     1   2   3   4   5   6   7   8   9  10  11  12  13  14  15  16  17 

==================================

Please enter Instruction (`exit` for quit): 
<=========----> 75% EXECUTING [10s]
> :ClientMain
```

#### Commands:

1. Request Item: `request '<item>' from '<building>'`
2. Connect Buildings: `connect '<building 1>' to '<building 2>'`
3. Step: `step N`
4. Set verbose level: `verbose i`
5. Finish: `finish`
6. Print the map: `printMap`
7. Exit: `exit`

### Client Webpage:

The initial status should be like this:

<img src="images/image-20250510215438122.png" alt="image-20250510215438122" style="zoom:50%;" />

#### 1. Request Item:

- Choose <Building> and <Item> in the list;

- You could see each building’s recipes by moving the cursor onto it:

  <img src="images/image-20250412152931306.png" alt="image-20250412152931306" style="zoom:50%;" />

  <img src="images/image-20250412155043693.png" alt="image-20250412155043693" style="zoom:50%;" />

- You can also click on the building’s image to choose the item to request.

  <img src="images/image-20250510215539966.png" alt="image-20250510215539966" style="zoom: 50%;" />

### 2. Connect Buildings:

- Click on Select Buildings from Connect Building’s command box.

  <img src="images/image-20250510220051336.png" alt="image-20250510220051336" style="zoom:50%;" />

- Choose two buildings (in the order of from … to …) to connect, they should have green shadow.

  <img src="images/image-20250510215824893.png" alt="image-20250510215824893" style="zoom:50%;" />

  <img src="images/image-20250510215936761.png" alt="image-20250510215936761" style="zoom:50%;" />

- Click on “Connect”, it should build a directed road from Ha to W1:

  <img src="images/image-20250510220009405.png" alt="image-20250510220009405" style="zoom:50%;" />

  <img src="images/image-20250510220526411.png" alt="image-20250510220526411" style="zoom:50%;" />

- Click on “Cancel Selection” to exit selecting mode.

### 3.Remove Connection:

- Click on Remove Connection from Connect Building’s command box.

- Choose two buildings (in the order of from … to …) to remove connection, they should have green shadow.

  <img src="images/image-20250510220207179.png" alt="image-20250510220207179" style="zoom:50%;" />

- Choose whether to use simple removal or complex removal.

  - Simple removal: Remove all roads and rebuild the road network for all remaining connections. This will give a valid answer, but may drastically change the road network.

  - The result should look like this in such occasion:

    <img src="images/image-20250510220309437.png" alt="image-20250510220309437" style="zoom:50%;" />

    <img src="images/image-20250510220504590.png" alt="image-20250510220504590" style="zoom:50%;" />

  - Complex removal: Remove only the roads that are no longer needed. Note that you can do this by computing the shortest path in the road network for all remaining source/destination pairs. As you do this, you can track which roads are part of some shortest path. Any road which is not “marked” during this algorithm can then be deleted.

### 4. Build Building:

- Click on an empty block to build a building of an existing type (to make a copy of the existing building type)

  <img src="images/image-20250510220643916.png" alt="image-20250510220643916" style="zoom:50%;" />

  <img src="images/image-20250510220731581.png" alt="image-20250510220731581" style="zoom:50%;" />

  <img src="images/image-20250510220711820.png" alt="image-20250510220711820" style="zoom:50%;" />

- The new building should be a copy of the existing building of the same type, except has difference in name. In this occasion, both Door_Factory_1 and Door_Factory_2 are replica of Fdoor. <img src="images/image-20250510220856516.png" alt="image-20250510220856516" style="zoom: 50%;" />

### 5. Remove Building:

- Click on an existing building to remove both this building and all connections from/to it.

  <img src="images/image-20250510221155277.png" alt="image-20250510221155277" style="zoom:50%;" />

- A storage building can only be removed when it has no item in stock.

### 7. Step:

- Choose step number N:

  <img src="images/image-20250412155107829.png" alt="image-20250412155107829" style="zoom:50%;" />

### 8. Finish:

- This causes the simulation to run until all user-made requests are completed. 

### Real-time mode:

The global timer will run automatically and result in Time Step changing continuously.

All functions above are available.

<img src="images/image-20250510221519041.png" alt="image-20250510221519041" style="zoom:50%;" />

### 9. Time Rate:

- Set the rate of global time in **real-time** mode.

### 10. Pause:

- Pause the global timer in **real-time** mode.

### Webpage and Terminal Consistency:

1. In the beginning:

   ```bash
   > Task :ClientMain
   ======== Simulation State ========
   Current Time Step: 0
   Verbosity Level  : 0
   Buildings        : Hi, D, W, Ha, M, S1
   Recipes          : door, hinge, metal, handle, wood
   Map Text         :
   Map:
         1   2   3   4   5   6   7   8   9  10  11  12  13  14  15  16  17 
       -------------------------------------------------------------------------------------
     1| D |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
       -------------------------------------------------------------------------------------
     2|   |   |   | W |   |   |   |   |   |   |   |   |   |   |   |   |   |
       -------------------------------------------------------------------------------------
     3|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
       -------------------------------------------------------------------------------------
     4|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
       -------------------------------------------------------------------------------------
     5|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
       -------------------------------------------------------------------------------------
     6|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
       -------------------------------------------------------------------------------------
     7|   |   |   |   |   |   |   |   |   |   |   | Hi|   |   |   |   |   |
       -------------------------------------------------------------------------------------
     8|   |   |   |   |   |   |   |   |   |   |   | l |   |   |   |   |   |
       -------------------------------------------------------------------------------------
     9|   |   |   |   |   |   |   |   |   |   |   | l |   |   |   |   |   |
       -------------------------------------------------------------------------------------
    10|   |   |   |   |   |   |   |   |   |   |   | l |   |   |   |   |   |
       -------------------------------------------------------------------------------------
    11|   |   |   |   |   |   |   |   |   |   |   | l |   |   |   |   |   |
       -------------------------------------------------------------------------------------
    12|   |   |   |   |   |   |   | F | - | - | - | J |   |   |   |   |   |
       -------------------------------------------------------------------------------------
    13|   |   |   |   |   |   | Ha| L | - | - | - | - | 7 |   |   |   |   |
       -------------------------------------------------------------------------------------
    14|   | S1|   |   |   |   |   |   |   |   |   |   | L | - | - | - | 7 |
       -------------------------------------------------------------------------------------
    15|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   | l |
       -------------------------------------------------------------------------------------
    16|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   | l |
       -------------------------------------------------------------------------------------
    17|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   | l |
       -------------------------------------------------------------------------------------
    18|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   | l |
       -------------------------------------------------------------------------------------
    19|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   | M |
       -------------------------------------------------------------------------------------
        1   2   3   4   5   6   7   8   9  10  11  12  13  14  15  16  17 
   
   ==================================
   ```

   <img src="images/image-20250412160851403.png" alt="image-20250412160851403" style="zoom:50%;" />

2. `connect 'W' to 'D'` in the terminal:

   ```bash
   Current Time Step: 0
   Verbosity Level  : 0
   Buildings        : Hi, D, W, Ha, M, S1
   Recipes          : door, hinge, metal, handle, wood
   Map Text         :
   Map:
         1   2   3   4   5   6   7   8   9  10  11  12  13  14  15  16  17 
       -------------------------------------------------------------------------------------
     1| D | - | - | 7 |   |   |   |   |   |   |   |   |   |   |   |   |   |
       -------------------------------------------------------------------------------------
     2|   |   |   | W |   |   |   |   |   |   |   |   |   |   |   |   |   |
       -------------------------------------------------------------------------------------
     3|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
       -------------------------------------------------------------------------------------
     4|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
       -------------------------------------------------------------------------------------
     5|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
       -------------------------------------------------------------------------------------
     6|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
       -------------------------------------------------------------------------------------
     7|   |   |   |   |   |   |   |   |   |   |   | Hi|   |   |   |   |   |
       -------------------------------------------------------------------------------------
     8|   |   |   |   |   |   |   |   |   |   |   | l |   |   |   |   |   |
       -------------------------------------------------------------------------------------
     9|   |   |   |   |   |   |   |   |   |   |   | l |   |   |   |   |   |
       -------------------------------------------------------------------------------------
    10|   |   |   |   |   |   |   |   |   |   |   | l |   |   |   |   |   |
       -------------------------------------------------------------------------------------
    11|   |   |   |   |   |   |   |   |   |   |   | l |   |   |   |   |   |
       -------------------------------------------------------------------------------------
    12|   |   |   |   |   |   |   | F | - | - | - | J |   |   |   |   |   |
       -------------------------------------------------------------------------------------
    13|   |   |   |   |   |   | Ha| L | - | - | - | - | 7 |   |   |   |   |
       -------------------------------------------------------------------------------------
    14|   | S1|   |   |   |   |   |   |   |   |   |   | L | - | - | - | 7 |
       -------------------------------------------------------------------------------------
    15|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   | l |
       -------------------------------------------------------------------------------------
    16|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   | l |
       -------------------------------------------------------------------------------------
    17|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   | l |
       -------------------------------------------------------------------------------------
    18|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   | l |
       -------------------------------------------------------------------------------------
    19|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   | M |
       -------------------------------------------------------------------------------------
        1   2   3   4   5   6   7   8   9  10  11  12  13  14  15  16  17 
   
   ==================================
   ```

   <img src="images/image-20250412161101021.png" alt="image-20250412161101021" style="zoom:50%;" />

3. Connect <S1> to <W> in the webpage:

   <img src="images/image-20250412161222485.png" alt="image-20250412161222485" style="zoom:50%;" />

   printMap in the terminal:

   ```bash
   <<=====<=========----> 75% EXECUTING [8m 51s======== Simulation State ========
   Current Time Step: 0
   Verbosity Level  : 0
   Buildings        : Hi, D, W, Ha, M, S1
   Recipes          : door, hinge, metal, handle, wood
   Map Text         :
   Map:
         1   2   3   4   5   6   7   8   9  10  11  12  13  14  15  16  17 
       -------------------------------------------------------------------------------------
     1| D | - | - | 7 |   |   |   |   |   |   |   |   |   |   |   |   |   |
       -------------------------------------------------------------------------------------
     2|   |   |   | W |   |   |   |   |   |   |   |   |   |   |   |   |   |
       -------------------------------------------------------------------------------------
     3|   |   | F | - |   |   |   |   |   |   |   |   |   |   |   |   |   |
       -------------------------------------------------------------------------------------
     4|   | F | J |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
       -------------------------------------------------------------------------------------
     5|   | l |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
       -------------------------------------------------------------------------------------
     6|   | l |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |
       -------------------------------------------------------------------------------------
     7|   | l |   |   |   |   |   |   |   |   |   | Hi|   |   |   |   |   |
       -------------------------------------------------------------------------------------
     8|   | l |   |   |   |   |   |   |   |   |   | l |   |   |   |   |   |
       -------------------------------------------------------------------------------------
     9|   | l |   |   |   |   |   |   |   |   |   | l |   |   |   |   |   |
       -------------------------------------------------------------------------------------
    10|   | l |   |   |   |   |   |   |   |   |   | l |   |   |   |   |   |
       -------------------------------------------------------------------------------------
    11|   | l |   |   |   |   |   |   |   |   |   | l |   |   |   |   |   |
       -------------------------------------------------------------------------------------
    12|   | l |   |   |   |   |   | F | - | - | - | J |   |   |   |   |   |
       -------------------------------------------------------------------------------------
    13|   | l |   |   |   |   | Ha| L | - | - | - | - | 7 |   |   |   |   |
       -------------------------------------------------------------------------------------
    14|   | S1|   |   |   |   |   |   |   |   |   |   | L | - | - | - | 7 |
       -------------------------------------------------------------------------------------
    15|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   | l |
       -------------------------------------------------------------------------------------
    16|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   | l |
       -------------------------------------------------------------------------------------
    17|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   | l |
       -------------------------------------------------------------------------------------
    18|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   | l |
       -------------------------------------------------------------------------------------
    19|   |   |   |   |   |   |   |   |   |   |   |   |   |   |   |   | M |
       -------------------------------------------------------------------------------------
        1   2   3   4   5   6   7   8   9  10  11  12  13  14  15  16  17 
   
   ==================================
   ```

   



