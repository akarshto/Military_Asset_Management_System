import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

function Bases() {
    const navigate = useNavigate();

    const [bases, setBases] = useState([]);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(true);
    const [showForm, setShowForm] = useState(false);
    const [saving, setSaving] = useState(false);
    const [form, setForm] = useState({ name: "", location: "" });

    const username = localStorage.getItem("username");

    useEffect(() => {
        const token = localStorage.getItem("token");

        if (!token) {
            navigate("/login");
            return;
        }

        const loadBases = async () => {
            try {
                const response = await api.get("/bases");
                setBases(response.data);
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
                    setError("Unable to load bases.");
                }
            } finally {
                setLoading(false);
            }
        };

        loadBases();
    }, [navigate]);

    const logout = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("username");
        navigate("/login");
    };

    const addBase = async (event) => {
        event.preventDefault();
        setSaving(true);
        setError("");

        try {
            const response = await api.post("/bases", form);
            setBases((currentBases) => [...currentBases, response.data]);
            setForm({ name: "", location: "" });
            setShowForm(false);
        } catch (err) {
            setError(err.response?.data?.message || "Unable to add base.");
        } finally {
            setSaving(false);
        }
    };

    return (
        <div className="app-layout">

            <aside className="sidebar">

                <div className="sidebar-brand">
                    <h1>MAMS</h1>
                    <p>Military Asset<br />Management System</p>
                </div>

                <nav className="sidebar-nav">

                    <button onClick={() => navigate("/dashboard")}>
                        <span>▣</span>
                        Dashboard
                    </button>

                    <button className="active">
                        <span>◆</span>
                        Bases
                    </button>

                    <button onClick={() => navigate("/equipment")}>
                        <span>◇</span>
                        Equipment
                    </button>

                    <button onClick={() => navigate("/purchases")}>
                        <span>＋</span>
                        Purchases
                    </button>

                    <button onClick={() => navigate("/transfers")}>
                        <span>⇄</span>
                        Transfers
                    </button>

                    <button onClick={() => navigate("/assignments")}>
                        <span>✓</span>
                        Assignments
                    </button>

                    <button onClick={() => navigate("/expenditures")}>
                        <span>↘</span>
                        Expenditures
                    </button>

                    <button onClick={() => navigate("/audit-logs")}>
                        <span>≡</span>
                        Audit Logs
                    </button>

                </nav>

            </aside>

            <div className="main-area">

                <header className="topbar">

                    <div>
                        <strong>Welcome, {username}</strong>
                    </div>

                    <button className="logout-button" onClick={logout}>
                        Logout
                    </button>

                </header>

                <main className="page-content">

                    <div className="page-header">
                        <div>
                            <h1>Bases</h1>
                            <p>Manage military bases and locations.</p>
                        </div>

                        <button className="primary-button" onClick={() => setShowForm((visible) => !visible)}>
                            + Add Base
                        </button>
                    </div>

                    {showForm && (
                        <form className="form-panel" onSubmit={addBase}>
                            <label>
                                Base name
                                <input
                                    required
                                    value={form.name}
                                    onChange={(event) => setForm({ ...form, name: event.target.value })}
                                    placeholder="e.g. Northern Command Base"
                                />
                            </label>
                            <label>
                                Location
                                <input
                                    required
                                    value={form.location}
                                    onChange={(event) => setForm({ ...form, location: event.target.value })}
                                    placeholder="City or region"
                                />
                            </label>
                            <div className="form-actions">
                                <button className="primary-button" type="submit" disabled={saving}>
                                    {saving ? "Adding..." : "Save Base"}
                                </button>
                                <button className="secondary-button" type="button" onClick={() => setShowForm(false)}>
                                    Cancel
                                </button>
                            </div>
                        </form>
                    )}

                    {loading && (
                        <div className="loading">
                            Loading bases...
                        </div>
                    )}

                    {error && (
                        <div className="error-message">
                            {error}
                        </div>
                    )}

                    {!loading && !error && (
                        <div className="table-card">

                            <table className="data-table">

                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Base Name</th>
                                        <th>Location</th>
                                    </tr>
                                </thead>

                                <tbody>

                                    {bases.length === 0 ? (
                                        <tr>
                                            <td colSpan="3" className="empty-state">
                                                No bases found.
                                            </td>
                                        </tr>
                                    ) : (
                                        bases.map((base) => (
                                            <tr key={base.id}>
                                                <td>{base.id}</td>
                                                <td>{base.name}</td>
                                                <td>{base.location}</td>
                                            </tr>
                                        ))
                                    )}

                                </tbody>

                            </table>

                        </div>
                    )}

                </main>

            </div>

        </div>
    );
}

export default Bases;
