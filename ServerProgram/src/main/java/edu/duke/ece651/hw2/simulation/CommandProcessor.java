package edu.duke.ece651.hw2.simulation;

/**
 * Processes commands for the simulation.
 */
public class CommandProcessor {
    private Simulation simulation;

    /**
     * Constructs a CommandProcessor with the given simulation.
     *
     * @param simulation the simulation instance.
     */
    public CommandProcessor(Simulation simulation) {
        this.simulation = simulation;
    }

    /**
     * Parses a command string and returns the corresponding Command object.
     * <p>
     * Supported command formats:
     * <ul>
     *   <li>request 'ITEM' from 'BUILDING'</li>
     *   <li>step N</li>
     *   <li>finish</li>
     *   <li>verbose N</li>
     * </ul>
     *
     * @param commandStr the command string.
     * @return the parsed Command.
     * @throws SimulationException if the command is invalid.
     */
    public Command parseCommand(String commandStr) throws SimulationException {
        commandStr = commandStr.trim();
        if (commandStr.isEmpty()) {
            throw new SimulationException("Empty command");
        }
        if (commandStr.startsWith("request")) {
            // Expected format: request 'ITEM' from 'BUILDING'
            int firstQuote = commandStr.indexOf('\'');
            int secondQuote = commandStr.indexOf('\'', firstQuote + 1);
            int fromIndex = commandStr.indexOf("from", secondQuote);
            int thirdQuote = commandStr.indexOf('\'', fromIndex);
            int fourthQuote = commandStr.indexOf('\'', thirdQuote + 1);
            if (firstQuote < 0 || secondQuote < 0 || fromIndex < 0 || thirdQuote < 0 || fourthQuote < 0) {
                throw new SimulationException("Invalid request command format");
            }
            String item = commandStr.substring(firstQuote + 1, secondQuote);
            String building = commandStr.substring(thirdQuote + 1, fourthQuote);
            return new RequestCommand(item, building);
        } else if (commandStr.startsWith("step")) {
            // Expected format: step N
            String[] parts = commandStr.split("\\s+");
            if (parts.length != 2) {
                throw new SimulationException("Invalid step command format");
            }
            try {
                int steps = Integer.parseInt(parts[1]);
                if (steps < 1) {
                    throw new SimulationException("Step value must be >= 1");
                }
                return new StepCommand(steps);
            } catch (NumberFormatException e) {
                throw new SimulationException("Invalid number for step command");
            }
        } else if (commandStr.equals("finish")) {
            return new FinishCommand();
        } else if (commandStr.startsWith("verbose")) {
            // Expected format: verbose N
            String[] parts = commandStr.split("\\s+");
            if (parts.length != 2) {
                throw new SimulationException("Invalid verbose command format");
            }
            try {
                int level = Integer.parseInt(parts[1]);
                return new VerboseCommand(level);
            } catch (NumberFormatException e) {
                throw new SimulationException("Invalid number for verbose command");
            }
        } else if (commandStr.startsWith("connect")) {
            // 解析格式：connect 'SOURCE_NAME' to 'DEST_NAME'
            int firstQuote = commandStr.indexOf('\'');
            int secondQuote = commandStr.indexOf('\'', firstQuote + 1);
            int toIndex = commandStr.indexOf("to", secondQuote);
            int thirdQuote = commandStr.indexOf('\'', toIndex);
            int fourthQuote = commandStr.indexOf('\'', thirdQuote + 1);
            if (firstQuote < 0 || secondQuote < 0 || toIndex < 0 || thirdQuote < 0 || fourthQuote < 0) {
                throw new SimulationException("Invalid connect command format");
            }
            String sourceName = commandStr.substring(firstQuote + 1, secondQuote);
            String destName = commandStr.substring(thirdQuote + 1, fourthQuote);
            return new ConnectCommand(sourceName, destName);
        } else if (commandStr.startsWith("printMap")) {
            return new PrintCommand();
        } else if (commandStr.startsWith("srm")) {
            int firstQuote = commandStr.indexOf('\'');
            int secondQuote = commandStr.indexOf('\'', firstQuote + 1);
            int toIndex = commandStr.indexOf("to", secondQuote);
            int thirdQuote = commandStr.indexOf('\'', toIndex);
            int fourthQuote = commandStr.indexOf('\'', thirdQuote + 1);
            if (firstQuote < 0 || secondQuote < 0 || toIndex < 0 || thirdQuote < 0 || fourthQuote < 0) {
                throw new SimulationException("Invalid connect command format");
            }
            String sourceName = commandStr.substring(firstQuote + 1, secondQuote);
            String destName = commandStr.substring(thirdQuote + 1, fourthQuote);
            return new SimpleRemoveCommand(sourceName, destName);
        } else if (commandStr.startsWith("crm")) {
            int firstQuote = commandStr.indexOf('\'');
            int secondQuote = commandStr.indexOf('\'', firstQuote + 1);
            int toIndex = commandStr.indexOf("to", secondQuote);
            int thirdQuote = commandStr.indexOf('\'', toIndex);
            int fourthQuote = commandStr.indexOf('\'', thirdQuote + 1);
            if (firstQuote < 0 || secondQuote < 0 || toIndex < 0 || thirdQuote < 0 || fourthQuote < 0) {
                throw new SimulationException("Invalid connect command format");
            }
            String sourceName = commandStr.substring(firstQuote + 1, secondQuote);
            String destName = commandStr.substring(thirdQuote + 1, fourthQuote);
            return new ComplexRemoveCommand(sourceName, destName);
        } else if (commandStr.equals("pause")) {
            return new PauseCommand();
        } else if (commandStr.startsWith("rate")) {
            // Expected format: rate N, where N is a non-negative integer
            String[] parts = commandStr.split("\\s+");
            if (parts.length != 2) {
                throw new SimulationException("Invalid rate command format");
            }
            try {
                int rate = Integer.parseInt(parts[1]);
                if (rate < 0) {
                    throw new SimulationException("Rate must be a non-negative integer");
                }
                return new RateCommand(rate);
            } catch (NumberFormatException e) {
                throw new SimulationException("Invalid number for rate command");
            }
        } else if (commandStr.startsWith("build")) {
            String[] parts = commandStr.split("\\s+", 4);
            if (parts.length != 4) {
                throw new SimulationException("Invalid build command format");
            }
            try {
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                String typeName = parts[3];
                return new BuildCommand(x, y, typeName);
            } catch (NumberFormatException e) {
                throw new SimulationException("Invalid coordinates for build command");
            }
        } else if (commandStr.startsWith("remove")) {
            String[] parts = commandStr.split("\\s+", 2);
            if (parts.length != 2) {
                throw new SimulationException("Invalid remove command format");
            }
            String buildingName = parts[1];
            return new RemoveCommand(buildingName);
        } else {
            throw new SimulationException("Unknown command");
        }
    }

    /**
     * Processes a command string by parsing and executing it.
     *
     * @param commandStr the command string.
     */
    public void processCommand(String commandStr) {
        try {
            Command cmd = parseCommand(commandStr);
            cmd.execute(simulation);
        } catch (SimulationException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}