import { useNavigate } from "react-router-dom";

const navigationItems = [
    { label: "Dashboard", path: "/dashboard", icon: "▦" },
    { label: "Bases", path: "/bases", icon: "◆" },
    { label: "Equipment", path: "/equipment", icon: "◇" },
    { label: "Purchases", path: "/purchases", icon: "＋" },
    { label: "Transfers", path: "/transfers", icon: "⇄" },
    { label: "Assignments", path: "/assignments", icon: "✓" },
    { label: "Expenditures", path: "/expenditures", icon: "↘" },
    { label: "Audit Logs", path: "/audit-logs", icon: "≡" }
];

function ModuleShell({ activePage, children }) {
    const navigate = useNavigate();
    const username = localStorage.getItem("username");

    const logout = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("username");
        navigate("/login");
    };

    return (
        <div className="app-layout">
            <aside className="sidebar">
                <div className="sidebar-brand">
                    <h1>MAMS</h1>
                    <p>Military Asset<br />Management System</p>
                </div>
                <nav className="sidebar-nav">
                    {navigationItems.map((item) => (
                        <button
                            key={item.path}
                            className={activePage === item.label ? "active" : ""}
                            onClick={() => navigate(item.path)}
                        >
                            <span>{item.icon}</span>
                            {item.label}
                        </button>
                    ))}
                </nav>
            </aside>
            <div className="main-area">
                <header className="topbar">
                    <strong>Welcome, {username}</strong>
                    <button className="logout-button" onClick={logout}>Logout</button>
                </header>
                <main className="page-content">{children}</main>
            </div>
        </div>
    );
}

export default ModuleShell;