import React, { useEffect, useState } from "react";

interface Simulation {
  currentTimeStep: number;
  verbosityLevel: number;
  buildings: string[];
  recipes: string[];
  buildableTypes: string[];  // 添加可建造类型列表
  // roadMap 中的每个单元格为：null、一个字符串（road）或一个两元素字符串数组（[getName, toString]）
  roadMap: (string | [string, string] | null)[][];
}

const defaultSimulation: Simulation = {
  currentTimeStep: 0,
  verbosityLevel: 0,
  buildings: [],
  recipes: [],
  buildableTypes: [],  // 初始化可建造类型列表
  roadMap: Array(50).fill(null).map(() => Array(50).fill(null)),
};

// images
const imageMapping: { [key: string]: string } = {
  Road_n1: "./src/images/road_n1.png",
  Road_n2: "./src/images/road_n2.png",
  Road_e1: "./src/images/road_e1.png",
  Road_e2: "./src/images/road_e2.png",
  Road_s1: "./src/images/road_s1.png",
  Road_s2: "./src/images/road_s2.png",
  Road_w1: "./src/images/road_w1.png",
  Road_w2: "./src/images/road_w2.png",
  Building_entry: "./src/images/building_entry.png",
  Building_Factory: "./src/images/building_factory.png",
  Building_Mine: "./src/images/building_mine.png",
  Building_Storage: "./src/images/building_storage.png",
  Building_Drone: "./src/images/building_drone.png",
};

const App: React.FC = () => {
  const [simulation, setSimulation] = useState<Simulation>(defaultSimulation);
  const [output, setOutput] = useState<string>("");
  const [selectedBuilding, setSelectedBuilding] = useState<string>("");
  const [selectedRecipe, setSelectedRecipe] = useState<string>("");
  const [selectedFrom, setSelectedFrom] = useState<string>("");
  const [selectedTo, setSelectedTo] = useState<string>("");
  const [selectedRemoveFrom, setSelectedRemoveFrom] = useState<string>("");
  const [selectedRemoveTo, setSelectedRemoveTo] = useState<string>("");
  const [stepCount, setStepCount] = useState<number>(1);
  const [rateCount, setRateCount] = useState<number>(1);
  const [isRequesting, setIsRequesting] = useState<boolean>(false);
  const [isConnecting, setIsConnecting] = useState<boolean>(false);
  const [isStepping, setIsStepping] = useState<boolean>(false);
  const [isRating, setIsRating] = useState<boolean>(false);
  const [isFinishing, setIsFinishing] = useState<boolean>(false);
  const [isSimpleRemoving, setIsSimpleRemoving] = useState<boolean>(false);
  const [isComplexRemoving, setIsComplexRemoving] = useState<boolean>(false);
  const [isPausing, setIsPausing] = useState<boolean>(false);
  const [connectBuildings, setConnectBuildings] = useState<string[]>([]);
  const [removeBuildings, setRemoveBuildings] = useState<string[]>([]);
  const [isConnectMode, setIsConnectMode] = useState<boolean>(false);
  const [isRemoveMode, setIsRemoveMode] = useState<boolean>(false);
  const [contextMenu, setContextMenu] = useState<{
    visible: boolean;
    buildingName: string;
    buildingDesc: string;
    x: number;
    y: number;
    isEmptyCell: boolean;
    cellPosition?: { row: number; col: number };
  }>({
    visible: false,
    buildingName: "",
    buildingDesc: "",
    x: 0,
    y: 0,
    isEmptyCell: false,
  });
  const [hoveredRecipe, setHoveredRecipe] = useState<string>("");
  const URL = "http://localhost:3000";

  // tooltip 状态：用于显示鼠标旁的提示信息
  const [tooltip, setTooltip] = useState<{
    visible: boolean;
    text: string;
    x: number;
    y: number;
  }>({
    visible: false,
    text: "",
    x: 0,
    y: 0,
  });

  // 记录当前鼠标悬停的单元格（行、列索引）
  const [hoveredCell, setHoveredCell] = useState<{ row: number; col: number } | null>(null);

  useEffect(() => {
    // Initial fetch
    fetch(URL + "/Simulation")
      .then((res) => res.json())
      .then((data: Simulation) => {
        console.log(data);
        setSimulation(data);
      })
      .catch((err: Error) => {
        console.error(err);
        setOutput("Error fetching simulation data: " + err.message);
      });

    // Set up auto-refresh every second
    const intervalId = setInterval(() => {
      fetch(URL + "/Simulation")
        .then((res) => res.json())
        .then((data: Simulation) => {
          setSimulation(data);
        })
        .catch((err: Error) => {
          console.error(err);
          setOutput("Error fetching simulation data: " + err.message);
        });
    }, 1000);

    // Clean up interval on component unmount
    return () => clearInterval(intervalId);
  }, []);

  const handleRequest = () => {
    if (!selectedBuilding || !selectedRecipe) {
      setOutput("Please select both Building and Item!");
      return;
    }
    setIsRequesting(true);
    const command = `request '${selectedRecipe}' from '${selectedBuilding}'`;
    fetch(URL + "/Instruction", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: command,
    })
      .then((res) => res.json())
      .then((data) => {
        if (data.simulation) {
          setSimulation(data.simulation);
        } else {
          setSimulation(data);
        }
        setOutput(data.output || "Request completed.");
      })
      .catch((err: Error) => {
        console.error(err);
        setOutput("Error in request: " + err.message);
      })
      .finally(() => setIsRequesting(false));
  };

  const handleConnect = () => {
    if (!selectedFrom || !selectedTo) {
      setOutput("Please select both From and To buildings!");
      return;
    }
    setIsConnecting(true);
    const command = `connect '${selectedFrom}' to '${selectedTo}'`;
    fetch(URL + "/Instruction", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: command,
    })
      .then((res) => res.json())
      .then((data) => {
        if (data.simulation) {
          setSimulation(data.simulation);
        } else {
          setSimulation(data);
        }
        setOutput(data.output || `Connect '${selectedFrom}' to '${selectedTo}' completed.`);
      })
      .catch((err: Error) => {
        console.error(err);
        setOutput("Error in connect: " + err.message);
      })
      .finally(() => setIsConnecting(false));
  };

  const handleSimpleRemove = () => {
    if (!selectedRemoveFrom || !selectedRemoveTo) {
      setOutput("Please select both From and To buildings!");
      return;
    }
    setIsSimpleRemoving(true);
    const command = `srm '${selectedRemoveFrom}' to '${selectedRemoveTo}'`;
    fetch(URL + "/Instruction", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: command,
    })
      .then((res) => res.json())
      .then((data) => {
        if (data.simulation) {
          setSimulation(data.simulation);
        } else {
          setSimulation(data);
        }
        setOutput(data.output || `Simple remove from '${selectedRemoveFrom}' to '${selectedRemoveTo}' completed.`);
      })
      .catch((err: Error) => {
        console.error(err);
        setOutput("Error in simple remove: " + err.message);
      })
      .finally(() => setIsSimpleRemoving(false));
  };
  
  const handleComplexRemove = () => {
    if (!selectedRemoveFrom || !selectedRemoveTo) {
      setOutput("Please select both From and To buildings!");
      return;
    }
    setIsComplexRemoving(true);
    const command = `crm '${selectedRemoveFrom}' to '${selectedRemoveTo}'`;
    fetch(URL + "/Instruction", { 
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: command,
    })
      .then((res) => res.json())
      .then((data) => {
        if (data.simulation) {
          setSimulation(data.simulation);
        } else {
          setSimulation(data);
        }
        setOutput(data.output || `Complex remove from '${selectedRemoveFrom}' to '${selectedRemoveTo}' completed.`);
      })
      .catch((err: Error) => {
        console.error(err);
        setOutput("Error in complex remove: " + err.message);
      })
      .finally(() => setIsComplexRemoving(false));
  };

  const handleRate = () => {
    setIsRating(true);
    const command = `rate ${rateCount}`;
    fetch(URL + "/Instruction", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: command,
    })
      .then((res) => res.json())
      .then((data) => {
        if (data.simulation) {
          setSimulation(data.simulation);
        } else {
          setSimulation(data);
        }
        setOutput(data.output || `Set time rate ${rateCount} completed.`);
      })
      .catch((err: Error) => {
        console.error(err);
        setOutput("Error in setting rate: " + err.message);
      })
      .finally(() => setIsRating(false));
  };

  const handlePause = () => {
    setIsPausing(true);
    const command = `pause`;
    fetch(URL + "/Instruction", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: command,
    })
      .then((res) => res.json())
      .then((data) => {
        if (data.simulation) {
          setSimulation(data.simulation);
        } else {
          setSimulation(data);
        }
        setOutput(data.output || "Pause completed.");
      })
      .catch((err: Error) => {
        console.error(err);
        setOutput("Error in pause: " + err.message);
      })
      .finally(() => setIsPausing(false));
  };

  const handleStep = () => {
    setIsStepping(true);
    const command = `step ${stepCount}`;
    fetch(URL + "/Instruction", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: command,
    })
      .then((res) => res.json())
      .then((data) => {
        if (data.simulation) {
          setSimulation(data.simulation);
        } else {
          setSimulation(data);
        }
        setOutput(data.output || `Step ${stepCount} completed.`);
      })
      .catch((err: Error) => {
        console.error(err);
        setOutput("Error in step: " + err.message);
      })
      .finally(() => setIsStepping(false));
  };

  const handleFinish = () => {
    setIsFinishing(true);
    const command = `finish`;
    fetch(URL + "/Instruction", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: command,
    })
      .then((res) => res.json())
      .then((data) => {
        if (data.simulation) {
          setSimulation(data.simulation);
        } else {
          setSimulation(data);
        }
        setOutput(data.output || "Finish completed.");
      })
      .catch((err: Error) => {
        console.error(err);
        setOutput("Error in finish: " + err.message);
      })
      .finally(() => setIsFinishing(false));
  };

  // 修改：处理建筑选择
  const handleBuildingSelect = (buildingName: string) => {
    if (isConnectMode) {
      if (connectBuildings.length === 0) {
        setConnectBuildings([buildingName]);
        setSelectedFrom(buildingName);
      } else if (connectBuildings.length === 1) {
        setConnectBuildings([...connectBuildings, buildingName]);
        setSelectedTo(buildingName);
      }
    } else if (isRemoveMode) {
      if (removeBuildings.length === 0) {
        setRemoveBuildings([buildingName]);
        setSelectedRemoveFrom(buildingName);
      } else if (removeBuildings.length === 1) {
        setRemoveBuildings([...removeBuildings, buildingName]);
        setSelectedRemoveTo(buildingName);
      }
    }
  };

  // 新增：处理上下文菜单点击
  const handleContextMenuClick = (buildingName: string, buildingDesc: string, e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    
    // 计算菜单位置，确保显示在建筑上方
    const rect = e.currentTarget.getBoundingClientRect();
    setContextMenu({
      visible: true,
      buildingName,
      buildingDesc,
      x: rect.left,
      y: rect.top - 100, // 显示在建筑上方
      isEmptyCell: false,
    });
  };

  // 新增：处理配方选择
  const handleRecipeSelect = (buildingName: string, recipe: string) => {
    setSelectedBuilding(buildingName);
    setSelectedRecipe(recipe);
    handleRequest();
    setContextMenu({ ...contextMenu, visible: false });
  };

  // 修改：处理单元格点击
  const handleCellClick = (row: number, col: number, cell: string | [string, string] | null, e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();

    if (cell === null || cell === "") {
      // 空格子，显示建筑类型选择菜单
      const rect = e.currentTarget.getBoundingClientRect();
      setContextMenu({
        visible: true,
        buildingName: "",
        buildingDesc: "",
        x: rect.left,
        y: rect.top - 100,
        isEmptyCell: true,
        cellPosition: { row, col },
      });
    } else if (Array.isArray(cell)) {
      // 现有建筑，显示原有的上下文菜单
      handleBuildingClick(cell[0], cell[1], e);
    }
  };

  // 新增：处理建筑移除
  const handleRemoveBuilding = (buildingName: string) => {
    const command = `remove ${buildingName}`;
    fetch(URL + "/Instruction", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: command,
    })
      .then((res) => res.json())
      .then((data) => {
        if (data.simulation) {
          setSimulation(data.simulation);
        } else {
          setSimulation(data);
        }
        setOutput(data.output || `Removed building ${buildingName}`);
      })
      .catch((err: Error) => {
        console.error(err);
        setOutput("Error in removing building: " + err.message);
      });
  };

  // 修改：渲染上下文菜单
  const renderContextMenu = () => {
    if (!contextMenu.visible) return null;

    if (contextMenu.isEmptyCell && contextMenu.cellPosition) {
      return (
        <div
          style={{
            position: "fixed",
            top: contextMenu.y,
            left: contextMenu.x,
            backgroundColor: "white",
            borderRadius: "8px",
            boxShadow: "0 2px 10px rgba(0,0,0,0.1)",
            padding: "8px 0",
            minWidth: "200px",
            zIndex: 1000,
          }}
          onMouseLeave={() => setContextMenu({ ...contextMenu, visible: false })}
        >
          <div style={{ padding: "8px 16px", borderBottom: "1px solid #eee" }}>
            <span style={{ fontWeight: "bold" }}>Build</span>
          </div>
          {simulation.buildableTypes.map((type, index) => (
            <div
              key={index}
              style={{
                padding: "8px 16px",
                cursor: "pointer",
                whiteSpace: "nowrap",
                backgroundColor: "transparent",
                transition: "background-color 0.2s",
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.backgroundColor = "#f0f0f0";
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.backgroundColor = "transparent";
              }}
              onClick={() => {
                const command = `build ${contextMenu.cellPosition!.col} ${contextMenu.cellPosition!.row} ${type}`;
                fetch(URL + "/Instruction", {
                  method: "POST",
                  headers: { "Content-Type": "application/json" },
                  body: command,
                })
                  .then((res) => res.json())
                  .then((data) => {
                    if (data.simulation) {
                      setSimulation(data.simulation);
                    } else {
                      setSimulation(data);
                    }
                    setOutput(data.output || `Built ${type} at position (${contextMenu.cellPosition!.col},${contextMenu.cellPosition!.row})`);
                  })
                  .catch((err: Error) => {
                    console.error(err);
                    setOutput("Error in building creation: " + err.message);
                  });
                setContextMenu({ ...contextMenu, visible: false });
              }}
            >
              {type}
            </div>
          ))}
        </div>
      );
    }

    // 从建筑描述中提取配方列表，只获取方括号内的内容
    const recipes = contextMenu.buildingDesc.split('\n')
      .filter(line => line.includes('Type Recipes:'))
      .map(line => line.split('Type Recipes:')[1].trim())
      .join(',')
      .split(',')
      .map(recipe => {
        // 使用正则表达式匹配方括号内的内容
        const match = recipe.match(/\[(.*?)\]/);
        return match ? match[1].trim() : recipe.trim();
      })
      .filter(recipe => recipe.length > 0);

    return (
      <div
        style={{
          position: "fixed",
          top: contextMenu.y,
          left: contextMenu.x,
          backgroundColor: "white",
          borderRadius: "8px",
          boxShadow: "0 2px 10px rgba(0,0,0,0.1)",
          padding: "8px 0",
          minWidth: "150px",
          zIndex: 1000,
        }}
        onMouseLeave={() => setContextMenu({ ...contextMenu, visible: false })}
      >
        <div
          style={{
            padding: "8px 16px",
            cursor: "pointer",
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            position: "relative",
          }}
          onMouseEnter={() => setHoveredRecipe("show")}
          onMouseLeave={() => setHoveredRecipe("")}
        >
          <span>Request Item</span>
          <span style={{ marginLeft: "8px" }}>▶</span>
          {hoveredRecipe === "show" && (
            <div
              style={{
                position: "absolute",
                left: "100%",
                top: "0",
                backgroundColor: "white",
                borderRadius: "8px",
                boxShadow: "0 2px 10px rgba(0,0,0,0.1)",
                padding: "8px 0",
                minWidth: "150px",
              }}
            >
              {recipes.map((recipe, index) => (
                <div
                  key={index}
                  style={{
                    padding: "8px 16px",
                    cursor: "pointer",
                    whiteSpace: "nowrap",
                    backgroundColor: "transparent",
                    transition: "background-color 0.2s",
                  }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.backgroundColor = "#f0f0f0";
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.backgroundColor = "transparent";
                  }}
                  onClick={() => {
                    setSelectedBuilding(contextMenu.buildingName);
                    setSelectedRecipe(recipe);
                    handleRequest();
                    setContextMenu({ ...contextMenu, visible: false });
                  }}
                >
                  {recipe}
                </div>
              ))}
            </div>
          )}
        </div>
        <div
          style={{
            padding: "8px 16px",
            cursor: "pointer",
            color: "#dc3545",
            transition: "background-color 0.2s",
          }}
          onMouseEnter={(e) => {
            e.currentTarget.style.backgroundColor = "#f0f0f0";
          }}
          onMouseLeave={(e) => {
            e.currentTarget.style.backgroundColor = "transparent";
          }}
          onClick={() => {
            handleRemoveBuilding(contextMenu.buildingName);
            setContextMenu({ ...contextMenu, visible: false });
          }}
        >
          Remove Building
        </div>
      </div>
    );
  };

  // 修改：处理建筑点击
  const handleBuildingClick = (buildingName: string, buildingDesc: string, e: React.MouseEvent) => {
    if (isConnectMode || isRemoveMode) {
      handleBuildingSelect(buildingName);
    } else {
      handleContextMenuClick(buildingName, buildingDesc, e);
    }
  };

  // 渲染 50x50 网格视图
  const renderGrid = () => {
    // 定义四个方向，顺序分别为：north, east, south, west
    const directions = ["n", "e", "s", "w"];
    return (
      <table
        border={1}
        style={{
          borderCollapse: "collapse",
          tableLayout: "fixed",
          width: "1000px", // 固定1000px宽度
        }}
      >
        <tbody>
          {simulation.roadMap.map((row, rowIndex) => (
            <tr key={rowIndex}>
              {row.map((cell, colIndex) => {
                const baseStyle: React.CSSProperties = {
                  width: "50px",
                  height: "50px",
                  textAlign: "center",
                  padding: 0,
                  margin: 0,
                  overflow: "hidden",
                  cursor: cell === null || cell === "" ? "pointer" : "default",
                  lineHeight: "50px",
                };

                // 判断是否处于鼠标悬停状态
                const isHovered =
                  hoveredCell &&
                  hoveredCell.row === rowIndex &&
                  hoveredCell.col === colIndex;
                const hoverStyle = isHovered ? { backgroundColor: "lightgray" } : {};

                // 为所有 <td> 添加 onMouseEnter/onMouseLeave 控制悬停状态
                const tdEventHandlers = {
                  onMouseEnter: (e: React.MouseEvent<HTMLTableCellElement>) => {
                    setHoveredCell({ row: rowIndex, col: colIndex });
                    // 如果 cell 是空或 string 类型显示简单信息
                    if (cell === null || cell === "") {
                      setTooltip({
                        visible: true,
                        text: "Click to build",
                        x: e.clientX,
                        y: e.clientY,
                      });
                    } else if (typeof cell === "string" && cell.length !== 4) {
                      setTooltip({
                        visible: true,
                        text: `Road Cell: ${cell}`,
                        x: e.clientX,
                        y: e.clientY,
                      });
                    }
                  },
                  onMouseLeave: () => {
                    setHoveredCell(null);
                    setTooltip({ visible: false, text: "", x: 0, y: 0 });
                  },
                  onMouseMove: (e: React.MouseEvent<HTMLTableCellElement>) =>
                    setTooltip((prev) => ({ ...prev, x: e.clientX, y: e.clientY }))
                };

                // 处理 cell 为 null 或空字符串的情况
                if (cell === null || cell === "") {
                  return (
                    <td
                      key={colIndex}
                      style={baseStyle}
                      onClick={(e) => handleCellClick(rowIndex, colIndex, cell, e)}
                      onMouseEnter={(e) => {
                        setHoveredCell({ row: rowIndex, col: colIndex });
                        setTooltip({
                          visible: true,
                          text: "Click to build",
                          x: e.clientX,
                          y: e.clientY,
                        });
                      }}
                      onMouseLeave={() => {
                        setHoveredCell(null);
                        setTooltip({ visible: false, text: "", x: 0, y: 0 });
                      }}
                      onMouseMove={(e) =>
                        setTooltip((prev) => ({ ...prev, x: e.clientX, y: e.clientY }))
                      }
                    />
                  );
                }

                if (typeof cell === "string") {
                  // 判断是否为四位字符，代表复合单元格（例如 "0112"）
                  if (cell.length === 4) {
                    const imgs = cell
                      .split("")
                      .map((digit, idx) => {
                        if (digit === "0") return null;
                        const key = `Road_${directions[idx]}${digit}`;
                        return imageMapping[key] || null;
                      })
                      .filter((src) => src !== null);
                    return (
                      <td key={colIndex} style={{ ...baseStyle, ...hoverStyle }} {...tdEventHandlers}>
                        <div style={{ position: "relative", width: "100%", height: "100%" }}>
                          {imgs.map((imgSrc, index) => (
                            <img
                              key={index}
                              src={imgSrc!}
                              alt=""
                              style={{
                                position: "absolute",
                                top: 0,
                                left: 0,
                                width: "100%",
                                height: "100%",
                                objectFit: "cover",
                                display: "block",
                              }}
                            />
                          ))}
                        </div>
                      </td>
                    );
                  } else {
                    // 非四位字符串：直接拼接 key 显示图片
                    const displayText = `Road_${cell}`;
                    const imgSrc = imageMapping[displayText];
                    return (
                      <td key={colIndex} style={{ ...baseStyle, ...hoverStyle }} {...tdEventHandlers}>
                        {imgSrc ? (
                          <img
                            src={imgSrc}
                            alt={displayText}
                            style={{
                              width: "100%",
                              height: "100%",
                              objectFit: "cover",
                              display: "block",
                            }}
                          />
                        ) : (
                          displayText
                        )}
                      </td>
                    );
                  }
                }
                if (Array.isArray(cell)) {
                  const buildingName = cell[0];
                  const buildingDesc = cell[1].replace(/,\s*/g, "\n");
                  // 判断 Building Type
                  let buildingType = "Unknown";
                  if (cell[1].includes("Type: FactoryBuilding")) {
                    buildingType = "FactoryBuilding";
                  } else if (cell[1].includes("Type: MineBuilding")) {
                    buildingType = "MineBuilding";
                  } else if (cell[1].includes("Type: StorageBuilding")) {
                    buildingType = "StorageBuilding";
                  } else if (cell[1].includes("Type: DroneBuilding")) {
                    buildingType = "DroneBuilding";
                  }

                  // 判断是否被选中
                  const isSelected = (isConnectMode && connectBuildings.includes(buildingName)) || 
                                    (isRemoveMode && removeBuildings.includes(buildingName));
                  const isSelectable = isConnectMode || isRemoveMode;

                  // 小图标叠加图层
                  const overlayIcon =
                    buildingType === "FactoryBuilding"
                      ? imageMapping["Building_Factory"]
                      : buildingType === "MineBuilding"
                      ? imageMapping["Building_Mine"]
                      : buildingType === "StorageBuilding"
                      ? imageMapping["Building_Storage"]
                      : buildingType === "DroneBuilding"
                      ? imageMapping["Building_Drone"]
                      : null;
                
                  return (
                    <td
                      key={colIndex}
                      style={{
                        ...baseStyle,
                        position: "relative",
                        overflow: "visible",
                        boxSizing: "border-box",
                        backgroundColor:
                          buildingType === "FactoryBuilding"
                            ? "#ecd8d9"
                            : buildingType === "MineBuilding"
                            ? "#cad8d8"
                            : buildingType === "StorageBuilding"
                            ? "#ccddef"
                            : buildingType === "DroneBuilding"
                            ? "#98a4b2"
                            : "lightgray",
                        ...hoverStyle,
                        cursor: "pointer",
                        border: isSelected ? "2px solid #28a745" : "2px solid transparent",
                        boxShadow: isSelected ? "inset 0 0 10px rgba(40, 167, 69, 0.5)" : "none",
                      }}
                      onClick={(e) => handleBuildingClick(buildingName, buildingDesc, e)}
                      onMouseEnter={(e) => {
                        setHoveredCell({ row: rowIndex, col: colIndex });
                        setTooltip({
                          visible: true,
                          text: buildingDesc,
                          x: e.clientX,
                          y: e.clientY,
                        });
                      }}
                      onMouseMove={(e) =>
                        setTooltip((prev) => ({ ...prev, x: e.clientX, y: e.clientY }))
                      }
                      onMouseLeave={() => {
                        setTooltip({ visible: false, text: "", x: 0, y: 0 });
                        setHoveredCell(null);
                      }}
                    >
                    {/* 底层背景大图 */}
                    <img
                      src={imageMapping["Building_entry"]}
                      alt="Building base"
                      style={{
                        position: "absolute",
                        top: "-34%",
                        left: "-34%",
                        width: "168%",
                        height: "168%",
                        objectFit: "contain",
                        pointerEvents: "none",
                        zIndex: 1,
                      }}
                    />

                    {/* 建筑类型图标叠加（小图） */}
                    {overlayIcon && (
                      <img
                        src={overlayIcon}
                        alt={buildingType}
                        style={{
                          position: "absolute",
                          top: "0%",
                          left: "0%",
                          width: "50px",
                          height: "50px",
                          objectFit: "contain",
                          pointerEvents: "none",
                          zIndex: 2,
                        }}
                      />
                    )}
                     
                      <div
                        style={{
                          position: "absolute",
                          top: "65%",                   // 往下定位
                          left: "50%",
                          transform: "translate(-50%, -30%)",  // 调整体偏移
                          zIndex: 3,
                          fontWeight: "bold",
                          fontSize: "10px",
                          color: "#000",
                          textShadow: "0 0 2px white",
                          pointerEvents: "none",
                        }}
                      > 
                        {buildingName}
                      </div>
                    </td>
                  );
                }
                
                // 其他情况：直接转换为字符串显示
                return (
                  <td key={colIndex} style={{ ...baseStyle, ...hoverStyle }} {...tdEventHandlers}>
                    {/* {cell.toString()} */}
                  </td>
                );
              })}
            </tr>
          ))}
        </tbody>
      </table>
    );
  };

  const tooltipStyle: React.CSSProperties = {
    position: "fixed",
    top: tooltip.y + 10,
    left: tooltip.x + 10,
    backgroundColor: "rgba(0, 0, 0, 0.8)",  // 深一点的背景
    color: "#fff",
    padding: "8px",                       // 内边距让内容不紧贴边框
    borderRadius: "5px",
    pointerEvents: "none",
    zIndex: 1000,
    fontSize: "14px",
    whiteSpace: "pre-wrap",               // 支持换行符显示
    lineHeight: "1.5",
  };

  const renderButton = (label: string, onClick: () => void, disabled: boolean, color: string) => (
    <button
      onClick={onClick}
      disabled={disabled}
      style={{
        width: "100%",
        padding: "10px",
        backgroundColor: disabled ? "#aaa" : color,
        color: "#fff",
        border: "none",
        borderRadius: "8px",
        cursor: disabled ? "not-allowed" : "pointer",
        fontWeight: "bold",
      }}
    >
      {label}
    </button>
  );

  const renderSelect = (label: string, value: string, onChange: (e: React.ChangeEvent<HTMLSelectElement>) => void, options: string[]) => (
    <div style={{ marginBottom: "12px" }}>
      <label style={{ fontWeight: 500 }}>{label}</label>
      <select
        value={value}
        onChange={onChange}
        style={{
          width: "100%",
          padding: "6px 10px",
          borderRadius: "6px",
          border: "1px solid #ccc",
          marginTop: "4px",
        }}
      >
        <option value="">Please select</option>
        {options.map((o, idx) => (
          <option key={idx} value={o}>{o}</option>
        ))}
      </select>
    </div>
  );

  // 修改：更新 Connect Buildings 面板
  const renderConnectPanel = () => (
    <div style={{ marginBottom: "20px", padding: "16px", backgroundColor: "#f9f9f9", borderRadius: "12px", boxShadow: "0 2px 6px rgba(0,0,0,0.1)" }}>
      <h3 style={{ fontSize: "18px", fontWeight: 700, marginBottom: "12px" }}>Connect Buildings</h3>
      <div style={{ marginBottom: "12px" }}>
        <div style={{ display: "flex", gap: "8px", marginBottom: "8px" }}>
          <button
            onClick={() => {
              setIsConnectMode(!isConnectMode);
              setIsRemoveMode(false);
              setConnectBuildings([]);
              setSelectedFrom("");
              setSelectedTo("");
            }}
            style={{
              padding: "8px 16px",
              backgroundColor: isConnectMode ? "#28a745" : "#6c757d",
              color: "#fff",
              border: "none",
              borderRadius: "4px",
              cursor: "pointer",
              flex: 1
            }}
          >
            {isConnectMode ? "Cancel Selection" : "Select Buildings"}
          </button>
          {connectBuildings.length === 2 && (
            <button
              onClick={handleConnect}
              disabled={isConnecting}
              style={{
                padding: "8px 16px",
                backgroundColor: "#28a745",
                color: "#fff",
                border: "none",
                borderRadius: "4px",
                cursor: "pointer",
                flex: 1
              }}
            >
              Connect
            </button>
          )}
        </div>
        <div style={{ fontSize: "14px", color: "#6c757d", marginBottom: "8px" }}>
          {isConnectMode ? "Click two buildings to connect" : "Click 'Select Buildings' to start"}
        </div>
      </div>
    </div>
  );

  // 修改：更新 Remove Connections 面板
  const renderRemovePanel = () => (
    <div style={{ 
      marginBottom: "20px", 
      padding: "12px", 
      backgroundColor: "#f9f9f9", 
      borderRadius: "12px", 
      boxShadow: "0 2px 6px rgba(0,0,0,0.1)",
      maxWidth: "100%"
    }}>
      <h3 style={{ fontSize: "18px", fontWeight: 700, marginBottom: "12px" }}>Remove Connection</h3>
      <div>
        <button
          onClick={() => {
            setIsRemoveMode(!isRemoveMode);
            setIsConnectMode(false);
            setRemoveBuildings([]);
            setSelectedRemoveFrom("");
            setSelectedRemoveTo("");
          }}
          style={{
            padding: "6px 12px",
            backgroundColor: isRemoveMode ? "#dc3545" : "#6c757d",
            color: "#fff",
            border: "none",
            borderRadius: "4px",
            cursor: "pointer",
            width: "100%",
            marginBottom: "4px",
            fontSize: "14px"
          }}
        >
          {isRemoveMode ? "Cancel Selection" : "Select Buildings"}
        </button>
        {removeBuildings.length === 2 && (
          <div style={{ display: "flex", gap: "4px", width: "100%" }}>
            <button
              onClick={handleSimpleRemove}
              disabled={isSimpleRemoving}
              style={{
                padding: "4px 8px",
                backgroundColor: "#dc3545",
                color: "#fff",
                border: "none",
                borderRadius: "4px",
                cursor: "pointer",
                flex: 1,
                fontSize: "12px",
                whiteSpace: "nowrap"
              }}
            >
              Simple
            </button>
            <button
              onClick={handleComplexRemove}
              disabled={isComplexRemoving}
              style={{
                padding: "4px 8px",
                backgroundColor: "#ffc107",
                color: "#fff",
                border: "none",
                borderRadius: "4px",
                cursor: "pointer",
                flex: 1,
                fontSize: "12px",
                whiteSpace: "nowrap"
              }}
            >
              Complex
            </button>
          </div>
        )}
        <div style={{ 
          fontSize: "12px", 
          color: "#6c757d", 
          marginTop: "4px",
          textAlign: "center"
        }}>
          {isRemoveMode ? "Click two buildings to remove" : "Click to start"}
        </div>
      </div>
    </div>
  );

  return (
    <div style={{ display: "flex", height: "100vh", flexDirection: "row" }}>
      {/* Left Control Panel */}
      <div style={{ width: "300px", padding: "10px", display: "flex", flexDirection: "column", height: "100%" }}>
        <div style={{ flex: "0 0 auto" }}>
          {/* Progress Bar */}
          <div style={{ 
            marginBottom: "20px", 
            padding: "16px", 
            backgroundColor: "#f9f9f9", 
            borderRadius: "12px", 
            boxShadow: "0 2px 6px rgba(0,0,0,0.1)" 
          }}>
            <h3 style={{ fontSize: "18px", fontWeight: 700, marginBottom: "12px" }}>Time Step</h3>
            <div style={{ 
              width: "100%", 
              height: "20px", 
              backgroundColor: "#e9ecef", 
              borderRadius: "10px", 
              overflow: "hidden" 
            }}>
              <div style={{ 
                width: `${(simulation.currentTimeStep % 100) || 0}%`, 
                height: "100%", 
                backgroundColor: "#17a2b8", 
                transition: "width 0.3s ease-in-out" 
              }} />
            </div>
            <div style={{ 
              textAlign: "center", 
              marginTop: "8px", 
              fontSize: "14px", 
              color: "#6c757d" 
            }}>
              Step: {simulation.currentTimeStep}
            </div>
          </div>

          <div style={{ marginBottom: "20px", padding: "16px", backgroundColor: "#f9f9f9", borderRadius: "12px", boxShadow: "0 2px 6px rgba(0,0,0,0.1)" }}>
            <h3 style={{ fontSize: "18px", fontWeight: 700, marginBottom: "12px" }}>Request Item</h3>
            {renderSelect("Building:", selectedBuilding, (e) => setSelectedBuilding(e.target.value), simulation.buildings)}
            {renderSelect("Item:", selectedRecipe, (e) => setSelectedRecipe(e.target.value), simulation.recipes)}
            {renderButton("Request", handleRequest, isRequesting, "#007bff")}
          </div>

          {renderConnectPanel()}
          {renderRemovePanel()}
        </div>

        <div style={{ flex: 1, marginTop: "10px" }}>
          <textarea
            style={{ width: "100%", height: "100%", boxSizing: "border-box", resize: "none", overflow: "auto" }}
            value={output}
            readOnly
          />
        </div>
      </div>

      {/* Grid View */}
      <div style={{ width: "1000px", height: "100%", padding: "10px", boxSizing: "border-box", overflow: "auto", border: "2px solid #ccc" }}>
        {renderGrid()}
      </div>

      {/* Right Control Panel */}
      <div style={{ width: "300px", padding: "10px", display: "flex", flexDirection: "column", height: "100%" }}>
        <div style={{ flex: "0 0 auto" }}>
          <div style={{ marginBottom: "20px", padding: "16px", backgroundColor: "#f9f9f9", borderRadius: "12px", boxShadow: "0 2px 6px rgba(0,0,0,0.1)" }}>
            <h3 style={{ fontSize: "18px", fontWeight: 700, marginBottom: "12px" }}>Step</h3>
            <div style={{ marginBottom: "12px" }}>
              <label style={{ fontWeight: 500 }}>N:</label>
              <select
                value={stepCount}
                onChange={(e) => setStepCount(Number(e.target.value))}
                style={{
                  width: "100%",
                  padding: "6px 10px",
                  borderRadius: "6px",
                  border: "1px solid #ccc",
                  marginTop: "4px",
                }}
              >
                {Array.from({ length: 50 }, (_, i) => i + 1).map((n) => (
                  <option key={n} value={n}>{n}</option>
                ))}
              </select>
            </div>
            {renderButton("Step", handleStep, isStepping, "#17a2b8")}
          </div>

          <div style={{ marginBottom: "20px", padding: "16px", backgroundColor: "#f9f9f9", borderRadius: "12px", boxShadow: "0 2px 6px rgba(0,0,0,0.1)" }}>
            <h3 style={{ fontSize: "18px", fontWeight: 700, marginBottom: "12px" }}>Pause</h3>
            {renderButton("Pause", handlePause, isPausing, "#343a40")}
          </div>

          <div style={{ marginBottom: "20px", padding: "16px", backgroundColor: "#f9f9f9", borderRadius: "12px", boxShadow: "0 2px 6px rgba(0,0,0,0.1)" }}>
            <h3 style={{ fontSize: "18px", fontWeight: 700, marginBottom: "12px" }}>Time Rate</h3>
            <div style={{ marginBottom: "12px" }}>
              <label style={{ fontWeight: 500 }}>N:</label>
              <select
                value={rateCount}
                onChange={(e) => setRateCount(Number(e.target.value))}
                style={{
                  width: "100%",
                  padding: "6px 10px",
                  borderRadius: "6px",
                  border: "1px solid #ccc",
                  marginTop: "4px",
                }}
              >
                {Array.from({ length: 50 }, (_, i) => i + 1).map((n) => (
                  <option key={n} value={n}>{n}</option>
                ))}
              </select>
            </div>
            {renderButton("Set Rate", handleRate, isRating, "#17a2b8")}
          </div>

          <div style={{ marginBottom: "20px", padding: "16px", backgroundColor: "#f9f9f9", borderRadius: "12px", boxShadow: "0 2px 6px rgba(0,0,0,0.1)" }}>
            <h3 style={{ fontSize: "18px", fontWeight: 700, marginBottom: "12px" }}>Finish</h3>
            {renderButton("Finish", handleFinish, isFinishing, "#6c757d")}
          </div>
        </div>
      </div>

      {tooltip.visible && <div style={tooltipStyle}>{tooltip.text}</div>}
      {renderContextMenu()}
    </div>
  );

};

export default App;
