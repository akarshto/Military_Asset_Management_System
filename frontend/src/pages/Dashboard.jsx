import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";
import "./dashboard.css";

function Dashboard() {

    const navigate = useNavigate();

    const [data, setData] = useState(null);
    const [error, setError] = useState("");
    const [activePage, setActivePage] = useState("Dashboard");

    const username = localStorage.getItem("username");

    useEffect(() => {

        const token = localStorage.getItem("token");

        if (!token) {
            navigate("/login");
            return;
        }

        const loadDashboard = async () => {

            try {

                const response = await api.get("/dashboard");

                setData(response.data);

            } catch (err) {

                console.error(err);

                if (
                    err.response?.status === 401 ||
                    err.response?.status === 403
                ) {

                    localStorage.removeItem("token");
                    localStorage.removeItem("username");

                    navigate("/login");

                } else {

                    setError("Unable to load dashboard.");

                }
            }
        };

        loadDashboard();

    }, [navigate]);

    const logout = () => {

        localStorage.removeItem("token");
        localStorage.removeItem("username");

        navigate("/login");
    };

    const menuItems = [
        "Dashboard",
        "Bases",
        "Equipment",
        "Purchases",
        "Transfers",
        "Assignments",
        "Expenditures",
        "Audit Logs"
    ];

    const handleMenuClick = (item) => {

        setActivePage(item);

        if (item === "Dashboard") {
            return;
        }

        if (item === "Bases") {
            navigate("/bases");
            return;
        }

        if (item === "Equipment") {
            navigate("/equipment");
            return;
        }

        if (item === "Purchases") {
            navigate("/purchases");
            return;
        }

        const moduleRoutes = {
            Transfers: "/transfers",
            Assignments: "/assignments",
            Expenditures: "/expenditures",
            "Audit Logs": "/audit-logs"
        };

        if (moduleRoutes[item]) {
            navigate(moduleRoutes[item]);
        }
    };

    return (
        <div className="mams-layout">

            {/* SIDEBAR */}

            <aside className="sidebar">

                <div className="sidebar-brand">

                    <h1>MAMS</h1>

                    <p>
                        Military Asset
                        <br />
                        Management System
                    </p>

                </div>

                <nav className="sidebar-menu">

                    {menuItems.map((item) => (

                        <button
                            key={item}
                            className={
                                activePage === item
                                    ? "menu-item active"
                                    : "menu-item"
                            }
                            onClick={() => handleMenuClick(item)}
                        >
                            <span className="menu-icon">
                                {item === "Dashboard" && "▦"}
                                {item === "Bases" && "◆"}
                                {item === "Equipment" && "◇"}
                                {item === "Purchases" && "＋"}
                                {item === "Transfers" && "⇄"}
                                {item === "Assignments" && "✓"}
                                {item === "Expenditures" && "↘"}
                                {item === "Audit Logs" && "≡"}
                            </span>

                            <span>{item}</span>

                        </button>

                    ))}

                </nav>

                <div className="sidebar-bottom">

                    <button
                        className="logout-sidebar"
                        onClick={logout}
                    >
                        <span>↪</span>
                        Logout
                    </button>

                </div>

            </aside>


            {/* MAIN AREA */}

            <div className="main-area">

                {/* TOP NAVBAR */}

                <header className="topbar">

                    <div>

                        <h2>{activePage}</h2>

                        <p>
                            Military Asset Management System
                        </p>

                    </div>

                    <div className="user-section">

                        <div className="user-info">

                            <span className="user-avatar">
                                {username?.charAt(0).toUpperCase()}
                            </span>

                            <div>
                                <strong>{username}</strong>
                                <small>Administrator</small>
                            </div>

                        </div>

                        <button
                            className="topbar-logout"
                            onClick={logout}
                        >
                            Logout
                        </button>

                    </div>

                </header>


                {/* CONTENT */}

                <main className="dashboard-content">

                    {activePage === "Dashboard" && (

                        <>

                            <div className="page-heading">

                                <div>
                                    <h1>Dashboard</h1>

                                    <p>
                                        Overview of military asset movements
                                        and inventory.
                                    </p>
                                </div>

                            </div>


                            {error && (
                                <div className="dashboard-error">
                                    {error}
                                </div>
                            )}


                            {data && (

                                <div className="stats-grid">

                                    <div className="stat-card">
                                        <div className="stat-title">
                                            Opening Balance
                                        </div>

                                        <div className="stat-value">
                                            {data.openingBalance}
                                        </div>
                                    </div>


                                    <div className="stat-card">
                                        <div className="stat-title">
                                            Purchases
                                        </div>

                                        <div className="stat-value">
                                            {data.purchases}
                                        </div>
                                    </div>


                                    <div className="stat-card">
                                        <div className="stat-title">
                                            Equipment Units
                                        </div>

                                        <div className="stat-value">
                                            {data.equipmentUnits ?? 0}
                                        </div>
                                    </div>


                                    <div className="stat-card">
                                        <div className="stat-title">
                                            Transfer In
                                        </div>

                                        <div className="stat-value">
                                            {data.transferIn}
                                        </div>
                                    </div>


                                    <div className="stat-card">
                                        <div className="stat-title">
                                            Transfer Out
                                        </div>

                                        <div className="stat-value">
                                            {data.transferOut}
                                        </div>
                                    </div>


                                    <div className="stat-card">
                                        <div className="stat-title">
                                            Assigned
                                        </div>

                                        <div className="stat-value">
                                            {data.assigned}
                                        </div>
                                    </div>


                                    <div className="stat-card">
                                        <div className="stat-title">
                                            Expended
                                        </div>

                                        <div className="stat-value">
                                            {data.expended}
                                        </div>
                                    </div>


                                    <div className="stat-card">
                                        <div className="stat-title">
                                            Net Movement
                                        </div>

                                        <div className="stat-value">
                                            {data.netMovement}
                                        </div>
                                    </div>


                                    <div className="stat-card">
                                        <div className="stat-title">
                                            Closing Balance
                                        </div>

                                        <div className="stat-value">
                                            {data.closingBalance}
                                        </div>
                                    </div>

                                </div>

                            )}

                        </>

                    )}


                    {activePage !== "Dashboard" && (

                        <div className="module-placeholder">

                            <div className="placeholder-icon">
                                 {activePage === "Bases" ? "◆" : "?"}
                            </div>

                            <h1>{activePage}</h1>

                            <p>
                                The {activePage.toLowerCase()} management
                                module will be available here.
                            </p>

                        </div>

                    )}

                </main>

            </div>

        </div>
    );
}

export default Dashboard;
