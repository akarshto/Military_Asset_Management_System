import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

function Equipment() {
    const navigate = useNavigate();
    const [equipment, setEquipment] = useState([]);
    const [bases, setBases] = useState([]);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(true);
    const [showForm, setShowForm] = useState(false);
    const [saving, setSaving] = useState(false);
    const [form, setForm] = useState({
        assetName: "",
        assetCode: "",
        equipmentTypeName: "",
        baseId: "",
        quantity: "1"
    });
    const username = localStorage.getItem("username");

    useEffect(() => {
        if (!localStorage.getItem("token")) {
            navigate("/login");
            return;
        }

        const loadData = async () => {
            try {
                const [equipmentResponse, basesResponse] = await Promise.all([
                    api.get("/equipment"),
                    api.get("/bases")
                ]);
                setEquipment(equipmentResponse.data);
                setBases(basesResponse.data);
                setForm((currentForm) => ({
                    ...currentForm,
                    baseId: currentForm.baseId || String(basesResponse.data[0]?.id || "")
                }));
            } catch (err) {
                if (err.response?.status === 401 || err.response?.status === 403) {
                    localStorage.removeItem("token");
                    localStorage.removeItem("username");
                    navigate("/login");
                } else {
                    setError("Unable to load equipment and bases.");
                }
            } finally {
                setLoading(false);
            }
        };

        loadData();
    }, [navigate]);

    const logout = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("username");
        navigate("/login");
    };

    const addEquipment = async (event) => {
        event.preventDefault();
        setSaving(true);
        setError("");

        try {
            const response = await api.post("/equipment", {
                ...form,
                baseId: Number(form.baseId),
                quantity: Number(form.quantity)
            });
            setEquipment((currentEquipment) => [...currentEquipment, response.data]);
            setForm({
                assetName: "",
                assetCode: "",
                equipmentTypeName: "",
                baseId: String(bases[0]?.id || ""),
                quantity: "1"
            });
            setShowForm(false);
        } catch (err) {
            setError(err.response?.data?.message || "Unable to add equipment.");
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
                    <button onClick={() => navigate("/dashboard")}><span>▣</span>Dashboard</button>
                    <button onClick={() => navigate("/bases")}><span>◆</span>Bases</button>
                    <button className="active"><span>◇</span>Equipment</button>
                    <button onClick={() => navigate("/purchases")}><span>＋</span>Purchases</button>
                    <button onClick={() => navigate("/transfers")}><span>⇄</span>Transfers</button>
                    <button onClick={() => navigate("/assignments")}><span>✓</span>Assignments</button>
                    <button onClick={() => navigate("/expenditures")}><span>↘</span>Expenditures</button>
                    <button onClick={() => navigate("/audit-logs")}><span>≡</span>Audit Logs</button>
                </nav>
            </aside>
            <div className="main-area">
                <header className="topbar">
                    <strong>Welcome, {username}</strong>
                    <button className="logout-button" onClick={logout}>Logout</button>
                </header>
                <main className="page-content">
                    <div className="page-header">
                        <div>
                            <h1>Equipment</h1>
                            <p>Track equipment inventory by base and type.</p>
                        </div>
                        <button className="primary-button" onClick={() => setShowForm((visible) => !visible)}>
                            + Add Equipment
                        </button>
                    </div>

                    {showForm && (
                        <form className="form-panel" onSubmit={addEquipment}>
                            <label>
                                Asset name
                                <input required value={form.assetName} onChange={(event) => setForm({ ...form, assetName: event.target.value })} />
                            </label>
                            <label>
                                Asset code
                                <input value={form.assetCode} onChange={(event) => setForm({ ...form, assetCode: event.target.value })} />
                            </label>
                            <label>
                                Equipment type
                                <input required value={form.equipmentTypeName} onChange={(event) => setForm({ ...form, equipmentTypeName: event.target.value })} placeholder="e.g. Vehicle, Radio" />
                            </label>
                            <label>
                                Base
                                <select required value={form.baseId} onChange={(event) => setForm({ ...form, baseId: event.target.value })}>
                                    <option value="" disabled>Select a base</option>
                                    {bases.map((base) => <option key={base.id} value={base.id}>{base.name}</option>)}
                                </select>
                            </label>
                            <label>
                                Quantity
                                <input type="number" min="1" required value={form.quantity} onChange={(event) => setForm({ ...form, quantity: event.target.value })} />
                            </label>
                            <div className="form-actions">
                                <button className="primary-button" type="submit" disabled={saving || bases.length === 0}>
                                    {saving ? "Adding..." : "Save Equipment"}
                                </button>
                                <button className="secondary-button" type="button" onClick={() => setShowForm(false)}>Cancel</button>
                            </div>
                            {bases.length === 0 && <p className="form-hint">Add a base before registering equipment.</p>}
                        </form>
                    )}

                    {loading && <div className="loading">Loading equipment...</div>}
                    {error && <div className="error-message">{error}</div>}
                    {!loading && !error && (
                        <div className="table-card">
                            <table className="data-table">
                                <thead>
                                    <tr><th>ID</th><th>Asset</th><th>Code</th><th>Type</th><th>Base</th><th>Quantity</th></tr>
                                </thead>
                                <tbody>
                                    {equipment.length === 0 ? (
                                        <tr><td colSpan="6" className="empty-state">No equipment found.</td></tr>
                                    ) : equipment.map((item) => (
                                        <tr key={item.id}>
                                            <td>{item.id}</td>
                                            <td>{item.assetName}</td>
                                            <td>{item.assetCode || "-"}</td>
                                            <td>{item.equipmentType?.name || "-"}</td>
                                            <td>{item.base?.name || "-"}</td>
                                            <td>{item.quantity}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </main>
            </div>
        </div>
    );
}

export default Equipment;