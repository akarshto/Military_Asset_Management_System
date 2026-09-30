import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

function Purchases() {
    const navigate = useNavigate();
    const [purchases, setPurchases] = useState([]);
    const [bases, setBases] = useState([]);
    const [equipmentTypes, setEquipmentTypes] = useState([]);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(true);
    const [showForm, setShowForm] = useState(false);
    const [saving, setSaving] = useState(false);
    const [form, setForm] = useState({
        baseId: "",
        equipmentTypeId: "",
        quantity: "1",
        purchaseDate: new Date().toISOString().slice(0, 16),
        supplier: ""
    });
    const username = localStorage.getItem("username");

    useEffect(() => {
        if (!localStorage.getItem("token")) {
            navigate("/login");
            return;
        }

        const loadData = async () => {
            try {
                const [purchaseResponse, baseResponse, typeResponse] = await Promise.all([
                    api.get("/purchases"),
                    api.get("/bases"),
                    api.get("/equipment/types")
                ]);
                setPurchases(purchaseResponse.data);
                setBases(baseResponse.data);
                setEquipmentTypes(typeResponse.data);
                setForm((currentForm) => ({
                    ...currentForm,
                    baseId: currentForm.baseId || String(baseResponse.data[0]?.id || ""),
                    equipmentTypeId: currentForm.equipmentTypeId || String(typeResponse.data[0]?.id || "")
                }));
            } catch (err) {
                if (err.response?.status === 401 || err.response?.status === 403) {
                    localStorage.removeItem("token");
                    localStorage.removeItem("username");
                    navigate("/login");
                } else {
                    setError("Unable to load purchases, bases, or equipment types.");
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

    const addPurchase = async (event) => {
        event.preventDefault();
        setSaving(true);
        setError("");

        try {
            const response = await api.post("/purchases", {
                ...form,
                baseId: Number(form.baseId),
                equipmentTypeId: Number(form.equipmentTypeId),
                quantity: Number(form.quantity)
            });
            setPurchases((currentPurchases) => [...currentPurchases, response.data]);
            setForm((currentForm) => ({
                ...currentForm,
                quantity: "1",
                purchaseDate: new Date().toISOString().slice(0, 16),
                supplier: ""
            }));
            setShowForm(false);
        } catch (err) {
            setError(err.response?.data?.message || "Unable to record purchase.");
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
                    <button onClick={() => navigate("/equipment")}><span>◇</span>Equipment</button>
                    <button className="active"><span>＋</span>Purchases</button>
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
                            <h1>Purchases</h1>
                            <p>Record incoming equipment purchases.</p>
                        </div>
                        <button className="primary-button" onClick={() => setShowForm((visible) => !visible)}>
                            + Add Purchase
                        </button>
                    </div>

                    {showForm && (
                        <form className="form-panel" onSubmit={addPurchase}>
                            <label>
                                Base
                                <select required value={form.baseId} onChange={(event) => setForm({ ...form, baseId: event.target.value })}>
                                    <option value="" disabled>Select a base</option>
                                    {bases.map((base) => <option key={base.id} value={base.id}>{base.name}</option>)}
                                </select>
                            </label>
                            <label>
                                Equipment type
                                <select required value={form.equipmentTypeId} onChange={(event) => setForm({ ...form, equipmentTypeId: event.target.value })}>
                                    <option value="" disabled>Select a type</option>
                                    {equipmentTypes.map((type) => <option key={type.id} value={type.id}>{type.name}</option>)}
                                </select>
                            </label>
                            <label>
                                Quantity
                                <input type="number" min="1" required value={form.quantity} onChange={(event) => setForm({ ...form, quantity: event.target.value })} />
                            </label>
                            <label>
                                Purchase date
                                <input type="datetime-local" required value={form.purchaseDate} onChange={(event) => setForm({ ...form, purchaseDate: event.target.value })} />
                            </label>
                            <label>
                                Supplier
                                <input value={form.supplier} onChange={(event) => setForm({ ...form, supplier: event.target.value })} />
                            </label>
                            <div className="form-actions">
                                <button className="primary-button" type="submit" disabled={saving || !bases.length || !equipmentTypes.length}>
                                    {saving ? "Saving..." : "Save Purchase"}
                                </button>
                                <button className="secondary-button" type="button" onClick={() => setShowForm(false)}>Cancel</button>
                            </div>
                            {!equipmentTypes.length && <p className="form-hint">Add equipment with a type first; its type will be available here.</p>}
                        </form>
                    )}

                    {loading && <div className="loading">Loading purchases...</div>}
                    {error && <div className="error-message">{error}</div>}
                    {!loading && !error && (
                        <div className="table-card">
                            <table className="data-table">
                                <thead>
                                    <tr><th>ID</th><th>Base</th><th>Equipment Type</th><th>Quantity</th><th>Purchase Date</th><th>Supplier</th></tr>
                                </thead>
                                <tbody>
                                    {purchases.length === 0 ? (
                                        <tr><td colSpan="6" className="empty-state">No purchases found.</td></tr>
                                    ) : purchases.map((purchase) => (
                                        <tr key={purchase.id}>
                                            <td>{purchase.id}</td>
                                            <td>{purchase.base?.name || "-"}</td>
                                            <td>{purchase.equipmentType?.name || "-"}</td>
                                            <td>{purchase.quantity}</td>
                                            <td>{purchase.purchaseDate?.replace("T", " ") || "-"}</td>
                                            <td>{purchase.supplier || "-"}</td>
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

export default Purchases;