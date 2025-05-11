# React Simulation Client

This is a React + TypeScript client for interacting with a simulation server. The application supports the following commands using POST requests:
- **Request Item**: Sends a command in the format `"request {Item} from {Building}"`
- **Connect Buildings**: Sends a command in the format `"connect {From} to {To}"`
- **Step**: Sends a command in the format `"step {N}"` where N is the number of steps.
- **Finish**: Sends the command `"finish"`

Additionally, the client fetches initial simulation data from a GET `/Simulation` request and displays a 50x50 grid based on the simulation's roadMap.

## Features

- **Left Panel**: Four operation sections for "Request Item", "Connect Buildings", "Step", and "Finish".
- **Right Panel**: A grid view where each cell displays:
  - Empty for null or empty strings.
  - "Road" if the cell contains a numeric string.
  - The cell content otherwise.
- **Bottom Panel**: A read-only text area showing output from the server.

## Setup

```bash
git clone https://gitlab.oit.duke.edu/jh925/651-simulationclient.git
npm install
npm run dev
```

## Project Structure
	•	src/App.tsx: Main application component containing the UI and logic.
	•	public/: Contains the HTML template and static assets.
	•	tsconfig.json: TypeScript configuration file.