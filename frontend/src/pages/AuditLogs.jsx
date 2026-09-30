import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";
import ModuleShell from "../components/ModuleShell";

function AuditLogs() {
    const navigate = useNavigate();
    const [logs, setLogs] = useState([]);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (!localStorage.getItem("token")) {
            navigate("/login");
            return;
        }

        api.get("/audit-logs")
            .then((response) => setLogs(response.data))
            .catch((err) => {
                if (err.response?.status === 401 || err.response?.status === 403) {
                    localStorage.removeItem("token");
                    localStorage.removeItem("username");
                    navigate("/login");
                } else {
                    setError("Unable to load audit logs.");
                }
            })
            .finally(() => setLoading(false));
    }, [navigate]);

    return (
        <ModuleShell activePage="Audit Logs">
            <div className="page-header">
                <div>
                    <h1>Audit Logs</h1>
                    <p>Review recorded system activity.</p>
                </div>
            </div>
            {error && <div className="error-message">{error}</div>}
            {loading ? (
                <div className="loading">Loading audit logs...</div>
            ) : (
                <div className="table-card">
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>User</th>
                                <th>Action</th>
                                <th>Entity</th>
                                <th>Entity ID</th>
                                <th>Timestamp</th>
                                <th>Details</th>
                            </tr>
                        </thead>
                        <tbody>
                            {logs.length === 0 ? (
                                <tr><td colSpan="7" className="empty-state">No audit logs found.</td></tr>
                            ) : logs.map((log) => (
                                <tr key={log.id}>
                                    <td>{log.id}</td>
                                    <td>{log.username}</td>
                                    <td>{log.action}</td>
                                    <td>{log.entityType}</td>
                                    <td>{log.entityId ?? "-"}</td>
                                    <td>{log.timestamp?.replace("T", " ") || "-"}</td>
                                    <td>{log.details || "-"}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </ModuleShell>
    );
}

export default AuditLogs;